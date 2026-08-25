package com.era.app.ui.register

import com.era.app.utils.CriteriosContrasena
import com.era.app.utils.EraError
import com.era.app.utils.PasswordPolicy

enum class CampoRegistro {
    NOMBRE_MENOR,
    FECHA_NACIMIENTO,
    NOMBRE_ACUDIENTE,
    CEDULA_ACUDIENTE,
    CORREO,
    NOMBRE_USUARIO,
    AVATAR,
    CONTRASENA,
    CONFIRMAR_CONTRASENA,
    CODIGO_OTP,
}

data class RegistroUiState(
    val nombreMenor: String = "",
    val fechaNacimientoDisplay: String = "",
    val nombreAcudiente: String = "",
    val cedulaAcudiente: String = "",
    val correo: String = "",
    val nombreUsuario: String = "",
    val avatarSeleccionado: Int? = null,
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val codigoOtp: String = "",
    val criteriosContrasena: CriteriosContrasena =
        PasswordPolicy.criterios(contrasena, nombreUsuario, nombreMenor),
    val reenvioSegundosRestantes: Int = 0,
    val errores: Set<CampoRegistro> = emptySet(),
    val errorGeneral: EraError? = null,
)

sealed interface RegistroEvento {
    data object NavegarAPaso2 : RegistroEvento
    data object NavegarAPaso3 : RegistroEvento
    data object RegistroVerificadoIrALogin : RegistroEvento
    data class Aviso(val error: EraError) : RegistroEvento
}
