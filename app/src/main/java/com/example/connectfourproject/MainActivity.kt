package com.example.connectfourproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            MainMenu {
                startActivity(Intent(this, GameActivity::class.java))
            }
        }
    }
}


@Composable
fun MainMenu(onNavigateToGame: () -> Unit) {

    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.activity_main, null)
            },
            modifier = Modifier.fillMaxWidth()
        )

    Column(
        modifier = Modifier.align(Alignment.Center)) {
        Text(
            text = "Connect Four",
            color = Color.White,
            fontSize = 45.sp,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
                .graphicsLayer { translationY = -550f }
        )

            Button(
                onClick = { onNavigateToGame() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(200.dp)
                    .graphicsLayer { translationY = -450f }
            ) {
                Text(text = "Single Player")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainMenu() {
    MainMenu {
        println("Navigate to Game")
    }
}
