package com.example.byhm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byhm.data.model.AiChatMessage
import com.example.byhm.data.model.ChatSender
import com.example.byhm.data.repository.AiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiViewModel(
    private val repository: AiRepository = AiRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                id = "msg_init",
                sender = ChatSender.ASSISTANT,
                text = "Hello! I am your AI Disaster Assistant. How can I help you today? You can ask about emergency guidance, first-aid advice, or flood/fire safety steps.",
                timestamp = getCurrentTime()
            )
        )
    )
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedAttachmentName = MutableStateFlow<String?>(null)
    val selectedAttachmentName: StateFlow<String?> = _selectedAttachmentName.asStateFlow()

    private val _selectedAttachmentUri = MutableStateFlow<String?>(null)
    val selectedAttachmentUri: StateFlow<String?> = _selectedAttachmentUri.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun attachFile(name: String, uri: String) {
        _selectedAttachmentName.value = name
        _selectedAttachmentUri.value = uri
    }

    fun removeAttachment() {
        _selectedAttachmentName.value = null
        _selectedAttachmentUri.value = null
    }

    fun sendMessage() {
        val userQuery = _inputText.value.trim()
        val attachmentName = _selectedAttachmentName.value
        val attachmentUri = _selectedAttachmentUri.value

        if (userQuery.isEmpty() && attachmentName == null) return

        val userMessage = AiChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = ChatSender.USER,
            text = userQuery.ifEmpty { "[Attached File: $attachmentName]" },
            timestamp = getCurrentTime(),
            attachmentName = attachmentName,
            attachmentUri = attachmentUri
        )

        val historyPairs = _messages.value.map {
            (if (it.sender == ChatSender.USER) "user" else "assistant") to it.text
        }

        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        removeAttachment()
        _isGenerating.value = true
        _errorState.value = null

        viewModelScope.launch {
            try {
                delay(300)
                val responseText = repository.generateConversationalResponse(userQuery, attachmentName, historyPairs)
                val assistantMessage = AiChatMessage(
                    id = "msg_ai_${System.currentTimeMillis()}",
                    sender = ChatSender.ASSISTANT,
                    text = responseText,
                    timestamp = getCurrentTime()
                )
                _messages.value = _messages.value + assistantMessage
            } catch (e: Exception) {
                _errorState.value = "Failed to connect to AI Assistant. Please check network connection."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun retryLastMessage() {
        val lastUserMsg = _messages.value.lastOrNull { it.sender == ChatSender.USER }
        if (lastUserMsg != null) {
            _errorState.value = null
            _isGenerating.value = true
            val historyPairs = _messages.value.map {
                (if (it.sender == ChatSender.USER) "user" else "assistant") to it.text
            }
            viewModelScope.launch {
                try {
                    delay(300)
                    val responseText = repository.generateConversationalResponse(lastUserMsg.text, lastUserMsg.attachmentName, historyPairs)
                    val assistantMessage = AiChatMessage(
                        id = "msg_ai_${System.currentTimeMillis()}",
                        sender = ChatSender.ASSISTANT,
                        text = responseText,
                        timestamp = getCurrentTime()
                    )
                    _messages.value = _messages.value + assistantMessage
                } catch (e: Exception) {
                    _errorState.value = "Retry failed. Please check network connection."
                } finally {
                    _isGenerating.value = false
                }
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            AiChatMessage(
                id = "msg_init_${System.currentTimeMillis()}",
                sender = ChatSender.ASSISTANT,
                text = "Chat history cleared. How else can I assist you with emergency information?",
                timestamp = getCurrentTime()
            )
        )
        _errorState.value = null
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
