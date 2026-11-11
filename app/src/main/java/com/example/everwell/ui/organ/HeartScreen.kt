
package com.example.everwell.ui.organ

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import io.github.sceneview.SceneView
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

@Composable
fun HeartScreen() {

    Box(modifier = Modifier.fillMaxSize()) {

        SceneView(
            modifier = Modifier.fillMaxSize()
        ) {

            val heartInstance = rememberModelInstance(
                modelLoader,
                "models/heart-dequantized.glb"
            )

            heartInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 1.0f,
                    centerOrigin = Position(0f, 0f, 0f),
                    autoAnimate = false
                )
            }
        }

        Text(
            text = "Everwell · 3D Heart",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
    }
}
