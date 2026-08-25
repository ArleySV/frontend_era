package com.era.app.repository

import com.era.app.utils.EraError

sealed interface Resultado<out T> {
    data class Exito<T>(val data: T) : Resultado<T>
    data class Fallo(val error: EraError) : Resultado<Nothing>
}
