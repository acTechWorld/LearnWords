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
import kotlinx.android.synthetic.main.word_section.view.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class WordSectionAdapter(val listWord: ArrayList<Word>, val context: Context) :
    RecyclerView.Adapter<WordSectionAdapter.ViewHolder>() {

    lateinit var groupDeleteSection : LinearLayout
    lateinit var closeGroupDeleteSectionButton : ImageButton
    lateinit var groupDeleteButton : TextView


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        groupDeleteSection = MainActivity.groupDeleteSectionVar
        closeGroupDeleteSectionButton = MainActivity.closeGroupDeleteSectionButtonVar
        groupDeleteButton = MainActivity.groupDeleteButtonVar


        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.word_section,
                parent, false))
    }



    override fun getItemCount(): Int {
        return listWord.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.english_word.text = listWord[position].english_word
        holder.itemView.french_word.text = listWord[position].french_word
        var truePosi = position
        var gson = Gson()
        val jsonString = BufferedReader(InputStreamReader(context.openFileInput("words.json"),"UTF-8")).use { it.readText()}
        //Save SaredPref the jsonString
        val sharedPreferences = context.getSharedPreferences("main",  Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jsonString", jsonString).apply()
        var arrayWords = gson.fromJson(jsonString, Array<Word>::class.java).toCollection(ArrayList())

        //Find the true position of the list (if there are some researches with the searchBar)
        for (word in arrayWords){
            if (word.french_word == listWord[position].french_word && word.english_word == listWord[position].english_word){
                truePosi = arrayWords.indexOf(word)
            }
        }

        //Adapt layout wether or not case is checked
        if (listWord[position].checked){
            holder.itemView.background = context.getDrawable(R.drawable.border_background_checked)
            holder.itemView.deleteButton.setImageResource(R.drawable.trach_red_icon)
        }else{
            holder.itemView.background = context.getDrawable(R.drawable.border_background)
            holder.itemView.deleteButton.setImageResource(R.drawable.trash_icon)
        }
        //check case
        holder.itemView.setOnLongClickListener(){
            if (firstLongClick == 0){
                firstLongClick = 1
                groupDeleteSection.setBackgroundColor(context.getColor(R.color.gray))
                closeGroupDeleteSectionButton.visibility = View.VISIBLE
                groupDeleteButton.text = "supprimer"
            }
            if (!listWord[position].checked){
                selectedItemCounter+=1
                listWord[position].checked = true
                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }
        //uncheck case
        holder.itemView.setOnClickListener(){
            if ( listWord[position].checked){
                listWord[position].checked = false
                selectedItemCounter-=1
                notifyDataSetChanged()
                //If no item selected set groupDeleteSection Invisible
                if (selectedItemCounter == 0){
                    firstLongClick =0
                    groupDeleteSection.setBackgroundColor(context.getColor(R.color.transparent))
                    closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                    groupDeleteButton.text = "sélectionner"
                }
            }else{ // if first long click done possible to check with a short click
                if (firstLongClick == 1){
                    listWord[position].checked = true
                    selectedItemCounter+=1
                    notifyDataSetChanged()
                }
            }

        }
        //long cick trash -> select the case
        holder.itemView.deleteButton.setOnLongClickListener(){
            if (firstLongClick == 0){
                firstLongClick = 1
                groupDeleteSection.setBackgroundColor(context.getColor(R.color.gray))
                closeGroupDeleteSectionButton.visibility = View.VISIBLE
                groupDeleteButton.text = "supprimer"
            }
            if (!listWord[position].checked){
                selectedItemCounter+=1
                listWord[position].checked = true
                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }
        //short click on trash -> porpose to delete the case
        holder.itemView.deleteButton.setOnClickListener(){
            if (firstLongClick == 1){
                firstLongClick =0
                selectedItemCounter = 0
                groupDeleteSection.setBackgroundColor(context.getColor(R.color.transparent))
                closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                groupDeleteButton.text = "sélectionner"
            }
            val b = AlertDialog.Builder(context)
            val inflater : LayoutInflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.alert_layout, null)
            b.setView(dialogView)

            b.setTitle("Supprimer?")
            b.setMessage("Voulez-vous supprimer le mot définitivement :")
            b.setPositiveButton("Oui") {dialog, which ->
                var gsonParser = JsonParser()
                val sharedPreferences = context.getSharedPreferences("main",  Context.MODE_PRIVATE)
                var jsonString = sharedPreferences.getString("jsonString", "")
                var jsonObj = gsonParser.parse(jsonString)
                jsonObj.asJsonArray.remove(truePosi)
                var newJsonString = Gson().toJson(jsonObj)
                val file = File("/data/user/0/com.perso.learnwords/files/words.json")
                listWord.removeAt(position)
                file.writeText(newJsonString)
                notifyItemRemoved(position)
                notifyItemRangeRemoved(position, listWord.size)
                MainActivity.currentWordPosition = position
                sharedPreferences.edit().putString("jsonString", newJsonString).apply()
            }
            b.setNegativeButton(
                "Non"
            ) { dialog, which ->
                MainActivity.currentWordPosition = position
                dialog.dismiss() }
            b.show()
        }

        //long cick on edit button -> select the case
        holder.itemView.editButton.setOnLongClickListener(){
            if (firstLongClick == 0){
                firstLongClick = 1
                groupDeleteSection.setBackgroundColor(context.getColor(R.color.gray))
                closeGroupDeleteSectionButton.visibility = View.VISIBLE
                groupDeleteButton.text = "supprimer"
            }
            if (!listWord[position].checked){
                selectedItemCounter+=1
                listWord[position].checked = true
                notifyDataSetChanged()
            }
            return@setOnLongClickListener true
        }
        //short click on edit button -> run edit activity
        holder.itemView.editButton.setOnClickListener(){
            if (firstLongClick == 1){
                firstLongClick =0
                selectedItemCounter = 0
                groupDeleteSection.setBackgroundColor(context.getColor(R.color.transparent))
                closeGroupDeleteSectionButton.visibility = View.INVISIBLE
                groupDeleteButton.text = "sélectionner"
            }
            var intent = Intent(context, EditWordPopup().javaClass)



            intent.putExtra("position", truePosi)
            intent.putExtra("french_word", listWord[position].french_word)
            intent.putExtra("english_word", listWord[position].english_word)
            context.startActivity(intent)
        }

    }





    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}