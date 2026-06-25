package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.repository.DepthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class DepthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DepthRepository(AppDatabase.getDatabase(application).sessionDao())
    
    val allSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    val currentSessionMessages: StateFlow<List<ChatMessage>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessagesForSession(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSession: StateFlow<ChatSession?> = combine(_selectedSessionId, allSessions) { id, sessions ->
        sessions.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _currentSummary = MutableStateFlow<String?>(null)
    val currentSummary: StateFlow<String?> = _currentSummary.asStateFlow()

    init {
        // Auto-select the first session if available
        viewModelScope.launch {
            allSessions.collectFirst { sessions ->
                if (sessions.isNotEmpty() && _selectedSessionId.value == null) {
                    _selectedSessionId.value = sessions.first().id
                }
            }
        }
    }

    private suspend fun <T> Flow<T>.collectFirst(action: suspend (T) -> Unit) {
        take(1).collect(action)
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
        _currentSummary.value = null
    }

    fun createNewSession() {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            repository.createSession(newId, "New Inquiry")
            _selectedSessionId.value = newId
            _currentSummary.value = null
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                _selectedSessionId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun sendMessage(content: String) {
        val sessionId = _selectedSessionId.value ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            _isThinking.value = true
            try {
                // 1. Save user message
                repository.saveUserMessage(sessionId, content)
                
                // 2. Fetch AI response and save it
                repository.fetchAiResponse(sessionId)
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun generateClaritySummary() {
        val sessionId = _selectedSessionId.value ?: return
        viewModelScope.launch {
            _isSummarizing.value = true
            _currentSummary.value = null
            try {
                val summary = repository.generateClaritySummary(sessionId)
                _currentSummary.value = summary
            } finally {
                _isSummarizing.value = false
            }
        }
    }

    fun clearCurrentSummary() {
        _currentSummary.value = null
    }
}
