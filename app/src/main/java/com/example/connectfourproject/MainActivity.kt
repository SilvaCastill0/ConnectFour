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
fun MainMenu(onNavigateToLobby: (String) -> Unit, ) {
    var playerName by remember { mutableStateOf("") }

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
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Enter your Username") },
            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
                .padding(35.dp)

        )
            Button(
                onClick = {
                    if(playerName.isNotBlank()) { onNavigateToLobby(playerName) }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
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
        onNavigateToLobby = { println("Navigate to Game") }
    )
}


