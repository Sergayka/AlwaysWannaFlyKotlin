package com.example.alwayswannafly

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var gameCharacter: ImageView
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var coin: ImageView
    private var velocityY = 0f
    private var velocityX = 5f
    private var isGameStarted = false
    private var isGameOver = false
    private var score = 0
    private val gravity = 0.5f
    private val jumpStrengthY = -15f
    private val jumpStrengthX = 5f
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random.Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameCharacter = findViewById(R.id.gameCharacter)
        gameOverLayout = findViewById(R.id.gameOverLayout)
        coin = findViewById(R.id.coin)
        val mainMenuButton: Button = findViewById(R.id.mainMenuButton)

        mainMenuButton.setOnClickListener {
            saveScore()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.gameLayout).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !isGameOver) {
                if (!isGameStarted) {
                    isGameStarted = true
                    handler.post(updateRunnable)
                    handler.post(generateCoinRunnable)
                }
                velocityY = jumpStrengthY
                velocityX = if (velocityX > 0) jumpStrengthX else -jumpStrengthX
                gameCharacter.setImageResource(R.drawable.character_flying)
            }
            true
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isGameStarted && !isGameOver) {
                updateGameCharacterPosition()
                handler.postDelayed(this, 16)
            }
        }
    }

    private fun updateGameCharacterPosition() {
        velocityY += gravity
        val newY = gameCharacter.y + velocityY
        val newX = gameCharacter.x + velocityX
        val parentHeight = (gameCharacter.parent as View).height
        val parentWidth = (gameCharacter.parent as View).width

        val clampedY = max(0f, min(newY, parentHeight - gameCharacter.height.toFloat()))
        var clampedX = max(0f, min(newX, parentWidth - gameCharacter.width.toFloat()))

        gameCharacter.y = clampedY
        gameCharacter.x = clampedX

        if (velocityY > 0) {
            gameCharacter.setImageResource(R.drawable.character_falling)
        }

        if (clampedY >= parentHeight - gameCharacter.height || checkCollisionWithSpikes()) {
            velocityX = -velocityX
            clampedX = max(0f, min(gameCharacter.x + velocityX, parentWidth - gameCharacter.width.toFloat()))
            gameCharacter.x = clampedX
            gameOver()
        }

        if (clampedX <= 0f || clampedX >= parentWidth - gameCharacter.width) {
            velocityX = -velocityX
            gameCharacter.scaleX = if (velocityX > 0) 1f else -1f
            clampedX = max(0f, min(gameCharacter.x + velocityX, parentWidth - gameCharacter.width.toFloat()))
            gameCharacter.x = clampedX
        }

        // Check for coin collision
        if (checkCollisionWithCoin()) {
            score++
            coin.visibility = View.GONE
        }
    }

    private fun checkCollisionWithSpikes(): Boolean {
        val gameCharacterRect = Rect()
        gameCharacter.getHitRect(gameCharacterRect)
        val spikes = arrayOf(
            findViewById<LinearLayout>(R.id.bottomSpikesRow),
            findViewById<LinearLayout>(R.id.topSpikesRow)
        )

        for (spike in spikes) {
            for (i in 0 until spike.childCount) {
                val spikeView = spike.getChildAt(i)
                val spikeRect = Rect()
                spikeView.getHitRect(spikeRect)
                if (Rect.intersects(gameCharacterRect, spikeRect)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkCollisionWithCoin(): Boolean {
        if (coin.visibility != View.VISIBLE) {
            return false
        }
        val gameCharacterRect = Rect()
        gameCharacter.getHitRect(gameCharacterRect)
        val coinRect = Rect()
        coin.getHitRect(coinRect)
        return Rect.intersects(gameCharacterRect, coinRect)
    }

    private val generateCoinRunnable = object : Runnable {
        override fun run() {
            if (isGameStarted && !isGameOver) {
                generateCoin()
                handler.postDelayed(this, random.nextLong(3000, 5000))
            }
        }
    }

    private fun generateCoin() {
        val parent = coin.parent as View
        val parentHeight = parent.height
        val parentWidth = parent.width
        val coinSize = coin.layoutParams.width

        val x = random.nextInt(parentWidth - coinSize)
        val y = random.nextInt(parentHeight - coinSize - 200) + 100 // генерация в пределах между верхними и нижними шипами

        coin.x = x.toFloat()
        coin.y = y.toFloat()
        coin.visibility = View.VISIBLE
    }

    private fun saveScore() {
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("score", score)
            apply()
        }
    }

    private fun gameOver() {
        isGameOver = true
        isGameStarted = false
        gameCharacter.setImageResource(R.drawable.character_dead)
        gameOverLayout.visibility = View.VISIBLE
    }
}
