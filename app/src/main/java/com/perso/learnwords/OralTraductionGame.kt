package com.perso.learnwords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_oral_traduction_game.*
import android.speech.tts.TextToSpeech
import android.view.View
import java.util.*


class OralTraductionGame : AppCompatActivity() {

    //text to speech
    lateinit var mTTSFrench : TextToSpeech
    lateinit var mTTSEnglish : TextToSpeech
    var timerWords : CountDownTimer? =null
    var timerInit : CountDownTimer? =null
    var indexWords =0
    var state = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oral_traduction_game)


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
        timerStart()

        backButton.setOnClickListener(){
            this.finish()
        }

        stateButton.setOnClickListener(){
            if (state == 1){
                if(timerWords != null) {
                    state = 0
                    stateButton.setImageResource(R.drawable.start_icon)
                    timerWords!!.cancel()
                }
            }else{
                state = 1
                stateButton.setImageResource(R.drawable.pause_icon)
                teachWords()
            }
        }


    }


    fun teachWords(){

        //Check if language should be french or english
        var language = 0
        timerWords = object: CountDownTimer((OralTraductionMenu.listWordsToShow.size * 6000).toLong(), 3000) {
            override fun onTick(millisUntilFinished: Long) {
                if (language == 0){
                    displayFrenchWord(OralTraductionMenu.listWordsToShow[indexWords])
                }
                if (language == 1){
                    displayEnglishWord(OralTraductionMenu.listWordsToShow[indexWords])
                    indexWords = indexWords+1
                }
                //change language
                if (language ==0){
                    language = 1
                }else{
                    language = 0
                }
            }

            override fun onFinish() {
                finish()
            }
        }
        timerWords!!.start()
    }

    fun timerStart(){
        timerInit = object: CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var textCountDown = (millisUntilFinished/1000).toString()
                countdownText.text = textCountDown
                mTTSFrench.speak(textCountDown, TextToSpeech.QUEUE_FLUSH, null)

            }

            override fun onFinish() {
                countdownText.visibility = View.INVISIBLE
                stateButton.visibility = View.VISIBLE
                frenchTitle.visibility = View.VISIBLE
                frenchWord.visibility = View.VISIBLE
                teachWords()
            }
        }
        timerInit!!.start()
    }

    fun displayFrenchWord(word : Word){
        englishTitle.visibility = View.INVISIBLE
        englishWord.visibility = View.INVISIBLE
        frenchWord.text = word.french_word
        mTTSFrench.speak(word.french_word,TextToSpeech.QUEUE_FLUSH, null )
    }

    fun displayEnglishWord(word : Word){
        englishTitle.visibility = View.VISIBLE
        englishWord.visibility = View.VISIBLE
        englishWord.text = word.english_word
        mTTSEnglish.speak(word.english_word,TextToSpeech.QUEUE_FLUSH, null )
    }


    override fun onDestroy() {
        super.onDestroy()
        mTTSEnglish.shutdown()
        mTTSFrench.shutdown()
    }

    override fun onStop() {
        super.onStop()
        if(timerWords != null){
            timerWords!!.cancel()
        }else{
            timerInit!!.cancel()
            countdownText.visibility = View.INVISIBLE
            frenchTitle.visibility = View.VISIBLE
            frenchWord.visibility = View.VISIBLE
        }

    }


    override fun onRestart() {
        super.onRestart()
        teachWords()
    }


}
