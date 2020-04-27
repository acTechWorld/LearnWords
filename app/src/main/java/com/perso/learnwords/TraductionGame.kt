package com.perso.learnwords

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import kotlinx.android.synthetic.main.activity_traduction_game_reveal.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class TraductionGame() : AppCompatActivity() {

    lateinit var mTTSFrench : TextToSpeech
    lateinit var mTTSEnglish : TextToSpeech
    lateinit var listWordToShow : ArrayList<Word>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traduction_game_reveal)

        //Var to know if it is french to english or english to french game
        var mode = intent.getIntExtra("mode", 0)

        var isRestart = intent.getIntExtra("restart",0)
        var listWordError = ArrayList<Word>()

        if (isRestart == 1){
            listWordToShow = ScoreTraductionGame.copieListWordToShow
            copieListWordToShow = ArrayList<Word>()
            for (word in listWordToShow){
                copieListWordToShow.add(word)
            }
        }else{
            listWordToShow = TraductionMenu.listWordsToShow
            copieListWordToShow = ArrayList<Word>()
            for (word in listWordToShow){
                copieListWordToShow.add(word)
            }
        }
        if (mode == 0){
            word_section1Game.text = listWordToShow[0].french_word
            word_section2Game.text = listWordToShow[0].english_word
        }else{
            word_section1Game.text = listWordToShow[0].english_word
            word_section2Game.text = listWordToShow[0].french_word
        }
        var nbTotalWord = listWordToShow.size
        var errorNb = 0

        //init speacker languages
        mTTSFrench = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {status ->
            if(status !=  TextToSpeech.ERROR){
                //if there is no ERROR then set language
                mTTSFrench.language = Locale.FRANCE
            }
        })

        mTTSEnglish = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {status ->
            if(status !=  TextToSpeech.ERROR){
                //if there is no ERROR then set language
                mTTSEnglish.language = Locale.UK
            }
        })


        guessButton.setOnClickListener(){
           validateSection.visibility = View.VISIBLE
           section2.visibility = View.VISIBLE
           guessButton.visibility = View.INVISIBLE
        }

        validateButton.setOnClickListener(){
            validateSection.visibility = View.INVISIBLE
            section2.visibility = View.INVISIBLE
            guessButton.visibility = View.VISIBLE
            listWordToShow.removeAt(0)

            if (listWordToShow.size !=0){
                if (mode == 0){
                    word_section1Game.text = listWordToShow[0].french_word
                    word_section2Game.text = listWordToShow[0].english_word
                }else{
                    word_section1Game.text = listWordToShow[0].english_word
                    word_section2Game.text = listWordToShow[0].french_word
                }
            }else{
                var intent = Intent(applicationContext, ScoreTraductionGame().javaClass)
                intent.putExtra("errorNb", errorNb)
                intent.putExtra("nbTotalWord", nbTotalWord)
                intent.putExtra("mode", mode)
                startActivity(intent)
            }

        }

        soundButton1Game.setOnClickListener(){
            if (mode == 0){
                mTTSFrench.speak(listWordToShow[0].french_word, TextToSpeech.QUEUE_FLUSH, null)
            }else{
                mTTSEnglish.speak(listWordToShow[0].english_word, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        soundButton2Game.setOnClickListener(){
            if (mode == 0){
                mTTSEnglish.speak(listWordToShow[0].english_word, TextToSpeech.QUEUE_FLUSH, null)
            }else{
                mTTSFrench.speak(listWordToShow[0].french_word, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        refuseButton.setOnClickListener(){
            var j : Int
            validateSection.visibility = View.INVISIBLE
            section2.visibility = View.INVISIBLE
            guessButton.visibility = View.VISIBLE
            if(listWordToShow[0] !in listWordError){
                errorNb +=1
                listWordError.add(listWordToShow[0])
            }
            if (listWordToShow.size> 1){
                j = Random.nextInt(1, listWordToShow.size)
            }else{
                j= 0
            }

            var word = listWordToShow[0]
            listWordToShow.removeAt(0)
            listWordToShow.add(j, word)
            if (mode == 0){
                word_section1Game.text = listWordToShow[0].french_word
                word_section2Game.text = listWordToShow[0].english_word
            }else{
                word_section1Game.text = listWordToShow[0].english_word
                word_section2Game.text = listWordToShow[0].french_word
            }
        }

        backButton.setOnClickListener(){
            this.finish()
        }




    }

    companion object{
        var copieListWordToShow : ArrayList<Word> = ArrayList<Word>();
    }
}
