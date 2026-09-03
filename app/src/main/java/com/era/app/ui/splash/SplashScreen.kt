package com.era.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FRASES = listOf(
    "Educación, Repaso y Aprendizaje",
    "Aprende jugando cada día",
    "Tu aventura del saber empieza aquí",
)

@Composable
fun SplashScreen(
    onNavegarAHome: (String) -> Unit,
    onNavegarALogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is SplashEvento.NavegarAHome -> onNavegarAHome(evento.route)
                is SplashEvento.NavegarALogin -> onNavegarALogin()
            }
        }
    }

    SplashContent(modifier = modifier)
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "ERA",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = FRASES.random(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
            )
        }
    }
}
