package com.perso.learnwords

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_add_word_popup_auto.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class AddWordPopupAuto : AppCompatActivity() {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener : FirebaseAuth.AuthStateListener

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var currentUser :FirebaseUser
    private lateinit var mTTSEnglish : TextToSpeech
    private lateinit var currentInitialWordProposition : Word
    var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var listWordProposition = ArrayList<Word>()
        var listWordPreposition = ArrayList<Word>()
        var dm : DisplayMetrics = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(dm)
        var width = dm.widthPixels
        var height = dm.heightPixels

        var index = 1
        //Check if find 5 words already done or not
        var init = 0

        window.setLayout((width * 0.8).toInt(),(height * 0.7).toInt())

        var params : WindowManager.LayoutParams = window.attributes;
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20
        window.setAttributes(params)

        //get data from json file
        var gson = JsonParser()
        val sharedPreferences = applicationContext.getSharedPreferences("main",  Context.MODE_PRIVATE)
        var jsonString = sharedPreferences.getString("jsonString", "")
        var jsonObj = gson.parse(jsonString)

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


        //Firebase variable
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        // Write a message to the database
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference()


        myRef.child("words").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(init !=1){
                        //retrieve local liste words
                        var gson = Gson()
                        val jsonStringLocalList = BufferedReader(InputStreamReader(openFileInput("words.json"),"UTF-8")).use { it.readText()}
                        var localListWords : ArrayList<Word> = gson.fromJson(jsonStringLocalList, Array<Word>::class.java).toCollection(ArrayList())
                        init = 1
                        //Try 20 times to retrieve words the 5 first will be propose at the end
                        //If <5 words propose the first ones
                        for(i in 0..20){
                            var id = Random.nextInt(p0.childrenCount.toInt()).toString()
                            var french_word = p0.child(id).child("french_word").value as String
                            var english_word = p0.child(id).child("english_word").value as String
                            var index = p0.child(id).child("id").value.toString()
                            var word = Word(french_word , english_word, index, false)
                            var check = 0
                            //check if the proposed word is not in the local list
                            for (localWord in localListWords){
                                if (localWord.index == word.index){
                                    check = 1
                                }
                            }
                            for (localWord in listWordPreposition){
                                if (localWord.index == word.index){
                                    check = 1
                                }
                            }
                            if (check == 0){
                                listWordPreposition.add(word)
                            }
                        }
                        if (listWordPreposition.size>= 5){
                            for (i in 0..4){
                                listWordProposition.add(listWordPreposition[i])
                            }
                            currentInitialWordProposition = listWordProposition[0]
                            wordSection1.setText(listWordProposition[0].french_word)
                            wordSection2.setText(listWordProposition[0].english_word)
                        }
                        if(listWordPreposition.size>0 && listWordPreposition.size<5){
                            for (i in 0..listWordPreposition.size-1){
                                listWordProposition.add(listWordPreposition[i])
                            }
                            currentInitialWordProposition = listWordProposition[0]
                            wordSection1.setText(listWordProposition[0].french_word)
                            wordSection2.setText(listWordProposition[0].english_word)
                        }
                        if (listWordPreposition.size == 0){
                            noWordsMessage()
                        }

                    }
                }
        })
        setContentView(R.layout.activity_add_word_popup_auto)

        //Back to inititial word proposition
        backToInitialWordButton.setOnClickListener(){
            if (mode == 0){
                wordSection1.setText(currentInitialWordProposition.french_word)
                wordSection2.setText(currentInitialWordProposition.english_word)
            }
            else if(mode == 1){
                wordSection1.setText(currentInitialWordProposition.english_word)
                wordSection2.setText(currentInitialWordProposition.french_word)
            }
        }

        //Translate when click on auto translate button
        autoTranslateButton.setOnClickListener() {
            if (wordSection1.text != null) {
                //mode french to english translation
                if (mode == 0) {
                    frenchToEnglishTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully. Okay to start translating.
                            // (Set a flag, unhide the translation UI, etc.)
                            frenchToEnglishTranslator.translate(wordSection1.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordSection2.setText(translatedText)
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
                else if (mode == 1) {
                    englishToFrenchTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully. Okay to start translating.
                            // (Set a flag, unhide the translation UI, etc.)
                            englishToFrenchTranslator.translate(wordSection1.text.toString())
                                .addOnSuccessListener { translatedText ->
                                    // Translation successful.
                                    wordSection2.setText(translatedText)
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
                mTTSEnglish.speak(wordSection2.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
            else if (mode == 1){
                mTTSEnglish.speak(wordSection1.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        //Inverse the two languages sections
        inverseLanguageButton.setOnClickListener(){
            //Section 1 move to English - Section 2 move to french
            if (mode == 0){
                mode = 1
                titleSection1.text = "Mot en englais :"
                titleSection2.text = "Traduction en français :"

            }
            else if (mode == 1){//Section 1 move to French - Section 2 move to English
                mode = 0
                titleSection1.text = "Mot en français :"
                titleSection2.text = "Traduction en englais :"
            }
            //Exchange the two input word
            var currentWordSection1 = wordSection1.text
            var currentWordSection2 = wordSection2.text
            wordSection1.text = currentWordSection2
            wordSection2.text = currentWordSection1
        }

        //Add word to list
        validateButtonAuto.setOnClickListener(){
            if(listWordProposition.size == 1){
                //remove from DB
                //myRef.child(listWordProposition[0].index as String).removeValue()
                var jsonObjectAdded = JsonObject()

                if (mode == 0){
                    jsonObjectAdded.addProperty("english_word", wordSection2.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordSection1.text.toString())
                }
                else if (mode == 1){
                    jsonObjectAdded.addProperty("english_word", wordSection1.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordSection2.text.toString())
                }
                jsonObjectAdded.addProperty("index", listWordProposition[0].index)
                jsonObj.asJsonArray.add(jsonObjectAdded)
                var jsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                file.writeText(jsonString)
                sharedPreferences.edit().putString("jsonString", jsonString).apply()
                this.finish()
            }
            else if (listWordProposition.size >1){
                //remove from DB
                //myRef.child(listWordProposition[0].index as String).removeValue()

                //Add word to local list
                var jsonObjectAdded = JsonObject()
                if (mode == 0){
                    jsonObjectAdded.addProperty("english_word", wordSection2.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordSection1.text.toString())
                }
                else if (mode == 1){
                    jsonObjectAdded.addProperty("english_word", wordSection1.text.toString())
                    jsonObjectAdded.addProperty("french_word", wordSection2.text.toString())
                }
                jsonObjectAdded.addProperty("index", listWordProposition[0].index)
                jsonObj.asJsonArray.add(jsonObjectAdded)
                var jsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                file.writeText(jsonString)
                sharedPreferences.edit().putString("jsonString", jsonString).apply()
                listWordProposition.removeAt(0)

                //Modify currentInitialWordProposition var
                currentInitialWordProposition = listWordProposition[0]

                //Modify word sections
                if (mode == 0){
                    wordSection1.setText(listWordProposition[0].french_word)
                    wordSection2.setText(listWordProposition[0].english_word)
                }else if(mode == 1){
                    wordSection2.setText(listWordProposition[0].french_word)
                    wordSection1.setText(listWordProposition[0].english_word)
                }
                index = index+1
                tvIndex.text = index.toString()+"/5"
            }
        }

        //Pass to other word withour adding the current one
        refuseButtonAuto.setOnClickListener(){
            if(listWordProposition.size == 1){
                this.finish()
            }
            else if (listWordProposition.size > 1){
                listWordProposition.removeAt(0)

                //Modify currentInitialWordProposition var
                currentInitialWordProposition = listWordProposition[0]

                //Modify word sections
                if (mode == 0){
                    wordSection1.setText(listWordProposition[0].french_word)
                    wordSection2.setText(listWordProposition[0].english_word)
                }else if(mode == 1){
                    wordSection2.setText(listWordProposition[0].french_word)
                    wordSection1.setText(listWordProposition[0].english_word)
                }
                index = index+1
                tvIndex.text = index.toString()+"/5"
            }
        }

        //Quit page
        backButton.setOnClickListener(){
            this.finish()
        }





    }

    //If no words added
    fun noWordsMessage(){
        val b = AlertDialog.Builder(this)
        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.alert_layout, null)
        b.setView(dialogView)

        b.setTitle("Pas de mots trouvés")
        b.setMessage("Aucun nouveau mot n'a était trouvé, voulez vous réessayer?")
        b.setPositiveButton("Oui") {dialog, which ->
            this.finish()
            var intent = Intent(applicationContext, AddWordPopupAuto().javaClass)
            startActivity(intent)
        }
        b.setNegativeButton(
            "Non"
        ) { dialog, which ->
           this.finish()}
        b.show()
    }

}
