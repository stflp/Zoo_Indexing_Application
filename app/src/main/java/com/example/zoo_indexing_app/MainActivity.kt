package com.example.zoo_indexing_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var input: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animalText = findViewById<EditText>(R.id.nameOfAnimal)
        val continentText = findViewById<EditText>(R.id.nameOfContinent)
        val addButton = findViewById<Button>(R.id.addButton)
        val recyclerView = findViewById<RecyclerView>(R.id.animalRecyclerView)

        val inflater = LayoutInflater.from(this)
        val itemsLayout = inflater.inflate(R.layout.animal_items, null)
        val deleteButton = itemsLayout.findViewById<Button>(R.id.deleteButton)

        val animalList = readFromDatabase(applicationContext)

        recyclerView.adapter = AnimalAdapter(animalList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        continentText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                input = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        addButton.setOnClickListener {
            val text1 = animalText.text.toString()
            val text2 = input
            val animal = text1.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            if (isValidValue(text2) && text1.isNotEmpty()) {
                var animalFound=false
                for(item in animalList){
                    if(animal==item.first)
                    {
                        animalFound=true
                    }
                }

                if(!animalFound) {
                    saveToDatabase(animal, text2, applicationContext)
                    animalList.add(Pair(animal, text2))
                }
                else{
                    AlertDialog.Builder(this)
                        .setTitle("This animal already exists")
                        .setMessage("Please enter a different animal.")
                        .setPositiveButton("OK", null)
                        .show()
                }
                recyclerView.adapter?.notifyDataSetChanged()
            } else if(text1.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Invalid value")
                    .setMessage("Please enter a valid input.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            else if(!isValidValue(text2)){
                AlertDialog.Builder(this)
                    .setTitle("Invalid value")
                    .setMessage("Please enter a valid continent.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}

private fun saveToDatabase(text1: String, text2: String, context: Context) {
    val file = context.openFileOutput("db.txt", Context.MODE_APPEND)
    val writer = OutputStreamWriter(file)
    writer.write("$text1, $text2\n")
    writer.flush()
    writer.close()
}

private fun deleteFromDatabase(text1: String, text2: String, context: Context) {
    val file = context.getFileStreamPath("db.txt")
    val tempFile = context.getFileStreamPath("temp.txt")

    if (file.exists()) {
        val reader = BufferedReader(InputStreamReader(context.openFileInput("db.txt")))
        val writer = OutputStreamWriter(context.openFileOutput("temp.txt", Context.MODE_PRIVATE))

        var line: String? = reader.readLine()
        while (line != null) {
            if (line.trim() != "$text1, $text2") {
                writer.write("$line\n")
            }
            line = reader.readLine()
        }

        writer.flush()
        writer.close()
        reader.close()
        file.delete()
        tempFile.renameTo(file)
    }
}


private fun isValidValue(value: String): Boolean {
    val allowedValues = arrayOf("Africa", "Europe", "North America", "South America", "Asia", "Australia", "Antarctica")
    return allowedValues.contains(value)
}

private fun readFromDatabase(context: Context): ArrayList<Pair<String, String>> {
    val file = context.openFileInput("db.txt")
    val reader = BufferedReader(InputStreamReader(file))
    val list = ArrayList<Pair<String, String>>()

    reader.use { bufferedReader ->
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            val data = line.split(",").map { it.trim() }
            if (data.size == 2) {
                list.add(Pair(data[0], data[1]))
            }
            line = bufferedReader.readLine()
        }
    }

    return list
}


class AnimalAdapter(private val animalList: List<Pair<String, String>>) : RecyclerView.Adapter<AnimalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.animal_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(animalList[position])
    }

    override fun getItemCount() = animalList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val animalTextView: TextView = itemView.findViewById(R.id.animal_textView)
        private val continentTextView: TextView = itemView.findViewById(R.id.continent_textView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                    deleteFromDatabase(animalTextView.text.toString(), continentTextView.text.toString(), itemView.context)
                    (animalList as MutableList<Pair<String, String>>).removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
            }
        }

        fun bind(animal: Pair<String, String>) {
            animalTextView.text = animal.first
            continentTextView.text = animal.second
        }
    }
}
