package com.era.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryPale
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.ui.theme.RadiusInputReg

private val FormaInput = RoundedCornerShape(RadiusInputReg)

@Composable
fun EraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    obligatorio: Boolean = false,
    placeholder: String? = null,
    error: String? = null,
    textoAyuda: String? = null,
    iconoInicio: ImageVector? = null,
    iconoFin: ImageVector? = null,
    descripcionIconoFin: String? = null,
    onIconoFinClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth().widthIn(max = 358.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTextDark
            )
            if (obligatorio) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorError
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = ColorTextDark),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(ColorPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .clip(FormaInput)
                .background(ColorPrimaryPale),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .heightIn(min = 53.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (iconoInicio != null) {
                        Icon(
                            imageVector = iconoInicio,
                            contentDescription = null,
                            tint = ColorTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ColorTextMuted,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                    if (iconoFin != null) {
                        IconButton(
                            onClick = onIconoFinClick ?: {},
                            enabled = onIconoFinClick != null,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = iconoFin,
                                contentDescription = descripcionIconoFin,
                                tint = ColorTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        )
        val mensajeApoyo = error ?: textoAyuda
        if (mensajeApoyo != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mensajeApoyo,
                style = MaterialTheme.typography.labelMedium,
                color = if (error != null) ColorError else ColorTextMuted
            )
        }
    }
}

@Preview(showBackground = true, name = "Campo normal")
@Composable
private fun EraTextFieldNormalPreview() {
    ERATheme {
        Column(modifier = Modifier.padding(16.dp)) {
            EraTextField(
                value = "",
                onValueChange = {},
                label = "Correo electrónico",
                obligatorio = true,
                placeholder = "correo@ejemplo.com"
            )
        }
    }
}

@Preview(showBackground = true, name = "Campo con error")
@Composable
private fun EraTextFieldErrorPreview() {
    ERATheme {
        Column(modifier = Modifier.padding(16.dp)) {
            EraTextField(
                value = "abc",
                onValueChange = {},
                label = "Código de verificación",
                obligatorio = true,
                error = "Ingresa 6 dígitos numéricos"
            )
        }
    }
}
