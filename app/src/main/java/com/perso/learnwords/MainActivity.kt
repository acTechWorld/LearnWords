package com.perso.learnwords

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer : DrawerLayout
    lateinit var navMenu : NavigationView
    lateinit var arrayWords : ArrayList<Word>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var context = this
        groupDeleteSectionVar = groupDeleteSection
        closeGroupDeleteSectionButtonVar = closeGroupDeleteSectionButton
        groupDeleteButtonVar = groupDeleteButton

        drawer = this.findViewById(R.id.menuDrawer)
        navMenu = this.findViewById(R.id.navMenu)
        navMenu.setNavigationItemSelectedListener(this)

        //insert Page title in header
        titlePage.text = "Mon Dictionnaire"

        init()

        reviseWordButton.setOnClickListener(){
            var intent = Intent(applicationContext, ReviseMenu().javaClass)
            startActivity(intent)
        }


        addWordButton.setOnClickListener(){
            var intent = Intent(applicationContext, AddWordPopupMenu().javaClass)
            startActivity(intent)
        }

        homeButton.setOnClickListener(){
            var intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
        }

        menuButton.setOnClickListener(){
            drawer.openDrawer(GravityCompat.START, true)
        }

        upButton.setOnClickListener(){
            list_words.scrollToPosition(0)
        }

        downButton.setOnClickListener(){
            list_words.scrollToPosition(list_words.adapter!!.itemCount -1)
        }
        //Looking for action on the searchBar
        searchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                var ArrayWordReduced = ArrayList<Word>()
                if (s.length != 0) {
                   for (word in arrayWords){
                       if(word.french_word.contains(s) || word.english_word.contains(s)){
                           ArrayWordReduced.add(word)
                       }
                   }
                    list_words.adapter = WordSectionAdapter(ArrayWordReduced, context)
                    return true
                }else{
                    list_words.adapter = WordSectionAdapter(arrayWords, context)
                }
                return false
            }
        })


    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus == true){
            var gson = Gson()
            val jsonString = BufferedReader(InputStreamReader(openFileInput("words.json"),"UTF-8")).use { it.readText()}
            //Save SaredPref the jsonString
            val sharedPreferences = applicationContext.getSharedPreferences("main",  Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("jsonString", jsonString).apply()
            arrayWords = gson.fromJson(jsonString, Array<Word>::class.java).toCollection(ArrayList())
            list_words.layoutManager = LinearLayoutManager(this)
            list_words.adapter = WordSectionAdapter(arrayWords, this)
            list_words.scrollToPosition(currentWordPosition)
            currentWordPosition = 0
            nbWords.text = arrayWords.size.toString() +"mots"

            //Close groupDeleteSection and delete selected words from local list
            groupDeleteButton.setOnClickListener(){
                if (firstLongClick == 1){
                    val b = AlertDialog.Builder(this)
                    val inflater : LayoutInflater = LayoutInflater.from(this)
                    val dialogView = inflater.inflate(R.layout.alert_layout, null)
                    b.setView(dialogView)

                    b.setTitle("Supprimer?")
                    b.setMessage("Voulez-vous supprimer ces mots définitivement :")
                    b.setPositiveButton("Oui") {dialog, which ->
                        var gsonParser = JsonParser()
                        val sharedPreferences = this.getSharedPreferences("main",  Context.MODE_PRIVATE)
                        var jsonString = sharedPreferences.getString("jsonString", "")
                        var jsonObj = gsonParser.parse(jsonString)
                        var j = 0
                        for (i in 0..arrayWords.size-1){
                            if (arrayWords[j].checked == true){
                                jsonObj.asJsonArray.remove(j)
                                arrayWords.removeAt(j)
                            }else{
                                j+=1
                            }
                        }

                        var newJsonString = Gson().toJson(jsonObj)
                        val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                        file.writeText(newJsonString)

                        list_words.adapter!!.notifyDataSetChanged()
                        sharedPreferences.edit().putString("jsonString", newJsonString).apply()
                        groupDeleteSection.setBackgroundColor(getColor(R.color.transparent))
                        closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                        groupDeleteButton.text = "sélectionner"
                        firstLongClick = 0
                        selectedItemCounter = 0
                    }
                    b.setNegativeButton(
                        "Non"
                    ) { dialog, which ->
                        dialog.dismiss()
                        groupDeleteSection.setBackgroundColor(getColor(R.color.transparent))
                        closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                        groupDeleteButton.text = "sélectionner"
                        firstLongClick = 0
                        selectedItemCounter = 0}
                    b.show()
                }else{
                    firstLongClick =1
                    groupDeleteSection.setBackgroundColor(getColor(R.color.gray))
                    closeGroupDeleteSectionButton.visibility = View.VISIBLE
                    groupDeleteButton.text = "supprimer"

                }
            }

            //Close groupDeleteSection without deleting words from local list
            closeGroupDeleteSectionButton.setOnClickListener(){
                for (word in arrayWords){
                    word.checked = false
                }
                groupDeleteSection.setBackgroundColor(getColor(R.color.transparent))
                closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                groupDeleteButton.text = "sélectionner"
                firstLongClick = 0
                selectedItemCounter = 0
                list_words.adapter!!.notifyDataSetChanged()
            }

        }
    }


    fun init (){
        //init
        if(!File("/data/user/0/com.perso.learnwords/files/words.json").exists()){
            try {
                val FILENAME = "words.json"
                var text : String = "[]"
                val fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)
                fos.write(text.toByteArray())
            }catch (e : Exception){

            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.reviseWords -> {
                var intent = Intent(applicationContext, ReviseMenu().javaClass)
                startActivity(intent)
            }
            R.id.backups -> {
                var intent = Intent(applicationContext, UpdatePage().javaClass)
                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START, true)
        return true
    }


    companion object{
        var firstLongClick = 0
        var selectedItemCounter = 0
        lateinit var groupDeleteSectionVar : LinearLayout
        lateinit var closeGroupDeleteSectionButtonVar : ImageButton
        lateinit var groupDeleteButtonVar : TextView
        var currentWordPosition = 0

    }



}
