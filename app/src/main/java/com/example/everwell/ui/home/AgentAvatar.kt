package com.example.everwell.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.everwell.R
import com.google.android.filament.Camera
import io.github.sceneview.FrameRatePolicy
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Transform
import io.github.sceneview.node.ModelNode as SceneModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberFillLightNode
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.math.ceil

enum class AgentAvatarAction(internal val clipName: String) {
    IDLE("Idle"),
    WAVE("Wave"),
    YES("Yes"),
    THUMBS_UP("ThumbsUp")
}

/**
 * A local, transparent 3D companion. Give [modifier] a bounded width and height.
 *
 * Change [actionToken] to replay the same gesture. A gesture plays once, then returns to Idle;
 * its token is consumed when playback begins, so reopening a dialog does not replay it.
 * Set [active] to false when this avatar is covered or outside the visible page. Preview,
 * inactive and non-resumed states use the poster without allocating a Filament engine.
 */
@Composable
fun AgentAvatar(
    modifier: Modifier = Modifier,
    action: AgentAvatarAction = AgentAvatarAction.IDLE,
    actionToken: Int = 0,
    active: Boolean = true,
    onTap: () -> Unit = {}
) {
    val requestHistory = remember { AvatarRequestHistory() }

    Box(modifier, contentAlignment = Alignment.Center) {
        if (LocalInspectionMode.current || !active) {
            AgentAvatarPoster()
        } else {
            ResumedAgentAvatar(action, actionToken, requestHistory)
        }

        // The Compose hit target sits above TextureView, so scrolling/tapping never starts an
        // orbit or zoom. Keep this target available even when the model cannot be loaded.
        Box(
            Modifier.matchParentSize()
                .clickable(
                    enabled = active,
                    role = Role.Button,
                    onClickLabel = "和小伙伴打招呼",
                    onClick = onTap
                )
                .semantics {
                    contentDescription = "Everwell 的机器人小伙伴，轻点可以打招呼"
                }
        )
    }
}

@Composable
private fun AgentAvatarPoster() {
    Image(
        painter = painterResource(R.drawable.everwell_agent_poster),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ResumedAgentAvatar(
    action: AgentAvatarAction,
    actionToken: Int,
    requestHistory: AvatarRequestHistory
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { owner, _ ->
            resumed = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    if (resumed) {
        AvailableAgentAvatar(action, actionToken, requestHistory)
    } else {
        AgentAvatarPoster()
    }
}

@Composable
private fun AvailableAgentAvatar(
    action: AgentAvatarAction,
    actionToken: Int,
    requestHistory: AvatarRequestHistory
) {
    var unavailable by remember { mutableStateOf(false) }
    if (unavailable) {
        AgentAvatarPoster()
    } else {
        AgentAvatarScene(action, actionToken, requestHistory) { unavailable = true }
    }
}

@Composable
private fun AgentAvatarScene(
    action: AgentAvatarAction,
    actionToken: Int,
    requestHistory: AvatarRequestHistory,
    onUnavailable: () -> Unit
) {
    val reportUnavailable by rememberUpdatedState(onUnavailable)
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val instance = rememberModelInstance(modelLoader, "models/everwell-agent.glb")
    val animationController = remember {
        AvatarAnimationController { error ->
            Log.w("AgentAvatar", "Using the companion poster", error)
            reportUnavailable()
        }
    }
    DisposableEffect(animationController) {
        onDispose { animationController.dispose() }
    }

    var firstFramePresented by remember { mutableStateOf(false) }
    LaunchedEffect(firstFramePresented) {
        if (!firstFramePresented) {
            // Missing/broken models and surfaces that cannot present remain a usable poster.
            // Stop their scene too, rather than leaving a hidden loader alive indefinitely.
            delay(10_000L)
            reportUnavailable()
        }
    }

    val camera = rememberCameraNode(engine) {
        position = Position(0f, 0f, 3.4f)
        lookAt(Position(0f, 0f, 0f))
    }
    val keyLight = rememberMainLightNode(engine) {
        intensity = 10_000f
        isShadowCaster = false
    }
    val fillLight = rememberFillLightNode(engine) {
        intensity = 5_000f
        isShadowCaster = false
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val aspect = (maxWidth.value / maxHeight.value)
            .takeIf { it.isFinite() && it > 0f } ?: 1f
        LaunchedEffect(camera, aspect) {
            // Fit the full character in the smaller viewport dimension, including its wave.
            camera.setProjection(
                fovInDegrees = 40.0,
                aspect = aspect.toDouble(),
                near = 0.05,
                far = 100.0,
                direction = if (aspect < 1f) Camera.Fov.HORIZONTAL else Camera.Fov.VERTICAL
            )
        }

        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            surfaceType = SurfaceType.TextureSurface,
            isOpaque = false,
            renderQuality = RenderQuality.Performance,
            frameRatePolicy = FrameRatePolicy.OnDemand(maxFps = 30),
            autoCenterContent = false,
            autoFitContent = false,
            cameraNode = camera,
            cameraManipulator = null,
            onGestureListener = null,
            mainLightNode = keyLight,
            fillLightNode = fillLight,
            // A one-time readiness write; animation frames never update page Compose state.
            onFrame = if (firstFramePresented) null else {
                { _ ->
                    if (animationController.hasLiveNode) firstFramePresented = true
                }
            }
        ) {
            instance?.let { model ->
                ModelNode(
                    modelInstance = model,
                    scaleToUnits = 2.3f,
                    centerOrigin = Position(0f, 0f, 0f),
                    autoAnimate = false,
                    apply = {
                        isTouchable = false
                        nodes.forEach { it.isTouchable = false }
                        animationController.bind(this)
                    }
                )
            }
        }

        if (!firstFramePresented) AgentAvatarPoster()

        // This effect runs after the ModelNode has been bound in this composition. Native
        // animation advances inside SceneView; only the end of a one-shot needs a coroutine.
        LaunchedEffect(instance, animationController, action, actionToken) {
            if (instance == null || !animationController.hasLiveNode) return@LaunchedEffect
            val request = action to actionToken
            val nextAction = if (requestHistory.lastPlayed == request) {
                AgentAvatarAction.IDLE
            } else {
                requestHistory.lastPlayed = request
                action
            }
            try {
                animationController.play(nextAction)?.let { durationMillis ->
                    delay(durationMillis)
                    animationController.play(AgentAvatarAction.IDLE)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                animationController.fail(error)
            }
        }
    }
}

/** Ordinary fields: recording an event must not cause another animation/recomposition. */
private class AvatarRequestHistory {
    var lastPlayed: Pair<AgentAvatarAction, Int>? = null
}

/**
 * Main-thread controller. It borrows the model; rememberModelInstance/SceneScope own disposal.
 * Rigged models still do frame work at rest in SceneView 4.67.0. The caller removes the whole
 * scene when inactive instead of assuming that stopAnimation or OnDemand makes a rig sleep.
 */
private class AvatarAnimationController(private val onFailure: (Exception) -> Unit) {
    private data class Clip(val index: Int, val durationSeconds: Float)

    private var node: SceneModelNode? = null
    private var restPose: List<Pair<Node, Transform>> = emptyList()
    private var clips: Map<String, Clip> = emptyMap()
    private var disposed = false

    val hasLiveNode: Boolean
        get() = !disposed && node?.let { !it.isDestroyed && !it.isModelDestroyed } == true

    fun bind(modelNode: SceneModelNode) {
        if (disposed || node === modelNode) return
        node = modelNode
        try {
            // Clips target different subsets of joints. Save the authored local transforms so
            // Wave/ThumbsUp cannot leave an arm behind when the seven-track Idle clip resumes.
            restPose = modelNode.nodes.map { it to Transform(it.transform) }
            clips = (0 until modelNode.animationCount).associate { index ->
                modelNode.animator.getAnimationName(index) to Clip(
                    index,
                    modelNode.animator.getAnimationDuration(index)
                )
            }
            modelNode.onFrameError = ::fail
            play(AgentAvatarAction.IDLE)
        } catch (error: Exception) {
            fail(error)
        }
    }

    /** Returns a one-shot's duration, or null when the resolved animation is looping Idle. */
    fun play(action: AgentAvatarAction): Long? {
        if (!hasLiveNode) return null
        val modelNode = node ?: return null
        val requested = clips[action.clipName]
        val clip = requested ?: clips[AgentAvatarAction.IDLE.clipName] ?: return null
        val oneShot = requested != null && action != AgentAvatarAction.IDLE

        modelNode.playingAnimations.clear()
        restPose.forEach { (child, transform) -> child.transform = transform }
        modelNode.animator.applyAnimation(clip.index, 0f)
        modelNode.animator.updateBoneMatrices()
        modelNode.nodes.forEach {
            it.invalidateTransformCache()
            it.onWorldTransformChanged()
        }
        modelNode.playAnimation(clip.index, speed = 1f, loop = !oneShot)
        modelNode.requestRender()

        return if (oneShot) {
            val seconds = clip.durationSeconds.takeIf { it.isFinite() && it > 0f } ?: 1f
            ceil(seconds.toDouble() * 1_000.0).toLong()
        } else null
    }

    fun fail(error: Exception) {
        if (!disposed) onFailure(error)
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        node?.let { modelNode ->
            // These two properties are Kotlin-side. Do not dereference an Animator after its
            // owning model was freed; sibling Compose effects may dispose in either order.
            modelNode.onFrameError = null
            modelNode.playingAnimations.clear()
        }
        node = null
        restPose = emptyList()
        clips = emptyMap()
    }
}
