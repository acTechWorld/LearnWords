package com.perso.learnwords

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_update_page.*
import kotlinx.android.synthetic.main.header.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UpdatePage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    lateinit var idUser : String
    var listUpdate : ArrayList<Update> = ArrayList<Update>()
    lateinit var database: FirebaseDatabase
    lateinit var myRef : DatabaseReference
    lateinit var mFirebaseAnalytics: FirebaseAnalytics
    lateinit var  mAuth : FirebaseAuth
    lateinit var drawer : DrawerLayout
    lateinit var navMenu : NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        idUser = Settings.Secure.getString(applicationContext.getContentResolver(),
            Settings.Secure.ANDROID_ID);
        var context = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_page)

        drawer = this.findViewById(R.id.menuDrawer)
        navMenu = this.findViewById(R.id.navMenu)
        navMenu.setNavigationItemSelectedListener(this)

        //Firebase variable
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        // Write a message to the database
        database = FirebaseDatabase.getInstance()
        myRef = database.reference

        titlePage.text = "Sauvegardes"

        addUpdateButton.setOnClickListener(){
            popupAddUpdate()
        }

        menuButton.setOnClickListener(){
            drawer.openDrawer(GravityCompat.START, true)
        }

        homeButton.setOnClickListener(){
            var intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
        }

        //Check if there are more than 5 updates for a user delete the last one if it's the case
        myRef.child("users").child(idUser).child("updates").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                System.out.println(p0)
            }

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach(){update ->
                    //Check if the update is not already save on the list
                    var check = 0
                    for (updateList in listUpdate){
                        if(updateList.date == update.key){
                            check = 1
                        }
                    }
                    //If it not the case save it on the list
                    if (check == 0){
                        //Check if the update is completely upload on the dataBase
                        if (update.child("finish_upload").value == "true"){
                            var listWords = ArrayList<Word>()
                            var nbWords = update.child("words").childrenCount.toInt()
                            var date = update.key as String
                            update.child("words").children.forEach(){word ->
                                var french_word = word.child("french_word").value as String
                                var english_word = word.child("english_word").value as String
                                var index : String?= null
                                if (word.child("id").exists()){
                                    index = word.child("id").value as String
                                }
                                listWords.add(Word(french_word,english_word,index,false))
                            }
                            listUpdate.add(Update(date,nbWords,listWords))
                        }
                    }
                }

                //Check if the user got more than 5 updates on the database, if the case delete the older one
                if (p0.childrenCount.toInt() > 5){
                    var count = 0;
                    p0.children.forEach(){
                        if(it.child("finish_upload").exists()){
                            count+=1;
                        }
                    }
                    if (count > 5){
                        p0.child(p0.children.first().key as String).ref.removeValue()
                        System.out.println(listUpdate)
                        listUpdate.removeAt(0)
                    }
                }

                updateListView.layoutManager = LinearLayoutManager(applicationContext)
                updateListView.adapter = UpdateSectionAdapter(listUpdate.reversed().toCollection(ArrayList<Update>()),context )

            }

        })
    }

    fun popupAddUpdate(){
        val b = AlertDialog.Builder(this)
        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.alert_layout, null)
        b.setView(dialogView)

        b.setTitle("Ajouter une sauvegarde")
        b.setMessage("Voulez-vous ajouter une sauvegarde de votre dictionnaire ?")
        b.setPositiveButton("Oui") {dialog, which ->
            addUpdate()
        }
        b.setNegativeButton(
            "Non"
        ) { dialog, which ->
            dialog.dismiss()
        }
        b.show()
    }

    fun addUpdate(){

        //retrieve local liste words
        var gson = Gson()
        val jsonStringLocalList = BufferedReader(InputStreamReader(openFileInput("words.json"),"UTF-8")).use { it.readText()}
        var localListWords : ArrayList<Word> = gson.fromJson(jsonStringLocalList, Array<Word>::class.java).toCollection(ArrayList())

        //Current Date Time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDateTime: String = sdf.format(Date())



        //Add Word of local dictionnary into an update
        for ( i in 0..localListWords.size - 1){

            myRef.child("users").child(idUser).child("updates").child(currentDateTime).child("words")
                .child(i.toString()).child("english_word").setValue(localListWords[i].english_word)
            myRef.child("users").child(idUser).child("updates").child(currentDateTime).child("words")
                .child(i.toString()).child("french_word").setValue(localListWords[i].french_word)

            if (localListWords[i].index != null){
                myRef.child("users").child(idUser).child("updates").child(currentDateTime).child("words")
                    .child(i.toString()).child("id").setValue(localListWords[i].index)
            }
        }
        myRef.child("users").child(idUser).child("updates").child(currentDateTime)
            .child("finish_upload").setValue("true")



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


}


