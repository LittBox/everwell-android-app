package com.example.everwell.ui.organ

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.ui.graphics.Color
import com.google.android.filament.MaterialInstance
import com.google.android.filament.View
import io.github.sceneview.RenderInvalidator
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.node.ModelNode

/**
 * Visible-surface picking and reversible visual selection for SceneView 4.67.0.
 *
 * Keep one controller in remember(view, materialLoader, renderInvalidator), bind it from the
 * ModelNode's apply block, and dispose it from DisposableEffect. All calls run on the main thread.
 * [onNodePicked] receives the exact GLB node name, or null for empty / unrelated geometry. The
 * screen owns the anatomical meaning of those names and calls [select] after resolving one.
 *
 * This controller borrows the model and its original materials. It never destroys the model,
 * mutates shared glTF material parameters, or assumes that screen position identifies anatomy.
 */
internal class HeartSceneController(
    private val view: View,
    private val materialLoader: MaterialLoader,
    private val renderInvalidator: RenderInvalidator,
    private val onNodePicked: (String?) -> Unit,
) {
    private data class Part(
        val node: ModelNode.RenderableNode,
        val name: String?,
        val originalMaterials: List<MaterialInstance>,
        val originalVisibility: Boolean,
    )

    private data class PickRequest(
        val generation: Long,
        val model: ModelNode,
        val expiresAt: Long,
    )

    private val mainHandler = Handler(Looper.getMainLooper())
    private var disposed = false
    private var generation = 0L
    private var model: ModelNode? = null
    private var partsByEntity: Map<Int, Part> = emptyMap()
    private var selectedNames: Set<String> = emptySet()
    private var highlightedEntities: Set<Int> = emptySet()
    private var isolatedNames: Set<String>? = null
    private var highlightMaterial: MaterialInstance? = null
    private var pendingPick: PickRequest? = null

    // View.pick is resolved by rendered frames, not by a synchronous ray cast. SceneView's
    // default rendering policy can park the scene while Filament is still returning the query.
    // Wake a bounded sequence of frames until the result arrives; never keep an idle loop alive.
    private val pickFramePump = object : Runnable {
        override fun run() {
            val request = pendingPick ?: return
            if (disposed || request.generation != generation || model !== request.model ||
                !isAlive(request.model) || SystemClock.uptimeMillis() >= request.expiresAt
            ) {
                cancelPendingPick()
                return
            }
            renderInvalidator.requestRender()
            mainHandler.postDelayed(this, PICK_FRAME_INTERVAL_MS)
        }
    }

    /**
     * Captures original appearance once for this model. Safe inside ModelNode(apply = { ... }):
     * binding does not invoke [onNodePicked] or write the screen's Compose state.
     *
     * Replacing / removing a model restores its appearance and invalidates outstanding picks.
     * Selection and isolation names can be set before the model finishes loading. Both express
     * the screen's desired state and survive rebinding / unbinding until explicitly changed.
     */
    fun bind(node: ModelNode?) {
        checkMainThread()
        if (disposed) return
        cancelPendingPick()
        if (model === node) return

        restoreBoundAppearance()
        model = null
        partsByEntity = emptyMap()
        highlightedEntities = emptySet()

        if (node == null || !isAlive(node)) return

        val parts = node.renderableNodes
            .filterNot { it.isDestroyed }
            .associate { renderable ->
                renderable.entity to Part(
                    node = renderable,
                    name = renderable.name,
                    originalMaterials = renderable.materialInstances.toList(),
                    originalVisibility = renderable.isVisible,
                )
            }
        model = node
        partsByEntity = parts
        applySelection()
        renderInvalidator.requestRender()
    }

    /** Call from rememberOnGestureListener(onSingleTapConfirmed = { event, _ -> ... }). */
    fun pick(event: MotionEvent) {
        // Copy coordinates immediately. Android owns and recycles MotionEvent instances.
        pick(event.x, event.y)
    }

    /** Coordinates are physical pixels local to the SceneView, with Android's top-left origin. */
    fun pick(xPx: Float, yPx: Float) {
        checkMainThread()
        if (disposed) return
        val boundModel = model?.takeIf(::isAlive) ?: return
        cancelPendingPick()

        val viewport = view.viewport
        if (!xPx.isFinite() || !yPx.isFinite() || viewport.width <= 0 || viewport.height <= 0 ||
            xPx < 0f || yPx < 0f || xPx >= viewport.width || yPx >= viewport.height
        ) {
            return
        }

        val request = PickRequest(
            generation = generation,
            model = boundModel,
            expiresAt = SystemClock.uptimeMillis() + PICK_TIMEOUT_MS,
        )
        pendingPick = request

        // Filament's picking origin is bottom-left. Its callback reports a renderable entity,
        // so the selected structure is the visible mesh under the pixel, not an overlapping AABB.
        view.pick(xPx.toInt(), viewport.height - 1 - yPx.toInt(), mainHandler) picked@{ result ->
            if (disposed || pendingPick !== request || request.generation != generation ||
                model !== boundModel || !isAlive(boundModel)
            ) {
                return@picked
            }
            pendingPick = null
            mainHandler.removeCallbacks(pickFramePump)

            val part = partsByEntity[result.renderable]
                ?.takeIf { !it.node.isDestroyed && it.node.isVisible }
            onNodePicked(part?.name)
        }
        pickFramePump.run()
    }

    /** Highlights all renderables with one of these exact node names; does not notify the UI. */
    fun select(nodeNames: Set<String>) {
        checkMainThread()
        if (disposed) return
        cancelPendingPick()
        selectedNames = nodeNames.toSet()
        applySelection()
    }

    fun clearSelection() = select(emptySet())

    /**
     * Displays only the named renderables. Passing null restores their original visibility.
     * The supplied heart asset has independent sibling renderables, including cavity meshes;
     * isolating one makes its interior representation accessible without a fabricated hit area.
     */
    fun isolate(nodeNames: Set<String>?) {
        checkMainThread()
        if (disposed) return
        cancelPendingPick()
        isolatedNames = nodeNames?.toSet()
        val boundModel = model?.takeIf(::isAlive) ?: return
        applyVisibility(boundModel)
        renderInvalidator.requestRender()
    }

    /** Also useful from onDown so a new drag cannot be interrupted by an earlier tap result. */
    fun cancelPendingPick() {
        checkMainThread()
        generation++
        pendingPick = null
        mainHandler.removeCallbacks(pickFramePump)
        // Do not remove Filament's own callback messages: they can safely return and be ignored
        // by the generation / disposal guards, without retaining a recycled MotionEvent.
    }

    /** Idempotent; restore borrowed resources before releasing this controller's own material. */
    fun dispose() {
        checkMainThread()
        if (disposed) return
        disposed = true
        cancelPendingPick()
        restoreBoundAppearance()
        model = null
        partsByEntity = emptyMap()
        highlightedEntities = emptySet()
        selectedNames = emptySet()
        isolatedNames = null
        highlightMaterial?.let(materialLoader::destroyMaterialInstance)
        highlightMaterial = null
    }

    private fun applySelection() {
        val boundModel = model?.takeIf(::isAlive) ?: return
        val next = partsByEntity.filterValues { part ->
            !part.node.isDestroyed && part.name != null && part.name in selectedNames
        }.keys

        (highlightedEntities - next).forEach { entity ->
            partsByEntity[entity]?.takeIf { !it.node.isDestroyed }?.let { part ->
                part.node.materialInstances = part.originalMaterials
            }
        }
        if (next.isNotEmpty()) {
            val material = highlightMaterial ?: materialLoader.createColorInstance(
                color = Color(0xFF6750A4),
                metallic = 0f,
                roughness = 0.55f,
            ).also { highlightMaterial = it }
            (next - highlightedEntities).forEach { entity ->
                partsByEntity.getValue(entity).node.setMaterialInstances(material)
            }
        }
        highlightedEntities = next.toSet()
        applyVisibility(boundModel)
        renderInvalidator.requestRender()
    }

    private fun applyVisibility(boundModel: ModelNode) {
        if (!isAlive(boundModel)) return
        val isolated = isolatedNames
        partsByEntity.values.forEach { part ->
            if (!part.node.isDestroyed) {
                part.node.isVisible = part.originalVisibility &&
                    (isolated == null || (part.name != null && part.name in isolated))
            }
        }
    }

    private fun restoreBoundAppearance() {
        val boundModel = model?.takeIf(::isAlive) ?: return
        partsByEntity.forEach { (entity, part) ->
            if (!part.node.isDestroyed) {
                if (entity in highlightedEntities) {
                    part.node.materialInstances = part.originalMaterials
                }
                part.node.isVisible = part.originalVisibility
            }
        }
        // Check only Java-side lifetime flags before touching any borrowed native components.
        if (isAlive(boundModel)) renderInvalidator.requestRender()
    }

    private fun isAlive(node: ModelNode): Boolean =
        !node.isDestroyed && !node.isModelDestroyed

    private fun checkMainThread() {
        check(Looper.myLooper() === mainHandler.looper) {
            "HeartSceneController must be used on the Android main thread."
        }
    }

    private companion object {
        const val PICK_FRAME_INTERVAL_MS = 16L
        const val PICK_TIMEOUT_MS = 1_000L
    }
}
