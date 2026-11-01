package com.example.byhm.data.model

enum class ChatSender {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AiChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: String,
    val attachmentName: String? = null,
    val attachmentUri: String? = null,
    val isError: Boolean = false
)
