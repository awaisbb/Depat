package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.local.SessionDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DepthRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<ChatSession>> = sessionDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return sessionDao.getMessagesForSession(sessionId)
    }

    suspend fun createSession(id: String, title: String) = withContext(Dispatchers.IO) {
        sessionDao.insertSession(ChatSession(id = id, title = title))
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.deleteSession(sessionId)
        sessionDao.deleteMessagesForSession(sessionId)
    }

    suspend fun saveUserMessage(sessionId: String, content: String) = withContext(Dispatchers.IO) {
        val message = ChatMessage(sessionId = sessionId, role = "user", content = content)
        sessionDao.insertMessage(message)

        // If the session title is still default, let's update it to a short preview of the message
        val session = sessionDao.getSessionById(sessionId)
        if (session != null && (session.title == "New Inquiry" || session.title.isBlank())) {
            val words = content.split(" ")
            val shortTitle = if (words.size > 4) {
                words.take(4).joinToString(" ") + "..."
            } else {
                content
            }
            sessionDao.updateSession(session.copy(title = shortTitle))
        }
    }

    suspend fun fetchAiResponse(sessionId: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val errorMsg = "Gemini API Key is not configured. Please add your key in the AI Studio Secrets panel."
            sessionDao.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = errorMsg
                )
            )
            return@withContext errorMsg
        }

        val messages = sessionDao.getMessagesForSessionSync(sessionId)
        
        // Prepare contents history for Gemini API
        val contents = messages.map { msg ->
            Content(
                role = msg.role,
                parts = listOf(Part(text = msg.content))
            )
        }

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = """
                        You are Depth, a warm, calm, and wise thinking companion. Your purpose is to help the user find clarity on what they are wrestling with (e.g. a decision, feeling, or life question).
                        
                        CRITICAL INSTRUCTIONS:
                        1. NEVER give direct answers, advice, solutions, or suggestions. Do NOT tell them what to do.
                        2. Do NOT act like a therapist, and do NOT act like a standard conversational chatbot. Speak like a wise, compassionate, and quiet friend.
                        3. Actively listen and mirror the user's language and feelings back gently to make them feel deeply heard and validated.
                        4. Always ask exactly one powerful, thoughtful, open-ended question at a time to prompt further self-reflection and exploration.
                        5. Keep your responses brief, gentle, and highly focused, leaving plenty of space for the user's thoughts.
                    """.trimIndent()
                )
            )
        )

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            ),
            systemInstruction = systemInstruction
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                sessionDao.insertMessage(
                    ChatMessage(
                        sessionId = sessionId,
                        role = "model",
                        content = responseText
                    )
                )
                return@withContext responseText
            } else {
                val errorMsg = "Depth is in quiet contemplation. (No response received)"
                sessionDao.insertMessage(
                    ChatMessage(
                        sessionId = sessionId,
                        role = "model",
                        content = errorMsg
                    )
                )
                return@withContext errorMsg
            }
        } catch (e: Exception) {
            Log.e("DepthRepository", "Error fetching AI response", e)
            val errorMsg = "I felt a ripple in our connection: ${e.localizedMessage ?: "Unknown error"}. Let's try reflecting on this again."
            sessionDao.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = errorMsg
                )
            )
            return@withContext errorMsg
        }
    }

    suspend fun generateClaritySummary(sessionId: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key missing. Cannot generate summary."
        }

        val messages = sessionDao.getMessagesForSessionSync(sessionId)
        if (messages.isEmpty()) return@withContext "Our canvas is still empty."

        val conversationText = messages.joinToString("\n") { msg ->
            "${if (msg.role == "user") "User" else "Companion"}: ${msg.content}"
        }

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = """
                        You are Depth, reflecting back a warm, profound, and structured synthesis of what the user has discovered or is wrestling with during this self-inquiry session.
                        
                        CRITICAL INSTRUCTION:
                        - Do NOT tell them what to do.
                        - Do NOT give generic advice.
                        - Reflect back what they seem to believe, feel, or need based strictly on the conversation.
                        - Be warm, gentle, and poetic. Speak directly to them as a supportive, wise friend.
                        - Format your output with clear, beautiful headings, bullet points, or sections (e.g. "What you are holding", "Underlying beliefs", "Emerging clarity").
                    """.trimIndent()
                )
            )
        )

        val promptContent = Content(
            parts = listOf(
                Part(
                    text = "Here is our complete conversation. Please synthesize my thoughts and help me see my own clarity:\n\n$conversationText"
                )
            )
        )

        val request = GeminiRequest(
            contents = listOf(promptContent),
            generationConfig = GenerationConfig(temperature = 0.5f),
            systemInstruction = systemInstruction
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val summaryText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!summaryText.isNullOrBlank()) {
                val session = sessionDao.getSessionById(sessionId)
                if (session != null) {
                    sessionDao.updateSession(session.copy(summary = summaryText))
                }
                return@withContext summaryText
            } else {
                return@withContext "I sat in silence and couldn't formulate a reflection just yet."
            }
        } catch (e: Exception) {
            Log.e("DepthRepository", "Error generating clarity summary", e)
            return@withContext "I was unable to complete the synthesis: ${e.localizedMessage}"
        }
    }
}
