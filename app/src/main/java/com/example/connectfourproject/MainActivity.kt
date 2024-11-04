package com.example.connectfourproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.connectfourproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.singleplayerBtn.setOnClickListener {
            createOfflineGame()
        }
    }

    fun createOfflineGame(){
        startGame()
    }

    fun startGame(){
        startActivity(Intent(this,GameActivity::class.java))
    }

}