package com.example.byhm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.UserProfile
import com.example.byhm.data.model.UserRole
import com.example.byhm.data.repository.AppLanguage
import com.example.byhm.data.repository.AppThemeMode

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfile?,
    currentThemeMode: AppThemeMode,
    currentLanguage: AppLanguage,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Profile & System Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        userProfile?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Role: ${user.role.displayName}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. App Theme Switching Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("App Appearance Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = currentThemeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    label = { Text(mode.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Language Selection Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("App Language / भाषा चुनें", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AppLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = currentLanguage == lang,
                    onClick = { onLanguageChange(lang) },
                    label = { Text(lang.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Role Switcher for Testing
        Text("Switch User Role (Demo Testing)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onSwitchRole(UserRole.RESIDENT) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Public")
            }
            Button(
                onClick = { onSwitchRole(UserRole.RESPONDER) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Responder")
            }
            Button(
                onClick = { onSwitchRole(UserRole.ADMIN) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Admin")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Sign Out")
        }
    }
}
