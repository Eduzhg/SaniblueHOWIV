package com.saniblue.app.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saniblue.app.BuildConfig
import com.saniblue.app.R
import com.saniblue.app.presentation.theme.SaniblueBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(600))
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(1800)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaniblueBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_saniblue),
                    contentDescription = "Logo Saniblue",
                    modifier = Modifier.size(84.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "SANIBLUE",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Text(
                text = "Metrologia",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Sistema de Verificação\nMetrológica de Hidrômetros",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Versão ${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
