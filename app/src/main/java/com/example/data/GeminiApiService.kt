package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiService {
    private const val TAG = "GeminiApiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a chat prompt or history along with an optional system instruction to the Gemini API.
     */
    suspend fun generateContent(
        history: List<ChatMessage>,
        systemInstruction: String = "You are Auden, a hyper-intelligent, friendly AI High School Librarian. You help students summarize books, answer homework questions, create interactive quizzes and flashcards, and recommend literature."
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Gemini API key is not configured. Using local AI Assistant simulation.")
                return@withContext simulateOfflineResponse(history)
            }
            return@withContext "AI service is not configured for this build."
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        try {
            // Build the JSON body manually using standard org.json framework for rock-solid stability
            val root = JSONObject()
            
            // System instructions (optional on Gemini)
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            sysPartsArray.put(JSONObject().put("text", systemInstruction))
            sysInstructionObj.put("parts", sysPartsArray)
            root.put("systemInstruction", sysInstructionObj)

            // Conversation history / current prompt
            val contentsArray = JSONArray()
            history.forEach { msg ->
                val contentObj = JSONObject()
                val roleStr = if (msg.sender == "user") "user" else "model"
                contentObj.put("role", roleStr)

                val partsArray = JSONArray()
                val partObj = JSONObject()
                
                // If there's an attachment, let's prepend details about the in-app document
                val textWithAttachment = if (msg.attachmentName != null) {
                    "[ATTACHMENT: Name = ${msg.attachmentName}, Mime = ${msg.attachmentMime}]\n${msg.content}"
                } else {
                    msg.content
                }
                
                partObj.put("text", textWithAttachment)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            root.put("contents", contentsArray)

            val jsonString = root.toString()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Sending Gemini request with ${history.size} message(s)")
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(jsonString.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Gemini response code: ${response.code}")
                }

                if (response.isSuccessful && bodyString != null) {
                    val respJson = JSONObject(bodyString)
                    val candidates = respJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        if (content != null) {
                            val parts = content.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                return@withContext parts.getJSONObject(0).optString("text", "Let me check that.")
                            }
                        }
                    }
                }
                
                return@withContext "Error: Failed to fetch valid response from AI services (Code ${response.code})."
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Gemini content generation failed")
            }
            return@withContext "Network Error: AI service is temporarily unavailable. Preserving offline mode..."
        }
    }

    /**
     * Offline simulation mode if Gemini API key is missing or internet is down.
     */
    private fun simulateOfflineResponse(history: List<ChatMessage>): String {
        val lastMessage = history.lastOrNull()?.content?.lowercase() ?: ""
        
        // Let's generate clever, fully contextual educational simulation responses based on trigger words
        return when {
            lastMessage.contains("quiz") -> {
                """### 📝 Practice Quiz: *To Kill a Mockingbird*
Generated especially for you! Let's test your reading comprehension.

1. **Who is the narrator of the novel?**
   - A) Scout Finch
   - B) Jem Finch
   - C) Atticus Finch
   - D) Dill Harris

2. **In what fictional town does the story take place?**
   - A) Oxford, Mississippi
   - B) Maycomb, Alabama
   - C) Monroeville, Georgia
   - D) Atlanta, Georgia

*Reply with your answers (e.g., 'Answers: 1A, 2B') and I'll grade them instantly!*"""
            }
            lastMessage.contains("flashcard") || lastMessage.contains("card") -> {
                """### 📇 Flashcards: Literary Devices
Here are highly visual cards to help study for your upcoming English finals:

**Card 1/3 (Term):** **Simile**
*Definition:* A figure of speech involving the comparison of one thing with another of a different kind, used to make a description more vivid (using 'like' or 'as').
*Example:* "Her eyes were as bright as stars."

**Card 2/3 (Term):** **Metaphor**
*Definition:* A figure of speech in which a word or phrase is applied to an object or action to which it is not literally applicable.
*Example:* "The classroom was a zoo during group time."

**Card 3/3 (Term):** **Foreshadowing**
*Definition:* A warning or indication of a future event.
*Example:* Weather in Mary Shelley's *Frankenstein* signaling danger."""
            }
            lastMessage.contains("summarize") || lastMessage.contains("summary") -> {
                """### 📚 Custom Book Summary: *Hamlet* (by William Shakespeare)

- **Act I**: Hamlet, Prince of Denmark, meets the ghost of his deceased father who reveals he was murdered by Claudius (Hamlet's uncle, now King).
- **Act II**: Hamlet simulates madness to gather intelligence. He arranges a play to capture Claudius's reaction.
- **Act III**: Hamlet delivers his famous "To be or not to be" monologue. Claudius's guilt is revealed during the play performance.
- **Act IV**: Hamlet is banished to England but returns as tragedies spiral out of control.
- **Act V**: The famous duel. Almost all major characters fall to poison and steel as Prince Fortinbras of Norway succeeds to the throne.

*Key Theme*: Delay of action, revenge, moral corruption, and existential crisis."""
            }
            lastMessage.contains("recommend") || lastMessage.contains("what should i read") -> {
                """### 🔍 My Personal Book Recommendations

Based on high-school literary catalogs, I highly recommend:
1. **The Great Gatsby** by F. Scott Fitzgerald (Themes: Wealth, Class, The American Dream)
2. **Frankenstein** by Mary Shelley (Themes: Ethics, Creation, Alienation)
3. **Fahrenheit 451** by Ray Bradbury (Themes: Censorship, Media, Critical Thinking)

*Would you like me to reserve any of these for you locally to checkout?*"""
            }
            else -> {
                """Hello! I'm **Auden**, your AI High School Librarian. 

I can assist with:
1. **Summarizing books** (e.g., *"Summarize Hamlet"*)
2. **Helping with homework** and answering difficult concept questions
3. **Generating interactive quizzes** (e.g., *"Create a To Kill a Mockingbird quiz"*)
4. **Creating study flashcards** (e.g., *"Generate some flashcards on literary terms"*)
5. **Recommending highly relevant literature**

*How can I assist your reading journey today?*"""
            }
        }
    }
}
