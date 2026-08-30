# Fase 5 — Recuperación de contraseña (Módulo C): Análisis y Diseño

> Documento de análisis del módulo frontend. Registra el alcance, el diseño,
> las decisiones propuestas (D-37…) y los archivos a crear/modificar.
> **Estado:** **EN REVISIÓN — correcciones del propietario y del auditor
> aplicadas (2026-08-29). No se ha implementado ninguna capa.** Pendiente solo
> de la aprobación final para iniciar la implementación capa por capa
> (ver §12 y §13).

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-07 (Recuperación de contraseña, prioridad **Alta**) |
| Requisitos no funcionales asociados | REQ-NF-02 (seguridad: anti-enumeración, bcrypt, mensajes genéricos) |
| Caso de uso | CU-03 (Recuperar contraseña) + CU-11 (Verificar código, `<<include>>`) |
| Historia de usuario | HU-07 |
| Endpoints backend consumidos | `POST /auth/password-reset/request`, `verify`, `confirm` |
| Fuente visual | `decisiones-tecnicas.md` §14.2–14.4 (Recuperar contraseña 1/3, 2/3, 3/3) y §16.5/§16.6 — **no existe prototipo JPG** (ver §12.3) |
| Fuente de contrato | `BACKEND_ERA/docs/modulo-c-analisis.md` (decisiones C-1…C-6), `BACKEND_ERA/README.md` |

## 2. Alcance

**Incluye:**
- Flujo de 3 pasos en 3 pantallas (REQ-FUN-07): (1) ingreso de correo y
  "Enviar código"; (2) verificación OTP de 6 dígitos y "Verificar código"
  (con reenvío y countdown); (3) nueva contraseña + confirmar y guardar, con
  redirección al login al finalizar.
- Capa `repository/`: `requestPasswordReset()`, `verifyPasswordReset()`,
  `confirmPasswordReset()` añadidos a `AuthRepository` + `RemoteAuthRepository`
  (reuso del wrapper `llamar`; `AuthApi` y los 4 DTOs de reseteo **ya existen**
  desde Fase 0 — no se tocan).
- Errores: 2 subtipos nuevos en `EraError` (`ResetTokenInvalido`,
  `PasswordReusada`) + ramas en `ErrorMapper` y `MensajeError` (D-40).
- UI: nuevo paquete `ui/recuperacion/` (1 ViewModel + UiState + 3 pantallas con
  sus `Content`), enlace "¿Olvidaste la contraseña?" en `LoginScreen` que
  **reemplaza** la snackbar "Próximamente" por navegación real (D-45), rutas y
  grafo de navegación (D-38), fin de flujo → Login con `popUpTo(0){inclusive=true}`
  + snackbar de confirmación (patrón D-14).
- El **resetToken puente** se conserva **solo en memoria** durante el flujo
  (D-37), acorde a CLAUDE.md §5.
- Tests unitarios (ViewModels + Repository MockWebServer + ErrorMapper),
  ajuste de fakes que implementan `AuthRepository` y androidTest de las 3
  pantallas nuevas.

**Fuera de alcance (cerrado):**
- **No se valida, decodifica ni inspecciona el JWT puente** en el cliente: se
  recibe de `/verify`, se conserva en memoria y se reenvía a `/confirm` tal cual.
- Reenvío de OTP: es una **nueva llamada a `/request`** (el backend responde
  429 si < 60 s, C-2). No hay endpoint nuevo, no se replica el throttle local
  como límite real (solo countdown de UX).
- "Olvidé mi usuario": el login ya acepta usuario **o** correo; no es objeto de
  esta fase.
- No se toca `TokenManager`, `SesionRepository` ni `TokenManagerSesion`: el
  flujo **no crea sesión** y el JWT de sesión existente **no se modifica**.
- No hay pantalla de éxito propia: se redirige a Login con snackbar (REQ-FUN-07
  CA5: "se redirige al inicio de sesión").
- Limpieza de Room / capa `data/` → **Fase 7** (en esta fase no hay datos
  locales sensibles más allá del JWT, que no se toca).
- Sidebar y Home reales → **Fase 10**.

> **Nota de numeración D-XX:** la secuencia real registrada es Fase 1 →
> D-01…D-14, Fase 2 → D-16…D-22 (D-15 sin sección propia), Fase 3 →
> D-23…D-30 (dónde D-31 quedó referenciado sin sección), Fase 4 →
> D-32…D-36. Por tanto, Fase 5 usa **D-37…D-45** (sin colisión). Confirmado
> por el propietario (§12.4).

## 3. Estado previo aprovechado (verificado en código, no reescribe)

| Componente | Archivo | Estado |
|---|---|---|
| `AuthApi.requestPasswordReset/verifyPasswordReset/confirmPasswordReset` (`@POST("auth/password-reset/…")`) | `remote/api/AuthApi.kt:33-40` | ✅ **Ya declarado** — sin cambios |
| DTOs `PasswordResetRequest`, `PasswordResetVerifyRequest`, `PasswordResetConfirmRequest`, `PasswordResetVerifyResponse` (+ `MessageResponse`) | `remote/dto/auth/*.kt` | ✅ Ya existen — sin cambios |
| `AuthRepository` (interfaz) + `RemoteAuthRepository` (wrapper `llamar` + `aEraError`) | `repository/AuthRepository.kt`, `repository/RemoteAuthRepository.kt:40-56` | ✅ Extender con 3 métodos (D-39); no duplicar wrapper |
| `EraError.OtpInvalido` (401 `OTP_INVALID_OR_EXPIRED`) y `EraError.ReenvioThrottled` (429) | `utils/EraError.kt:8-9` | ✅ Reutilizar tal cual |
| `ErrorMapper.desdeHttp` — mapea por `body.error`, nunca por mensaje | `utils/ErrorMapper.kt:8-30` | ✅ Añadir 2 ramas (D-40) |
| `MensajeError` (`mensajeUsuario()` `when` exhaustivo + `mapsTo` + `mensajeCampo(CampoRegistro)`) | `utils/MensajeError.kt` | ✅ Añadir 2 mensajes + `mensajeCampo(CampoRecuperacion)` (D-40/D-41) |
| `PasswordPolicy.criterios/errores/esValida(contrasena, nombreUsuario, nombreMenor)` | `utils/PasswordPolicy.kt` | ✅ Reutilizar con `""` (D-41); **firma intacta** (protege 13 tests) |
| `Validators.isValidEmail`, `isValidOtp`, `OTP_LENGTH` | `utils/Validators.kt:19,26-42` | ✅ Reutilizar |
| Patrón countdown reenvío 60 s síncrono (D-10) con límite real delegado al backend (D-16) | `ui/register/RegistroViewModel.kt:222-233` | ✅ Réplica exacta para el paso 2 |
| Patrón OTP/pantallas: `RegistroPaso2Screen`/`RegistroPaso3Screen` (icono circular, `EraTextField`, countdown, `InfoBox`) | `ui/register/RegistroPaso2Screen.kt`, `RegistroPaso3Screen.kt` | ✅ Espejo para los pasos 2/3 |
| Componentes: `CompactGreenHeader(titulo, subtitulo)`, `StepIndicator(pasoActual, totalPasos=3)`, `EraTextField` (label/error/iconoFin/visualTransformation), `InfoBox`, `EraRegPrimaryButton` | `ui/components/*.kt` | ✅ Reutilizar, cero componentes nuevos |
| `LoginViewModel.onOlvidasteContrasena()` → snackbar "Próximamente" | `ui/login/LoginViewModel.kt:94-96` | ✅ Reemplazar por evento `NavegarARecuperacion` (D-45) |
| `LoginUiState`/`LoginEvento` (patrón evento→navegación) | `ui/login/LoginUiState.kt`, `ui/login/LoginScreen.kt:236-250` | ✅ Añadir evento; `LoginContent` sin cambios |
| `EraRoutes` y patrón de grafo con `hiltViewModel(parentEntry)` (Registro Fase 1) | `ui/navigation/EraRoutes.kt`, `ui/navigation/EraNavHost.kt:79-134` | ✅ Grafo paralelo para recuperación (D-38) |
| Snackbar post-flujo vía `savedStateHandle["registro_exitoso"]` (D-14) | `ui/navigation/EraNavHost.kt:124-131`, `ui/login/LoginScreen.kt:236-239` | ✅ Réplica con `"recuperacion_exitosa"` (D-44) |
| Fakes de `AuthRepository` en tests | `test/…/ui/register/RegistroViewModelTest.kt:534`, `test/…/ui/login/LoginViewModelTest.kt:181`, `test/…/ui/login/HomePlaceholderViewModelTest.kt:192` | ⚠️ **Compilan-break**: al añadir 3 métodos deben añadir 3 `override` stub (ver §7.1) |
| SSL/BASE_URL dev `http://192.168.20.64:8080/api/v1/`, `JwtInterceptor`, `NetworkModule`, `RepositoryModule` (@Binds `AuthRepository`→`RemoteAuthRepository`) | `remote/…`, `di/…` | ✅ Sin cambios |

## 4. Contrato exacto consumido (fuente: modulo-c-analisis.md + README)

Ruta base: `/api/v1/auth` (el prefijo `/api/v1` lo añade `NetworkModule`).

### 4.1 `POST /auth/password-reset/request` — `PasswordResetRequest`

| Campo | Tipo | Regla |
|---|---|---|
| `correo` | String | formato email (≤ 255) |

**Response 200 OK (idéntica exista o no la cuenta, C-1):**
```json
{ "message": "Si el correo está registrado, recibirás un código de verificación." }
```

**Códigos de error:**
| Status | `error` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Falla de forma del correo (con `details`) |
| 429 | `OTP_RESEND_THROTTLED` | Reenvío antes de 60 s (C-2) — solo con OTP previo |
| 500 | `INTERNAL_ERROR` | Inesperado |

### 4.2 `POST /auth/password-reset/verify` — `PasswordResetVerifyRequest`

| Campo | Tipo | Regla |
|---|---|---|
| `correo` | String | email válido |
| `codigo` | String | exactamente 6 dígitos (`^\d{6}$`) |

**Response 200 OK:**
```json
{ "resetToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9…" }
```
El token puente es JWT HS256 de 10 min, single-use, doble vínculo `jti`+`sub`
(C-3). Máx. 3 intentos fallidos (P1, R2).

**Códigos de error:**
| Status | `error` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Forma (correo/código) |
| 401 | `OTP_INVALID_OR_EXPIRED` | Incorrecto, vencido, usado o **sin cuenta activa** — mensaje genérico (C-1) |
| 500 | `INTERNAL_ERROR` | Inesperado |

### 4.3 `POST /auth/password-reset/confirm` — `PasswordResetConfirmRequest`

| Campo | Tipo | Regla |
|---|---|---|
| `resetToken` | String | token puente del paso 2; no blanco |
| `nuevaContrasena` | String | política compartida (C-6); ≤ 72 (tope bcrypt) |
| `confirmarContrasena` | String | coincide con `nuevaContrasena` |

**Response 200 OK:**
```json
{ "message": "Contraseña actualizada. Ya puedes iniciar sesión." }
```

**Códigos de error:**
| Status | `error` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Política de contraseña (C-6) o incoherencia, con `details` por campo |
| 401 | `RESET_TOKEN_INVALID` | Token inválido, vencido, consumido o vínculo roto — mensaje genérico |
| 409 | `PASSWORD_REUSED` | La nueva contraseña repite la anterior (REQ-FUN-07 CA5) |
| 500 | `INTERNAL_ERROR` | Inesperado |

**No crea sesión.** El frontend no recibe un JWT de sesión en ningún paso: al
terminar, el usuario vuelve a Login (se redirige manualmente; "redirige al inicio
de sesión", REQ-FUN-07 desc. y CA5).

## 5. Decisiones de diseño (D-37…)

### D-37 — El `resetToken` vive solo en memoria: campo privado del ViewModel de grafo

**Decisión:** el token puente devuelto por `/verify` se conserva en un
`private var resetToken: String?` del `RecuperacionViewModel` *scoped al grafo*
de recuperación. Se asigna al recibir el 200 de `/verify` (justo antes de navegar
al paso 3), se **limpia** al obtener 200 en `/confirm`, al reiniciar el flujo
(D-44) y al `cancelar()`. No se persiste en `TokenManager`, `SharedPreferences`
(planas o cifradas) ni `DataStore`.

| Alternativa evaluada | Motivo del rechazo |
|---|---|
| Pasar el token por **nav-arguments** (`recuperacion/paso3?token=…`) | Queda en `savedState` del `NavController`, viaja en el Bundle de restauración y sería visible en deep links/logs de navegación; cruza la línea "solo en memoria; no persistir entre reinicios" de CLAUDE.md §5 |
| `savedStateHandle` del destino | Navigation persiste el `NavBackStackEntry` en el Bundle de instancia guardada: el token sobreviviría a la muerte de proceso y se restauraría — prohibido por §5 |
| ViewModel por paso (3 VMs conectados por args) | Mismo defecto del traspaso por args + triple duplicación de estado transitorio |

**Por qué:** CLAUDE.md §5 es explícito para `era-app-reset`: "**solo en memoria
durante el flujo de recuperación; no persistir entre reinicios de la app**". El
ViewModel de un grafo sobrevive a la rotación (no se pierde al girar), **no**
sobrevive a la muerte de proceso (el token muere con él, que es justo lo que
pide la regla) y se descarta al `popBackStack` del grafo (V2: al salir del
flujo, el token desaparece). Es el **mismo patrón que Registro** (estado
transitorio compartido entre pasos vía ViewModel de grafo), sin arquitectura
nueva.

### D-38 — Un grafo de navegación + ViewModel único (espejo del Registro)

**Decisión:** se añade el grafo `navigation(route = RECUPERACION, startDestination
= RECUPERACION_PASO1)` con tres destinos `recuperacion/paso1|paso2|paso3`
(§15.2 de `decisiones-tecnicas.md`). Cada destino obtiene el **mismo**
`RecuperacionViewModel` vía `hiltViewModel(parentEntry)` (patrón
`EraNavHost.kt:84-86`). Las transiciones paso→paso son `navigate(PASO_N)` y
volver es `popBackStack()`; en el paso 1, "Volver al inicio de sesión" hace
`popBackStack()` al grafo completo.

```
EraRoutes (nuevo):
  RECUPERACION       = "recuperacion"
  RECUPERACION_PASO1 = "recuperacion/paso1"
  RECUPERACION_PASO2 = "recuperacion/paso2"
  RECUPERACION_PASO3 = "recuperacion/paso3"
```

**Por qué:** es un flujo de 3 pasos con **estado transitorio compartido**
(correo del paso 1, `resetToken` del paso 2) — exactamente el problema que
Resolvió el Registro con un ViewModel de grafo. Tres VMs independientes
obligarían a clavar el correo/token en args (§5 de la fase-01); un solo VM
elimina el traspaso y garantiza el borrado conjunto al salir (D-37).

### D-39 — Repository: 3 métodos en `AuthRepository` (espejo de D-33)

**Decisión:**

| Método | Firma |
|---|---|
| `requestPasswordReset` | `suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit>` |
| `verifyPasswordReset` | `suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse>` |
| `confirmPasswordReset` | `suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit>` |

Implementados en `RemoteAuthRepository` como `llamar { api.…(request) }`
(reutilizando el wrapper `llamar` + `aEraError` de
`RemoteAuthRepository.kt:40-56`, que relanza `CancellationException` y mapea
`HttpException` → `ErrorMapper`).

**Por qué:** `request/verify/confirm` pertenecen al dominio `/auth/*`, y
`AuthApi` + los 4 DTOs **ya existen** desde Fase 0 (contrato remoto aprobado
2026-08-23) — no se crea DTO, ni interfaz nueva (`PasswordResetRepository`
separado no se justifica: mismo path de autenticación, mismo wrapper). `request`
y `confirm` devuelven `MessageResponse` → se exponen como `Resultado<Unit>`
(espejo de `register`, que también devuelve `Resultado<Unit>`), porque al
cliente solo le importa el resultado; `verify` necesita el `resetToken`.

**Consecuencia obligatoria en tests:** los **3** fakes que implementan
`AuthRepository` (`RegistroViewModelTest.kt:534`, `LoginViewModelTest.kt:181`,
`HomePlaceholderViewModelTest.kt:192`) deben añadir 3 `override` con
`error("No usado en … tests")` (D-40 no los toca) para seguir compilando (ver §9).

### D-40 — Dos subtipos de error nuevos; cero refactor de ramas existentes

**Decisión:**

```kotlin
// EraError.kt
data object ResetTokenInvalido : EraError()   // 401 RESET_TOKEN_INVALID
data object PasswordReusada      : EraError() // 409 PASSWORD_REUSED

// ErrorMapper.kt (ramas nuevas en desdeHttp)
"RESET_TOKEN_INVALID" -> EraError.ResetTokenInvalido
"PASSWORD_REUSED"     -> EraError.PasswordReusada

// MensajeError.kt (mensajeUsuario)
is EraError.ResetTokenInvalido -> "El enlace de recuperación expiró. Vuelve a solicitar un nuevo código"
is EraError.PasswordReusada    -> "No puedes repetir tu contraseña anterior"
```

El `when` exhaustivo de `MensajeError.mensajeUsuario()` exige sumar ambas (el
compilador lo marca). El `mapsTo` privado de `CampoRegistro` (el existente) suma
las 2 ramas nuevas → `false` (no son errores de campo en Registro). Para el
flujo nuevo se define el **contrato completo** campo/global, espejo del de
`CampoRegistro` (D-41):

```kotlin
// MensajeError.kt
fun mensajeCampo(campo: CampoRecuperacion): String = when (campo) {
    CampoRecuperacion.CORREO                -> "Ingresa un correo válido"
    CampoRecuperacion.CODIGO_OTP            -> "Ingresa 6 dígitos numéricos"
    CampoRecuperacion.NUEVA_CONTRASENA      -> "La contraseña no cumple los requisitos"
    CampoRecuperacion.CONFIRMAR_CONTRASENA  -> "Las contraseñas no coinciden"
}

private infix fun CampoRecuperacion.mapsTo(error: EraError): Boolean = when (error) {
    // Ningún EraError del flujo se asocia a un campo: los errores de servidor
    // (Validacion, OtpInvalido, ResetTokenInvalido, PasswordReusada, …) se
    // muestran siempre como errorGeneral; solo los errores de forma LOCALES
    // de validación van por campo, vía mensajeCampo.
    is EraError.OtpInvalido,
    is EraError.ReenvioThrottled,
    is EraError.Validacion,
    is EraError.ErrorServidor,
    is EraError.ErrorConexion,
    is EraError.Desconocido,
    is EraError.ResetTokenInvalido,
    is EraError.PasswordReusada,
    is EraError.CorreoRegistrado,
    is EraError.CorreoBloqueado,
    is EraError.UsuarioEnUso,
    is EraError.CredencialesInvalidas,
    is EraError.CuentaBloqueada,
    is EraError.CuentaInactiva,
    is EraError.SesionExpirada,
    is EraError.PerfilNoEncontrado -> false
}

fun CampoRecuperacion.mensaje(
    errores: Set<CampoRecuperacion>,
    errorGeneral: EraError?,
): String? {
    if (this !in errores) return null
    return when {
        errorGeneral != null && this mapsTo errorGeneral -> errorGeneral.mensajeUsuario()
        else -> mensajeCampo(this)
    }
}
```

Decisiones por pantalla (campo vs `errorGeneral`): **paso 1** `Validacion` (400)
→ `errorGeneral` con los `details` del servidor; la forma **local** del correo →
campo `CORREO` vía `mensajeCampo`. **Paso 2** `OtpInvalido` (401) →
`errorGeneral` (mismo texto genérico del Registro, D-42/D-43). **Paso 3**
`ResetTokenInvalido`, `PasswordReusada` y `Validacion` → `errorGeneral` (D-44).

**Por qué:** `RESET_TOKEN_INVALID` y `PASSWORD_REUSED` son códigos nuevos del
contrato Módulo C; sin ellos el `error` caería en `Desconocido` y el usuario
vería "Error inesperado" (regla §7: mapear por el campo `error` en un solo
lugar). `OtpInvalido` y `ReenvioThrottled` **ya existen** y se reutilizan sin
duplicar. No se renombra ni refactoriza ninguna rama existente → los tests de
`ErrorMapperTest` previos siguen intactos.

### D-41 — Validación cliente espejo, con límite honesto de conocimiento

**Decisión:**
- **Correo** (paso 1): `Validators.isValidEmail` + trim. Error de campo:
  "Ingresa un correo válido".
- **OTP** (paso 2): filtro de entrada `\d{6}` (`onCodigoOtpChange` toma solo
  dígitos y `take(6)`, espejo de `RegistroViewModel.kt:153-158`) + validación
  con `Validators.isValidOtp`. Error: "Ingresa 6 dígitos numéricos".
- **Contraseña** (paso 3): `PasswordPolicy.esValida(nueva, nombreUsuario = "",
  nombreMenor = "")` + coincidencia con `confirmarContrasena`. Error de campo
  cuando no cumple, con `criteriosContrasena` en vivo (patrón Registro paso 2).

**Por qué:** el cliente **solo conoce el correo**, no el `nombreUsuario` ni el
`nombreMenor` (el usuario los olvidó; por eso recupera, y login acepta el correo).
Con `""` los criterios dependientes de datos (`distintaDeUsuario`,
`sinDatosPersonales`) quedan vacíos y solo se validan en cliente los criterios
de forma/tamaño (≥ 8, mayúscula, minúscula, número, símbolo, ≤ 72) — exactamente
lo que el cliente puede verificar. El veto "sin datos personales / igual al
usuario" lo impone el backend (C-6) y llega como `VALIDATION_ERROR` con
`details` por campo → se muestra como `errorGeneral` con los mensajes del
servidor. Es la regla §4.8 ("validar todo lo que el backend valida… sin asumir
que el cliente reemplaza al servidor") aplicada al límite real de datos. No se
modifica la firma de `PasswordPolicy` (protege los 13 tests de
`PasswordPolicyTest` y el uso del Registro).

### D-42 — Anti-enumeración en UI: el 200 de `/request` es indistinguible

**Decisión:** el paso 1 **nunca** diferencia el resultado del envío:

| Resultado de `requestPasswordReset` | UX |
|---|---|
| 200 | Avanza al paso 2 con el **mismo** flujo; sin snackbar de éxito ni texto distinto. El paso 2 muestra "Código enviado a `<correo>`" (el correo que el propio usuario escribió), que no enumera |
| 400 `VALIDATION_ERROR` | `Validacion` como `errorGeneral` con los `details` del servidor (multi-línea, D-40); la forma **local** del correo va por campo vía `mensajeCampo(CORREO)` (D-41) |
| 429 `OTP_RESEND_THROTTLED` | Error `ReenvioThrottled` ("Debes esperar antes de reenviar"). Solo ocurre tras un envío previo < 60 s: no enumera |
| 500 / sin red | `ErrorServidor` / `ErrorConexion` como `errorGeneral` |

**Por qué:** REQ-FUN-07 CA1 / CU-03 flujo alt. 1a / HU-07 CA1 exigen "mismo
mensaje genérico, **sin confirmar si el correo existe**". El backend garantiza
C-1 (respuesta y timing idénticos con `HASH_DUMMY`); la UI debe comportarse
igual: si el cliente mostrara "Correo no registrado" o un camino distinto
cuando el correo no existe, sería un oráculo de enumeración que **anularía**
la garantía del servidor. Un correo no registrado llega a `/verify` y falla con
el mismo 401 genérico que un código incorrecto (C-1 también en verify) — el
flujo "fracasa tarde y de forma genérica", que es lo que exige la
arquitectura.

### D-43 — Paso 2: espejo del Registro (D-10), countdown como UX, límite real en el backend

**Decisión:** el paso 2 replica el patrón `RegistroPaso3Screen`:
- Entrada OTP filtrada a 6 dígitos; "Verificar código" llama
  `verifyPasswordReset(correo, codigo)`.
  - 200 → `resetToken = respuesta.resetToken` (D-37) + `NavegarAPaso3`.
  - 401 `OtpInvalido` → `errorGeneral` con el **mismo texto genérico** del
    registro ("Código inválido o expirado") — anti-enumeración (D-42).
- "Reenviar código" = **nueva llamada a `requestPasswordReset`** (el reenvío
  real del Módulo C es otro `/request`; no existe `resend-otp` para reseteo).
  - 200 → `iniciarCountdownReenvio()` (60 s síncronos, patrón
    `RegistroViewModel.kt:222-233`).
  - 429 `ReenvioThrottled` → `Aviso` (snackbar) con "Debes esperar antes de
    reenviar"; no se reinicia el countdown.
  - 400/500/sin red → `Validacion`/`ErrorServidor`/`ErrorConexion`.
- El countdown local (60 s) es **solo UX**: el límite autoritativo lo aplica el
  backend con 429 (decisión D-16 de Fase 2: countdown delegado al backend).

**Por qué:** es el mismo modelo probado en Registro (D-10) y Login (D-16):
consistencia visual de UX (el niño siempre ve un countdown) y límite real de
seguridad en servidor. El texto del `InfoBox` del paso 2: "El código expira en
10 minutos. Si no lo recibes, revisa spam o reenvíalo".

### D-44 — Paso 3: resultado y fin de flujo (sin sesión)

**Decisión:**

| Resultado de `confirmPasswordReset` | UX |
|---|---|
| 200 | `resetToken = null` (D-37) y `NavegarARecuperacionExitosa` → `navigate(LOGIN){popUpTo(0){inclusive=true}}` + snackbar "Contraseña actualizada" en Login vía `savedStateHandle["recuperacion_exitosa"]` (patrón D-14) |
| 401 `ResetTokenInvalido` | Mensaje genérico + `resetToken = null` + `ReiniciarFlujo` → vuelve al paso 1 con el estado limpio (el correo se conserva para comodidad; el token no) |
| 409 `PasswordReusada` | `errorGeneral` ("No puedes repetir tu contraseña anterior") sobre el formulario, sin limpiar campos |
| 400 `Validacion` | Snackbar con los `details` del servidor mostrados **multi-línea** (unidos con `\n` en `mensajeUsuario()`, `MensajeError.kt:6`) — política/veto que el cliente no puede pre-calcular (D-41) |
| 500 / sin red | `ErrorServidor` / `ErrorConexion` |

**Guard tras restauración (muerte de proceso):** si el sistema restaura la pila
de navegación, el paso 3 puede recrearse con `resetToken == null` (el token es
**solo memoria** y muere con el ViewModel, D-37). Al pulsar "Guardar contraseña",
`guardarContrasena()` ejecuta primero
`val token = resetToken ?: run { _eventos.trySend(ReiniciarFlujo); return }`:
**no** se llama al repo con un token nulo (evita un `confirm` sin `resetToken`)
y el flujo vuelve limpio al paso 1 (correo conservado, token sin valor; D-44).
Cubierto con test unitario (§7.1, caso 19).

**Por qué:** REQ-FUN-07 termina "el sistema invalida el código usado y redirige
al **inicio de sesión**" (CA5) — la recuperación **no crea sesión** (por
diseño del Módulo C: ningún paso responde un token de sesión). Volver a Login
con `popUpTo(0){inclusive=true}` limpia todo el backstack del flujo (igual que
logout D-35 y Mi cuenta D-25). El snackbar usa el mensaje del backend
("Contraseña actualizada. Ya puedes iniciar sesión.") — feedback no enumerador.
`ResetTokenInvalido` (token vencido/consumido/vínculo roto) obliga a reiniciar:
reintentar `/confirm` con un token muerto es inútil y repetiría el mismo 401.

### D-45 — Enlace en Login: de snackbar "Próximamente" a navegación real

**Decisión:**
- `LoginViewModel.onOlvidasteContrasena()` deja de emitir
  `MostrarSnackbar("Próximamente")` y emite `LoginEvento.NavegarARecuperacion`
  (nuevo `data object` en `LoginUiState.kt`).
- `LoginScreen` gana `onNavigateARecuperacion: () -> Unit = {}` (default), y su
  `LaunchedEffect` ya existente (`LoginScreen.kt:236-250`) mapea el evento →
  `onNavigateARecuperacion()`.
- `EraNavHost`: `onNavigateARecuperacion = { navController.navigate(EraRoutes.RECUPERACION) }`.
- `LoginContent` y su firma **no cambian** (`onOlvidasteContrasena` ya existía)
  → los androidTest de `LoginScreenTest` (que ejercitan `LoginContent`) **no se
  rompen** y `LoginScreenTest.kt:147` (el enlace se muestra) sigue verde.

**Por qué:** el enlace "¿Olvidaste la contraseña?" está declarado en
`decisiones-tecnicas.md` §14.1 (enlace desde login al flujo 1/3); el snackbar
"Próximamente" es un placeholder de Fases 1–2 que la Fase 5 completa. No se
crea `RecuperacionViewModel`-de-login: la navegación es un evento puro, como
`NavegarARegistro`.

## 6. Arquitectura propuesta (capa por capa)

### 6.1 Remote (ya declarado — NO se edita)

`AuthApi.kt:33-40` (los 3 endpoints) y los 4 DTOs existen desde Fase 0 y se
verificaron contra el backend. `JwtInterceptor` **no** adjunta token aquí (son
endpoints anónimos; el interceptor no interfere con peticiones sin token).
**Sin cambios.**

### 6.2 Repository

```
AuthRepository (interfaz)  → repository/AuthRepository.kt
  + suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit>
  + suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse>
  + suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit>

RemoteAuthRepository (impl @Singleton)  → repository/RemoteAuthRepository.kt
  override suspend fun requestPasswordReset(request) = llamar { api.requestPasswordReset(request) }
  override suspend fun verifyPasswordReset(request)  = llamar { api.verifyPasswordReset(request) }
  override suspend fun confirmPasswordReset(request) = llamar { api.confirmPasswordReset(request) }
```
Sin cambios en DI (`RemoteAuthRepository` ya está ligado vía `@Binds` en
`RepositoryModule.kt`).

### 6.3 Errores

`EraError.kt` (+2 `data object`), `ErrorMapper.kt` (+2 ramas en `desdeHttp`),
`MensajeError.kt` (+2 casos en `mensajeUsuario`, +2 ramas `→ false` en el
`mapsTo` de `CampoRegistro`, + contrato completo `CampoRecuperacion`:
`mensajeCampo`, `mapsTo` y resolver `mensaje(errores, errorGeneral)`).
Detalle en D-40/D-41.

### 6.4 ViewModel — `ui/recuperacion/RecuperacionViewModel.kt` (nuevo)

Nuevo archivo `RecuperacionUiState.kt`:
```kotlin
enum class CampoRecuperacion {
    CORREO, CODIGO_OTP, NUEVA_CONTRASENA, CONFIRMAR_CONTRASENA,
}

data class RecuperacionUiState(
    val correo: String = "",
    val codigoOtp: String = "",
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarVisible: Boolean = false,
    val criteriosContrasena: CriteriosContrasena = PasswordPolicy.criterios("", "", ""),
    val reenvioSegundosRestantes: Int = 0,
    val errores: Set<CampoRecuperacion> = emptySet(),
    val errorGeneral: EraError? = null,
)

sealed interface RecuperacionEvento {
    data object NavegarAPaso2 : RecuperacionEvento
    data object NavegarAPaso3 : RecuperacionEvento
    data object RecuperacionExitosa : RecuperacionEvento
    data object ReiniciarFlujo : RecuperacionEvento
    data class Aviso(val error: EraError) : RecuperacionEvento
}
```

`RecuperacionViewModel` (@HiltViewModel, inyecta `AuthRepository`):
| Método | Comportamiento |
|---|---|
| `onCorreoChange` | actualiza + limpia error/`errorGeneral` |
| `enviarEnlace()` | valida `isValidEmail`; `requestPasswordReset`; 200 → `iniciarCountdownReenvio()` + `NavegarAPaso2`; errores → campo/general (D-42) |
| `onCodigoOtpChange` | filtro `\d{6}` + `take(6)` |
| `verificarCodigo()` | valida `isValidOtp`; `verifyPasswordReset`; 200 → `resetToken = …` + `NavegarAPaso3` (D-37/D-43) |
| `reenviarCodigo()` | no-op si `reenvioSegundosRestantes > 0`; `requestPasswordReset`; 200 → countdown; 429 → `Aviso(ReenvioThrottled)` (D-43) |
| `onNuevaContrasenaChange` / `onConfirmarContrasenaChange` | actualiza + limpia errores; criterios en vivo |
| `guardarContrasena()` | **guard:** `val token = resetToken ?: run { _eventos.trySend(ReiniciarFlujo); return }` — si `resetToken` es null (p. ej. restauración tras muerte de proceso, D-37) **no** llama al repo y reinicia el flujo; valida política (D-41) + coincidencia; `confirmPasswordReset` (con `token`); 200 → resetToken=null + `RecuperacionExitosa`; 401 → resetToken=null + `ReiniciarFlujo`; 409 → `errorGeneral` (D-44) |
| `toggleNuevaContrasenaVisible` / `toggleConfirmarVisible` | visibilidad de contraseñas |
| `cancelar()` | cancela `countdownJob`, `resetToken = null`, `_uiState.value = RecuperacionUiState()` |

### 6.5 Pantallas — `ui/recuperacion/` (nuevo, `Screen` + `Content` para tests)

Cada pantalla sigue el patrón Registro (`Screen` suscribe `vm.eventos` y
`state`; `Content` puro y testable). Los `testTag`s (camelCase, regla del repo):
`campoCorreo`, `botonEnviarCodigo`, `linkVolverAlLogin`,
`campoCodigo`, `botonVerificarCodigo`, `botonReenviarCodigo`,
`campoNuevaContrasena`, `campoConfirmarContrasena`, `botonGuardarContrasena`.

- **Paso 1 (`RecuperacionPaso1Screen`) — §14.2 + REQ-FUN-07:** `CompactGreenHeader`
  ("Recuperar contraseña", "Te enviamos un código de verificación a tu correo"); `StepIndicator(1)`;
  icono de correo sobre círculo (~100 dp, `ColorPrimary` al 12 % de opacidad,
  icono ~52 dp); título; `EraTextField` correo (Email keyboard); botón
  `EraRegPrimaryButton` "Enviar código"; error de campo /
  `errorGeneral`; enlace "Volver al inicio de sesión".
- **Paso 2 (`RecuperacionPaso2Screen`) — §14.3/§16.5:** `CompactGreenHeader`
  ("Recuperar contraseña", "Te enviamos un código de verificación a tu correo"); `StepIndicator(2)`;
  "Código enviado a" + `correo`; `EraTextField` OTP (Number, filtro 6 dígitos);
  `EraRegPrimaryButton` "Verificar código"; "Reenviar código (`Xs`)" con countdown
  (patrón `RegistroPaso3Screen.kt:80-93,179-187`); `InfoBox` "El código expira en
  10 minutos…".
- **Paso 3 (`RecuperacionPaso3Screen`) — §14.4/§16.6:** `CompactGreenHeader`;
  `StepIndicator(3)`; dos `EraTextField` de contraseña (icono ojo, `KeyboardType.Password`);
  `InfoBox` de política; `EraRegPrimaryButton` "Guardar contraseña";
  `errorGeneral` (409/401).

### 6.6 Navegación (`EraRoutes.kt` + `EraNavHost.kt`)

```
navigation(route = EraRoutes.RECUPERACION, startDestination = EraRoutes.RECUPERACION_PASO1) {
    composable(RECUPERACION_PASO1) {
        val parentEntry = remember(it) { navController.getBackStackEntry(RECUPERACION) }
        val vm: RecuperacionViewModel = hiltViewModel(parentEntry)
        RecuperacionPaso1Screen(vm = vm, snackbarHostState = …, onVolverAlLogin = { vm.cancelar(); navController.popBackStack() }, onNavegarAPaso2 = { navController.navigate(RECUPERACION_PASO2) })
    }
    composable(RECUPERACION_PASO2) { /* parentEntry; onAtras = popBackStack; onNavegarAPaso3 = navigate(PASO3) */ }
    composable(RECUPERACION_PASO3) {
        /* parentEntry */
        LaunchedEffect(Unit) { vm.eventos.collect { … ReiniciarFlujo → navController.popBackStack(RECUPERACION_PASO1, inclusive = false); RecuperacionExitosa → navController.getBackStackEntry(LOGIN).savedStateHandle["recuperacion_exitosa"] = true; navigate(LOGIN){popUpTo(0){inclusive=true} } } }
    }
}
```
La snackbar post-recuperación se consume en `LoginScreen.kt:236-239` (mismo
`LaunchedEffect` que `"registro_exitoso"`).

`ReiniciarFlujo` usa `popBackStack(RECUPERACION_PASO1, inclusive = false)` y
**no** `navigate(PASO1)`: desde el paso 3, navegar a `PASO1` duplicaría ese
destino en el backstack (quedaría `[paso1, paso2, paso3, paso1]`); el
`popBackStack` descarta `paso2`/`paso3` y deja `paso1` como tope con la pila
limpia para reintentar el flujo.

## 7. Capas de testing

### 7.1 Tests unitarios (JUnit + `kotlinx-coroutines-test`)

**`RecuperacionViewModelTest.kt` (nuevo)** — fake único `FakeAuthRepository`
(con `encolarRequestPasswordReset`/`encolarVerifyPasswordReset`/
`encolarConfirmPasswordReset`); el ViewModel inyecta **solo** `AuthRepository`
(no hay `FakeSesionRepository`):

| # | Caso | Verifica |
|---|---|---|
| 1 | Estado inicial (campos vacíos, countdown 0; **sin** flag `cargando`, espejo de Registro) | Defaults |
| 2 | `enviarEnlace` con correo inválido → error `CORREO`, **sin** llamada | D-41 |
| 3 | `enviarEnlace` + 200 → llama request, inicia countdown, emite `NavegarAPaso2` | D-42 happy path |
| 4 | `enviarEnlace` + 429 → `ReenvioThrottled` como `errorGeneral`, sin navegar | D-42 |
| 5 | `enviarEnlace` + 400 `Validacion` → `errorGeneral` con detalles | D-40/D-42 |
| 6 | `onCodigoOtpChange` filtra no-dígitos y limita a 6 | D-43 |
| 7 | `verificarCodigo` con OTP incompleto → error `CODIGO_OTP`, sin llamada | D-41 |
| 8 | `verificarCodigo` + 200 → guarda `resetToken` (internamente) + `NavegarAPaso3` | D-37 happy path |
| 9 | `verificarCodigo` + 401 `OtpInvalido` → `errorGeneral`, sin navegar | D-42/D-43 |
| 10 | `reenviarCodigo` con countdown > 0 → no llama al repo | D-43 |
| 11 | `reenviarCodigo` + 200 → reinicia countdown 60 s | D-43 |
| 12 | `reenviarCodigo` + 429 → `Aviso(ReenvioThrottled)` | D-43 |
| 13 | `guardarContrasena` con política inválida → error `NUEVA_CONTRASENA` + criterios, sin llamada | D-41 |
| 14 | `guardarContrasena` con confirmar ≠ nueva → error `CONFIRMAR_CONTRASENA`, sin llamada | D-41 |
| 15 | `guardarContrasena` + 200 → limpia `resetToken`, emite `RecuperacionExitosa` | D-44 |
| 16 | `guardarContrasena` + 409 `PasswordReusada` → `errorGeneral`, campos conservados | D-40/D-44 |
| 17 | `guardarContrasena` + 401 `ResetTokenInvalido` → limpia `resetToken`, emite `ReiniciarFlujo` | D-40/D-44 |
| 18 | `cancelar()` → cancela countdown, estado inicial, `resetToken` null | D-37 |
| 19 | `guardarContrasena` con `resetToken == null` (restauración tras muerte de proceso) → emite `ReiniciarFlujo` **sin llamar** al repo | D-37/D-44 |

**`AuthRepositoryTest.kt` (extensión, MockWebServer):**

| # | Mock | Verifica |
|---|---|---|
| 1 | `request` 200 `{"message":"Si el correo está registrado…"}` | `Exito(Unit)` + ruta `POST /api/v1/auth/password-reset/request` + body `{correo}` |
| 2 | `request` 429 `OTP_RESEND_THROTTLED` | `Fallo(ReenvioThrottled)` |
| 3 | `request` 400 `VALIDATION_ERROR` con `details` | `Fallo(Validacion([mensaje]))` |
| 4 | `verify` 200 `{"resetToken":"jwt…"}` | `Exito(PasswordResetVerifyResponse("jwt…"))` + ruta `…/verify` + body `{correo,codigo}` |
| 5 | `verify` 401 `OTP_INVALID_OR_EXPIRED` | `Fallo(OtpInvalido)` |
| 6 | `confirm` 200 `{"message":"Contraseña actualizada…"}` | `Exito(Unit)` + ruta `…/confirm` + body `{resetToken,nuevaContrasena,confirmarContrasena}` |
| 7 | `confirm` 401 `RESET_TOKEN_INVALID` | `Fallo(ResetTokenInvalido)` |
| 8 | `confirm` 409 `PASSWORD_REUSED` | `Fallo(PasswordReusada)` |
| 9 | `confirm` 400 `VALIDATION_ERROR` (política) | `Fallo(Validacion([…]))` |
| 10 | `verify` sin red (`DISCONNECT_AT_START`) | `Fallo(ErrorConexion)` |

**`ErrorMapperTest.kt` (extensión):** +2 casos: `RESET_TOKEN_INVALID` →
`ResetTokenInvalido`, `PASSWORD_REUSED` → `PasswordReusada`.

**Ajuste obligatorio de compilación en fakes existentes:** `FakeAuthRepository`
de `RegistroViewModelTest.kt`, `LoginViewModelTest.kt` y
`HomePlaceholderViewModelTest.kt` añaden 3 `override`:
```kotlin
override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> = error("No usado en … tests")
override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> = error("No usado en … tests")
override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> = error("No usado en … tests")
```

### 7.2 Tests androidTest (Compose)

**Nuevos** `RecuperacionPaso1ScreenTest.kt`, `RecuperacionPaso2ScreenTest.kt`,
`RecuperacionPaso3ScreenTest.kt` (Content puro con callbacks, mismo estilo que
los `RegistroPasoXScreenTest`):
| Archivo | Casos |
|---|---|
| `RecuperacionPaso1ScreenTest` | Se muestra el header/cabecera; correo vacío al "Enviar código" llama el callback; campo correo visible; "Volver al inicio de sesión" invoca `onVolverAlLogin` |
| `RecuperacionPaso2ScreenTest` | Muestra "Código enviado a <correo>"; entrada OTP (SetText 6 dígitos) se propaga; verificar invoca callback; reenviar deshabilitado con countdown > 0 y habilitado en 0 |
| `RecuperacionPaso3ScreenTest` | Campos de contraseña visibles; toggle ojo conmuta; guardar invoca callback; error 409 mostrado como `errorGeneral` |

`LoginScreenTest.kt` **no se modifica** (solo ejercita `LoginContent`, cuya
firma no cambia, D-45). `HomePlaceholderViewModelTest`/`LoginViewModelTest`/
`RegistroViewModelTest` solo reciben los stubs de compilación.

### 7.3 Objetivo de conteo sobre 138 verdes actuales

| Depósito | Antes | Nuevos (estimado) | Después |
|---|---|---|---|
| Unitarios existentes (Fases 1–4) | 138 | — | 138 |
| `RecuperacionViewModelTest` | — | ~19 | +19 |
| `AuthRepositoryTest` (reset) | — | ~10 | +10 |
| `ErrorMapperTest` (2 ramas) | — | ~2 | +2 |
| **Total unitarios** | **138** | **~31** | **~169** |

(androidTest NO suma a `testDebugUnitTest`. Estimado 47 → ~57: 3 archivos nuevos
con ~3–4 casos cada uno. Fakes: solo stubs, suman 0 casos nuevos.)

## 8. Seguridad (CLAUDE.md §5 aplicado a esta fase)

- **`resetToken` solo en memoria** (D-37): nunca se persiste, nunca se loguea,
  nunca viaja en nav-arguments. Muere con el ViewModel al salir del grafo.
- **Anti-enumeración de extremo a extremo** (D-42): la UI no diferencia
  correos existentes/inexistentes; el mensaje de éxito y el de OTP inválido son
  genéricos. No se inventa retroalimentación que el backend decidió suprimir.
- **Nunca loguear en claro:** correo, OTP, `resetToken`, contraseñas. El flujo
  no imprime cuerpos; `aEraError` no loguea bodies (patrón Fases 1–4).
- **Validación cliente espejo** (D-41) sin asumir que reemplaza al servidor:
  todo `error` HTTP se mapea por campo `error` en el mapper central, jamás por
  mensaje (regla §7).
- **El JWT de sesión no se toca:** la recuperación es anónima; si hubiera una
  sesión activa en el dispositivo, el flujo no la lee ni la invalida.
- **`countdownJob` siempre cancelado** en `cancelar()`/éxito/`ReiniciarFlujo`
  (no quedan corrutinas colgadas al navegar).

## 9. Archivos a crear / modificar

**Crear:**
| Archivo | Descripción |
|---|---|
| `ui/recuperacion/RecuperacionUiState.kt` | `CampoRecuperacion` + `RecuperacionUiState` + `RecuperacionEvento` (D-38) |
| `ui/recuperacion/RecuperacionViewModel.kt` | VM del flujo (D-37/D-39/D-43/D-44) |
| `ui/recuperacion/RecuperacionPaso1Screen.kt` | Pantalla paso 1 + `Content` |
| `ui/recuperacion/RecuperacionPaso2Screen.kt` | Pantalla paso 2 + `Content` (OTP + countdown) |
| `ui/recuperacion/RecuperacionPaso3Screen.kt` | Pantalla paso 3 + `Content` (contraseñas) |
| `test/…/ui/recuperacion/RecuperacionViewModelTest.kt` | Tests unitarios del VM (7.1) |
| `androidTest/…/ui/recuperacion/RecuperacionPaso1ScreenTest.kt` | androidTest paso 1 |
| `androidTest/…/ui/recuperacion/RecuperacionPaso2ScreenTest.kt` | androidTest paso 2 |
| `androidTest/…/ui/recuperacion/RecuperacionPaso3ScreenTest.kt` | androidTest paso 3 |

**Modificar:**
| Archivo | Cambio |
|---|---|
| `repository/AuthRepository.kt` | + 3 métodos (D-39) |
| `repository/RemoteAuthRepository.kt` | + 3 `override` con `llamar` (D-39) |
| `utils/EraError.kt` | + `ResetTokenInvalido`, `PasswordReusada` (D-40) |
| `utils/ErrorMapper.kt` | + ramas `RESET_TOKEN_INVALID`, `PASSWORD_REUSED` (D-40) |
| `utils/MensajeError.kt` | + 2 mensajes + `mapsTo` + `mensajeCampo(CampoRecuperacion)` (D-40/D-41) |
| `ui/login/LoginViewModel.kt` | `onOlvidasteContrasena` → `NavegarARecuperacion` (D-45) |
| `ui/login/LoginUiState.kt` | + `NavegarARecuperacion` (D-45) |
| `ui/login/LoginScreen.kt` | + `onNavigateARecuperacion` default `{}` + manejo de evento (D-45) |
| `ui/navigation/EraRoutes.kt` | + `RECUPERACION`, `RECUPERACION_PASO1/2/3` (D-38) |
| `ui/navigation/EraNavHost.kt` | + grafo `recuperacion` + wiring de `LoginScreen` (D-38/D-45/D-44) |
| `test/…/ui/register/RegistroViewModelTest.kt` | Fake: + 3 stubs (compilación, D-39) |
| `test/…/ui/login/LoginViewModelTest.kt` | Fake: + 3 stubs (compilación, D-39) |
| `test/…/ui/login/HomePlaceholderViewModelTest.kt` | Fake: + 3 stubs (compilación, D-39) |
| `test/…/repository/AuthRepositoryTest.kt` | + casos MockWebServer (10) |
| `test/…/utils/ErrorMapperTest.kt` | + 2 casos (D-40) |

**No se modifica:** `AuthApi.kt`, DTOs (`PasswordReset*`, `MessageResponse`),
`PasswordPolicy` (firma y lógica), `Validators`, `TokenManager`,
`SesionRepository`, `TokenManagerSesion`, `JwtInterceptor`, `NetworkModule`,
`RepositoryModule`, `Color.kt`, componentes (`CompactGreenHeader`,
`StepIndicator`, `EraTextField`, `InfoBox`, `EraRegButtons`, `LoginInputPill`,
`LoginButton`…), `LoginScreenTest.kt`. **Cero dependencias nuevas**
(`build.gradle.kts` y `libs.versions.toml` intactos). **No se elimina** ningún
archivo.

## 10. Flujo de navegación

```
LoginScreen ("¿Olvidaste la contraseña?")            [D-45: evento NavegarARecuperacion]
   │  navigate(RECUPERACION)
   ▼
grafo "recuperacion" (recuperacion/paso1)           [D-38: VM de grafo]
   │  correo + "Enviar código" → request → 200 (indistinguible, D-42)
   ▼
recuperacion/paso2 — OTP 6 dígitos + countdown 60 s  [D-43]
   ├─ "Verificar código" → verify → 200 { resetToken } (D-37 en VM) → paso3
   │                     → 401 genérico (anti-enumeración)
   └─ "Reenviar código" → request (429 <60 s → snackbar / 200 → countdown)
   ▼
recuperacion/paso3 — nueva + confirmar, InfoBox       [D-44]
   ├─ 200 → resetToken = null → navigate(LOGIN){popUpTo(0){inclusive=true}} + snackbar
   ├─ 409 PASSWORD_REUSED → errorGeneral
   ├─ 401 RESET_TOKEN_INVALID → resetToken = null → ReiniciarFlujo → paso1 (genérico)
   └─ 400 VALIDATION_ERROR · 500 · sin red → snackbar/general
Login ("Contraseña actualizada. Ya puedes iniciar sesión.")

Volver: paso3→paso2 (popBackStack) · paso2→paso1 (popBackStack) · paso1→Login (popBackStack del grafo + cancelar())
```

## 11. Definition of Done de la fase

1. Suite unitaria verde: `.\gradlew.bat :app:testDebugUnitTest --console=plain -q`
   → **138 previos + ~31 nuevos** (VM + Repository + ErrorMapper + stubs). Regla
   §4.13: los 138 previos siguen verdes.
2. Compilación limpia: `.\gradlew.bat :app:assembleDebug` y
   `:app:assembleDebugAndroidTest` → BUILD SUCCESSFUL.
3. `connectedDebugAndroidTest` en físico ABR-LX3: 47 previos + ~10 nuevos
   (3 pantallas de recuperación). Reintentos por adb inalámbrico si se cae.
4. Flujo manual contra backend dev (OTP fijo `123456`, `APP_DEV_MODE=true`):
   login → enlace → paso1 (correo existente y **no existente**: misma UX) →
   paso2 (OTP `123456`) → paso3 → "Contraseña actualizada" → login con la nueva
   contraseña. Reenviar a los pocos segundos → 429. Repetir la contraseña
   anterior → 409. Cancelar token (esperar 10 min o reiniciar flujo) → 401 →
   reinicio a paso 1. Con backend apagado → `ErrorConexion` en cada paso.
5. Sin secretos ni PII en logs (inspección logcat debug): correo, OTP,
   `resetToken` y contraseñas jamás logueados.
6. Cero dependencias nuevas; `AuthApi`, DTOs, `PasswordPolicy`, `Validators`,
   `Color.kt` sin cambios (verificable por `git diff` del propietario).
7. Este documento y `CLAUDE.md` §10 actualizados al cerrar (mover a "Fase 5
   completada", fecha y conteo final). `decisiones-tecnicas.md` y `README.md`
   no se tocan hasta el cierre.
8. Sugerir mensaje de commit; **el propietario ejecuta `git add`/`git commit`**
   (regla §4.5).

## 12. Puntos abiertos / requieren palabra del propietario

**Resueltos por el propietario y el auditor (2026-08-29).** Los 6 puntos fueron
confirmados y quedan incorporados al cuerpo del documento:

1. **Textos de botones:** **"Enviar código"**, **"Verificar código"** y
   **"Guardar contraseña"** (REQ-FUN-07 prevalece sobre el prototipo, cláusula
   §13.h). Coherente con el copy "código" (no "enlace") de §6.5 pasos 1 y 2.
2. **Guarda de sesión en el grafo:** **sin guarda** (el login se muestra solo
   cuando no hay token válido).
3. **Feedback post-éxito:** **sí**, snackbar "Contraseña actualizada. Ya puedes
   iniciar sesión." en Login vía `savedStateHandle` (patrón D-14).
4. **Continuidad D-XX:** **D-37…D-45 confirmada**.
5. **Countdown en paso 1:** **sin countdown**; el 429 inline ("Debes esperar
   antes de reenviar") cubre el re-envío antes de 60 s.
6. **`ReiniciarFlujo` (401 en paso 3):** **conservar el correo** y **limpiar el
   token** (el mensaje genérico se muestra al volver al paso 1, D-44).

---

## 13. Aprobación del propietario y del auditor

- **Diseño Fase 5 (D-37…D-45):** las 4 correcciones obligatorias y las menores
  del auditor fueron aplicadas (2026-08-29) y los 6 puntos de §12 están
  resueltos. **Pendiente:** confirmación escrita del propietario y del auditor
  para iniciar la implementación capa por capa (§14).

---

## 14. Registro de implementación

Pendiente: esta fase está **en revisión** (correcciones aplicadas, aprobación
final pendiente de §13). No se ha implementado ninguna capa.
La implementación, una vez aprobada, seguirá el orden capa por capa
(repository/ → errores → ViewModel → pantallas+navegación → tests), con portón
de auditoría entre capas y detención para revisión del propietario (regla §4.3).