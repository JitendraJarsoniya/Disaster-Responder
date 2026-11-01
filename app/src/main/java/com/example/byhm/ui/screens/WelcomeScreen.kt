package com.example.byhm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun WelcomeScreen(
    onExploreClicked: () -> Unit,
    onLoginClicked: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkMainBackground,
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
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Upper Section: Shield Logo & Branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // Cinematic Illuminated Shield Emblem
                    Box(
                        modifier = Modifier
                            .size(100.dp)
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
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Disaster Responder Logo",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // App Title
                    Text(
                        text = "Disaster Responder",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        color = DarkPrimaryText,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tagline
                    Text(
                        text = "Be Ready. Stay Safe. Get Help.",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = DarkPrimaryEmergencyRed,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Bottom Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = onExploreClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimaryEmergencyRed,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "Explore App as Guest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onLoginClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkRedBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkPrimaryText
                        )
                    ) {
                        Text(
                            text = "Sign In / Role Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
