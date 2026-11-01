package com.example.byhm.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.repository.EmergencyProfileData
import com.example.byhm.data.repository.EmergencyProfileRepository
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusInfoBlue

@Composable
fun EmergencyProfileScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { EmergencyProfileRepository(context) }
    val savedProfile = remember { repository.getProfile() }

    var fullName by remember { mutableStateOf(savedProfile?.fullName ?: "") }
    var mobileNumber by remember { mutableStateOf(savedProfile?.mobileNumber ?: "") }
    var emergencyContact by remember { mutableStateOf(savedProfile?.emergencyContact ?: "") }
    var medicalNotes by remember { mutableStateOf(savedProfile?.medicalNotes ?: "") }
    var photoUris by remember { mutableStateOf(savedProfile?.photoUris ?: emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            if (photoUris.size < 3) {
                photoUris = photoUris + newUri.toString()
            } else {
                Toast.makeText(context, "Maximum 3 reference photos allowed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Emergency Profile & Contacts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy Info Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = StatusInfoBlue.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = StatusInfoBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved information is stored securely on your phone and shared strictly with authorized Command Center Admins when Emergency SOS is triggered.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Form Fields
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name *") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Phone Number *") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = emergencyContact,
            onValueChange = { emergencyContact = it },
            label = { Text("Emergency Contact Phone Number *") },
            leadingIcon = { Icon(imageVector = Icons.Filled.ContactPage, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicalNotes,
            onValueChange = { medicalNotes = it },
            label = { Text("Medical / Emergency Notes (Optional)") },
            placeholder = { Text("e.g. Blood Group O+, Asthma, Severe Allergies...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Reference Photos Section
        Text("Emergency Reference Photos (Optional - Max 3):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            photoUris.forEachIndexed { index, _ ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Photo ${index + 1}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Center))
                        IconButton(
                            onClick = { photoUris = photoUris.filterIndexed { i, _ -> i != index } },
                            modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove Photo", tint = StatusCriticalRed)
                        }
                    }
                }
            }

            if (photoUris.size < 3) {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = "Add Photo")
                }
            }
        }

        errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = err, color = StatusCriticalRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = {
                if (fullName.isBlank()) {
                    errorMessage = "Please enter your Full Name."
                    return@Button
                }
                if (mobileNumber.isBlank() || mobileNumber.length < 10) {
                    errorMessage = "Please enter a valid 10-digit Mobile Number."
                    return@Button
                }
                if (emergencyContact.isBlank() || emergencyContact.length < 10) {
                    errorMessage = "Please enter a valid Emergency Contact Number."
                    return@Button
                }

                errorMessage = null
                repository.saveProfile(
                    EmergencyProfileData(
                        fullName = fullName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        emergencyContact = emergencyContact.trim(),
                        medicalNotes = medicalNotes.trim(),
                        photoUris = photoUris
                    )
                )
                Toast.makeText(context, "Emergency Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                onBackClick()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Emergency Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (savedProfile != null) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    repository.deleteProfile()
                    fullName = ""
                    mobileNumber = ""
                    emergencyContact = ""
                    medicalNotes = ""
                    photoUris = emptyList()
                    Toast.makeText(context, "Emergency Profile Deleted.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCriticalRed)
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Saved Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
