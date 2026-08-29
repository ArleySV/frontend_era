package com.era.app.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun HomePlaceholderScreen(
    onCerrarSesion: () -> Unit,
    onNavigatePerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sesión iniciada ✅",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextDark,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Home placeholder — Fase 10",
                fontSize = 16.sp,
                color = ColorTextDark,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigatePerfil,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorTextWhite,
                ),
            ) {
                Text(text = "Mi cuenta", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorError,
                    contentColor = ColorTextWhite,
                ),
            ) {
                Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Placeholder")
@Composable
private fun HomePlaceholderPreview() {
    ERATheme {
        HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = {})
    }
}
