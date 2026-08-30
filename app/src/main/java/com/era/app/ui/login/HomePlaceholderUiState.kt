package com.era.app.ui.login

data class HomePlaceholderUiState(
    val dialogoCierreVisible: Boolean = false,
    val cerrando: Boolean = false,
)

sealed interface HomePlaceholderEvento {
    data object NavegarALogin : HomePlaceholderEvento
}