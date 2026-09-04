package com.era.app.ui.home

data class HomeUiState(
    val nombreMenor: String = "",
    val correo: String = "",
    val avatar: String? = null,
    val bytesAvatarCustom: ByteArray? = null,
    val cargandoPerfil: Boolean = true,
    val dialogoCierreVisible: Boolean = false,
    val cerrando: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        other is HomeUiState &&
            nombreMenor == other.nombreMenor &&
            correo == other.correo &&
            avatar == other.avatar &&
            (bytesAvatarCustom?.contentEquals(other.bytesAvatarCustom)
                ?: (other.bytesAvatarCustom == null)) &&
            cargandoPerfil == other.cargandoPerfil &&
            dialogoCierreVisible == other.dialogoCierreVisible &&
            cerrando == other.cerrando

    override fun hashCode(): Int = listOf(
        nombreMenor,
        correo,
        avatar,
        cargandoPerfil,
        dialogoCierreVisible,
        cerrando,
    ).hashCode()
}

sealed interface HomeEvento {
    data object NavegarALogin : HomeEvento
}
