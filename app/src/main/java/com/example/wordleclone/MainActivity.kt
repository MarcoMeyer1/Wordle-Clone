package com.example.wordleclone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editTexts = arrayOf(
            findViewById<EditText>(R.id.txtR1Letter1),
            findViewById<EditText>(R.id.txtR1Letter2),
            findViewById<EditText>(R.id.txtR1Letter3),
            findViewById<EditText>(R.id.txtR1Letter4),
            findViewById<EditText>(R.id.txtR1Letter5)
        )

        // Set focus to the first EditText
        editTexts[0].requestFocus()

        // Add TextWatcher for each EditText
        for (i in 0 until editTexts.size - 1) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString()?.uppercase()
                    if (text?.length == 1) {
                        editTexts[i + 1].requestFocus()
                    }
                }
            })

            val btnEnter: Button = findViewById(R.id.btnEnter)


            btnEnter.setOnClickListener {

                val enteredWord = editTexts.joinToString("") { it.text.toString() }
                Toast.makeText(this, "Entered Word: $enteredWord", Toast.LENGTH_SHORT).show()
            }
        }
    }


}