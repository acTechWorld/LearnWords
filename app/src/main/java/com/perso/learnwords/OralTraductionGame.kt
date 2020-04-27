package com.perso.learnwords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_oral_traduction_game.*
import android.speech.tts.TextToSpeech
import android.view.View
import java.util.*


class OralTraductionGame : AppCompatActivity() {

    //var text speacker
    lateinit var mTTSFrench : TextToSpeech
    lateinit var mTTSEnglish : TextToSpeech
    var mode = 0
    var timerWords : CountDownTimer? =null
    var timerInit : CountDownTimer? =null
    var indexWords =0
    var state = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oral_traduction_game)

        //Var to know if it is french to english or english to french game
        mode = intent.getIntExtra("mode", 0)

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
        var language = mode
        timerWords = object: CountDownTimer((OralTraductionMenu.listWordsToShow.size * 6000).toLong(), 3000) {
            override fun onTick(millisUntilFinished: Long) {
                if (language == 0){
                    displayFrenchWord(OralTraductionMenu.listWordsToShow[indexWords])
                    if (mode == 1){
                            indexWords = indexWords+1
                    }
                }
                if (language == 1){
                    displayEnglishWord(OralTraductionMenu.listWordsToShow[indexWords])
                    if (mode == 0){
                        indexWords = indexWords+1
                    }
                }
                //change language
                if (language ==0){
                    language = 1
                }else{
                    language = 0
                }
                System.out.println(language)
            }

            override fun onFinish() {
                finish()
            }
        }
        timerWords!!.start()
    }

    //Initial countdown speacker (5 secs countdown)
    fun timerStart(){
        timerInit = object: CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var textCountDown = (millisUntilFinished/1000).toString()
                countdownText.text = textCountDown
                if (mode == 0){
                    mTTSFrench.speak(textCountDown, TextToSpeech.QUEUE_FLUSH, null)
                }else{
                    mTTSEnglish.speak(textCountDown, TextToSpeech.QUEUE_FLUSH, null)
                }

            }

            override fun onFinish() {
                countdownText.visibility = View.INVISIBLE
                stateButton.visibility = View.VISIBLE
                titleSection1.visibility = View.VISIBLE
                wordSection1.visibility = View.VISIBLE
                teachWords()
            }
        }
        timerInit!!.start()
    }

    fun displayFrenchWord(word : Word){
        if (mode == 0){
            wordSection1.text = word.french_word
            titleSection1.text = "Mot en francais :"
            titleSection2.visibility = View.INVISIBLE
            wordSection2.visibility = View.INVISIBLE
        }else{
            wordSection2.text = word.french_word
            titleSection2.text = "Traduction en francais :"
            titleSection2.visibility = View.VISIBLE
            wordSection2.visibility = View.VISIBLE
        }
        mTTSFrench.speak(word.french_word,TextToSpeech.QUEUE_FLUSH, null )
    }

    fun displayEnglishWord(word : Word){
        if (mode == 0){
            wordSection2.text = word.english_word
            titleSection2.text = "Traduction en anglais :"
            titleSection2.visibility = View.VISIBLE
            wordSection2.visibility = View.VISIBLE
        }else{
            wordSection1.text = word.english_word
            titleSection1.text = "Mot en anglais :"
            titleSection2.visibility = View.INVISIBLE
            wordSection2.visibility = View.INVISIBLE
        }
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
            titleSection1.visibility = View.VISIBLE
            wordSection1.visibility = View.VISIBLE
        }

    }


    override fun onRestart() {
        super.onRestart()
        teachWords()
    }


}
