package com.example.wordleclone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var wordleApiService: WordleApiService
    private var currentWord: String = ""

    private lateinit var txtCorrectWord: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://wordlecloneapi.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        wordleApiService = retrofit.create(WordleApiService::class.java)

        val editTexts = arrayOf(
            findViewById<EditText>(R.id.txtR1Letter1),
            findViewById<EditText>(R.id.txtR1Letter2),
            findViewById<EditText>(R.id.txtR1Letter3),
            findViewById<EditText>(R.id.txtR1Letter4),
            findViewById<EditText>(R.id.txtR1Letter5)
        )

        txtCorrectWord = findViewById(R.id.txtCorrectWord)


        editTexts[0].requestFocus()
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (it.length == 1 && i < editTexts.lastIndex) {
                            editTexts[i + 1].requestFocus()
                        }
                    }
                }
            })
        }

        findViewById<Button>(R.id.btnEnter).setOnClickListener {
            val enteredWord = editTexts.joinToString("") { it.text.toString() }.uppercase()
            checkWord(enteredWord)
        }
    }

    override fun onStart() {
        super.onStart()
        generateNewWord()
    }

    private fun generateNewWord() {
        wordleApiService.generateWord().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    currentWord = response.body()?.get("word") ?: ""
                    // Update the TextView with the correct word
                    txtCorrectWord.text = currentWord
                } else {
                    Toast.makeText(this@MainActivity, "Failed to generate word", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkWord(guessedWord: String) {
        wordleApiService.checkWord(guessedWord).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val feedback = response.body()?.get("feedback") ?: "Error"
                    Toast.makeText(this@MainActivity, feedback, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid word submitted", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private interface WordleApiService {
        @GET("wordle/generateWord")
        fun generateWord(): Call<Map<String, String>>

        @POST("wordle/checkWord")
        fun checkWord(@Body guessedWord: String): Call<Map<String, String>>
    }
}
