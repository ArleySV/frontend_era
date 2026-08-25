package com.era.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorBorderInfo
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ERATheme
import com.era.app.ui.theme.RadiusInfoBox

@Composable
fun InfoBox(
    texto: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = ColorTextDark,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = ColorBorderInfo, shape = RoundedCornerShape(RadiusInfoBox))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Preview(showBackground = true, name = "InfoBox contraseña")
@Composable
private fun InfoBoxPreview() {
    ERATheme {
        InfoBox(
            texto = "La contraseña no puede contener tu nombre, ser una palabra del diccionario ni ser igual al usuario",
            modifier = Modifier.padding(16.dp)
        )
    }
}
