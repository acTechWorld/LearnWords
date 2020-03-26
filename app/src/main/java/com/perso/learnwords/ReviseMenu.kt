package com.perso.learnwords

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_revise_menu.*

class ReviseMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revise_menu)
        traductionButton.setOnClickListener(){
            var intent = Intent(applicationContext, TraductionMenu().javaClass)
            startActivity(intent)
        }
        oralRevisionButton.setOnClickListener(){
            var intent = Intent(applicationContext, OralTraductionMenu().javaClass)
            startActivity(intent)
        }

        homeButton.setOnClickListener(){
            var intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
        }
    }
}
