# Fase 2 — Login (Módulo B): Análisis y Diseño

> Documento de análisis del módulo frontend. Registra el alcance, el diseño,
> las decisiones aprobadas (D-16…D-21) y los archivos a crear/modificar.
> **Estado:** Diseño aprobado con correcciones del auditor (2026-08-26) — listo para implementar.

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-02 (Login) |
| Requisitos no funcionales asociados | REQ-NF-02 (seguridad), REQ-NF-03 (usabilidad) |
| Casos de uso | CU-04 (Iniciar sesión) |
| Historias de usuario | HU-02 |
| Endpoint backend consumido | `POST /auth/login` |

## 2. Alcance

**Incluye:**
- Pantalla de Login real reemplazando `LoginPlaceholderScreen`.
- `LoginViewModel` con estado mínimo (D-16).
- Extensión de `AuthRepository` + `RemoteAuthRepository` con método `login()`.
- Extensión de `EraError` + `ErrorMapper` para errores de login (D-22).
- Navegación: ruta `login` real, destino post-login (D-18), "Olvidaste la contraseña" (D-19).
- Componentes nuevos: hero-login, input-login-pill, btn-primary-pill login.
- Tests unitarios (ViewModel + Repository) y androidTest (pantalla).

**Fuera de alcance:**
- Home real (Fase 10) — se usa placeholder temporal (D-18).
- Recuperación de contraseña (Fase 5) — solo el enlace "¿Olvidaste la contraseña?" (D-19).
- Logout (Fase 4) — se crea la ruta pero no se implementa el flujo.
- Perfil/Mi cuenta (Fase 3).
- Pantalla de carga/Splash (Fase 10).

## 3. Estado previo aprovechado

Ya existe y **no se reescribe** (regla §4.13):

| Componente | Archivo | Estado |
|---|---|---|
| `AuthApi.login()` | `remote/api/AuthApi.kt:27–28` | ✅ Declarado, firma correcta |
| `LoginRequest(usuarioOCorreo, contrasena)` | `remote/dto/auth/LoginRequest.kt` | ✅ DTO correcto |
| `LoginResponse(token)` | `remote/dto/auth/LoginResponse.kt` | ✅ DTO correcto |
| `TokenManager.saveToken()` | `utils/TokenManager.kt:26–28` | ✅ EncryptedSharedPreferences |
| `TokenManager.hasToken()` | `utils/TokenManager.kt:36` | ✅ Para Splash futuro |
| `JwtInterceptor` | `remote/JwtInterceptor.kt` | ✅ Auto-attach Bearer |
| `ErrorMapper.desdeHttp()` | `utils/ErrorMapper.kt:8–22` | ✅ Reutilizar para 401/403/423 |
| `Resultado<T>` / `llamar` wrapper | `repository/RemoteAuthRepository.kt:32–38` | ✅ Reutilizar |
| `ui/theme/` tokens (Color, Type, Theme) | `ui/theme/` | ✅ Completo |
| 6 componentes compartidos | `ui/components/` | ✅ Fase 1 |

## 4. Decisiones de diseño (D-16…D-21)

### D-16 — Scope del LoginViewModel

**Decisión:** ViewModel mínimo con solo 2 campos de entrada + estados de UI.

| Campo | Tipo | Default | Observación |
|---|---|---|---|
| `usuarioOCorreo` | `String` | `""` | Input del usuario |
| `contrasena` | `String` | `""` | Input de contraseña |
| `contrasenaVisible` | `Boolean` | `false` | Toggle ojo |
| `cargando` | `Boolean` | `false` | Spinner/bloqueo de botón durante llamada |
| `errorGeneral` | `EraError?` | `null` | Mensaje de estado (§13.10.2) |
| `campoConError` | `CampoLogin?` | `null` | Resalta campo inválido si aplica |

**Countdown de bloqueo (423):** **Delegado al backend.** El cliente NO implementa countdown local.
Razón: el backend es la autoridad del bloqueo (B-2, B-3 del modulo-b-analisis.md). El cliente
recibe `423 ACCOUNT_LOCKED` y muestra "Cuenta bloqueada temporalmente. Intenta de nuevo más tarde."
Sin temporizador local — si el usuario cierra y reabre la app, el backend resolverá el estado real.
Implementar un countdown local crearía desincronización (el usuario ve "0:42" pero el backend aún
bloquea, o viceversa).

**Patrón:** Mismo que `RegistroViewModel`: `@HiltViewModel`, `MutableStateFlow<LoginUiState>`,
`Channel<LoginEvento>` para eventos one-shot (navegación, snackbar). Se inyecta `AuthRepository`.

### D-17 — Naming de rutas

**Decisión:** Actualizar `EraRoutes.LOGIN` de `"login_placeholder"` a `"login"`.

`LoginPlaceholderScreen` se **elimina** y se reemplaza por `LoginScreen` real.
La ruta `"login_placeholder"` deja de existir — todos los referentes (`EraNavHost.kt`,
`RegistroPaso3Screen.kt` en `onRegistroExitoso`) se actualizan a usar `EraRoutes.LOGIN = "login"`.

### D-18 — Destino post-login exitoso

**Decisión:** Navegar a `"home_placeholder"` — ruta temporal hasta Fase 10.

`HomePlaceholderScreen` es un Box simple con texto "Sesión iniciada ✅" y un botón
"Cerrar sesión" que limpia `TokenManager` y navega a `login`. Cumple la regla §5
(sesión persistente: el JWT queda guardado; el usuario no vuelve a login salvo que
cierre sesión o el token expire).

**Por qué no redirigir a registro o mantener en login:** el usuario acabó de autenticarse;
navegar a cualquier otra pantalla real rompería la experiencia. Un placeholder simple
confirma el éxito y permite probar el flujo completo (login → sesión activa → logout).

### D-19 — "¿Olvidaste la contraseña?" → placeholder Fase 5

**Decisión:** Renderizar el enlace según §14.1 del prototipo, pero al pulsarlo mostrar
un `Snackbar` "Próximamente" y **NO** navegar.

Razón: Fase 5 (`password-reset/request`) aún no existe. Navegar a una pantalla inexistente
rompería la app. Cuando Fase 5 esté lista, se reemplaza el Snackbar por
`navController.navigate(EraRoutes.RECUPERAR_CONTRASENA)`.

### D-20 — Componentes reutilizados de Fase 1

| Componente existente (`ui/components/`) | Reutilizado en Login | Notas |
|---|---|---|
| `EraIcons.kt` (iconos custom) | Sí — `EmailOutline`, `LockOutline`, `Visibility`, `VisibilityOff` | Los iconos del hero-login no existen; son SVGs nuevos (D-21) |
| `EraTextField.kt` | **No** — el input de login tiene estilo pill diferente (§13.3.1 vs §13.3.2) | Se crea `LoginInputPill` nuevo |

**Componentes nuevos a crear para esta fase:**

| Componente | Descripción | Patrón §13 | Uso |
|---|---|---|---|
| `HeroLogin.kt` | Sección verde superior con SVGs decorativos, título y subtítulo | §13.1.1 (`hero-login`) | Solo pantalla Login |
| `LoginInputPill.kt` | Input pill (radio 25.5dp, fondo blanco, icono izq/der, toggle ojo) | §13.3.1 (`input-login`) | Login (email/contraseña) |
| `LoginButton.kt` | Botón primario pill para "Iniciar sesión" | §13.2.1 (`btn-primary-pill`) | Login |

**Justificación de no reusar `EraTextField`:** el input de registro (§13.3.2) tiene radio 10dp,
fondo `ColorPrimaryPale`, label arriba y asterisco rojo. El input de login (§13.3.1) tiene
radio 25.5dp (pill), fondo blanco, borde `ColorSurface`, icono integrado sin label. Son
componentes visualmente distintos — forzar la reutilización requeriría tantos parámetros
opcionales que el componente se vuelve ilegible.

### D-21 — SVGs decorativos del hero-login

**Decisión:** Implementar los 3 SVGs como Vector Drawables en `res/drawable-nodpi/` y
posicionarlos con `Modifier.graphicsLayer { translationX/Y }` dentro del `HeroLogin`.

| SVG | Archivo fuente | Ancho aprox. | Posición |
|---|---|---|---|
| `signo_igual.svg` | Prototipo HTML | ~140dp | Arriba-izquierda |
| `signo_abc123.svg` | Prototipo HTML | ~170dp | Arriba-derecha |
| `signomas.svg` | Prototipo HTML | ~140dp | Abajo-derecha |

**Por qué Vector Drawables y no Compose Canvas:** los SVGs son decorativos complejos
(múltiples paths, gradientes). Reimplementarlos en Canvas Compose sería trabajo enorme
y frágil. Vector Drawables es el mecanismo nativo de Android para importar SVGs y
funciona directamente con `painterResource()`. Se exportan los SVG a Android Studio
(auto-conversión) o se usa el wizard de Vector Asset.

**¿Se implementan en Fase 2 o placeholder simplificado?** Se implementan completos.
Razón: el hero-login es el componente visual más distintivo de la pantalla de login.
Un placeholder sin SVGs haría que la pantalla pareciera incompleta y dificultaría
la validación visual del diseño. Los SVGs son assets estáticos sin lógica — su
implementación es de bajo riesgo.

### D-22 — Abstracción de sesión `SesionRepository` + `TokenManagerSesion`

**Decisión:** Adición aprobada por el auditor (no estaba en el diseño original; regla
§4.3). No se revierte, se documenta.

Clase concreta `TokenManager` (JWT en `EncryptedSharedPreferences`, §5) no es una
interfaz, por lo que no se puede hacer un fake para testear `LoginViewModel` /
`HomePlaceholderViewModel`. Se introduce:

- `SesionRepository` (interfaz): `guardarToken()`, `obtenerToken()`, `limpiarToken()`,
  `tieneToken()`.
- `TokenManagerSesion` (adapter `@Singleton`): implementa `SesionRepository`
  delegando en `TokenManager`.
- `@Binds` en `RepositoryModule` para proveer `SesionRepository` desde
  `TokenManagerSesion`.

`LoginViewModel` y `HomePlaceholderViewModel` inyectan `SesionRepository` en vez de
`TokenManager`, y los tests usan `FakeSesionRepository`. El `TokenManager` real sigue
siendo el backend de almacenamiento seguro (§5).

## 5. Arquitectura propuesta (capa por capa)

### 5.1 Repository

**Extender** `AuthRepository` (interfaz) + `RemoteAuthRepository` (implementación) con un
nuevo método:

```
AuthRepository (interfaz):
  + suspend fun login(request: LoginRequest): Resultado<LoginResponse>

RemoteAuthRepository (impl):
  + override suspend fun login(request: LoginRequest) = llamar { api.login(request) }
```

Se reutiliza el wrapper `llamar` existente. No se modifica `RepositoryModule` — el binding
`@Binds` ya cubre `RemoteAuthRepository → AuthRepository`.

**Nota:** a diferencia de `register`/`verifyEmail`/`resendOtp` (que retornan `Resultado<Unit>`),
`login` retorna `Resultado<LoginResponse>` porque el token debe propagarse al ViewModel
para guardarlo en `TokenManager`.

### 5.2 ViewModel: `LoginViewModel`

**Nuevo archivo:** `ui/login/LoginViewModel.kt`

```
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sesionRepository: SesionRepository,   // D-22 (antes TokenManager)
) : ViewModel()
```

**UiState (`LoginUiState`):**

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `usuarioOCorreo` | `String` | `""` | Input del usuario |
| `contrasena` | `String` | `""` | Input de contraseña |
| `contrasenaVisible` | `Boolean` | `false` | Toggle ojo |
| `cargando` | `Boolean` | `false` | Bloquea UI durante llamada |
| `errorGeneral` | `EraError?` | `null` | Mensaje de estado (§13.10.2) |
| `campoConError` | `CampoLogin?` | `null` | Resalta campo inválido |

**Eventos (`LoginEvento`):**

| Evento | Trigger |
|---|---|
| `NavegarAHome` | Login exitoso → guardar JWT → navegar |
| `NavegarALogin` | Cuenta inactiva (403) → clearToken → navegar a login |
| `NavegarARegistro` | "Regístrate" |
| `MostrarSnackbar(msg)` | Placeholder "Próximamente" (Fase 5) |

**Flujo del login:**

```
1. usuarioOCorreo + contrasena no vacíos → btn habilitado
2. onLoginClick → validación cliente (no vacío) → cargando = true
3. authRepository.login(LoginRequest(usuarioOCorreo, contrasena))
4. Si Exito(token) → sesionRepository.guardarToken(token) → NavegarAHome
5. Si Fallo(EraError) → errorGeneral = error → cargando = false
   - INVALID_CREDENTIALS → "Correo/usuario o contraseña incorrectos"
   - ACCOUNT_LOCKED → "Cuenta bloqueada temporalmente. Intenta de nuevo más tarde."
   - ACCOUNT_INACTIVE → errorGeneral = CuentaInactiva (mensaje inline, no snackbar)
                       + sesionRepository.limpiarToken() → NavegarALogin
   - ErrorConexion → "Sin conexión. Intenta de nuevo."
```

> **Nota §5.2 (decisión final):** en `CuentaInactiva` el mensaje "Tu cuenta fue
> desactivada." se muestra **inline vía `errorGeneral`** (patrón del resto de errores
> de la pantalla), no como snackbar. Funcionalmente visible; preferible por
> consistencia con los demás errores del formulario.

### 5.3 UI: `LoginScreen.kt`

**Reemplaza:** `LoginPlaceholderScreen.kt` (se elimina).

**Estructura visual (§14.1 de decisiones-tecnicas.md):**

```
┌─────────────────────────────────────┐
│          HeroLogin.kt               │  ColorPrimary fondo, SVGs decorativos
│  ¡Bienvenidos!                      │  32sp Light, blanco
│  ERA - Educación, Repaso...         │  16sp Light, blanco 85%
├─────────────────────────────────────┤  ← Panel gris superpuesto (RadiusPanel 30dp)
│                                     │
│  [mensaje de estado si aplica]      │  §13.10.2 (fondo tintado, 14sp) — arriba del form
│                                     │
│     Inicio de sesión                │  36sp Bold, ColorPrimary
│                                     │
│  [  ✉  ID/E-mail              ]    │  LoginInputPill (max 276dp, centrado)
│                                     │
│  [  🔒  Contraseña          👁 ]   │  LoginInputPill con toggle ojo
│                                     │
│      ¿Olvidaste la contraseña?     │  16sp Medium, ColorPrimary, centrado
│                                     │  → Snackbar "Próximamente" (D-19)
│                                     │
│  [     Iniciar sesión     ]         │  LoginButton (pill, ColorPrimary)
│                                     │
│  ¿No tienes cuenta? Regístrate     │  16sp, "Regístrate" Bold+ColorPrimary
└─────────────────────────────────────┘
```

**Estados de UI:**
- **Normal:** campos vacíos, botón deshabilitado hasta que ambos campos tengan contenido.
- **Cargando:** botón muestra CircularProgressIndicator inline, campos deshabilitados.
- **Error:** mensaje de estado (§13.10.2) arriba del formulario, campos re-habilitados.
- **Éxito:** navegación a `home_placeholder`.

### 5.4 Navegación

**Modificar:** `EraRoutes.kt` y `EraNavHost.kt`.

```
EraRoutes.kt:
  - const val LOGIN = "login"                    // antes: "login_placeholder"
  - const val HOME_PLACEHOLDER = "home_placeholder"  // nuevo

EraNavHost.kt:
  startDestination = EraRoutes.LOGIN  // sin cambios (ya era LOGIN)

  composable(EraRoutes.LOGIN) {
      LoginScreen(
          onNavigateToHome = { navController.navigate(EraRoutes.HOME_PLACEHOLDER) },
          onNavigateToRegistro = { navController.navigate(EraRoutes.REGISTRO) },
          onNavigateToRecuperar = { /* D-19: snackbar "Próximamente" */ },
      )
  }

  composable(EraRoutes.HOME_PLACEHOLDER) {
      val vm: HomePlaceholderViewModel = hiltViewModel()
      HomePlaceholderScreen(
          onCerrarSesion = {
              vm.cerrarSesion()
              navController.navigate(EraRoutes.LOGIN) {
                  popUpTo(0) { inclusive = true }
              }
          },
      )
  }
```

**`HomePlaceholderViewModel`:** ViewModel mínimo con un solo método `cerrarSesion()` que
llama `tokenManager.clearToken()`. Consistente con el patrón Hilt (`@HiltViewModel`,
inyección de `TokenManager`) — la UI nunca accede directamente a `TokenManager`.

**Actualización en RegistroPaso3Screen:** `onRegistroExitoso` ya navega a `EraRoutes.LOGIN`
(antes `"login_placeholder"`, ahora `"login"` — la referencia al nombre de la constante
no cambia, solo el valor string). Además, se modifica `RegistroPaso3Screen.kt` para guardar
`"registro_exitoso" = true` en el `savedStateHandle` del `NavBackStackEntry` destino
antes de navegar (D-14 diferido de Fase 1).

**Recibir snackbar post-registro (D-14 de Fase 1 diferido):** usar `savedStateHandle` del
`NavBackStackEntry` del login destino. Al recibir el evento `RegistroVerificadoIrALogin`,
se guarda `"registro_exitoso" = true` en el `savedStateHandle` del destino. `LoginScreen`
lee este valor al recomponer y muestra el snackbar una vez, limpiándolo después.

### 5.5 Errores a mapear (extensión de `EraError` + `ErrorMapper`)

**Nuevos subtipos de `EraError`:**

| HTTP + campo `error` | `EraError` | Mensaje en UI |
|---|---|---|
| 401 `INVALID_CREDENTIALS` | `CredencialesInvalidas` *(nuevo)* | "Correo/usuario o contraseña incorrectos" |
| 423 `ACCOUNT_LOCKED` | `CuentaBloqueada` *(nuevo)* | "Cuenta bloqueada temporalmente. Intenta de nuevo más tarde." |
| 403 `ACCOUNT_INACTIVE` | `CuentaInactiva` *(nuevo)* | "Tu cuenta fue desactivada." + cerrar sesión local |
| 400 `VALIDATION_ERROR` | `Validacion` *(ya existe)* | Detalle genérico |
| 500 `INTERNAL_ERROR` | `ErrorServidor` *(ya existe)* | "Intenta más tarde" |
| `IOException` | `ErrorConexion` *(ya existe)* | "Sin conexión" |

**Extensión de `ErrorMapper.desdeHttp()`:** agregar 3 nuevas ramas al `when`:
- `"INVALID_CREDENTIALS"` → `EraError.CredencialesInvalidas`
- `"ACCOUNT_LOCKED"` → `EraError.CuentaBloqueada`
- `"ACCOUNT_INACTIVE"` → `EraError.CuentaInactiva`

### 5.6 Validaciones cliente

| Campo | Regla | Feedback |
|---|---|---|
| `usuarioOCorreo` | No vacío, no en blanco | Error bajo campo |
| `contrasena` | No vacía | Error bajo campo |

No hay policy de password en login (eso es registro). No hay formato de email estricto
(un usuario puede logear con username sin `@`). Solo se verifica que no estén vacíos.

## 6. Flujo de navegación

```
Login
  │  "Iniciar sesión" (éxito)
  │  → [POST /auth/login] → 200 + token → saveToken →
  ▼
Home (placeholder Fase 10)
  │  "Cerrar sesión"
  │  → clearToken →
  ▼
Login (startDestination)

Login → "Iniciar sesión" (403 ACCOUNT_INACTIVE)
  │  → clearToken + NavegarALogin
  ▼
Login + snackbar "Tu cuenta fue desactivada."

Login → "Regístrate" → Registro paso 1/3
  │
  │  Registro paso 3 → "Verificar" (éxito)
  │  → [verify-email] → navigate(LOGIN) con savedStateHandle
  ▼
Login + snackbar "Cuenta verificada"  ← D-14 diferido de Fase 1

Login → "¿Olvidaste la contraseña?" → Snackbar "Próximamente" (D-19)
```

**Sesión persistente (REQ-FUN-02 CA1):** tras login exitoso, el JWT se guarda en
`EncryptedSharedPreferences`. Si el usuario cierra y reabre la app, Splash (Fase 10)
verifica `TokenManager.hasToken()` y navega directamente a Home si existe.

## 7. Flujo del backend (resumen — fuente: modulo-b-analisis.md)

```
POST /auth/login
  Request:  { usuarioOCorreo, contrasena }
  Response: { token }  (JWT 30 días, HS256, solo sub/iss/aud/iat/exp/jti)

  200  → token emitido
  400  → VALIDATION_ERROR (vacíos o demasiado largos)
  401  → INVALID_CREDENTIALS (genérico, anti-enumeración)
  403  → ACCOUNT_INACTIVE (soft delete, solo tras contraseña correcta)
  423  → ACCOUNT_LOCKED (5 fallos → 2 min)
  500  → INTERNAL_ERROR
```

Reglas de negocio del backend (NO replicar en cliente):
- Anti-enumeración: 401 genérico para credenciales incorrectas e usuario inexistente (B-4).
- Anti-enumeración: 403 solo tras contraseña correcta (B-5).
- Bloqueo con limpieza lazy (B-2): el cliente no gestiona estado de bloqueo.
- Username case-insensitive (B-6): el backend resuelve.

## 8. Seguridad (CLAUDE.md §5 aplicado a esta fase)

- **JWT solo en EncryptedSharedPreferences** — `TokenManager.saveToken()` ya lo garantiza.
- **Nunca loguear el token ni la contraseña** — logging OkHttp `BASIC` (D-05, Fase 1) no incluye bodies.
- **No hay countdown local de bloqueo** — se delega al backend (D-16).
- **Cuenta inactiva (403):** cerrar sesión local inmediatamente (`clearToken`) y navegar a login.
  No reintentar la petición (CLAUDE.md §5).
- **Validación cliente = retroalimentación inmediata;** el servidor es autoridad (CLAUDE.md §4.8).

## 9. Archivos a crear / modificar

**Crear:**

| Archivo | Descripción |
|---|---|
| `ui/login/LoginViewModel.kt` | ViewModel con `LoginUiState`, `LoginEvento` |
| `ui/login/LoginUiState.kt` | Data class de estado + sealed interface de eventos |
| `ui/login/LoginScreen.kt` | Pantalla completa (reemplaza placeholder) |
| `ui/components/HeroLogin.kt` | Sección verde hero con SVGs (§13.1.1) |
| `ui/components/LoginInputPill.kt` | Input pill login (§13.3.1) |
| `ui/components/LoginButton.kt` | Botón primario pill login (§13.2.1) |
| `ui/login/HomePlaceholderScreen.kt` | Placeholder temporal post-login (D-18) |
| `ui/login/HomePlaceholderViewModel.kt` | ViewModel mínimo con `cerrarSesion()` (D-18) |
| `res/drawable-nodpi/signo_igual.xml` | Vector Drawable del SVG decorativo |
| `res/drawable-nodpi/signo_abc123.xml` | Vector Drawable del SVG decorativo |
| `res/drawable-nodpi/signomas.xml` | Vector Drawable del SVG decorativo |
| `repository/SesionRepository.kt` | Interfaz de sesión (D-22) |
| `repository/TokenManagerSesion.kt` | Adapter `@Singleton` que implementa `SesionRepository` delegando en `TokenManager` (D-22) |
| `test/…/LoginViewModelTest.kt` | Tests unitarios del ViewModel |
| `androidTest/…/LoginScreenTest.kt` | Tests de UI de la pantalla |

**Modificar:**

| Archivo | Cambio |
|---|---|
| `repository/AuthRepository.kt` | Agregar `suspend fun login(request: LoginRequest): Resultado<LoginResponse>` |
| `repository/RemoteAuthRepository.kt` | Agregar `override suspend fun login(...)` usando `llamar` |
| `utils/EraError.kt` | Agregar: `CredencialesInvalidas`, `CuentaBloqueada`, `CuentaInactiva` |
| `utils/ErrorMapper.kt` | Agregar ramas `INVALID_CREDENTIALS`, `ACCOUNT_LOCKED`, `ACCOUNT_INACTIVE` |
| `ui/navigation/EraRoutes.kt` | Cambiar `LOGIN = "login"`, agregar `HOME_PLACEHOLDER` |
| `ui/navigation/EraNavHost.kt` | Reemplazar `LoginPlaceholderScreen` por `LoginScreen`, agregar `HomePlaceholderScreen` |
| `ui/register/RegistroPaso3Screen.kt` | Guardar `"registro_exitoso" = true` en `savedStateHandle` destino (D-14 diferido) |
| `di/RepositoryModule.kt` | Agregar `@Binds` de `SesionRepository` → `TokenManagerSesion` (D-22) |
| `test/…/AuthRepositoryTest.kt` | Agregar 7 casos MockWebServer de login (en vez de `AuthRepositoryLoginTest.kt` separado) |
| `test/…/ErrorMapperTest.kt` | Agregar 1 caso de mapeo de códigos de login (D-22) |

**Eliminar:**

| Archivo | Razón |
|---|---|
| `ui/login/LoginPlaceholderScreen.kt` | Reemplazado por `LoginScreen.kt` |

## 10. Capas de testing

### 10.1 Tests unitarios

**`LoginViewModelTest.kt`** (JUnit + `kotlinx-coroutines-test`):

| # | Caso | Verifica |
|---|---|---|
| 1 | Campos vacíos → botón deshabilitado | `uiState.cargando == false`, sin eventos |
| 2 | Campos con contenido → btn habilitado | `uiState` actualizado |
| 3 | Login exitoso → guardar token + evento `NavegarAHome` | `sesionRepository` fue llamado, evento emitido |
| 4 | 401 INVALID_CREDENTIALS → `errorGeneral = CredencialesInvalidas` | UI muestra mensaje genérico |
| 5 | 423 ACCOUNT_LOCKED → `errorGeneral = CuentaBloqueada` | UI muestra mensaje de bloqueo |
| 6 | 403 ACCOUNT_INACTIVE → `limpiarToken` + `NavegarALogin` (mensaje inline) | Token limpiado, navegación a login |
| 7 | IOException → `errorGeneral = ErrorConexion` | UI muestra "Sin conexión" |
| 8 | Toggle visibilidad contraseña | `contrasenaVisible` cambia |
| 9 | Limpiar error al modificar campo | `errorGeneral = null` al escribir |

> **Nota §10.1 (conteo real):** se implementaron **11** casos (los 9 del plan +
> `onLoginClick` con campos vacíos no llama al repo y `onUsuarioOCorreoChange`
> vacío marca `USUARIO_O_CORREO`). Los tests de login del repository se agregaron
> al archivo existente `AuthRepositoryTest.kt` (7 casos MockWebServer, no un
> `AuthRepositoryLoginTest.kt` separado) y se añadió 1 caso de mapeo de códigos
> login en `ErrorMapperTest.kt`.

**`AuthRepositoryTest.kt`** — login (MockWebServer):

| # | Mock response | Verifica |
|---|---|---|
| 1 | 200 `{ "token": "..." }` | `Resultado.Exito(LoginResponse(token))` |
| 2 | 401 `INVALID_CREDENTIALS` | `Resultado.Fallo(EraError.CredencialesInvalidas)` |
| 3 | 423 `ACCOUNT_LOCKED` | `Resultado.Fallo(EraError.CuentaBloqueada)` |
| 4 | 403 `ACCOUNT_INACTIVE` | `Resultado.Fallo(EraError.CuentaInactiva)` |
| 5 | 400 `VALIDATION_ERROR` | `Resultado.Fallo(EraError.Validacion(...))` |
| 6 | IOException (sin red) | `Resultado.Fallo(EraError.ErrorConexion)` |
| 7 | Request body correcto | Verificar `usuarioOCorreo` y `contrasena` en el body |

### 10.2 Tests androidTest (Compose)

**`LoginScreenTest.kt`:**

| # | Caso | Verifica |
|---|---|---|
| 1 | Pantalla carga correctamente | Título "Inicio de sesión" visible |
| 2 | Botón deshabilitado con campos vacíos | `assertIsNotEnabled()` |
| 3 | Botón habilitado con ambos campos | `assertIsEnabled()` |
| 4 | Toggle visibilidad contraseña | Icono cambia de `Visibility` a `VisibilityOff` |
| 5 | Error de credenciales se muestra | Texto de error visible |
| 6 | "Regístrate" navega a registro | `onNodeWithText("Regístrate").performClick()` |

## 11. Definition of Done de la fase

1. Suite de tests verde: ViewModel (11 casos), Repository MockWebServer login (7 casos)
   en `AuthRepositoryTest.kt`, ErrorMapper login (1 caso), androidTest (6+ casos).
2. Compilación limpia (`assembleDebug`).
3. Flujo manual contra backend dev: login con credenciales válidas → JWT guardado
   → Home placeholder → Cerrar sesión → Login.
4. Flujo de credenciales incorrectas: 401 → mensaje genérico.
5. Flujo de cuenta bloqueada: 423 → mensaje de bloqueo.
6. Flujo de registro exitoso → Login con snackbar "Cuenta verificada" (D-14 diferido).
7. Sin secretos ni PII en logs verificable (inspección logcat en debug).
8. Los 74 tests unitarios de Fase 1 siguen verdes (regla §4.13).
9. Este documento y `CLAUDE.md` §10 actualizados.

## 12. Puntos abiertos

- **Snackbar post-registro (D-14 diferido de Fase 1):** el patrón correcto es
  `savedStateHandle` del `NavBackStackEntry`. Se implementa al crear `LoginScreen`.
  Pendiente de verificación en dispositivo real.
- **Home placeholder y logout:** la ruta `HOME_PLACEHOLDER` y `HomePlaceholderScreen`
  son mínimas — solo validan sesión + botón cerrar. El Sidebar y Home real llegan
  en Fase 10.
- **Splash (Fase 10):** verificará `TokenManager.hasToken()` al arrancar y
  navegará a Home o Login según corresponda.
