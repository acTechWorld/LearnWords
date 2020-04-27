package com.perso.learnwords

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_traduction_menu.*
import kotlin.random.Random

class TraductionMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traduction_menu)
        var gson = Gson()
        val sharedPreferences = applicationContext.getSharedPreferences("main",  Context.MODE_PRIVATE)
        var jsonString = sharedPreferences.getString("jsonString", "")
        listWords = gson.fromJson(jsonString, Array<Word>::class.java)

        var toggleMode = 0

        toggleButtonLanguage.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                // The toggle is enabled French to English mode
                toggleMode = 1
            } else {
                // The toggle is disabled English to french mode
                toggleMode = 0
            }
        }

        lastCreatedWordsButton.setOnClickListener(){
            var id = nbWordsRadioButtons.checkedRadioButtonId
            var radioButton = findViewById<RadioButton>(id)
            var value = radioButton.text.toString().toInt()
            if (listWords.size != 0){// check si la liste de mot n'est pas vide
                if(value < listWords.size){
                    listWordsToShow = ArrayList<Word>()
                    for ( i in 0..value-1){
                        var reverseList =  listWords.reversedArray()
                        listWordsToShow.add(reverseList[i])
                    }
                }else{
                    listWordsToShow = ArrayList<Word>()
                    for ( i in 0..listWords.size-1){
                        var reverseList =  listWords.reversedArray()
                        listWordsToShow.add(reverseList[i])
                    }
                }
                listWordsToShow.shuffle()
                var intent = Intent(applicationContext, TraductionGame().javaClass)
                intent.putExtra("mode", toggleMode)
                startActivity(intent)
            }else{
                val b = AlertDialog.Builder(this)
                val inflater : LayoutInflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.alert_layout, null)
                b.setView(dialogView)

                b.setTitle("Action impossible")
                b.setMessage("Votre liste de mot est vide, veuillez insérer des mots de vocabulaire à votre dictionnaire :")
                b.setNegativeButton(
                    "Ok"
                ) { dialog, which -> dialog.dismiss() }
                b.show()
            }

        }

        randomWordsButton.setOnClickListener(){
            var id = nbWordsRadioButtons.checkedRadioButtonId
            var radioButton = findViewById<RadioButton>(id)
            var value = radioButton.text.toString().toInt()
            if (listWords.size != 0) {// check si la liste de mot n'est pas vide
                if(value < listWords.size){
                    var copieListWords = listWords.toCollection(ArrayList<Word>())
                    listWordsToShow = ArrayList<Word>()
                    for ( i in 0..value-1){
                        var j = Random.nextInt(copieListWords.size)
                        if(j>0){
                            listWordsToShow.add(copieListWords[j-1])
                            copieListWords.removeAt(j-1)
                        }else{
                            listWordsToShow.add(copieListWords[0])
                            copieListWords.removeAt(0)
                        }
                    }
                }else{
                    var copieListWords = listWords.toCollection(ArrayList<Word>())
                    listWordsToShow = ArrayList<Word>()
                    for ( i in 0..listWords.size-1){
                        var j = Random.nextInt(copieListWords.size)
                        if(j>0){
                            listWordsToShow.add(copieListWords[j-1])
                            copieListWords.removeAt(j-1)
                        }else{
                            listWordsToShow.add(copieListWords[0])
                            copieListWords.removeAt(0)
                        }
                    }
                }
                var intent = Intent(applicationContext, TraductionGame().javaClass)
                intent.putExtra("mode", toggleMode)
                startActivity(intent)
            }else{
                val b = AlertDialog.Builder(this)
                val inflater : LayoutInflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.alert_layout, null)
                b.setView(dialogView)

                b.setTitle("Action impossible")
                b.setMessage("Votre liste de mot est vide, veuillez insérer des mots de vocabulaire à votre dictionnaire :")
                b.setNegativeButton(
                    "Ok"
                ) { dialog, which -> dialog.dismiss() }
                b.show()
            }
        }

        allWordsButton.setOnClickListener(){
            listWordsToShow = ArrayList<Word>()
            if (listWords.size != 0) {// check si la liste de mot n'est pas vide
                for ( i in 0..listWords.size-1){
                    listWordsToShow.add(listWords[i])
                }
                listWordsToShow.shuffle()
                var intent = Intent(applicationContext, TraductionGame().javaClass)
                intent.putExtra("mode", toggleMode)
                startActivity(intent)
            }else{
                val b = AlertDialog.Builder(this)
                val inflater : LayoutInflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.alert_layout, null)
                b.setView(dialogView)

                b.setTitle("Action impossible")
                b.setMessage("Votre liste de mot est vide, veuillez insérer des mots de vocabulaire à votre dictionnaire :")
                b.setNegativeButton(
                    "Ok"
                ) { dialog, which -> dialog.dismiss() }
                b.show()
            }

        }

        homeButton.setOnClickListener(){
            var intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
        }


    }

    companion object{
       lateinit var listWordsToShow : ArrayList<Word>;
       lateinit var listWords : Array<Word>;
    }
}

