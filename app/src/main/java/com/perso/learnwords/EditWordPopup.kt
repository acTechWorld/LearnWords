package com.perso.learnwords

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_add_word_popup_manual.*
import kotlinx.android.synthetic.main.activity_edit_word_popup.*
import kotlinx.android.synthetic.main.activity_edit_word_popup.backButton
import java.io.File
import java.util.*

class EditWordPopup : AppCompatActivity() {
    lateinit var mTTSEnglish : TextToSpeech
    var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word_popup)

        var dm : DisplayMetrics = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(dm)
        var width = dm.widthPixels
        var height = dm.heightPixels

        mTTSEnglish = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if(status !=  TextToSpeech.ERROR){
                //if there is no ERROR then set language
                mTTSEnglish.language = Locale.UK
            }
        })

        window.setLayout((width * 0.8).toInt(),(height * 0.7).toInt())

        var params : WindowManager.LayoutParams = window.attributes;
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20
        window.setAttributes(params)

        var position = intent.getIntExtra("position", 0)
        var french_word = intent.getStringExtra("french_word")
        var english_word = intent.getStringExtra("english_word")

        wordInputSection1Edit.setText(french_word)
        wordInputSection2Edit.setText(english_word)

        // Create an English-French and reverse translator:
        val optionsFrenchToEnglish = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.FR)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build()

        val optionsEnglishToFrench = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.FR)
            .build()

        val frenchToEnglishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(optionsFrenchToEnglish)
        val englishToFrenchTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(optionsEnglishToFrench)


        //Translate when click on auto translate button
        autoTranslateButtonEdit.setOnClickListener() {
            if (wordInputSection1Edit.text != null) {
                //mode french to english translation
                if (mode == 0) {
                    frenchToEnglishTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully. Okay to start translating.
                            // (Set a flag, unhide the translation UI, etc.)
                            frenchToEnglishTranslator.translate(wordInputSection1Edit.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordInputSection2Edit.setText(translatedText)
                                }
                                .addOnFailureListener { exception ->
                                    // Error.
                                    System.out.println(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            // Model couldn’t be downloaded or other internal error.
                            System.out.println(exception)
                        }
                }
                // mode english to french translation
                if (mode == 1) {
                    englishToFrenchTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully. Okay to start translating.
                            // (Set a flag, unhide the translation UI, etc.)
                            englishToFrenchTranslator.translate(wordInputSection1Edit.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordInputSection2Edit.setText(translatedText)
                                }
                                .addOnFailureListener { exception ->
                                    // Error.
                                    System.out.println(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            // Model couldn’t be downloaded or other internal error.
                            System.out.println(exception)
                        }
                }
            }
        }

        editWordButton.setOnClickListener(){
            var gson = JsonParser()
            val sharedPreferences = this.getSharedPreferences("main",  Context.MODE_PRIVATE)
            var jsonString = sharedPreferences.getString("jsonString", "")
            var jsonObj = gson.parse(jsonString)


            var jsonObjectEdited = JsonObject()
            if (wordInputSection1Edit != null && wordInputSection2Edit != null) {
                if (mode == 0){
                    jsonObjectEdited.addProperty("english_word", wordInputSection2Edit.text.toString())
                    jsonObjectEdited.addProperty("french_word", wordInputSection1Edit.text.toString())
                    jsonObjectEdited.addProperty("checked", false)
                }
                if (mode == 1){
                    jsonObjectEdited.addProperty("english_word", wordInputSection1Edit.text.toString())
                    jsonObjectEdited.addProperty("french_word", wordInputSection2Edit.text.toString())
                    jsonObjectEdited.addProperty("checked", false)
                }
            }



            jsonObj.asJsonArray.set(position, jsonObjectEdited)
            var newJsonString = Gson().toJson(jsonObj)
            val file = File("/data/user/0/com.perso.learnwords/files/words.json")
            file.writeText(newJsonString)
            MainActivity.currentWordPosition = position
            this.finish()
        }

        //make english speeker telling the english word
        audioButtonEdit.setOnClickListener() {
            if (mode == 0){
                mTTSEnglish.speak(wordInputSection2Edit.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
            if (mode == 1){
                mTTSEnglish.speak(wordInputSection1Edit.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        inverseLanguageButtonEdit.setOnClickListener(){
            //Section 1 move to English - Section 2 move to french
            if (mode == 0){
                mode = 1
                titleSection1Edit.text = "Mot en englais :"
                titleSection2Edit.text = "Traduction en français :"

            }else{//Section 1 move to French - Section 2 move to English
                mode = 0
                titleSection1Edit.text = "Mot en français :"
                titleSection2Edit.text = "Traduction en englais :"
            }
            //Exchange the two input word
            var currentWordSection1 = wordInputSection1Edit.text
            var currentWordSection2 = wordInputSection2Edit.text
            wordInputSection1Edit.text = currentWordSection2
            wordInputSection2Edit.text = currentWordSection1
        }

        backButton.setOnClickListener(){
            MainActivity.currentWordPosition = position
            this.finish()
        }
    }
}
