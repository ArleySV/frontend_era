package com.era.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorSurface
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun LoginInputPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = ColorTextMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = ColorTextMuted,
                modifier = Modifier.size(16.dp),
            )
        },
        trailingIcon = if (trailingIcon != null && onTrailingIconClick != null) {
            {
                IconButton(
                    onClick = onTrailingIconClick,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Mostrar/ocultar contraseña",
                        tint = ColorTextMuted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        } else null,
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(25.5.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ColorTextWhite,
            unfocusedContainerColor = ColorTextWhite,
            focusedBorderColor = if (isError) ColorError else ColorSurface,
            unfocusedBorderColor = if (isError) ColorError else ColorSurface,
            cursorColor = ColorPrimary,
        ),
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "Login Input Pill")
@Composable
private fun LoginInputPillPreview() {
    ERATheme {
        LoginInputPill(
            value = "",
            onValueChange = {},
            placeholder = "ID/E-mail",
            leadingIcon = EraIcons.EmailOutline,
        )
    }
}
