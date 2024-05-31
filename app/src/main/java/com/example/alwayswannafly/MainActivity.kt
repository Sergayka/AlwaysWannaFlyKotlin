package com.example.alwayswannafly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreTextView = findViewById(R.id.scoreTextView)
        val playButton: Button = findViewById(R.id.playButton)

        // Load the score from shared preferences
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        score = sharedPreferences.getInt("score", 0)
        updateScoreTextView()

        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateScoreTextView() {
        scoreTextView.text = "Score: $score"
    }
}
