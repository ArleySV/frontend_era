package com.era.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import com.era.app.ui.theme.ColorCardBorder
import com.era.app.ui.theme.ColorDivider
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryPale
import com.era.app.ui.theme.ColorSettingsLabel
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(ColorTextWhite)
            .border(width = 1.dp, color = ColorCardBorder, shape = RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        content()
    }
}

@Composable
fun SettingsCardRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    descripcionIcono: String? = null,
    accion: (@Composable () -> Unit)? = null,
    mostrarDivisor: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icono != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorPrimaryPale, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = descripcionIcono,
                        tint = ColorPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorSettingsLabel,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    lineHeight = 22.sp,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorTextDark,
                )
            }
            accion?.invoke()
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (mostrarDivisor) {
            HorizontalDivider(thickness = 1.dp, color = ColorDivider)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Tarjeta Mi cuenta")
@Composable
private fun SettingsCardPreview() {
    ERATheme {
        SettingsCard(modifier = Modifier.padding(24.dp)) {
            SettingsCardRow(
                label = "Nombre del menor",
                value = "María López",
                icono = Icons.Filled.Person,
                descripcionIcono = "Icono del menor",
            )
            SettingsCardRow(
                label = "Nombre de usuario",
                value = "@maria_lopez",
                icono = Icons.Filled.Person,
                descripcionIcono = "Icono de usuario",
                accion = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorPrimary,
                        )
                    }
                },
            )
            SettingsCardRow(
                label = "Fecha de nacimiento",
                value = "15/03/2015",
                icono = Icons.Filled.DateRange,
                descripcionIcono = "Icono de fecha",
                mostrarDivisor = false,
            )
        }
    }
}
