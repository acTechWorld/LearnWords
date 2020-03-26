package com.perso.learnwords

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_add_word_popup.*


class AddWordPopupMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word_popup)
        var dm : DisplayMetrics = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(dm)
        var width = dm.widthPixels
        var height = dm.heightPixels

        window.setLayout((width * 0.8).toInt(),(height * 0.7).toInt())

        var params : WindowManager.LayoutParams = window.attributes;
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20
        window.setAttributes(params)

        backButton.setOnClickListener(){
            this.finish()
        }

        manualModeButton.setOnClickListener(){
            var intent = Intent(applicationContext, AddWordPopupManual().javaClass)
            startActivity(intent)
        }
        autoModeButton.setOnClickListener(){
            var intent = Intent(applicationContext, AddWordPopupAuto().javaClass)
            startActivity(intent)
        }
    }
}
