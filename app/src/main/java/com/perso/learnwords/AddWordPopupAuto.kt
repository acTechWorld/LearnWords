package com.perso.learnwords

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_add_word_popup_auto.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random

class AddWordPopupAuto : AppCompatActivity() {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener : FirebaseAuth.AuthStateListener

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var currentUser :FirebaseUser


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



        //Firebase variable
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        // Write a message to the database
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference()


        myRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(init !=1){
                        //retrieve local liste words
                        var gson = Gson()
                        val jsonStringLocalList = BufferedReader(InputStreamReader(openFileInput("words.json"),"UTF-8")).use { it.readText()}
                        var localListWords : ArrayList<Word> = gson.fromJson(jsonStringLocalList, Array<Word>::class.java).toCollection(ArrayList())
                        init = 1
                        //Try 11 times to retrieve words the 5 first will be propose at the end
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
                            wordSection1.text = listWordProposition[0].french_word
                            wordSection2.text = listWordProposition[0].english_word
                        }
                        if(listWordPreposition.size>0 && listWordPreposition.size<5){
                            for (i in 0..listWordPreposition.size-1){
                                listWordProposition.add(listWordPreposition[i])
                            }
                            wordSection1.text = listWordProposition[0].french_word
                            wordSection2.text = listWordProposition[0].english_word
                        }
                        if (listWordPreposition.size == 0){
                            noWordsMessage()
                        }

                    }
                }
        })
        setContentView(R.layout.activity_add_word_popup_auto)

        validateButtonAuto.setOnClickListener(){
            if(listWordProposition.size == 1){
                //remove from DB
                //myRef.child(listWordProposition[0].index as String).removeValue()
                var jsonObjectAdded = JsonObject()
                jsonObjectAdded.addProperty("english_word", listWordProposition[0].english_word)
                jsonObjectAdded.addProperty("french_word", listWordProposition[0].french_word)
                jsonObjectAdded.addProperty("index", listWordProposition[0].index)
                jsonObj.asJsonArray.add(jsonObjectAdded)
                var jsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                file.writeText(jsonString)
                sharedPreferences.edit().putString("jsonString", jsonString).apply()
                this.finish()
            }
            if (listWordProposition.size >1){
                //remove from DB
                //myRef.child(listWordProposition[0].index as String).removeValue()

                //Add word to local list
                var jsonObjectAdded = JsonObject()
                jsonObjectAdded.addProperty("english_word", listWordProposition[0].english_word)
                jsonObjectAdded.addProperty("french_word", listWordProposition[0].french_word)
                jsonObjectAdded.addProperty("index", listWordProposition[0].index)
                jsonObj.asJsonArray.add(jsonObjectAdded)
                var jsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                file.writeText(jsonString)
                sharedPreferences.edit().putString("jsonString", jsonString).apply()
                listWordProposition.removeAt(0)
                wordSection1.text = listWordProposition[0].french_word
                wordSection2.text = listWordProposition[0].english_word
                index = index+1
                tvIndex.text = index.toString()+"/5"
            }
        }

        refuseButtonAuto.setOnClickListener(){
            if(listWordProposition.size == 1){
                this.finish()
            }
            if (listWordProposition.size > 1){
                listWordProposition.removeAt(0)
                wordSection1.text = listWordProposition[0].french_word
                wordSection2.text = listWordProposition[0].english_word
                index = index+1
                tvIndex.text = index.toString()+"/5"
            }
        }

        backButton.setOnClickListener(){
            this.finish()
        }





    }

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
