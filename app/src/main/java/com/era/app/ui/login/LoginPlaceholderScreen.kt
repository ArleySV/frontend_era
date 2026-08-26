package com.era.app.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.era.app.ui.theme.ERATheme

@Composable
fun LoginPlaceholderScreen(
    onNavigateToRegistro: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Button(onClick = onNavigateToRegistro) {
            Text(text = "Regístrate")
        }
    }
}

@Preview(showBackground = true, name = "Login placeholder")
@Composable
private fun LoginPlaceholderPreview() {
    ERATheme {
        LoginPlaceholderScreen(onNavigateToRegistro = {})
    }
}
