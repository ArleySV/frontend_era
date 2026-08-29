package com.era.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryDark
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun LoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cargando: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        shape = RoundedCornerShape(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimary,
            disabledContainerColor = ColorPrimary.copy(alpha = 0.6f),
            contentColor = ColorTextWhite,
            disabledContentColor = ColorTextWhite.copy(alpha = 0.6f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = ColorTextWhite,
                strokeWidth = 2.dp,
                modifier = Modifier.height(24.dp),
            )
        } else {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, name = "Login Button")
@Composable
private fun LoginButtonPreview() {
    ERATheme {
        Box(modifier = Modifier.padding(32.dp)) {
            LoginButton(text = "Iniciar sesión", onClick = {})
        }
    }
}
