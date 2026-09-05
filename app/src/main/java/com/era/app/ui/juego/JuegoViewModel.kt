package com.era.app.ui.juego

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

@HiltViewModel
class JuegoViewModel @Inject constructor(
    private val progresoRepository: ProgresoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val nivelOrden: Int = checkNotNull(savedStateHandle["nivelOrden"])

    private val _uiState = MutableStateFlow(JuegoUiState())
    val uiState: StateFlow<JuegoUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<JuegoEvento>(Channel.BUFFERED)
    val eventos: Flow<JuegoEvento> = _eventos.receiveAsFlow()

    private var cronometroJob: Job? = null
    private var pausaJob: Job? = null

    companion object {
        val FRASES_SABIAS = listOf(
            "🐙 Los pulpos tienen tres corazones. Dos ayudan a llevar la sangre hacia las branquias y uno la lleva al resto de su cuerpo.",
            "🦒 Las jirafas tienen un cuello muy largo, pero poseen el mismo número de huesos en el cuello que los seres humanos: siete.",
            "🌎 La Tierra no es una esfera perfectamente redonda. Está un poquito achatada en los polos y es más ancha alrededor del ecuador.",
            "🐝 Las abejas pueden comunicarse mediante una especie de baile. Con sus movimientos pueden indicar a otras abejas dónde encontrar flores con néctar.",
            "🌳 Algunos árboles pueden vivir durante miles de años. Existen árboles tan antiguos que ya estaban creciendo antes de que se construyeran muchas ciudades actuales.",
            "🦈 Los tiburones existen desde hace muchísimo tiempo. Aparecieron en los océanos antes que los dinosaurios.",
            "🌙 La Luna no produce su propia luz. La vemos brillante porque refleja la luz del Sol.",
            "🦋 Las mariposas saborean usando principalmente sus patas. Así pueden detectar si una planta es adecuada para alimentarse o poner sus huevos.",
            "🐘 Los elefantes pueden reconocerse frente a un espejo. Esta habilidad está relacionada con una forma de autoreconocimiento que pocos animales poseen.",
            "🌧️ El olor que aparece cuando llueve después de mucho tiempo de sequía tiene un nombre: petricor. ¡Es ese olor especial de la tierra mojada!",
            "🐧 Los pingüinos no vuelan por el aire, pero sus alas funcionan como aletas que les ayudan a nadar rápidamente bajo el agua.",
            "🧠 El cerebro humano utiliza una gran cantidad de energía. Aunque representa solo una pequeña parte del peso del cuerpo, necesita mucha energía para funcionar correctamente.",
            "🐬 Los delfines utilizan sonidos para comunicarse y también para orientarse. Este sistema se llama ecolocalización y les ayuda a encontrar objetos bajo el agua.",
            "🌻 Algunas plantas pueden responder a la luz y crecer en dirección hacia ella. Esto les permite aprovechar mejor la energía del Sol.",
            "🦇 Los murciélagos son los únicos mamíferos capaces de realizar un vuelo verdadero y sostenido. Otros mamíferos, como las ardillas voladoras, planean, pero no vuelan de la misma manera.",
            "🧊 El hielo puede flotar sobre el agua porque, al congelarse, el agua ocupa un poco más de espacio y se vuelve menos densa.",
            "🐜 Las hormigas pueden trabajar juntas para realizar tareas que serían muy difíciles para una sola hormiga. ¡En equipo pueden construir impresionantes colonias!",
            "☀️ La luz del Sol tarda aproximadamente 8 minutos y 20 segundos en llegar hasta la Tierra. Así que cuando miramos el Sol, estamos viendo cómo era hace unos minutos.",
            "🦴 Los bebés humanos nacen con más huesos que los adultos. Algunos de esos huesos se unen mientras crecemos.",
            "🌌 En el espacio existen muchísimas estrellas. Nuestro Sol es solamente una estrella entre una enorme cantidad de estrellas que forman nuestra galaxia, la Vía Láctea.",
        )

        val FRASES_FELICITACION = listOf(
            "¡Muy bien! ¡Estás aprendiendo mucho!",
            "¡Excelente! ¡Tu esfuerzo dio resultado!",
            "¡Correcto! ¡Cada vez sabes más!",
            "¡Lo lograste! ¡Sigue aprendiendo!",
            "¡Genial! ¡Tu conocimiento crece!",
            "¡Muy bien pensado! 🧠✨",
            "¡Excelente trabajo! ¡Sigue así!",
            "¡Tu curiosidad te llevó a la respuesta! 🔎",
            "¡Aprendiste y acertaste! 🌟",
            "¡Tu esfuerzo está dando frutos! 🌱",
            "¡BOOM! 💥 ¡Respuesta correcta!",
            "¡Punto para ti! 🏆",
            "¡Acertaste! 🎯",
            "¡Misión cumplida! 🚀",
            "¡Nivel superado! ⭐",
            "¡Esa era! 😎",
            "¡Qué gran respuesta! 🔥",
            "¡Tu cerebro está en modo experto! 🧠⚡",
            "¡Respuesta de campeón! 🏅",
            "¡Increíble! ¡Vas con todo! 🚀",
        )

        val FRASES_MOTIVACION = listOf(
            "¡Casi lo logras! 💪",
            "¡Buen intento! Sigue adelante.",
            "¡No te rindas! 🚀",
            "¡La próxima será!",
            "¡Sigue intentando! ⭐",
            "¡Cada intento te hace aprender!",
            "¡Estuviste cerca!",
            "¡Tú puedes hacerlo!",
            "¡Vamos por otra! 🎯",
            "¡Ups! 😯 ¡Prueba otra vez!",
            "¡Oh-oh! 🐾 ¡Casi!",
            "¡Esta vez no fue! ¡Vamos de nuevo!",
            "¡Respuesta sorpresa! 😄 ¡Inténtalo otra vez!",
            "¡Casi, casi! 🔎",
            "¡El siguiente desafío te espera!",
            "¡No te preocupes, aventurero! ¡Sigue!",
            "¡Fallaste esta, pero aún quedan muchas por descubrir!",
        )
    }

    init {
        viewModelScope.launch {
            val lista = progresoRepository.obtenerNivelesConProgreso().first()
            val nivel = lista.firstOrNull { it.orden == nivelOrden }
            if (nivel == null) return@launch
            _uiState.update { it.copy(nivel = nivel, fase = FaseJuego.JUGANDO) }
            iniciarCronometro()
        }
    }

    // REQ-FUN-11 CA1: 15 s automático, NO pausable. Solo se detiene al responder
    // (CA2) o al entrar en RESULTADO/PAUSA. Abrir el menú NO lo detiene.
    // Bucle finito (15 ticks): no deja corutinas vivas al terminar la partida.
    private fun iniciarCronometro() {
        cronometroJob?.cancel()
        cronometroJob = viewModelScope.launch {
            repeat(15) {
                delay(1000)
                val s = _uiState.value
                if (s.fase !in setOf(FaseJuego.JUGANDO, FaseJuego.MENU)) return@launch
                val restantes = s.segundosRestantes - 1
                _uiState.update { it.copy(segundosRestantes = restantes) }
                if (restantes <= 0) {
                    resolver(exito = false)
                    return@launch
                }
            }
        }
    }

    fun onOpcionClick(indice: Int) {
        val s = _uiState.value
        if (s.fase != FaseJuego.JUGANDO || s.opcionSeleccionada != null) return
        val nivel = s.nivel ?: return
        _uiState.update { it.copy(opcionSeleccionada = indice) }
        resolver(exito = indice == nivel.respuestaCorrecta)
    }

    private fun resolver(exito: Boolean) {
        cronometroJob?.cancel()
        viewModelScope.launch {
            progresoRepository.registrarResultado(nivelOrden, exito)
            val mensaje = if (exito) {
                FRASES_FELICITACION.random()
            } else {
                FRASES_MOTIVACION.random()
            }
            _uiState.update { it.copy(fase = FaseJuego.RESULTADO, resultadoCorrecto = exito, mensajeResultado = mensaje) }
            delay(3000)
            if (exito) {
                val siguienteOrden = nivelOrden + 1
                if (siguienteOrden <= 20) {
                    val lista = progresoRepository.obtenerNivelesConProgreso().first()
                    val siguiente = lista.firstOrNull { it.orden == siguienteOrden }
                    if (siguiente != null && (siguiente.estado == "DISPONIBLE" || siguiente.estado == "COMPLETADO")) {
                        _eventos.trySend(JuegoEvento.NavegarANiveles(siguienteOrden))
                    } else {
                        _eventos.trySend(JuegoEvento.VolverANiveles)
                    }
                } else {
                    _eventos.trySend(JuegoEvento.VolverANiveles)
                }
            } else {
                val fallos = progresoRepository.obtenerNivelesConProgreso()
                    .first()
                    .firstOrNull { it.orden == nivelOrden }
                    ?.intentosFallidosConsecutivos ?: 0
                if (fallos >= 2) {
                    iniciarPausa()
                } else {
                    reiniciarPregunta()
                }
            }
        }
    }

    // REQ-FUN-11 CA3: tras 2 fallos consecutivos, pausa de 60 s y reinicio.
    private fun iniciarPausa() {
        val frase = FRASES_SABIAS.random()
        _uiState.update { it.copy(fase = FaseJuego.PAUSA, segundosPausa = 60, fraseSabia = frase) }
        pausaJob?.cancel()
        pausaJob = viewModelScope.launch {
            repeat(60) {
                delay(1000)
                _uiState.update { it.copy(segundosPausa = it.segundosPausa - 1) }
            }
            reiniciarPregunta()
        }
    }

    // Reinicia la MISMA pregunta (15 s, selección null). Nunca cambia de nivel.
    private fun reiniciarPregunta() {
        pausaJob?.cancel()
        _uiState.update {
            it.copy(
                fase = FaseJuego.JUGANDO,
                segundosRestantes = 15,
                opcionSeleccionada = null,
                resultadoCorrecto = null,
                segundosPausa = 60,
                mensajeResultado = "",
            )
        }
        iniciarCronometro()
    }

    // Ajuste S4 (auditor, CA1): el menú se superpone pero el cronómetro SIGUE
    // decreciendo; onAbrirMenu/onContinuar NO tocan segundosRestantes.
    fun onAbrirMenu() {
        if (_uiState.value.fase == FaseJuego.JUGANDO) {
            _uiState.update { it.copy(fase = FaseJuego.MENU) }
        }
    }

    fun onContinuar() {
        if (_uiState.value.fase == FaseJuego.MENU) {
            _uiState.update { it.copy(fase = FaseJuego.JUGANDO) }
        }
    }

    fun onReiniciar() {
        if (_uiState.value.fase == FaseJuego.MENU) {
            reiniciarPregunta()
        }
    }

    fun onSalir() {
        cronometroJob?.cancel()
        pausaJob?.cancel()
        _eventos.trySend(JuegoEvento.VolverANiveles)
    }

    // Plan §6.5: back físico/gesto equivale a "Salir" del overlay.
    fun onAtrasSistema() = onSalir()
}