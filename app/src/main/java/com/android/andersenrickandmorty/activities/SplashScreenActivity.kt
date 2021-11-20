package com.android.andersenrickandmorty.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.android.andersenrickandmorty.R

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity(R.layout.activity_splash_screen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        introAnimationAndStartMainActivity()
    }

    private fun introAnimationAndStartMainActivity() {
        findViewById<ImageView>(R.id.splash_holder).animate()
            .setDuration(1500).alpha(1f)
            .withEndAction {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
                finish()
            }
    }
}