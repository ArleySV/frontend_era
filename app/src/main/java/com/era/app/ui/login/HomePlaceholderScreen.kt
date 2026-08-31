package com.era.app.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun HomePlaceholderScreen(
    onCerrarSesion: () -> Unit,
    onNavigatePerfil: () -> Unit,
    onNavigateProgreso: () -> Unit,
    onNavigateFaq: () -> Unit,
    modifier: Modifier = Modifier,
    dialogoCierreVisible: Boolean = false,
    cerrando: Boolean = false,
    onCancelarCierre: () -> Unit = {},
    onConfirmarCierre: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sesión iniciada ✅",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextDark,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Home placeholder — Fase 10",
                fontSize = 16.sp,
                color = ColorTextDark,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigatePerfil,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorTextWhite,
                ),
            ) {
                Text(text = "Mi cuenta", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateProgreso,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorTextWhite,
                ),
            ) {
                Text(text = "Mi progreso", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateFaq,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorTextWhite,
                ),
            ) {
                Text(text = "Ayuda y Comentarios", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCerrarSesion,
                enabled = !cerrando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorError,
                    contentColor = ColorTextWhite,
                ),
                modifier = Modifier.testTag("botonCerrarSesion"),
            ) {
                Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (dialogoCierreVisible) {
        AlertDialog(
            onDismissRequest = {
                if (!cerrando) {
                    onCancelarCierre()
                }
            },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark,
                )
            },
            text = {
                Text(
                    text = "¿Deseas cerrar sesión?",
                    fontSize = 16.sp,
                    color = ColorTextDark,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmarCierre,
                    enabled = !cerrando,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError,
                        contentColor = ColorTextWhite,
                    ),
                    modifier = Modifier.testTag("botonConfirmarCierre"),
                ) {
                    if (cerrando) {
                        CircularProgressIndicator(
                            color = ColorTextWhite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = "Sí, cerrar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelarCierre,
                    enabled = !cerrando,
                    modifier = Modifier.testTag("botonCancelarCierre"),
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary,
                    )
                }
            },
            modifier = Modifier.testTag("dialogoCierre"),
        )
    }
}

@Preview(showBackground = true, name = "Home Placeholder")
@Composable
private fun HomePlaceholderPreview() {
    ERATheme {
        HomePlaceholderScreen(
            onCerrarSesion = {},
            onNavigatePerfil = {},
            onNavigateProgreso = {},
            onNavigateFaq = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Placeholder — diálogo")
@Composable
private fun HomePlaceholderDialogoPreview() {
    ERATheme {
        HomePlaceholderScreen(
            onCerrarSesion = {},
            onNavigatePerfil = {},
            onNavigateProgreso = {},
            onNavigateFaq = {},
            dialogoCierreVisible = true,
        )
    }
}
