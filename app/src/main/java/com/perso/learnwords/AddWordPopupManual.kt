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
import java.io.File
import java.util.*

class AddWordPopupManual : AppCompatActivity() {
    lateinit var mTTSEnglish : TextToSpeech
    var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word_popup_manual)

        var dm : DisplayMetrics = DisplayMetrics();

        windowManager.defaultDisplay.getMetrics(dm)
        var width = dm.widthPixels
        var height = dm.heightPixels
        //make window small popul
        window.setLayout((width * 0.8).toInt(),(height * 0.7).toInt())

        var params : WindowManager.LayoutParams = window.attributes;
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20
        window.setAttributes(params)



        //audio english speeker
        mTTSEnglish = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if(status !=  TextToSpeech.ERROR){
                //if there is no ERROR then set language
                mTTSEnglish.language = Locale.UK
            }
        })


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

        //get data from json file
        var gson = JsonParser()
        val sharedPreferences = applicationContext.getSharedPreferences("main",  Context.MODE_PRIVATE)
        var jsonString = sharedPreferences.getString("jsonString", "")
        var jsonObj = gson.parse(jsonString)





        //Translate when click on auto translate button
        autoTranslateButton.setOnClickListener() {
            if (wordInputSection1.text != null) {
                //mode french to english translation
                if (mode == 0) {
                    frenchToEnglishTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully. Okay to start translating.
                            // (Set a flag, unhide the translation UI, etc.)
                            frenchToEnglishTranslator.translate(wordInputSection1.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordInputSection2.setText(translatedText)
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
                            englishToFrenchTranslator.translate(wordInputSection1.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordInputSection2.setText(translatedText)
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

        //make english speeker telling the english word
        audioButton.setOnClickListener() {
            if (mode == 0){
                mTTSEnglish.speak(wordInputSection2.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
            if (mode == 1){
                mTTSEnglish.speak(wordInputSection1.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
        }
        //Return previous page
        backButton.setOnClickListener() {
            this.finish()
        }

        addWordButton.setOnClickListener() {
            if (wordInputSection1 != null && wordInputSection2 != null) {
                var jsonObjectAdded = JsonObject()
                if (mode == 0){
                    jsonObjectAdded.addProperty("english_word", wordInputSection2.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordInputSection1.text.toString())
                    jsonObjectAdded.addProperty("checked", false)
                }
                if (mode == 1){
                    jsonObjectAdded.addProperty("english_word", wordInputSection1.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordInputSection2.text.toString())
                    jsonObjectAdded.addProperty("checked", false)
                }
                jsonObj.asJsonArray.add(jsonObjectAdded)
                var jsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                file.writeText(jsonString)
                sharedPreferences.edit().putString("jsonString", jsonString).apply()
                this.finish()
            }
        }

        inverseLanguageButton.setOnClickListener(){
            //Section 1 move to English - Section 2 move to french
            if (mode == 0){
                mode = 1
                titleSection1.text = "Mot en englais :"
                titleSection2.text = "Traduction en français :"

            }else{//Section 1 move to French - Section 2 move to English
                mode = 0
                titleSection1.text = "Mot en français :"
                titleSection2.text = "Traduction en englais :"
            }
            //Exchange the two input word
            var currentWordSection1 = wordInputSection1.text
            var currentWordSection2 = wordInputSection2.text
            wordInputSection1.text = currentWordSection2
            wordInputSection2.text = currentWordSection1
        }
    }

}


