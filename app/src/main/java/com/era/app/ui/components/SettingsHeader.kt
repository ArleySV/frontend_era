package com.era.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.theme.ColorSettingsBackBg
import com.era.app.ui.theme.ColorSettingsBackIcon
import com.era.app.ui.theme.ColorSettingsHeaderBg
import com.era.app.ui.theme.ColorTextWhite

@Composable
fun SettingsHeader(
    titulo: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    iconoVolver: ImageVector,
    descripcionVolver: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorSettingsHeaderBg)
            .heightIn(min = 230.dp),
    ) {
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .padding(start = 24.dp, top = 24.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(ColorSettingsBackBg),
        ) {
            Icon(
                imageVector = iconoVolver,
                contentDescription = descripcionVolver,
                tint = ColorSettingsBackIcon,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = titulo,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
    }
}
