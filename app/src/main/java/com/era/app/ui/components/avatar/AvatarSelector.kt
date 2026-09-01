package com.era.app.ui.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorAvatarBorderDefault
import com.era.app.ui.theme.ColorAvatarPlusBg
import com.era.app.ui.theme.ColorAvatarPlusIcon
import com.era.app.ui.theme.ColorAvatarPreset1
import com.era.app.ui.theme.ColorAvatarPreset2
import com.era.app.ui.theme.ColorAvatarPreset3
import com.era.app.ui.theme.ColorPrimary

/**
 * Selector de avatar reutilizable (D-59). Muestra los 3 presets y, opcionalmente,
 * un botón "+" para subir una foto personalizada.
 * En el registro se invoca con [mostrarMas]=false/[onMas]=null (mismo comportamiento
 * que el AvatarSelector original de RegistroPaso2Screen).
 */
@Composable
fun AvatarSelector(
    seleccionado: Int?,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mostrarMas: Boolean = false,
    onMas: (() -> Unit)? = null,
) {
    val presets = listOf(
        1 to ColorAvatarPreset1,
        2 to ColorAvatarPreset2,
        3 to ColorAvatarPreset3,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { (id, color) ->
            val isSelected = seleccionado == id
            Box(
                modifier = Modifier
                    .size(49.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                4.dp,
                                CircleShape,
                                ambientColor = ColorPrimary.copy(alpha = 0.4f),
                                spotColor = ColorPrimary.copy(alpha = 0.4f),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.5.dp, ColorPrimary, CircleShape)
                        } else {
                            Modifier.border(1.5.dp, ColorAvatarBorderDefault, CircleShape)
                        },
                    )
                    .clip(CircleShape)
                    .clickable { onSeleccionar(id) }
                    .semantics { contentDescription = "Selector de avatar $id" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(37.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$id",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (mostrarMas && onMas != null) {
            Box(
                modifier = Modifier
                    .size(49.dp)
                    .border(1.5.dp, ColorAvatarBorderDefault, CircleShape)
                    .clip(CircleShape)
                    .background(ColorAvatarPlusBg)
                    .clickable(onClick = onMas)
                    .semantics { contentDescription = "Subir avatar" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = ColorAvatarPlusIcon,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
