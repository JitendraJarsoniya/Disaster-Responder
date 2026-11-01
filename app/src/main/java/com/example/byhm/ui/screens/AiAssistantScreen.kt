package com.example.byhm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byhm.data.model.AiChatMessage
import com.example.byhm.data.model.ChatSender
import com.example.byhm.theme.StatusWarningAmber

@Composable
fun AiAssistantScreen(
    messages: List<AiChatMessage>,
    inputText: String,
    isGenerating: Boolean,
    errorState: String?,
    selectedAttachmentName: String?,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFileClick: () -> Unit,
    onMicClick: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onClearChat: () -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Chat History?") },
            text = { Text("This will remove all current messages from the chat session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearChat()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Disaster Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { showClearDialog = true }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Safety Disclaimer Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = StatusWarningAmber.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = StatusWarningAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DECISION SUPPORT ONLY: AI suggestions assist emergency decisions but do not replace official emergency command.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable Chat Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(message = msg)
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is processing response...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            errorState?.let { err ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            Button(onClick = onRetry) {
                                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Attachment pill if present
        selectedAttachmentName?.let { name ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Attached: $name", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove")
                    }
                }
            }
        }

        // Input Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachFileClick) {
                Icon(imageVector = Icons.Filled.AttachFile, contentDescription = "Attach File", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onMicClick) {
                Icon(imageVector = Icons.Filled.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = { Text("Ask or describe emergency...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            IconButton(
                onClick = onSendMessage,
                enabled = inputText.isNotBlank() || selectedAttachmentName != null
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() || selectedAttachmentName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: AiChatMessage) {
    val isUser = message.sender == ChatSender.USER

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "AI Assistant",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
