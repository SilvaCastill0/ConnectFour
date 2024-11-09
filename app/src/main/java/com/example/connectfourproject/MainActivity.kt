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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            MainMenu (
                onNavigateToGame = { startActivity(Intent(this, GameActivity::class.java)) },
                onPlayerAdded = { playerName -> savePlayerName(this, playerName) }
            )
        }
    }
}


@Composable
fun MainMenu(
    onNavigateToGame: () -> Unit,
    onPlayerAdded: (String) -> Unit
) {

    var playerName = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.activity_main, null)
            },
            modifier = Modifier.fillMaxWidth()
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
                        onPlayerAdded(playerName.value)
                        onNavigateToGame()
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


fun savePlayerName(context: Context, name: String) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val existingPlayers = sharedPreferences.getStringSet("playerNames", mutableSetOf()) ?: mutableSetOf()
    existingPlayers.add(name)
    editor.putStringSet("playerNames", existingPlayers)
    editor.apply()
}
// Retrieves player names and send it to different activities
fun getPlayerNames(context: Context): Set<String> {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getStringSet("playerNames", mutableSetOf()) ?: mutableSetOf()
}



@Preview(showBackground = true)
@Composable
fun PreviewMainMenu() {
    MainMenu (
        onNavigateToGame = { println("Navigate to Game") },
        onPlayerAdded = { println("Player added: $it") }
    )
}
