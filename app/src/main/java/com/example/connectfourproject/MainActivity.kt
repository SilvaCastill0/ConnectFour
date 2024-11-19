package com.example.connectfourproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent{
            MainMenu { playerName ->
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("playerName", playerName)
                startActivity(intent)
            }
        }
    }
}


@Composable
fun MainMenu(onNavigateToGame: (String) -> Unit, ) {
    var playerName = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.backmain),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(16.dp)
    ) {
        Text(
            text = "Connect Four",
            color = Color.White,
            fontSize = 45.sp,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
                .graphicsLayer { translationY = -550f }
        )
        OutlinedTextField(
            value = playerName.value,
            onValueChange = { playerName.value = it },
            label = { Text("Enter your Username") },
            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
                .graphicsLayer { translationY = -250f }

        )
            Button(
                onClick = {
                    if(playerName.value.isNotEmpty()) {
                        onNavigateToGame(playerName.value)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(200.dp)
                    .graphicsLayer { translationY = -100f }
            ) {
                Text(text = "Join Lobby")
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewMainMenu() {
    MainMenu (
        onNavigateToGame = { println("Navigate to Game") }
    )
}


