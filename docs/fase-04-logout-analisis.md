# Fase 4 — Logout / Cierre de sesión (Módulo F): Análisis y Diseño

> Documento de análisis del módulo frontend. Registra el alcance, el diseño,
> las decisiones propuestas (D-32…) y los archivos a crear/modificar.
> **Estado:** **IMPLEMENTADA (D-34/D-35/D-36 aprobadas) — pendiente de la
> revisión del propietario en emulador y del versionado.** Ver §13 (aprobación)
> y §14 (registro de implementación).

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-04 (Cierre de sesión, prioridad Alta) |
| Requisitos no funcionales asociados | REQ-NF-02 (seguridad) |
| Caso de uso | CU-05 (Cerrar sesión) |
| Historia de usuario | HU-04 |
| Endpoint backend consumido | `POST /auth/logout` |
| Fuente visual | `decisiones-tecnicas.md` §13.9.4 (dialog-confirm) — **no existe prototipo JPG** (ver §12.3) |
| Fuente de contrato | `BACKEND_ERA/docs/modulo-f-analisis.md`, `BACKEND_ERA/README.md` |

## 2. Alcance

**Incluye:**
- Confirmación previa al cierre (REQ-FUN-04 CA1): diálogo "¿Deseas cerrar
  sesión?" con "Sí, cerrar sesión" y "Cancelar". Cancelar → se queda en la
  pantalla actual sin cambios (CA3).
- Llamada HTTP a `POST /api/v1/auth/logout` (confirmación formal; backend
  stateless) y **descarte del JWT del Keystore** + navegación a Login
  (CA2). El progreso/datos del usuario no se tocan (CA4).
- Capa `repository/`: `logout()` añadido a `AuthRepository` +
  `RemoteAuthRepository` (reuso del wrapper `llamar`).
- UI: evolución de `HomePlaceholderViewModel` y `HomePlaceholderScreen`
  (diálogo de confirmación) — la Sidebar real es Fase 10 y reutilizará el
  mismo flujo.
- Tests unitarios (ViewModel + Repository MockWebServer) y androidTest
  (diálogo).

**Fuera de alcance (cerrado):**
- Sidebar, Home real y el header del drawer → **Fase 10** (REQ-FUN-08).
- Limpieza de Room: la capa `data/` aún no existe (llega con Fase 7). El único
  estado de sesión local es el JWT cifrado (Keystore). El progreso vive en el
  servidor y no se borra (CA4 se satisface por diseño).
- Cualquier lógica de "revocación server-side" o blacklist → **no existe en el
  backend** (decisión de arquitectura §3 de `modulo-f-analisis.md`); no
  reinventarla en el cliente.
- Confirmación por doble-tap UI fuera del diálogo (sin cambios en otras
  pantallas).

## 3. Estado previo aprovechado (verificado en código, no reescribe)

| Componente | Archivo | Estado |
|---|---|---|
| `AuthApi.logout()` `@POST("auth/logout") suspend fun logout(): MessageResponse` (sin `@Body`) | `remote/api/AuthApi.kt:30-31` | ✅ **Ya declarado** — sin cambios |
| `MessageResponse(message)` (DTO `{ "message" }`) | `remote/dto/common/MessageResponse.kt` | ✅ Ya existe — sin cambios |
| `AuthRepository` (interfaz de `/auth/*`) + `RemoteAuthRepository` (wrapper `llamar` + `aEraError`) | `repository/AuthRepository.kt`, `repository/RemoteAuthRepository.kt:36-52` | ✅ Extender con `logout()` (D-33); no duplicar el wrapper |
| `SesionRepository` + `TokenManagerSesion` (adapter sobre `TokenManager`) | `repository/SesionRepository.kt`, `repository/TokenManagerSesion.kt` | ✅ Reutilizar `limpiarToken()` |
| `JwtInterceptor` (adjunta `Authorization: Bearer` automáticamente) | `remote/JwtInterceptor.kt` | ✅ El POST de logout la usará; **no se toca** |
| `EraError`: `SesionExpirada`, `ErrorServidor`, `ErrorConexion` ya existen | `utils/EraError.kt` | ✅ **Sin subtipos nuevos** (D-36) |
| `ErrorMapper`: `UNAUTHORIZED→SesionExpirada`, `INTERNAL_ERROR→ErrorServidor`, `IOException→ErrorConexion` ya existen | `utils/ErrorMapper.kt` | ✅ **Sin ramas nuevas** (D-36) |
| `MensajeError` (when exhaustivo) | `utils/MensajeError.kt` | ✅ **No se toca** (no hay errores nuevos) |
| `HomePlaceholderViewModel` (hoy solo `cerrarSesion()` → `limpiarToken()`) | `ui/login/HomePlaceholderViewModel.kt` | ✅ Evolucionar (D-34) |
| `HomePlaceholderScreen` (botón "Cerrar sesión" ya existe, Fase 2/3) | `ui/login/HomePlaceholderScreen.kt` | ✅ Añadir diálogo (D-34) |
| Patrón ViewModel: UiState StateFlow + eventos Channel + `popUpTo(0){inclusive=true}` para logout/401 | `ui/login/LoginViewModel.kt`, `ui/login/LoginUiState.kt`, `ui/login/LoginScreen.kt:236-250`, `ui/navigation/EraNavHost.kt:41-52` | ✅ Réplica para el flujo de cierre |
| `Resultado<T>` sellado `Exito/Fallo` | `repository/Resultado.kt` | ✅ Reutilizar |
| Fakes de `AuthRepository` en tests (`LoginViewModelTest.kt:181`, `RegistroViewModelTest.kt:534`) | `src/test/...` | ⚠️ **Compilan-break**: al añadir `logout()` a la interfaz deben añadir `override` stub (ver §9) |
| Referencia visual del diálogo (§13.9.4 `dialog-confirm`) | `docs/decisiones-tecnicas.md:832-848` | ✅ Usar como especificación (no hay JPG) |

**Nota de numeración D-XX (verificación solicitada):** la secuencia real en
los docs es Fase 1 → D-01…D-14, Fase 2 → D-16…D-22 (D-15 sin sección propia;
la cabecera declara D-16…D-21 y D-22 se añadió al final), Fase 3 → D-23…D-30
(cabecera) con **D-31 referenciado en tablas de `fase-03-perfil-analisis.md`
§6.4/§9 para `formatearFechaISO`, sin sección de decisión propia**. Por tanto,
el siguiente número **libre y sin colisión** es **D-32**. Ver punto abierto
§12.1.

## 4. Contrato exacto consumido (fuente: modulo-f-analisis.md + README)

### 4.1 `POST /api/v1/auth/logout`

**Request:** sin body. Header `Authorization: Bearer <JWT de sesión>`
(auto-adjuntado por `JwtInterceptor`). No hay DTO de entrada.

**Response 200 OK — `MensajeResponseDto`:**
```json
{ "message": "Sesión cerrada." }
```
→ `MessageResponse` (ya existe en el cliente).

**Códigos de estado (formato `ErrorDto` estándar):**
| Status | `error` | Tratamiento Fase 4 |
|---|---|---|
| 200 | — | Confirmación formal; el cliente procede a limpiar el JWT (D-32) |
| 401 | `UNAUTHORIZED` | Token ausente/malformado/expirado → `SesionExpirada` (ya mapeado). **Mismo tratamento D-32: limpiar + navegar** |
| 500 | `INTERNAL_ERROR` | `ErrorServidor` (ya mapeado). **Mismo tratamento D-32: limpiar + navegar** |
| sin red (IOException) | — | `ErrorConexion` (ya mapeado). **Mismo tratamento D-32: limpiar + navegar** |

**No hay 400, 404 ni 403** (sin body que validar, sin recurso que buscar y el
logout no evalúa el estado de la cuenta). **Idempotente:** repetir con el
mismo token vuelve a responder 200. El backend **no conserva ni modifica
estado** y **no toca ninguna tabla** → el progreso y los datos del usuario se
conservan intactos (REQ-FUN-04 CA4).

**Semántica clave del módulo (modulo-f-analisis.md §2):** *"La responsabilidad
de la invalidación del token es del cliente Android. El backend solo actúa
como confirmación formal del cierre."* El endpoint **no revoca, confirma**; el
token sigue siendo criptográficamente válido hasta su `exp` para quien lo
conserve (riesgo aceptado en la arquitectura §3). Esa es la base de la decisión
central D-32.

## 5. Decisiones de diseño (D-32…)

### D-32 — Semántica del cierre: confirmación best-effort con limpieza local garantizada

**Decisión:** sobre confirmación del usuario en el diálogo, el ViewModel: (1)
llama `POST /auth/logout` (await), (2) **cualquiera sea el resultado** limpiar
el JWT (`sesionRepository.limpiarToken()`), (3) navega a Login
(`popUpTo(0){inclusive=true}`). El diálogo permanece abierto (con botones
deshabilitados) mientras el POST está en vuelo y se cierra al navegar. **No se
muestra ningún mensaje de error** para fallos de la llamada: la redirección a
Login es el feedback.

| Resultado de `logout()` | Acción |
|---|---|
| `Exito(200)` | `limpiarToken()` + `NavegarALogin` |
| `Fallo(SesionExpirada)` (401) | `limpiarToken()` + `NavegarALogin` (token ya inválido) |
| `Fallo(ErrorServidor)` (500) | `limpiarToken()` + `NavegarALogin` |
| `Fallo(ErrorConexion)` (sin red) | `limpiarToken()` + `NavegarALogin` |
| `Fallo(otro)` | `limpiarToken()` + `NavegarALogin` |

**Por qué:**
- **REQ-FUN-04 CA2** exige: "la sesión se invalida **localmente**, se limpia el
  token de autenticación y se redirige a la pantalla de inicio de sesión" — sin
  condicionarlo al éxito de red (idéntico en HU-04 CA2 y CU-05).
- **modulo-f-analisis.md §2** es explícito: la invalidación es del cliente y el
  endpoint solo confirma. Condicionar el logout a un 200 dejaría a un niño sin
  conexión **sin poder cerrar sesión**, atrapado en la app (viola CA2 y HU-04
  el espíritu de "salida segura"); el backend no tiene estado que preservar.
- **Sequencing correcto:** `limpiarToken()` se ejecuta **después** de que
  finalice el POST para que `JwtInterceptor` aún adjunte el token al propio
  logout (misma corrutina, orden secuencial).
- **Seguridad:** no hay pérdida de seguridad al limpiar aunque el 200 no llegue
  (el token queda en el dispositivo; el riesgo de robo/dispositivo perdido ya
  está registrado y aceptado en §3 de `modulo-f-analisis.md`).
- **Alternativa rechazada (concordancia estricta al 200):** limpiar y navegar
  solo si la respuesta es 200; ante `ErrorConexion`/500 mostrar error y
  quedarse. Se rechaza porque deja la sesión sin poder cerrarse offline y
  contradice el principio de invalidación client-side. (Se documenta como
  descartada; si el propietario prefiere este modo, se ajusta la decisión — ver
  §12.2.)

### D-33 — Ubicación del endpoint en repository: `AuthRepository.logout()`

**Decisión:** añadir el método a la interfaz existente de autenticación:

| Método | Firma |
|---|---|
| `logout()` | `suspend fun logout(): Resultado<MessageResponse>` |

Implementado en `RemoteAuthRepository` como `llamar { api.logout() }`
(reutilizando exactamente el wrapper `llamar` + `aEraError` de
`RemoteAuthRepository.kt:36-52`, que ya relanza `CancellationException` y mapea
`HttpException` → `ErrorMapper`).

**Por qué:** `logout` pertenece al dominio `/auth/*` (mismo grupo que
`login`), y `AuthApi.logout()` y `MessageResponse` **ya existen** — no se crea
DTO, ni endpoint remoto, ni interfaz nueva (`LogoutRepository` no se justifica
para un solo método que reutiliza el mismo path de autenticación). Se replica
el patrón de decisión D-23 de Fase 3 (interface por dominio), sin refactorizar
los 93+ tests previos. **Consecuencia obligatoria en tests:** los dos fakes
existentes que implementan `AuthRepository` (`LoginViewModelTest.kt:181`,
`RegistroViewModelTest.kt:534`) deben añadir `override suspend fun logout()` con
`error("No usado en ... tests")` para seguir compilando (ver §9).

### D-34 — Ubicación del flujo en UI: evolución de `HomePlaceholderViewModel` + diálogo en `HomePlaceholderScreen`

**Decisión:** no se crean pantallas ni ViewModels nuevos. Se evoluciona
`HomePlaceholderViewModel` para que posea todo el flujo de cierre:

- Inyecta `AuthRepository` (nuevo) + `SesionRepository` (ya lo tenía).
- Estado `HomePlaceholderUiState`: `dialogoCierreVisible` y `cerrando`.
- Evento `HomePlaceholderEvento`: `NavegarALogin`.
- Métodos: `onCerrarSesionClick()` (abre el diálogo), `onCancelarCierre()`,
  `onConfirmarCierre()` (POST → limpiar → evento).
- El actual `cerrarSesion()` (limpieza directa) **se elimina**, sustituido por
  el flujo con confirmación.

`HomePlaceholderScreen` gana el diálogo de confirmación (Material3
`AlertDialog`, §13.9.4) invocado por `dialogoCierreVisible`, con textos exactos
de REQ-FUN-04 CA1. La navegación pasa a ser **por evento** (patrón
`LoginScreen.kt:236-250`): `EraNavHost` observa `eventos` y navega a `LOGIN`.

**Por qué:** la entrada actual de "Cerrar sesión" es el botón del
`HomePlaceholderScreen` (Fase 2/3); la Sidebar real (REQ-FUN-08) llega en Fase
10 y el "Cerrar sesión" del drawer **reutilizará el mismo ViewModel/flujo** —
solo cambiará el invocador (`navigate`) que abre el diálogo, no la lógica.
Crear un `LogoutViewModel`/`LogoutScreen` aparte sería _over-engineering_ para
un diálogo y una llamada; el patrón evento→navegación ya está probado en
login/Mi cuenta (D-18/Fase 3).

### D-35 — Estados del diálogo, anti doble-tap y navegación final

**Decisión:**
- `dialogoCierreVisible=true` al tocar "Cerrar sesión".
- `cerrando=true` durante el POST: los botones del diálogo y el botón "Cerrar
  sesión" de la pantalla se deshabilitan; el botón "Sí, cerrar sesión" muestra
  `CircularProgressIndicator` (patrón cargando de `LoginButton`).
- `onCancelarCierre()` solo tiene efecto si `cerrando == false` (una vez
  confirmado no se puede abortar el cierre).
- Re-entrada: `onCerrarSesionClick()` no re-abre el diálogo si ya está abierto
  o si `cerrando` (evita doble POST; además el endpoint es idempotente).
- Navegación final: `navigate(EraRoutes.LOGIN) { popUpTo(0) { inclusive = true } }`
  — el patrón ya usado en el logout previo (`EraNavHost.kt:45-49`),
  `LoginViewModel` y `MiCuentaViewModel`. Limpia todo el backstack.

**Por qué:** REQ-FUN-04 CA1 pide diálogo de confirmación para evitar cierres
accidentales (el botón deja de cerrar al primer toque). El anti doble-tap evita
dos POST simultáneos (inofensivo por idempotencia pero sucio en logs) y da
feedback visual de progreso. `popUpTo(0){inclusive=true}` asegura que al volver
al login el backstack no conserve `home_placeholder` ni `perfil` (mismo criterio
que el cierre por 401/403 de Fase 3, D-25).

### D-36 — Cero errores nuevos, cero dependencias, cero cambios colaterales

**Decisión:** no se modifica `EraError`, `ErrorMapper`, `MensajeError`,
`Validators`, `Color.kt`, `AuthApi`, DTOs, `build.gradle.kts` ni
`libs.versions.toml`.

**Por qué:** el contrato de `/auth/logout` solo produce estados ya mapeados por
el mapper central (D-30 de Fase 3): `UNAUTHORIZED→SesionExpirada`,
`INTERNAL_ERROR→ErrorServidor`, `IOException→ErrorConexion`, y un 200 sin
error. Como D-32 no muestra errores de esta llamada, ni siquiera se toca
`MensajeError`. Regla §4.4/§4.13: no tocar lo que no se necesita y no añadir
dependencias sin aprobación — en esta fase **cero dependencias nuevas**.
`AuthApi.logout()` y `MessageResponse` ya estaban declarados en Fase 0
(contrato completo aprobado 2026-08-23), de modo que la capa `remote/` **no se
edita en absoluto**.

## 6. Arquitectura propuesta (capa por capa)

### 6.1 Remote (ya declarado — NO se edita)

`AuthApi.logout()` (`AuthApi.kt:30-31`) y `MessageResponse` ya existen y se
verificaron contra el backend en Fase 0. `JwtInterceptor` adjunta el token. **Sin
cambios.**

### 6.2 Repository

```
AuthRepository (interfaz)  → repository/AuthRepository.kt
  + suspend fun logout(): Resultado<MessageResponse>

RemoteAuthRepository (impl @Singleton)  → repository/RemoteAuthRepository.kt
  override suspend fun logout(): Resultado<MessageResponse> = llamar { api.logout() }
```
Sin cambios en DI (`RemoteAuthRepository` ya está ligado vía `@Binds` en
`RepositoryModule.kt:19-21`).

### 6.3 UI / ViewModel

**Nuevo archivo:** `ui/login/HomePlaceholderUiState.kt`
```kotlin
data class HomePlaceholderUiState(
    val dialogoCierreVisible: Boolean = false,
    val cerrando: Boolean = false,
)

sealed interface HomePlaceholderEvento {
    data object NavegarALogin : HomePlaceholderEvento
}
```

**`ui/login/HomePlaceholderViewModel.kt` (evolución):**
| Estado | Default | Significado |
|---|---|---|
| `dialogoCierreVisible` | `false` | Muestra/oculta el diálogo de confirmación |
| `cerrando` | `false` | POST de logout en vuelo; botones deshabilitados |

**Flujo:**
1. `onCerrarSesionClick()` → si `!dialogoCierreVisible && !cerrando` →
   `dialogoCierreVisible=true`.
2. `onCancelarCierre()` → si `!cerrando` → `dialogoCierreVisible=false`
   (CA3: permanece en pantalla sin cambios; no llama red ni limpia token).
3. `onConfirmarCierre()` → si `cerrando` return;
   `cerrando=true`; `launch { authRepository.logout(); sesionRepository.limpiarToken();
   _uiState.update { it.copy(cerrando=false, dialogoCierreVisible=false) };
   _eventos.trySend(NavegarALogin) }` — el `Resultado` se consume (sin
   ramificación de acción, D-32). Si la llamada lanzara `CancellationException`
   la corrutina la relanza (no se limpia el token en ese caso: el job murió).

> Detalle de robustez: aunque D-32 trata todos los resultados por igual, el
> wrapper `llamar` ya re-lanza `CancellationException` — comportamiento
> correcto por defecto (no limpiar token cuando el scope se cancela por
> navegación/cierre de la UI).

**`HomePlaceholderScreen.kt` (evolución):** se conserva el layout actual y se
añade, si `dialogoCierreVisible`, un `AlertDialog` Material3 conforme a
§13.9.4:
```
┌─────────────────────────────────┐
│  Cerrar sesión                  │  título 20sp Bold
│                                 │
│  ¿Deseas cerrar sesión?         │  mensaje 16sp Regular (REQ-FUN-04 CA1)
│                                 │
│   [Cancelar]  [Sí, cerrar       │  TextButton (outline) | confirmación
│                sesión  ◠]       │  "Sí, cerrar sesión" (CA1)
└─────────────────────────────────┘
`confirmButton` deshabilitado + spinner circular cuando `cerrando`; `dismissButton`
disabled cuando `cerrando`. No se cierra por tap fuera/`Back` mientras `cerrando`
(`onDismissRequest` no-op si cerrando) para no abortar la operación a mitad.

### 6.4 Navegación (`EraNavHost.kt`)

Reemplazo del bloque actual `onCerrarSesion` (`EraNavHost.kt:41-52`) por el
patrón evento→navegación (como `LoginScreen.kt`):

```
composable(EraRoutes.HOME_PLACEHOLDER) {
    val vm: HomePlaceholderViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(Unit) {
        vm.eventos.collect { evento ->
            when (evento) {
                is HomePlaceholderEvento.NavegarALogin ->
                    navController.navigate(EraRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
    }
    HomePlaceholderScreen(
        onNavigatePerfil = { navController.navigate(EraRoutes.PERFIL) },
        dialogoCierreVisible = uiState.dialogoCierreVisible,
        cerrando = uiState.cerrando,
        onCerrarSesion = vm::onCerrarSesionClick,
        onCancelarCierre = vm::onCancelarCierre,
        onConfirmarCierre = vm::onConfirmarCierre,
    )
}
```

`HomePlaceholderScreen` gana parámetros con **valores por defecto**
(`dialogoCierreVisible = false`, `cerrando = false`, `onCancelarCierre = {}`,
`onConfirmarCierre = {}`) para no romper Previews ni los tests androidTest
existentes.

## 7. Capas de testing

### 7.1 Tests unitarios (JUnit + `kotlinx-coroutines-test`)

**`HomePlaceholderViewModelTest.kt` (nuevo)** — fakes `FakeAuthRepository`
(con `logout()` encolable) + `FakeSesionRepository`:

| # | Caso | Verifica |
|---|---|---|
| 1 | Estado inicial: `dialogoCierreVisible=false`, `cerrando=false` | Defaults |
| 2 | `onCerrarSesionClick()` → `dialogoCierreVisible=true` | Abre diálogo |
| 3 | `onCancelarCierre()` → diálogo cerrado, **sin** llamada red, **sin** `limpiarToken` | CA3 (1) |
| 4 | Confirmar + `logout` 200 → llama `logout()`, `limpiarToken()`, emite `NavegarALogin`, diálogo cerrado, `cerrando=false` | D-32 happy path (1) |
| 5 | Confirmar + `logout` `ErrorConexion` (sin red) → `limpiarToken()` + `NavegarALogin` | D-32 offline |
| 6 | Confirmar + `logout` `SesionExpirada` (401) → `limpiarToken()` + `NavegarALogin` | D-32 token inválido |
| 7 | Confirmar + `logout` `ErrorServidor` (500) → `limpiarToken()` + `NavegarALogin` | D-32 500 |
| 8 | `cerrando=true` mientras el POST está en vuelo (repo que suspende) y `onCancelarCierre()` no lo interrumpe | D-35 anti aborto |
| 9 | Doble `onConfirmarCierre()` → `logout()` llamado **1 sola vez** | D-35 anti doble-tap |
| 10 | `onCerrarSesionClick()` con diálogo ya abierto → no cambia estado | D-35 |

**`AuthRepositoryTest.kt` (extensión, MockWebServer):**

| # | Mock | Verifica |
|---|---|---|
| 1 | 200 `{"message":"Sesión cerrada."}` | `Exito(MessageResponse("Sesión cerrada."))` + ruta `POST /api/v1/auth/logout` |
| 2 | 200 | Request **sin body** (`peticion.body.readUtf8().isEmpty()`) |
| 3 | 401 `UNAUTHORIZED` | `Fallo(SesionExpirada)` |
| 4 | 500 `INTERNAL_ERROR` | `Fallo(ErrorServidor)` |
| 5 | `DISCONNECT_AT_START` (sin red) | `Fallo(ErrorConexion)` |

**No hay casos nuevos en `ErrorMapperTest`** (cero ramas nuevas, D-36) ni en
`ValidatorsTest`.

**Ajuste obligatorio de compilación en fakes existentes** (no son tests "nuevos"
pero cambian): `LoginViewModelTest.FakeAuthRepository` y
`RegistroViewModelTest.FakeAuthRepository` añaden
`override suspend fun logout(): Resultado<MessageResponse> = error("No usado…")`.

### 7.2 Tests androidTest (Compose)

**`HomePlaceholderScreenTest.kt` (extensión) — robot de la pantalla con
parámetros de diálogo:**
| # | Caso |
|---|---|
| 1 (reescrito) | Tap "Cerrar sesión" → **aparece el diálogo** "¿Deseas cerrar sesión?" con "Sí, cerrar sesión" y "Cancelar" (antes: incrementaba el callback directo) |
| 2 (nuevo) | "Cancelar" → diálogo desaparece; `onCancelarCierre` invocado; no se invoca `onConfirmarCierre` |
| 3 (nuevo) | "Sí, cerrar sesión" → `onConfirmarCierre` invocado |
| 4 (regresión) | "Mi cuenta" → `onNavigatePerfil` |
| 5 (regresión) | "Sesión iniciada ✅" visible |

Se usan `testTag`s en los botones del diálogo (lección Fase 2/3: matchers
robustos; `SetText` no aplica aquí al no haber campos).

### 7.3 Objetivo de conteo sobre 123 verdes actuales

| Depósito | Antes | Nuevos (estimado) | Después |
|---|---|---|---|
| Unitarios existentes (Fase 1+2+3) | 123 | — | 123 |
| `HomePlaceholderViewModelTest` | — | ~10 | +10 |
| `AuthRepositoryTest` (logout) | — | ~5 | +5 |
| **Total unitarios** | **123** | **~15** | **~138** |

(androidTest NO suma al `testDebugUnitTest` verde; se compilan y ejecutan con
`connectedDebugAndroidTest`. Estimado 45 → ~47: 2 nuevos + 1 reescrito.)

## 8. Seguridad (CLAUDE.md §5 aplicado a esta fase)

- **JWT solo en Keystore:** `limpiarToken()` (vía `SesionRepository`/`TokenManager`)
  ya garantiza el borrado cifrado. No se toca `TokenManager`.
- **Orden crítico:** la limpieza ocurre **después** de que el POST de logout
  termine, para que el `JwtInterceptor` adjunte el token a la propia petición.
- **Nunca loguear en claro:** el JWT, correo o datos personales. El POST/logout
  no imprime el body; `aEraError` no loguea cuerpos (patrón Fase 1–3).
- **CA4 / privacidad:** el progreso está en el servidor y no se borra; no existe
  todavía Room local (Fase 7), así que no hay datos locales sensibles que limpiar
  más allá del token. Cuando llegue Room, revisitar "limpiar estado sensible,
  no necesariamente el progreso" (CLAUDE.md §5, nota de logout).
- **Fallo de red no bloquea el cierre** (D-32): un menor sin conexión no queda
  atrapado en sesión abierta (HU-04: "proteger mi cuenta si otra persona usa el
  dispositivo").

## 9. Archivos a crear / modificar

**Crear:**
| Archivo | Descripción |
|---|---|
| `ui/login/HomePlaceholderUiState.kt` | UiState + evento del flujo de cierre (D-34) |
| `test/…/ui/login/HomePlaceholderViewModelTest.kt` | Tests unitarios del VM (7.1) |
| `androidTest/…/ui/login/HomePlaceholderScreenTest.kt` | **No es nuevo; se modifica** (ver abajo; 7.2) |

**Modificar:**
| Archivo | Cambio |
|---|---|
| `repository/AuthRepository.kt` | + `logout(): Resultado<MessageResponse>` (D-33) |
| `repository/RemoteAuthRepository.kt` | + `logout() = llamar { api.logout() }` (D-33) |
| `ui/login/HomePlaceholderViewModel.kt` | Evolución: inyectar `AuthRepository`, UiState, eventos, `onCerrarSesionClick/onCancelarCierre/onConfirmarCierre`; eliminar `cerrarSesion()` (D-34) |
| `ui/login/HomePlaceholderScreen.kt` | + `AlertDialog` de confirmación + parámetros con defaults (D-34/D-35) |
| `ui/navigation/EraNavHost.kt` | Reemplazar `onCerrarSesion` directo por eventos→navegación (D-34/D-35) |
| `test/…/ui/login/LoginViewModelTest.kt` | Fake `AuthRepository`: + stub `logout()` (compilación, D-33) |
| `test/…/ui/register/RegistroViewModelTest.kt` | Fake `AuthRepository`: + stub `logout()` (compilación, D-33) |
| `test/…/repository/AuthRepositoryTest.kt` | + casos logout MockWebServer (5) |
| `androidTest/…/ui/login/HomePlaceholderScreenTest.kt` | Reescribir test del botón + casos del diálogo (7.2) |

**No se modifica:** `AuthApi.kt`, DTOs (`MessageResponse` incluido),
`JwtInterceptor`, `TokenManager`, `SesionRepository`, `TokenManagerSesion`,
`ErrorMapper`, `EraError`, `MensajeError`, `Validators`, `Color.kt`,
`RepositoryModule`, `EraRoutes` (la ruta `login` ya existe), `NetworkModule`.
**Cero dependencias nuevas** (`build.gradle.kts` y `libs.versions.toml` intactos).
**No se elimina** ningún archivo.

## 10. Flujo de navegación

```
HomePlaceholderScreen ("Sesión iniciada", botones Mi cuenta / Cerrar sesión)
   │  tap "Cerrar sesión"
   ▼
Diálogo de confirmación: "¿Deseas cerrar sesión?"  [REQ-FUN-04 CA1]
   ├─ "Cancelar" → se cierra el diálogo, permanece (CA3) — sin red, sin limpiar
   └─ "Sí, cerrar sesión"
        │  cerrando=true (botones deshabilitados + spinner)
        ▼
        POST /auth/logout (JwtInterceptor adjunta token)   [confirmación formal]
        │  cualquier resultado (200/401/500/sin red)
        ▼
        sesionRepository.limpiarToken()                    [CA2: invalidación local]
        ▼
        evento NavegarALogin →
        navigate(LOGIN) { popUpTo(0) { inclusive = true } }  [limpieza backstack]
Login
```

## 11. Definition of Done de la fase

1. Suite unitaria verde: `.\gradlew.bat :app:testDebugUnitTest --console=plain -q`
   → **123 previos + ~15 nuevos** (ViewModel + Repository + stubs de fakes).
   Regla §4.13: los 123 previos siguen verdes.
2. Compilación limpia: `.\gradlew.bat :app:assembleDebug`.
3. `assembleDebugAndroidTest` compilado (y `connectedDebugAndroidTest` en físico
   si hay dispositivo): `HomePlaceholderScreenTest` con diálogo.
4. Flujo manual contra backend dev: login → home → "Cerrar sesión" → diálogo →
   "Cancelar" (queda) → "Cerrar sesión" → "Sí, cerrar sesión" → POST 200 →
   JWT limpio → nos lleva a Login; reintentar volver atrás no reabre home.
   Repetir con backend apagado: aún así se limpia sesión y navega a Login.
5. Sin secretos ni PII en logs (inspección logcat debug); el JWT nunca se loguea.
6. Cero dependencias nuevas; `AuthApi`, DTOs, `ErrorMapper`, `MensajeError`,
   `EraError`, `Color.kt` sin cambios (verificable por `git diff` del propietario).
7. Este documento y `CLAUDE.md` §10 actualizados al cerrar (mover a
   "Fase 4 completada", fecha y conteo final). **`decisiones-tecnicas.md` y
   `README.md` no se tocan hasta el cierre.**
8. Sugerir mensaje de commit; **el propietario ejecuta `git add`/`git commit`**
   (regla §4.5).

## 12. Puntos abiertos / requieren palabra del propietario

1. **Numeración D-XX (verificación solicitada):** se confirmó que D-31 ya fue
   referenciado en `fase-03-perfil-analisis.md` §6.4/§9 (para
   `formatearFechaISO`), sin sección de decisión propia, mientras la cabecera
   oficial de Fase 3 declara D-23…D-30. **Propuesta:** Fase 4 usa **D-32…D-36**
   y al cerrar Fase 4 se añade una línea al acta fase-03 aclarando que
   `formatearFechaISO` quedó como D-31 (o se renumeran a D-30 si el propietario
   prefiere continuar en D-31). Confirmar.
2. **Comportamiento ante fallos de red/500 (D-32):** se propone limpiar +
   redirigir siempre (invalidación cliente, sin logout offline bloqueado).
   ¿Se acepta, o se prefiere el modo estricto (solo limpiar tras 200, mostrar
   error y quedarse)? Recomendación: aceptar D-32.
3. **Prototipo del diálogo ausente:** `docs/prototipos/` está **vacío** (regla
   §4.11). Se toma `decisiones-tecnicas.md` §13.9.4 (dialog-confirm) como
   referencia visual y REQ-FUN-04 CA1 como texto vinculante. Textos literales
   propuestos: título "Cerrar sesión", mensaje "¿Deseas cerrar sesión?",
   botones "Cancelar" y "Sí, cerrar sesión". Confirmar.
4. **Test instrumentado reescrito:** `cerrarSesionInvocaOnCerrarSesion` cambia
   de semántica (el botón ya no cierra directo, abre el diálogo): se reescribe
   como "abre el diálogo". Aceptar el cambio del conteo 45 → ~47.
5. **Feedback visual:** no se muestra snackbar al cerrar (la redirección a
   Login es el feedback; D-32 no muestra errores de la llamada). ¿Aceptable?
6. **Anti doble-tap y no-abortable** (D-35): botones deshabilitados mientras
   `cerrando` y `onDismissRequest` no-op durante el POST. ¿Aceptable?

---

## 13. Aprobación del propietario y del auditor

- **Auditoría CAPA REPOSITORY (D-33):** APROBADA. Verificado `logout(): Resultado<MessageResponse>`
  en `AuthRepository.kt:15`, wrapper `llamar` en `RemoteAuthRepository.kt:37-38`, 5 casos
  MockWebServer, fakes con stub `error("No usado…")`, 128 unitarios verdes.
- **Auditoría CAPA VIEWMODEL (D-34/D-35):** APROBADA. Verificado best-effort D-32
  (`logout()` descarta resultado → `limpiarToken()` → `NavegarALogin`), anti re-apertura
  y anti re-entrada, diálogo M3 con textos/tamaños del diseño y 4 testTags, evento→navegación
  `popUpTo(0){inclusive=true}`, 138 unitarios verdes (10/10 del `HomePlaceholderViewModelTest`).
- **CAPA UI/NAVEGACIÓN:** luz verde tras ambas auditorías.

**Resolución de los 6 puntos abiertos (§12), aprobada por el propietario:**
1. Numeración **D-32…D-36** (D-31 quedó referenciado en Fase 3 sin sección propia).
2. **D-32 aprobado:** se limpia token y se navega a Login ante **cualquier** resultado
   (incluido offline).
3. Textos literales del diálogo aprobados: título "Cerrar sesión", mensaje "¿Deseas
   cerrar sesión?", botones "Cancelar" / "Sí, cerrar sesión".
4. Test instrumentado reescrito (el botón abre el diálogo, ya no cierra directo).
5. **Sin snackbar** de confirmación (la redirección a Login es el feedback).
6. **Anti doble-tap** con `cerrando` y `onDismissRequest` no-op durante el POST, aprobado.

---

## 14. Registro de implementación

Implementación capa por capa (2026-08-29), con portón de auditoría entre capas.

**Capa 1 — Repository (D-33):** `logout(): Resultado<MessageResponse>` en
`AuthRepository.kt` + `RemoteAuthRepository.kt` (`llamar { api.logout() }`). 5 tests
nuevos en `AuthRepositoryTest.kt` (200 Exito+ruta "POST /api/v1/auth/logout", 200 sin
body, 401→`SesionExpirada`, 500→`ErrorServidor`, sin red→`ErrorConexion`). Stubs
`logout()` añadidos a los fakes de `LoginViewModelTest.kt:202` y
`RegistroViewModelTest.kt:577`. → **128 unitarios verdes.**

**Capa 2 — ViewModel (D-34/D-35):** nuevo `ui/login/HomePlaceholderUiState.kt`
(`HomePlaceholderUiState(dialogoCierreVisible, cerrando)` + evento `NavegarALogin`);
`HomePlaceholderViewModel` evolucionado (inyecta `AuthRepository`; `onCerrarSesionClick`
anti re-apertura, `onCancelarCierre` no-op si `cerrando` (CA3), `onConfirmarCierre` =
best-effort D-32 → `limpiarToken()` → evento; se eliminó `cerrarSesion()`). Wiring de
compilación §6.3/§6.4: `HomePlaceholderScreen.kt` con parámetros con defaults + diálogo
M3, y `EraNavHost.kt` con evento→navegación `popUpTo(0){inclusive=true}`. Nuevo
`HomePlaceholderViewModelTest.kt` (10 tests: inicial, abrir diálogo, cancelar sin
efectos, 200/offline/401/500 → limpia+navega, cancel no interrumpe cierre en vuelo,
doble confirmación → 1 POST, re-apertura no cambia estado). → **138 unitarios verdes.**

**Capa 3 — UI/Navegación (D-34/D-35):** `HomePlaceholderScreenTest.kt` reescrito
(5 tests): regresiones "Sesión iniciada ✅" y "Mi cuenta", "Cerrar sesión" abre el
diálogo, "Cancelar" lo cierra sin confirmar (CA3), "Sí, cerrar sesión" dispara
`onConfirmarCierre`. Tags usados: `botonCerrarSesion`, `botonConfirmarCierre`,
`botonCancelarCierre`, `dialogoCierre`.

**Verificación final:**
- `.\gradlew.bat :app:testDebugUnitTest` → **138 tests, 0 fallos** (123 previos + 15 nuevos).
- `.\gradlew.bat :app:assembleDebug` y `:app:assembleDebugAndroidTest` → BUILD SUCCESSFUL.
- `.\gradlew.bat :app:connectedDebugAndroidTest` → **47/47 verdes** en ABR-LX3
  (2 nuevos + 1 reescrito + 44 previos). El dispositivo inalámbrico se cayó a mitad en
  3 intentos; reconectado por adb wireless y reintentado hasta completar (patrón conocido).
- Cero dependencias nuevas; sin cambios en `AuthApi`, DTOs, `ErrorMapper`, `MensajeError`,
  `EraError`, `Color.kt`, build files ni `libs.versions.toml` (D-36).

**Mensaje de commit sugerido:**
`feat(logout): Fase 4 — cierre de sesión con diálogo de confirmación y best-effort D-32, 138 unitarios + 47 instrumentados`

**Pendiente del propietario:** revisión en emulador (flujo manual del DoD §11.4, incluido
con backend apagado), cierre del doc/CLAUDE/README/decisiones-tecnicas y `git add`/`git commit`.