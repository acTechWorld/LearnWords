package com.perso.learnwords

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_score_traduction_game.*
import kotlin.math.round

class ScoreTraductionGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_traduction_game)
        copieListWordToShow = TraductionGame.copieListWordToShow
        var errorNb = intent.getIntExtra("errorNb", 0).toDouble()
        var nbTotalWord = intent.getIntExtra("nbTotalWord", 0).toDouble()
        var pourcentageSucceedNb = round(100.00 - (errorNb.div(nbTotalWord) * 100.00))
        var succeedNb = nbTotalWord - errorNb
        var mode = intent.getIntExtra("mode", 0)

        scorePourcentageText.setText("Résultat: " + pourcentageSucceedNb.toString() +"%")
        scoreText.setText("("+succeedNb.toInt().toString()+"/"+nbTotalWord.toInt().toString()+")")

        restartButton.setOnClickListener(){
            var intent = Intent(applicationContext, TraductionGame().javaClass)
            intent.putExtra("restart", 1)
            intent.putExtra("mode", mode)
            startActivity(intent)
        }

        returnMenuButton.setOnClickListener(){
            var intent = Intent(applicationContext, ReviseMenu().javaClass)
            startActivity(intent)
        }

        homeButton.setOnClickListener(){
            var intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
        }


    }

    companion object{
        lateinit var copieListWordToShow : ArrayList<Word>
    }

}
