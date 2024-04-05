package com.example.wordleclone

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.Button
import android.widget.Toast
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.inputmethod.EditorInfo
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
    private lateinit var allGuessRows: Array<Array<EditText>>

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

        // Set up the Enter button to submit a guess
        val btnEnter: Button = findViewById(R.id.btnEnter)
        btnEnter.setOnClickListener {
            val enteredWord = getCurrentGuess()
            if (enteredWord.length == 5) {
                checkWord(enteredWord)
            } else {

            }
        }
    }






    // This method sets up all EditTexts and assigns TextWatchers to them.
    private fun initializeEditTexts() {
        allGuessRows = Array(6) { row ->
            Array(5) { col ->
                val editTextId = resources.getIdentifier("txtR${row + 1}Letter${col + 1}", "id", packageName)
                findViewById<EditText>(editTextId).apply {
                    imeOptions = if (col < 4) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_DONE
                    addTextChangedListener(GuessTextWatcher(this, row, col))
                }
            }
        }
    }


    inner class GuessTextWatcher(private val currentEditText: EditText, private val currentRow: Int, private val currentCol: Int) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // Check if the change is because of the user typing in this particular box
            if (currentRow == currentAttempt - 1 && s != null) {
                if (s.isNotEmpty() && currentCol < 4) {
                    // Advance to the next box if there is a next box in the row
                    allGuessRows[currentRow][currentCol + 1].requestFocus()
                }
            }
        }
    }

    private fun getCurrentGuess(): String {
        return allGuessRows[currentAttempt].joinToString("") { it.text.toString().trim() }
    }


    private fun checkWord(guess: String) {
        // Check the guess with your logic
        if (guess == currentWord) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
        }

        allGuessRows[currentAttempt - 1].forEach { it.isEnabled = false }

        // Move to the next attempt if available
        currentAttempt++
        if (currentAttempt <= 6) {
            prepareNextAttempt()
        } else {
            // No more attempts left
            Toast.makeText(this, "No more attempts left!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun lockCurrentRow() {
        // Disable the current row to prevent further edits
        allGuessRows[currentAttempt].forEach { it.isEnabled = false }
    }

    private fun updateCurrentAttemptUI(guess: String) {
        for (i in 0 until 5) {
            val currentEditText = allGuessRows[currentAttempt - 1][i]
            currentEditText.setText(guess[i].toString())
            // Update background color based on the feedback
            // Use the actual game's logic to set the correct color
            currentEditText.setBackgroundColor(Color.GRAY)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun prepareNextAttempt() {
        if (currentAttempt <= 6) {
            // Clear any old listeners (important if the game restarts without restarting the activity)
            allGuessRows[currentAttempt - 1].forEach { editText ->
                editText.text.clear()
                editText.isEnabled = true
                editText.background = resources.getDrawable(android.R.drawable.edit_text)
            }

            // Request focus on the first EditText of the new current attempt
            allGuessRows[currentAttempt - 1][0].requestFocus()
        }
    }

    private fun clearTopRow() {
        // Clear the first row for the next input
        allGuessRows[0].forEach { it.text.clear() }
        allGuessRows[0][0].requestFocus()
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




    private interface WordleApiService {
        @GET("wordle/generateWord")
        fun generateWord(): Call<Map<String, String>>

        @POST("wordle/checkWord")
        fun checkWord(@Body guessedWord: String): Call<Map<String, String>>
    }
}
