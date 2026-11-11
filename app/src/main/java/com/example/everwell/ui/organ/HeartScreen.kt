
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

import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import io.github.sceneview.rememberCameraManipulator

@Composable
fun HeartScreen() {
    var resetCount by remember {
        mutableIntStateOf(0)
    }

    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = Position(0f, 0.4f, 2.75f),
        targetPosition = Position(0f, 0f, 0f),
        key = resetCount
    )

    Box(modifier = Modifier.fillMaxSize()) {

        SceneView(
            modifier = Modifier.fillMaxSize(),
            cameraManipulator = cameraManipulator
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

        Button(
            onClick = {
                resetCount++
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text("恢复默认视角")
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
