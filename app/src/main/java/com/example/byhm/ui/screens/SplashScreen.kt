package com.example.byhm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byhm.theme.DarkDeepRedSurface
import com.example.byhm.theme.DarkMainBackground
import com.example.byhm.theme.DarkPrimaryEmergencyRed
import com.example.byhm.theme.DarkPrimaryText
import com.example.byhm.theme.DarkRedBorder

@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkMainBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkMainBackground,
                            DarkDeepRedSurface.copy(alpha = 0.6f),
                            DarkMainBackground
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Shield Emblem
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            color = DarkPrimaryEmergencyRed.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DarkPrimaryEmergencyRed,
                                    DarkDeepRedSurface
                                )
                            )
                        )
                        .border(2.dp, DarkRedBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Disaster Responder Logo",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Disaster Responder",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    color = DarkPrimaryText,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Restoring Command Center Session...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkPrimaryEmergencyRed,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(
                    color = DarkPrimaryEmergencyRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
