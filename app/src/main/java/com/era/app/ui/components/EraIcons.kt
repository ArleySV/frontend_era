package com.era.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme

private const val RUTA_VISIBILITY =
    "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 " +
        "11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 " +
        "5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 " +
        "3-3-1.34-3-3-3z"

private const val RUTA_VISIBILITY_OFF =
    "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 " +
        "3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 " +
        "11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 " +
        "11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 " +
        "4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 " +
        ".44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 " +
        "0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"

// Paths de iconos del sidebar (Material Design, escala 24dp). assessment/logout no
// existen en material-icons-core; account_circle/settings/help se definen igual para
// uniformidad (O-2, decisión §9.1).
private const val RUTA_ACCOUNT_CIRCLE =
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 " +
        "1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 " +
        "4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"

private const val RUTA_ASSESSMENT =
    "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"

private const val RUTA_SETTINGS =
    "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"

private const val RUTA_HELP =
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z"

private const val RUTA_LOGOUT =
    "M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"

private const val RUTA_MENU =
    "M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"

private const val RUTA_CLOCK =
    "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"

private const val RUTA_PUZZLE =
    "M20.5 11H19V7c0-1.1-.9-2-2-2h-4V3.5C13 2.12 11.88 1 10.5 1S8 2.12 8 3.5V5H4c-1.1 0-1.99.9-1.99 2v3.8H3.5c1.49 0 2.7 1.21 2.7 2.7s-1.21 2.7-2.7 2.7H2V20c0 1.1.9 2 2 2h3.8v-1.5c0-1.49 1.21-2.7 2.7-2.7 1.49 0 2.7 1.21 2.7 2.7V22H17c1.1 0 2-.9 2-2v-4h1.5c1.38 0 2.5-1.12 2.5-2.5S21.88 11 20.5 11z"

private val NodosVisibilidad = PathParser().parsePathString(RUTA_VISIBILITY).toNodes()
private val NodosVisibilidadOff = PathParser().parsePathString(RUTA_VISIBILITY_OFF).toNodes()
private val NodosAccountCircle = PathParser().parsePathString(RUTA_ACCOUNT_CIRCLE).toNodes()
private val NodosAssessment = PathParser().parsePathString(RUTA_ASSESSMENT).toNodes()
private val NodosSettings = PathParser().parsePathString(RUTA_SETTINGS).toNodes()
private val NodosHelp = PathParser().parsePathString(RUTA_HELP).toNodes()
private val NodosLogout = PathParser().parsePathString(RUTA_LOGOUT).toNodes()
private val NodosMenu = PathParser().parsePathString(RUTA_MENU).toNodes()
private val NodosClock = PathParser().parsePathString(RUTA_CLOCK).toNodes()
private val NodosPuzzle = PathParser().parsePathString(RUTA_PUZZLE).toNodes()

object EraIcons {

    val EmailOutline: ImageVector by lazy { Icons.Outlined.Email }

    val LockOutline: ImageVector by lazy { Icons.Outlined.Lock }

    val Visibility: ImageVector by lazy {
        materialIcon(name = "Era.Visibility") {
            addPath(
                pathData = NodosVisibilidad,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val VisibilityOff: ImageVector by lazy {
        materialIcon(name = "Era.VisibilityOff") {
            addPath(
                pathData = NodosVisibilidadOff,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val AccountCircle: ImageVector by lazy {
        materialIcon(name = "Era.AccountCircle") {
            addPath(
                pathData = NodosAccountCircle,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Assessment: ImageVector by lazy {
        materialIcon(name = "Era.Assessment") {
            addPath(
                pathData = NodosAssessment,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Settings: ImageVector by lazy {
        materialIcon(name = "Era.Settings") {
            addPath(
                pathData = NodosSettings,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Help: ImageVector by lazy {
        materialIcon(name = "Era.Help") {
            addPath(
                pathData = NodosHelp,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Logout: ImageVector by lazy {
        materialIcon(name = "Era.Logout") {
            addPath(
                pathData = NodosLogout,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Menu: ImageVector by lazy {
        materialIcon(name = "Era.Menu") {
            addPath(
                pathData = NodosMenu,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Clock: ImageVector by lazy {
        materialIcon(name = "Era.Clock") {
            addPath(
                pathData = NodosClock,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }

    val Puzzle: ImageVector by lazy {
        materialIcon(name = "Era.Puzzle") {
            addPath(
                pathData = NodosPuzzle,
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            )
            this
        }
    }
}

@Preview(showBackground = true, name = "Iconos ojo")
@Composable
private fun EraIconsPreview() {
    ERATheme {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = EraIcons.Visibility,
                contentDescription = null,
                tint = ColorTextMuted,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                imageVector = EraIcons.VisibilityOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 16.dp)
            )
        }
    }
}
