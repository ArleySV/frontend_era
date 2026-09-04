package com.era.app.ui.components.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.era.app.R
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryLight
import com.era.app.ui.theme.ColorTextWhite

data class EraDrawerItem(
    val id: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val etiqueta: String,
    val habilitado: Boolean = true,
)

@Composable
fun EraDrawer(
    drawerState: androidx.compose.material3.DrawerState,
    nombre: String,
    correo: String,
    avatar: String?,
    cargandoPerfil: Boolean,
    items: List<EraDrawerItem>,
    onItemClick: (String) -> Unit,
    onCerrarSesionClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    drawerWidth: androidx.compose.ui.unit.Dp = 289.dp,
    bytesAvatarCustom: ByteArray? = null,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = scrimColor,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth),
            ) {
                CabeceraDrawer(
                    nombre = nombre,
                    correo = correo,
                    avatar = avatar,
                    cargandoPerfil = cargandoPerfil,
                    bytesCustom = bytesAvatarCustom,
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items.forEach { item ->
                        if (item.id == "separador") {
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            ItemDrawer(
                                icono = item.icono,
                                etiqueta = item.etiqueta,
                                habilitado = item.habilitado,
                                esCerrarSesion = item.id == "cerrar_sesion",
                                onClick = {
                                    if (item.id == "cerrar_sesion") onCerrarSesionClick()
                                    else onItemClick(item.id)
                                },
                                modifier = Modifier.testTag("drawer_item_${item.id}"),
                            )
                        }
                    }
                }
            }
        },
    ) {
        content()
    }
}

@Composable
private fun CabeceraDrawer(
    nombre: String,
    correo: String,
    avatar: String?,
    cargandoPerfil: Boolean,
    bytesCustom: ByteArray?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .background(ColorPrimary)
            .testTag("drawer_cabecera")
            .padding(start = 32.dp, top = 20.dp, end = 24.dp, bottom = 24.dp),
    ) {
        AvatarDrawer(avatar = avatar, nombre = nombre, bytesCustom = bytesCustom)
        Spacer(modifier = Modifier.height(16.dp))
        if (cargandoPerfil) {
            Text(
                text = "…",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTextWhite,
            )
        } else {
            Text(
                text = if (nombre.isBlank()) "¡Bienvenido!" else nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTextWhite,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = correo,
            fontSize = 16.sp,
            color = ColorTextWhite,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ERA - Educación, Repaso y Aprendizaje",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun AvatarDrawer(avatar: String?, nombre: String, bytesCustom: ByteArray?) {
    val iniciales = nombre
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "?" }

    val preset = when (avatar) {
        "preset:1" -> R.drawable.avatar_preset_1
        "preset:2" -> R.drawable.avatar_preset_2
        "preset:3" -> R.drawable.avatar_preset_3
        else -> null
    }
    // Foto remota (URL directa) o binario subido por el usuario (custom:*).
    val esUrl = avatar != null &&
        (avatar.startsWith("http://") || avatar.startsWith("https://"))
    val esCustom = avatar?.startsWith("custom:") == true && bytesCustom != null

    Box(
        modifier = Modifier
            .size(80.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(ColorPrimaryLight)
            .testTag("drawer_avatar"),
        contentAlignment = Alignment.Center,
    ) {
        when {
            preset != null -> {
                Image(
                    painter = painterResource(preset),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            esCustom -> {
                AsyncImage(
                    model = bytesCustom,
                    contentDescription = "Avatar",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            esUrl -> {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Avatar",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                Text(
                    text = iniciales,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary,
                )
            }
        }
    }
}

@Composable
private fun ItemDrawer(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    habilitado: Boolean,
    esCerrarSesion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = etiqueta,
            tint = if (esCerrarSesion) ColorError else ColorPrimary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = etiqueta,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = if (esCerrarSesion) ColorError else Color.Black,
        )
    }
}
