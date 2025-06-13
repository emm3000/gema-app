package com.emm.gema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.emm.gema.ui.theme.GemaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GemaTheme {
                Scaffold {
                    Button(modifier = Modifier.padding(it), onClick = {
                        throw RuntimeException("Test Crash")
                    }) {
                        Text("gaaa")
                    }
                }
            }
        }
    }
}