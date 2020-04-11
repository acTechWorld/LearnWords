package com.perso.learnwords

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.perso.learnwords.MainActivity.Companion.firstLongClick
import com.perso.learnwords.MainActivity.Companion.selectedItemCounter
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.update_section.view.*
import kotlinx.android.synthetic.main.word_section.view.*
import java.io.File

class UpdateSectionAdapter(val listUpdate : ArrayList<Update>, val context: Context) :
    RecyclerView.Adapter<UpdateSectionAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.update_section,
                parent, false))
    }



    override fun getItemCount(): Int {
        return listUpdate.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.updateDate.text = listUpdate[position].date
        holder.itemView.nbWords.text =  listUpdate[position].nbWords.toString() + " mots"
        holder.itemView.updateButton.setOnClickListener(){
            val b = AlertDialog.Builder(context)
            val inflater : LayoutInflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.alert_layout, null)
            b.setView(dialogView)

            b.setTitle("Modifier son dictionnaire")
            b.setMessage("Voulez-vous revenir à cette sauvegarde ?")
            b.setPositiveButton("Oui") {dialog, which ->
                saveUpdateIntoLocalListe(context,  listUpdate[position].listWords)
            }
            b.setNegativeButton(
                "Non"
            ) { dialog, which ->
                dialog.dismiss()
            }
            b.show()
        }
    }


    fun saveUpdateIntoLocalListe(context: Context, listWord : ArrayList<Word>){
        var gson = JsonParser()
        val sharedPreferences = context.getSharedPreferences("main",  Context.MODE_PRIVATE)
        var jsonString = Gson().toJson(listWord)
        System.out.println(jsonString)
        val file = File("/data/user/0/com.perso.learnwords/files/words.json")
        file.writeText(jsonString)
        sharedPreferences.edit().putString("jsonString", jsonString).apply()

        val b = AlertDialog.Builder(context)
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.alert_layout, null)
        b.setView(dialogView)

        b.setTitle("Réussite")
        b.setMessage("Votre dictionnaire a été mis à jour")
        b.setPositiveButton("Ok") {dialog, which ->
            dialog.dismiss()
        }
        b.show()
    }





    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}