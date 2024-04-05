package com.example.wordleclone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import android.widget.Toast
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {
    private lateinit var wordleApiService: WordleApiService
    private var currentWord: String = ""
    private var currentAttempt: Int = 1
    private lateinit var editTexts: Array<Array<EditText>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://wordlecloneapi.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        wordleApiService = retrofit.create(WordleApiService::class.java)

        // Load the correct word from the API and populate it into the TextView
        loadCorrectWord()

        // Initialize the EditTexts for the guesses
        initializeEditTexts()

        // Log statement to check if editTexts is initialized
        Log.d("MainActivity", "editTexts initialized: ${::editTexts.isInitialized}")

        // Set up the Enter button to submit a guess
        val btnEnter: Button = findViewById(R.id.btnEnter)
        btnEnter.setOnClickListener {
            val enteredWord = getCurrentGuess()
            if (enteredWord.length == 5) {
                checkWord(enteredWord)
            } else {
                Toast.makeText(this, "Enter a 5-letter word", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun initializeEditTexts() {
        // Initialize each EditText individually
        editTexts = Array(6) { row ->
            when (row) {
                0 -> arrayOf(
                    findViewById(R.id.txtR1Letter1),
                    findViewById(R.id.txtR1Letter2),
                    findViewById(R.id.txtR1Letter3),
                    findViewById(R.id.txtR1Letter4),
                    findViewById(R.id.txtR1Letter5)
                )
                1 -> arrayOf(
                    findViewById(R.id.txtR2Letter1),
                    findViewById(R.id.txtR2Letter2),
                    findViewById(R.id.txtR2Letter3),
                    findViewById(R.id.txtR2Letter4),
                    findViewById(R.id.txtR2Letter5)
                )
                2 -> arrayOf(
                    findViewById(R.id.txtR3Letter1),
                    findViewById(R.id.txtR3Letter2),
                    findViewById(R.id.txtR3Letter3),
                    findViewById(R.id.txtR3Letter4),
                    findViewById(R.id.txtR3Letter5)
                )
                3 -> arrayOf(
                    findViewById(R.id.txtR4Letter1),
                    findViewById(R.id.txtR4Letter2),
                    findViewById(R.id.txtR4Letter3),
                    findViewById(R.id.txtR4Letter4),
                    findViewById(R.id.txtR4Letter5)
                )
                4 -> arrayOf(
                    findViewById(R.id.txtR5Letter1),
                    findViewById(R.id.txtR5Letter2),
                    findViewById(R.id.txtR5Letter3),
                    findViewById(R.id.txtR5Letter4),
                    findViewById(R.id.txtR5Letter5)
                )
                5 -> arrayOf(
                    findViewById(R.id.txtR6Letter1),
                    findViewById(R.id.txtR6Letter2),
                    findViewById(R.id.txtR6Letter3),
                    findViewById(R.id.txtR6Letter4),
                    findViewById(R.id.txtR6Letter5)
                )
                else -> arrayOf()
            }
        }

        // Set up the text change listeners
        for (row in editTexts.indices) {
            for (col in editTexts[row].indices) {
                val editText = editTexts[row][col]
                val nextEditText = if (col < editTexts[row].size - 1) editTexts[row][col + 1] else null
                editText.addTextChangedListener(GuessTextWatcher(editText, nextEditText))
            }
        }

        // Set focus on the first EditText
        editTexts[0][0].requestFocus()
    }



    private fun getCurrentGuess(): String {
        return editTexts[0].joinToString("") { it.text.toString().trim() }
    }

    private fun checkWord(guess: String) {
        if (guess == currentWord) {
            // Guessed word matches the correct word
            val toastMessage = "Correct guess!"
            Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()

            // Update UI to reflect correct guess
            for (i in 0 until 5) {
                val editText = editTexts[currentAttempt - 1][i]
                editText.setText(guess[i].toString())
                editText.setBackgroundColor(Color.GREEN)
            }
        } else {
            // Guessed word does not match the correct word
            val toastMessage = "Wrong! Try again."
            Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
        }

        // Move the guesses down one row if applicable
        if (currentAttempt < 6) {
            for (i in 0 until 5) {
                editTexts[currentAttempt][i].text = editTexts[currentAttempt - 1][i].text
                editTexts[currentAttempt][i].setBackgroundColor(editTexts[currentAttempt - 1][i].solidColor)
            }
            // Clear the first row for the next input
            editTexts[0].forEach { it.text.clear() }
            editTexts[0][0].requestFocus()
        }

        currentAttempt++
    }






    private fun generateNewWord() {
        wordleApiService.generateWord().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    currentWord = response.body()?.get("word") ?: ""
                    Toast.makeText(this@MainActivity, "New word is $currentWord", Toast.LENGTH_SHORT).show() // for testing purposes
                } else {
                    Toast.makeText(this@MainActivity, "Failed to generate word", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCorrectWord() {
        wordleApiService.generateWord().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    currentWord = response.body()?.get("word") ?: ""
                    findViewById<TextView>(R.id.txtCorrectWord).text = currentWord
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load correct word", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class GuessTextWatcher(private val currentEditText: EditText, private val nextEditText: EditText?) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s != null && s.length == 1 && nextEditText != null) {
                nextEditText.requestFocus()
            }
        }
    }


    private interface WordleApiService {
        @GET("wordle/generateWord")
        fun generateWord(): Call<Map<String, String>>

        @POST("wordle/checkWord")
        fun checkWord(@Body guessedWord: String): Call<Map<String, String>>
    }
}
