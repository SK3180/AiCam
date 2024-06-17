package com.sksingh.radr.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(application.applicationContext, this)
    }

    override fun onCleared() {
        textToSpeech?.shutdown()
        super.onCleared()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }

    fun processImage(bitmap: Bitmap, onResult: (String) -> Unit) {

        // due to time issue i have used the api key directly...
        val apiKey = "AIzaSyDE0bDOAeM6-qULjT-9VJvOj-SZm25pRRg"

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )

        val inputContent = content {
            image(bitmap)
            text("tell me about these details Product Name - ,Description - , Colour - , Pattern - ,")
        }

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(inputContent)
                val jsonResponse = parseResponse(response)
                storeResponseInFirebase(jsonResponse)
                onResult(response.text.toString())
                speakOut(response.text.toString())
            } catch (e: Exception) {
                onResult("Failed to process image: ${e.message}")
            }
        }
    }

    private fun parseResponse(response: GenerateContentResponse): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("productName", response.text)
        jsonObject.addProperty("description", response.text)
        jsonObject.addProperty("color", response.text)
        jsonObject.addProperty("pattern", response.text)
        return jsonObject.toString()
    }

    private fun storeResponseInFirebase(jsonResponse: String) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject

        val productName = jsonObject.get("productName")?.asString ?: ""
        val description = jsonObject.get("description")?.asString ?: ""
        val color = jsonObject.get("color")?.asString ?: ""
        val pattern = jsonObject.get("pattern")?.asString ?: ""

        val formattedResponse = """
            Product Name: $productName
            Description: $description
            Color: $color
            Pattern: $pattern
        """.trimIndent()

        val product = hashMapOf(
            "productName" to productName,
            "description" to description,
            "color" to color,
            "pattern" to pattern
        )

        firebaseFirestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Log.d("Firebase", "Product added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error adding product", e)
            }

        Log.d("Camera", "Formatted Response:\n$formattedResponse")
    }

    private fun speakOut(text: String) {
        if (textToSpeech == null) {
            Log.e("TTS", "TextToSpeech not initialized")
            return
        }

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
