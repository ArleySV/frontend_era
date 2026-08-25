package com.era.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ERATheme

@Composable
fun StepIndicator(
    pasoActual: Int,
    totalPasos: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f))
        repeat(totalPasos) { index ->
            if (index > 0) {
                Box(modifier = Modifier.size(8.dp))
            }
            val activo = index == pasoActual - 1
            PuntoStepIndicator(activo = activo, indice = index, pasoActual = pasoActual, totalPasos = totalPasos)
        }
        Box(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PuntoStepIndicator(
    activo: Boolean,
    indice: Int,
    pasoActual: Int,
    totalPasos: Int
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer(alpha = if (activo) 1f else 0.3f)
            .clip(CircleShape)
            .background(ColorPrimary)
            .testTag("step_indicator_punto_$indice")
            .then(
                if (activo) {
                    Modifier.semantics {
                        contentDescription = "Paso $pasoActual de $totalPasos activo"
                    }
                } else {
                    Modifier
                }
            )
    )
}

@Preview(showBackground = true, name = "Indicador paso 1")
@Composable
private fun StepIndicatorPaso1Preview() {
    ERATheme {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            StepIndicator(pasoActual = 1)
            StepIndicator(pasoActual = 2, modifier = Modifier.padding(top = 16.dp))
            StepIndicator(pasoActual = 3, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
