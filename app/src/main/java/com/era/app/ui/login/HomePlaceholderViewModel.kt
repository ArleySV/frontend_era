package com.era.app.ui.login

import androidx.lifecycle.ViewModel
import com.era.app.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomePlaceholderViewModel @Inject constructor(
    private val sesionRepository: SesionRepository,
) : ViewModel() {

    fun cerrarSesion() {
        sesionRepository.limpiarToken()
    }
}
