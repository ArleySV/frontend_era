package com.era.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryLight
import com.era.app.ui.theme.ColorTextBody
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.ui.theme.RadiusBtnReg

private val FormaReg = RoundedCornerShape(RadiusBtnReg)

@Composable
fun EraRegPrimaryButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = Icons.AutoMirrored.Outlined.ArrowForward
) {
    val sombraTeal = ColorPrimary.copy(alpha = 0.4f)
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = FormaReg,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimary,
            contentColor = ColorTextWhite
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .shadow(elevation = 6.dp, shape = FormaReg, ambientColor = sombraTeal, spotColor = sombraTeal)
            .widthIn(min = 170.dp)
            .height(44.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        if (icono != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun EraRegSecondaryButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = Icons.Outlined.Close
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = FormaReg,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimaryLight,
            contentColor = ColorTextBody
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .widthIn(min = 170.dp)
            .height(44.dp)
    ) {
        if (icono != null) {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = texto, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, name = "Botones registro")
@Preview(showBackground = true, name = "Botones registro deshabilitados")
@Composable
private fun EraRegButtonsPreview() {
    ERATheme {
        Column {
            EraRegPrimaryButton(texto = "Continuar", onClick = {})
            EraRegSecondaryButton(
                texto = "Cancelar",
                onClick = {},
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
