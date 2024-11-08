package com.example.connectfourproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Paint
import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import com.example.connectfourproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.singleplayerBtn.setOnClickListener {
            createSinglePlayer()
        }
    }

    fun createSinglePlayer(){
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

}