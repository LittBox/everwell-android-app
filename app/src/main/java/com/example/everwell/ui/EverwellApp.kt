
package com.example.everwell.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.everwell.ui.navigation.EverwellNavHost

@Composable
fun EverwellApp() {
    MaterialTheme {
        EverwellNavHost()
    }
}
