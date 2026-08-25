package com.era.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun CompactGreenHeader(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorPrimary)
            .heightIn(min = 104.dp)
            .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 20.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = ColorTextWhite
        )
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyLarge,
            color = ColorTextWhite
        )
    }
}

@Preview(showBackground = true, name = "Cabecera registro paso 1")
@Composable
private fun CompactGreenHeaderPreview() {
    ERATheme {
        CompactGreenHeader(titulo = "Registro - Paso 1 de 3", subtitulo = "Datos de usuario")
    }
}
