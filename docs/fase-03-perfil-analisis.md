# Fase 3 — Perfil / Mi cuenta (Módulo D): Análisis y Diseño

> Documento de análisis del módulo frontend. Registra el alcance, el diseño,
> las decisiones aprobadas (D-23…) y los archivos a crear/modificar.
> **Estado:** **APROBADO** por el propietario y el auditor (2026-08-28).
> **Implementado** (2026-08-28), capa por capa según este acta. Resoluciones de
> los puntos abiertos y condiciones de aprobación en §13; cierre en §14.

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-06 (Cuenta del usuario) |
| Requisitos no funcionales asociados | REQ-NF-02 (seguridad), REQ-NF-03 (usabilidad) |
| Casos de uso | CU-06 (Editar cuenta) |
| Historias de usuario | HU-06 |
| Endpoints backend consumidos | `GET /users/me`, `PATCH /users/me` |
| Fuente visual | `decisiones-tecnicas.md` §14.9, §13.1.3, §13.1.4, §13.6.1/2, §16.1 |
| Fuente de contrato | `BACKEND_ERA/docs/modulo-d-analisis.md`, `BACKEND_ERA/README.md` |

## 2. Alcance

**Incluye:**
- `MiCuentaViewModel` (@HiltViewModel) con estados de carga/éxito/error y el
  patrón UiState + eventos de Fase 1/2.
- **GET** `/users/me`: carga los 5 campos (`nombreMenor`, `fechaNacimiento`
  ISO → DD/MM/AAAA, `correo`, `nombreUsuario`, `avatar`).
- **PATCH** `/users/me`: edición **solo** de `nombreUsuario` vía Dialog
  (estilo reg-input), validación cliente 3–60 sin espacios (reutilizando
  `Validators.isValidNombreUsuario`, no duplicada).
- Nueva capa `repository/` para perfil: `UserRepository` (interfaz) +
  `RemoteUserRepository` (impl Retrofit con wrapper `llamar` →
  `Resultado<T>`). `@Binds` en `RepositoryModule`.
- Componentes nuevos reutilizables: cabecera gris Settings (§13.1.3) y
  tarjeta contenedora con filas settings (§13.6.1/2), más los tokens de color
  que faltan en `Color.kt`.
- Navegación: ruta `perfil` y punto de entrada temporal desde
  `HomePlaceholderScreen` (la Sidebar real es Fase 10).
- Tests unitarios (ViewModel + Repository MockWebServer + ErrorMapper) y
  androidTest (pantalla).

**Fuera de alcance (cerrado):**
- Avatar personalizado (`PUT`/`GET /users/me/avatar`, Módulo I) → **Fase 9**.
  La carga con Coil y el link "Cambiar avatar" NO se implementan aquí (ver D-27).
- `DELETE /users/me` (Módulo E) → **Fase 6**.
- Logout HTTP (`POST /auth/logout`, Módulo F) → **Fase 4** (stateless; la
  sesión local ya la gestiona `SesionRepository`).
- Sidebar y Home reales → **Fase 10**.
- Edición de `correo` / `nombreMenor` / `fechaNacimiento`: solo lectura (el
  backend no los acepta en el PATCH, REQ-FUN-06 CA5).

## 3. Estado previo aprovechado (verificado en código, no reescribe)

| Componente | Archivo | Estado |
|---|---|---|
| `UsersApi.getProfile()` `@GET("users/me")` | `remote/api/UsersApi.kt:14-15` | ✅ Declarado, firma `(): UserProfile` |
| `UsersApi.updateUsername()` `@PATCH(..., @Body UpdateUsernameRequest)` | `remote/api/UsersApi.kt:17-18` | ✅ Declarado |
| `UserProfile` (5 campos camelCase sin `@SerialName`, `avatar: String? = null`) | `remote/dto/user/UserProfile.kt` | ✅ DTO correcto, verificado vs backend |
| `UpdateUsernameRequest(nombreUsuario)` | `remote/dto/user/UpdateUsernameRequest.kt` | ✅ DTO correcto (solo 1 campo) |
| `Validators.isValidNombreUsuario` + constantes 3–60 | `utils/Validators.kt:11-12,27-29` | ✅ Reutilizar (no duplicar) |
| `EraError` sealed + `ErrorMapper.desdeHttp` (por campo `error`) | `utils/EraError.kt`, `utils/ErrorMapper.kt:8-25` | ✅ Extender (D-30) |
| `MensajeError` when exhaustivo + `mapsTo` | `utils/MensajeError.kt:5-18,44-56` | ✅ Ampliar (D-30) |
| `Resultado<T>` / `llamar` wrapper / `aEraError` | `repository/RemoteAuthRepository.kt:36-52`, `repository/Resultado.kt` | ✅ Replicar patrón en `RemoteUserRepository` |
| `SesionRepository` + `TokenManagerSesion` | `repository/SesionRepository.kt`, `TokenManagerSesion.kt` | ✅ Reutilizar (401/403 → limpiar sesión) |
| `RepositoryModule` (`@Binds`) | `di/RepositoryModule.kt` | ✅ Añadir binding de `UserRepository` |
| Patrón ViewModel (@HiltViewModel + UiState StateFlow + eventos Channel) | `ui/login/LoginViewModel.kt`, `ui/login/LoginUiState.kt` | ✅ Réplica para `MiCuentaViewModel` |
| `EraTextField` (input reg: radio 10dp, fondo `ColorPrimaryPale`, label) | `ui/components/EraTextField.kt` | ✅ Reutilizar en el Dialog de edición (§14.9 "estilo reg-input") |
| Presets de avatar `avatar_preset_1..3.jpg` en `drawable-nodpi/` | `res/drawable-nodpi/` | ✅ Dibujar el avatar según `avatar` (D-27) |
| Tokens básicos de tema (ColorPrimary, ColorPrimaryLight, etc.) | `ui/theme/Color.kt` | ✅ Existen; **faltan** los tokens greys de Settings (D-29) |

**Nota:** los tokens `ColorSettingsHeaderBg`, `ColorSettingsBackBg`,
`ColorSettingsBackIcon`, `ColorSettingsLabel`, `ColorDivider`,
`ColorCardBorder`, `ColorSwitchTrackOn/Off` están documentados en
`decisiones-tecnicas.md` §14.10 pero **aún no existen en `Color.kt`**. Se crean
en esta fase (D-29) como fundación reutilizable por Ajustes/FAQ/Eliminar cuenta.

## 4. Contrato exacto consumido (fuente: modulo-d-analisis.md + README)

### 4.1 `GET /api/v1/users/me`

**Request:** sin body. Header `Authorization: Bearer <JWT de sesión>`
(auto-adjuntado por `JwtInterceptor`).

**Response 200 OK — `UsuarioPerfilDto`:**
| Campo | Tipo | Ejemplo |
|---|---|---|
| `nombreMenor` | String | `"Laura Pérez"` |
| `fechaNacimiento` | String ISO `yyyy-MM-dd` | `"2018-04-12"` |
| `correo` | String | `"laura.perez@example.com"` |
| `nombreUsuario` | String | `"laura2026"` |
| `avatar` | String? | `"preset:1"` (o `null`) |

**Códigos de error:**
| Status | `error` | Tratamiento Fase 3 |
|---|---|---|
| 200 | — | Cargar perfil |
| 401 | `UNAUTHORIZED` | Sesión inválida/expirada → cerrar sesión local (D-25) |
| 403 | `ACCOUNT_INACTIVE` | Cuenta eliminada → cerrar sesión local sin reintentar (regla §5, D-25) |
| 404 | `NOT_FOUND` | Defensivo (inconsistencia de datos) → error genérico (D-30) |
| 500 | `INTERNAL_ERROR` | `ErrorServidor` |

### 4.2 `PATCH /api/v1/users/me` (edición solo de username)

**Request:** body `{ "nombreUsuario": "<nuevo>" }` (**solo** ese campo; otra
clave → 400 `INVALID_REQUEST`).

**Response 200 OK:** mismo `UsuarioPerfilDto` **con `nombreUsuario` nuevo**
(el cliente actualiza sin round-trip extra).

**Códigos de error:**
| Status | `error` | Tratamiento Fase 3 |
|---|---|---|
| 200 | — | Aplicar username nuevo en pantalla |
| 400 | `VALIDATION_ERROR` | Forma (blanco / 3–60 sin espacios) → validación cliente la evita en la mayoría; `Validacion` |
| 400 | `INVALID_REQUEST` | Body malformado/clave desconocida → mapear a `Validacion` (D-30) |
| 401 | `UNAUTHORIZED` | → cerrar sesión local (D-25) |
| 403 | `ACCOUNT_INACTIVE` | → cerrar sesión local sin reintentar (D-25) |
| 404 | `NOT_FOUND` | Defensivo → error genérico (D-30) |
| 409 | `CONFLICT` | Username en uso → **mensaje inline en el Dialog** "El nombre de usuario ya está en uso" (D-26) |
| 500 | `INTERNAL_ERROR` | `ErrorServidor` |

### 4.3 Formato del campo `avatar` (confirmado)

El backend devuelve `avatar: String?`:
- `null` → el usuario usa el placeholder del cliente → se muestran las
  **iniciales** del menor en el círculo.
- `"preset:1" | "preset:2" | "preset:3"` → avatar preestablecido **local**
  (`avatar_preset_<n>.jpg`, `drawable-nodpi`) → dibujarlo.
- `"custom:<uuid>"` → foto personalizada (Módulo I, Fase 9). En Fase 3 **no
  se carga con Coil**; se degrada a iniciales hasta implementar Fase 9 (D-27).

## 5. Decisiones de diseño (D-23…)

### D-23 — Patrón de repositorio: `UserRepository` + `RemoteUserRepository`

**Decisión:** crear una **nueva** interfaz `UserRepository` (no extender
`AuthRepository`) con `remote/` de `UsersApi`, implementada por
`RemoteUserRepository` (mirror de `RemoteAuthRepository`), y un `@Binds`
nuevo en `RepositoryModule`.

| Método | Firma |
|---|---|
| `obtenerPerfil()` | `suspend fun obtenerPerfil(): Resultado<UserProfile>` |
| `actualizarNombreUsuario(nombre: String)` | `suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile>` |

**Por qué:** `AuthRepository` agrupa exclusivamente endpoints de autenticación
(`/auth/*`); el perfil pertenece a otro dominio (`/users/me`) y a otra vida de
ciclo (requiere sesión). Mezclarlo añadiría responsabilidades ajenas y
confundiría la inyección. Se replica exactamente el wrapper `llamar`
(`CancellationException` re-lanzada, `HttpException` → `ErrorMapper`) de
`RemoteAuthRepository.kt:36-52`. Ambos métodos retornan `Resultado<UserProfile>`
porque el PATCH devuelve el perfil ya actualizado (D-9 del backend): la UI
aplica el username nuevo sin round-trip.

### D-24 — Punto de entrada de navegación: botón en `HomePlaceholderScreen`

**Decisión:** mantener la Sidebar fuera de alcance (Fase 10) y añadir a
`HomePlaceholderScreen` **un botón/enlace "Mi cuenta"** (además del existente
"Cerrar sesión") que navega a la nueva ruta `perfil`. No se crea Sidebar, no
se dibuja `ModalDrawer`.

**Ruta nueva:** `EraRoutes.PERFIL = "perfil"` (no `"profile"`, para mantener
naming en español del resto del grafo: `login`, `registro`, `home_placeholder`).
En `EraNavHost` se registra `composable(EraRoutes.PERFIL)`.

**Por qué limitarse:** REQ-FUN-08 (Sidebar) es Fase 10 y el Home real no
existe; el único destino autenticado actual es `HomePlaceholderScreen`. Un
botón ahí permite probar el flujo completo (login → home → Mi cuenta → volver)
sin anticipar la Sidebar. El paso a Sidebar en Fase 10 solo cambia el
invocador de `navigate(PERFIL)`, no la pantalla ni el ViewModel. El botón
retroceso de la cabecera gris vuelve a `home_placeholder` (ira a Home real en
Fase 10, §14.9 "→ Home").

### D-25 — Tratamiento de 401/403 en el GET y el PATCH (regla §5)

**Decisión:** tanto en `obtenerPerfil()` (GET) como en `actualizarNombreUsuario()`
(PATCH), ante `EraError.CuentaInactiva` (403 `ACCOUNT_INACTIVE`) o una sesión
invalidada (401 `UNAUTHORIZED` → `SesionExpirada`), el ViewModel:

1. Llama `sesionRepository.limpiarToken()` (borra JWT del Keystore).
2. **No reintenta** la petición.
3. Emite evento `NavegarALogin` (con `popUpTo(0){inclusive=true}`).

**Por qué:** CLAUDE.md §5 es explícito: "Cuenta eliminada (403): cerrar sesión
local inmediatamente, sin reintentar la petición". El 401 `UNAUTHORIZED` en
`/me` indica token ausente/expirado; mantener la pantalla con datos sería
engañoso y reintentar es inútil. El 403 solo ocurre tras credencial válida,
así que es de cuenta eliminada, no de sesión caducada. Se replica el patrón ya
usado en `LoginViewModel.manejarFallo` (`LoginViewModel.kt:98-109`).

### D-26 — Manejo de 409 CONFLICT en el PATCH

**Decisión:** `"CONFLICT"` ya se mapea a `EraError.UsuarioEnUso` en
`ErrorMapper.kt:16` (heredado de Fase 1). En el Dialog de edición se muestra
**inline bajo el campo** el texto que ya produce `MensajeError`:
_"Este nombre de usuario ya está en uso"_ (`MensajeError.kt:9`). El Dialog
mantiene abierto el campo y el valor introducido (no se descarta); el usuario
corrige o cancela.

**Por qué:** REQ-FUN-06 CA3 ("si un dato no cumple las validaciones, se
muestra un mensaje de error y no se guarda ningún cambio") y la regla §4.8
(validación cliente no sustituye al servidor: el 409 solo lo detecta el
backend, por unicidad contra `usuario` activo/eliminado y `registro_pendiente`).
Se **reutiliza** `UsuarioEnUso` en vez de crear un subtipo nuevo: la semántica
es idéntica a la del registro y el mensaje en español ya es correcto.

### D-27 — Cómo mostrar el avatar en esta fase

**Decisión:** en `MiCuentaScreen`, el círculo de 100dp se dibuja así según
`UserProfile.avatar`:

| `avatar` | Rendering |
|---|---|
| `null` | Iniciales del menor (primeras letras de `nombreMenor`) en 32sp Bold `ColorPrimary` sobre fondo `ColorPrimaryLight` |
| `"preset:1\|2\|3"` | `painterResource(R.drawable.avatar_preset_<n>)` (local, `drawable-nodpi`) |
| `"custom:*"` | Iniciales (degradación; sin Coil en Fase 3) |

**Por qué:** el PUT/GET de avatar personalizado y Coil son Fase 9 y el campo
`avatar` confirma que los presets son locales y `null` equivale a placeholder
del cliente (modulo-d-analisis.md §3.2). No se dibuja el link "Cambiar avatar"
ni el selector (§14.9) porque dependen de Fase 9; se omite la fila de "Cambiar
avatar" del prototipo a la espera de esa fase (regla §4.13.h: prevalece el
requisito sobre el prototipo). `custom:*` no se puede alcanzar por UI en Fase 3
(sin edición de avatar), pero se hace degradación defensiva para no romper si
la cuenta ya lo tuviera.

### D-28 — Componentes: cuáles se crean vs. cuáles se reutilizan

**Nuevos componentes reutilizables (en `ui/components/`):**

| Componente | Archivo propuesto | Patrón §13 | Uso futuro |
|---|---|---|---|
| Cabecera gris Settings reutilizable | `SettingsHeader.kt` | §13.1.3 | Ajustes (Fase 10), FAQ, Eliminar cuenta (Fase 6) — mismo tratamiento visual |
| Tarjeta contenedora con filas | `SettingsCard.kt` | §13.6.1 + §13.6.2 | Ajustes, Mi cuenta, FAQ, Eliminar cuenta |
| Fila settings (label + valor/acción) | (dentro de `SettingsCard.kt`) | §13.6.2 | Mi cuenta (campos), Ajustes (switches) |

**Componentes existentes reutilizados:**
- `EraTextField` (input registro) → dentro del **Dialog** de edición de
  username (§14.9 "mismo estilo reg-input").
- Tokens `ColorPrimary`, `ColorPrimaryLight`, etc.

**Qué NO es reutilizable ni se fuerza:**
- `CompactGreenHeader` (§13.1.2, verde) **no** sirve para Mi cuenta (es gris
  Settings §13.1.3). Se crea `SettingsHeader` separado, ambos coexisten.

**Por qué:**
- La cabecera gris y la tarjeta con filas son la identidad visual compartida
  de 4 pantallas de "configuración" (§16.1/2/3/4). Crearlas ya como genéricas
  reutilizables evita duplicar en Fase 6/10.
- El Dialog de edición usa `EraTextField` (sin crear un input nuevo) porque
  §14.9 pide explícitamente el estilo reg-input que ya existe.

### D-29 — Tokens de color Settings faltantes en `Color.kt`

**Decisión:** añadir a `ui/theme/Color.kt` los tokens documentados en §14.10:

| Token | Valor HEX |
|---|---|
| `ColorSettingsHeaderBg` | `#767676` |
| `ColorSettingsBackBg` | `#F2F2F2` |
| `ColorSettingsBackIcon` | `#2C2C2C` |
| `ColorSettingsLabel` | `#2D3142` |
| `ColorDivider` | `#D8D8D8` |
| `ColorCardBorder` | `#E6E6E6` |

**Por qué:** la especificación §14.9/§14.10 los referencia y no existen aún
(verificado en `Color.kt`). Son solo constantes de color — **sin dependencias
nuevas** (regla §4.4/§4.13). **`ColorSwitchTrackOn/Off` NO se crean en Fase 3**
(condición de aprobación §13.1 #5: no dejar tokens sin uso; pertenecen a
Ajustes, Fase 10).

### D-30 — Errores nuevos a mapear (EraError / ErrorMapper / MensajeError)

**Decisión (sin tocar ramas existentes):**

| HTTP + campo `error` | `EraError` | Estado |
|---|---|---|
| 401 `UNAUTHORIZED` | `SesionExpirada` | **Nuevo** subtipo |
| 404 `NOT_FOUND` | `PerfilNoEncontrado` | **Nuevo** subtipo (defensivo) |
| 400 `INVALID_REQUEST` | → `EraError.Validacion(...)` | **Nueva rama** en `ErrorMapper` (antes caía a `Desconocido`) |
| 403 `ACCOUNT_INACTIVE` | `CuentaInactiva` | Ya existe (Fase 2) |
| 409 `CONFLICT` | `UsuarioEnUso` | Ya existe (Fase 1) |
| 400 `VALIDATION_ERROR` | `Validacion` | Ya existe |
| 500 `INTERNAL_ERROR` | `ErrorServidor` | Ya existe |
| `IOException` | `ErrorConexion` | Ya existe |

**Cambios concretos:**
- `EraError.kt`: + `SesionExpirada`, + `PerfilNoEncontrado`.
- `ErrorMapper.kt`: añadir ramas `"UNAUTHORIZED" → SesionExpirada`,
  `"NOT_FOUND" → PerfilNoEncontrado`, `"INVALID_REQUEST" → Validacion(...)`.
  Para `INVALID_REQUEST` (sin `details` en el cuerpo) usar un mensaje por
  defecto **"Solicitud inválida"** (condición de aprobación §13.2 #1), no el
  genérico "Error de validación".
- `MensajeError.kt`: **obligatorio** actualizar el `when` exhaustivo (D-30) y
  `mapsTo`: `SesionExpirada`/`PerfilNoEncontrado` → `false` (no se anclan a
  ningún `CampoRegistro`). Mensajes: `SesionExpirada` → "Tu sesión expiró.
  Vuelve a iniciar sesión", `PerfilNoEncontrado` → "No se pudo cargar el
  perfil".

**Por qué un subtipo para 404:** es un caso defensivo del backend
(`NotFoundException`, solo posible por inconsistencia de datos) y merece
mensaje propio en vez del genérico `Desconocido`. Se reutiliza `UsuarioEnUso`
y `CuentaInactiva` (no se duplica semántica). La extensión de `EraError`
obliga a tocar el `when` de `MensajeError` — se documenta aquí para que el
mapper y el mensaje se actualicen al mismo tiempo y la compilación siga
exhaustiva.

## 6. Arquitectura propuesta (capa por capa)

### 6.1 Remote (ya declarado — no se edita)

`UsersApi.kt` ya expone `getProfile()` y `updateUsername()`. **No se modifica.**
DTOs `UserProfile` y `UpdateUsernameRequest` ya verificados. **Sin cambios.**

### 6.2 Repository (nuevo archivo)

```
UserRepository (interfaz)  → repository/UserRepository.kt
  suspend fun obtenerPerfil(): Resultado<UserProfile>
  suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile>

RemoteUserRepository (impl @Singleton)  → repository/RemoteUserRepository.kt
  override ... = llamar { api.getProfile() }
  override ... = llamar { api.updateUsername(UpdateUsernameRequest(nombre)) }
```
Wrapper `llamar` + `aEraError` replicados de `RemoteAuthRepository.kt:36-52`
(Fase 3 no extrae ese helper a util compartido para no refactorizar código
aprobado de Fase 1/2 sin necesidad).

### 6.3 DI

`di/RepositoryModule.kt`: añadir
`@Binds @Singleton abstract fun bindUserRepository(impl: RemoteUserRepository): UserRepository`.

### 6.4 UI / ViewModel

**Nuevos archivos:**
- `ui/perfil/MiCuentaViewModel.kt` — `@HiltViewModel`, inyecta `UserRepository`
  y `SesionRepository`.
- `ui/perfil/MiCuentaUiState.kt` — data class + `MiCuentaEvento` (sin
  `CampoPerfil`: el error de username es un `String` inline, no un enum de
  campo, a diferencia de `CampoLogin`).
- `ui/perfil/MiCuentaScreen.kt` — pantalla.
- `ui/components/SettingsHeader.kt`, `ui/components/SettingsCard.kt` (decisión D-28).

**`MiCuentaUiState`:**
| Campo | Tipo | Default |
|---|---|---|
| `cargando` | `Boolean` | `false` |
| `perfil` | `UserProfile?` | `null` |
| `errorGeneral` | `EraError?` | `null` |
| `dialogoAbierto` | `Boolean` | `false` |
| `nombreUsuario` | `String` | `""` |
| `guardando` | `Boolean` | `false` |
| `errorNombreUsuario` | `String?` | `null` |

**`MiCuentaEvento`:** `NavegarALogin` (401/403), `MostrarSnackbar(msg)`.

**Flujo:**
1. `onEntrar()` (primera composición) → `cargando=true` →
   `userRepository.obtenerPerfil()`.
   - `Exito(perfil)` → `perfil=...`, `cargando=false`.
   - `Fallo(CuentaInactiva | SesionExpirada)` → `limpiarToken()` →
     `NavegarALogin`.
   - `Fallo(other)` → `errorGeneral=other`.
2. "Editar" → `dialogoAbierto=true`, precarga `nombreUsuario=perfil.nombreUsuario`.
3. "Guardar" (Dialog) → validación cliente `Validators.isValidNombreUsuario`:
   si falla, `errorNombreUsuario` inline (no llama a red).
4. `userRepository.actualizarNombreUsuario(...)`:
   - `Exito(nuevoPerfil)` → `perfil=nuevoPerfil`, cerrar Dialog.
   - `Fallo(UsuarioEnUso)` → `errorNombreUsuario` inline
     "Este nombre de usuario ya está en uso" (D-26), mantener abierto.
   - `Fallo(CuentaInactiva | SesionExpirada)` → `limpiarToken()` →
     `NavegarALogin`.
   - `Fallo(other)` → snackbar / inline.

**Validación username (reuso, no duplicación):** `Validators.isValidNombreUsuario`
(`Validators.kt:27-29`) → 3–60 sin espacios. El mismo validador que ya usa
`RegistroViewModel` — **no se crea otra copia**.

**Fecha de nacimiento (reuso):** la pantalla muestra `fechaNacimiento`
(ISO `yyyy-MM-dd`) en formato `DD/MM/AAAA`. Se añade un helper
`formatearFechaISO(iso: String): String` en `utils/Validators.kt` usando
`LocalDate.parse(iso)` + `DateTimeFormatter.ofPattern("dd/MM/uuuu")` — misma
familia de formatos que `Validators.kt:21-22` y `RegistroPaso1Screen.kt:201`.
(No hay hoy un conversor ISO→display compartido; se crea uno en el validador
para reutilización de Ajustes/Progreso.)

### 6.5 Pantalla `MiCuentaScreen.kt` (§14.9)

```
┌─────────────────────────────────────┐
│  ⟵  (círculo 64dp #F2F2F2)  Mi Cuenta│  SettingsHeader (gris #767676, ~230dp)
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  título 34–36sp Bold blanco
│  ┌─────────────────────────────┐    │
│  │         ┌───────┐           │    │  SettingsCard (borde 1dp #E6E6E6,
│  │         │avatar │           │    │  radio 24dp, padding 24dp)
│  │         └───────┘           │    │  avatar 100dp circular
│  │  Nombre del menor  <valor>  │    │  ColorPrimaryLight + iniciales/preset
│  │  ────────────────────────   │    │  label 16sp Bold ColorSettingsLabel
│  │  Correo electrónico <valor> │    │  valor 18sp Regular negro
│  │  ────────────────────────   │    │  solo lectura
│  │  Nombre de usuario  [Editar]│    │  único editable → abre Dialog
│  │  ────────────────────────   │    │  resultado de la fecha: solo lectura
│  │  Fecha de nacimiento <valor>│    │  DD/MM/AAAA
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

Estados: **cargando** (spinner en card), **error de carga** (mensaje +
reintento opcional), **cargado** (5 campos). Avatar según D-27.

**Dialog de edición de username:** `AlertDialog`/`Dialog` con campo
`EraTextField` (reg-input), botones "Cancelar" / "Guardar", y
`errorNombreUsuario` inline bajo el campo (validación local y 409).

### 6.6 Navegación

```
EraRoutes.kt:
  + const val PERFIL = "perfil"

EraNavHost.kt:
  composable(EraRoutes.HOME_PLACEHOLDER):
    HomePlaceholderScreen(..., onNavigatePerfil = { navController.navigate(EraRoutes.PERFIL) })
  composable(EraRoutes.PERFIL):
    val vm: MiCuentaViewModel = hiltViewModel()
    MiCuentaScreen(
      vm = vm,
      onVolver = { navController.popBackStack() },          // → home_placeholder
      onNavegarALogin = {
        navController.navigate(EraRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
      },
    )
```

`HomePlaceholderScreen` gana un parámetro `onNavigatePerfil` y un botón
"Mi cuenta" (D-24). Se conserva "Cerrar sesión".

## 7. Capas de testing

### 7.1 Tests unitarios (JUnit + `kotlinx-coroutines-test`)

**`MiCuentaViewModelTest.kt`:**

| # | Caso | Verifica |
|---|---|---|
| 1 | `onEntrar` éxito → `perfil` poblado, `cargando=false` | Datos cargados |
| 2 | GET `ErrorConexion` → `errorGeneral` | Sin conexión |
| 3 | GET `CuentaInactiva` → `limpiarToken` + `NavegarALogin` | Regla §5 |
| 4 | GET `SesionExpirada` → `limpiarToken` + `NavegarALogin` | Regla §5 |
| 5 | GET `PerfilNoEncontrado` → `errorGeneral` | Defensivo 404 |
| 6 | Abrir Dialog precarga username actual | `dialogoAbierto=true`, campo prefilled |
| 7 | Guardar con username inválido (<3 o espacios) → error inline, **sin llamar red** | Validación cliente |
| 8 | PATCH éxito → `perfil` con username nuevo + Dialog cerrado | D-9 backend |
| 9 | PATCH `UsuarioEnUso` (409) → error inline, Dialog **abierto** | D-26 |
| 10 | PATCH `CuentaInactiva` → `limpiarToken` + `NavegarALogin` | Regla §5 |
| 11 | PATCH `ErrorServidor` → error (no navega) | 500 |
| 12 | `formatearFechaISO("2018-04-12")` == `"12/04/2018"` | (en `ValidatorsTest`) |

**`UserRepositoryTest.kt` (MockWebServer) — `RemoteUserRepository`:**

| # | Mock | Verifica |
|---|---|---|
| 1 | GET 200 (5 campos con `avatar="preset:1"`) | `Exito(UserProfile)` |
| 2 | GET 200 con `avatar=null` | `Exito` con `avatar=null` |
| 3 | GET 401 `UNAUTHORIZED` | `Fallo(SesionExpirada)` |
| 4 | GET 403 `ACCOUNT_INACTIVE` | `Fallo(CuentaInactiva)` |
| 5 | GET 404 `NOT_FOUND` | `Fallo(PerfilNoEncontrado)` |
| 6 | GET IOException (sin red) | `Fallo(ErrorConexion)` |
| 7 | PATCH 200 (username nuevo en body de respuesta) | `Exito` refleja el nuevo username |
| 8 | PATCH 409 `CONFLICT` | `Fallo(UsuarioEnUso)` |
| 9 | PATCH 400 `INVALID_REQUEST` | `Fallo(Validacion)` |
| 10 | PATCH 400 `VALIDATION_ERROR` (con `details`) | `Fallo(Validacion(detalles))` |
| 11 | PATCH 401 `UNAUTHORIZED` | `Fallo(SesionExpirada)` |
| 12 | PATCH 403 `ACCOUNT_INACTIVE` | `Fallo(CuentaInactiva)` |
| 13 | Request body PATCH correcto (`{nombreUsuario}` solo) | Verificar body JSON |

**`ErrorMapperTest.kt` (extensión):** 3–4 casos nuevos: `UNAUTHORIZED` →
`SesionExpirada`; `NOT_FOUND` → `PerfilNoEncontrado`; `INVALID_REQUEST` →
`Validacion`; mantener `CONFLICT` → `UsuarioEnUso` (regresión).

**`ValidatorsTest.kt` (extensión):** `formatearFechaISO` (1–2 casos).

### 7.2 Tests androidTest (Compose)

**`MiCuentaScreenTest.kt`:**
| # | Caso |
|---|---|
| 1 | Carga → título "Mi Cuenta" y los 5 campos visibles |
| 2 | Avatar con `avatar=null` muestra iniciales |
| 3 | Avatar con `avatar="preset:1"` carga drawable (sin crash) |
| 4 | "Editar" abre Dialog con username actual |
| 5 | Guardar con username vacío → error inline |
| 6 | Guardar válido → Dialog se cierra |
| 7 | **Cancelar cierra el Dialog sin guardar** (condición §13.2 #3) |
| 8 | **409 inline muestra "Este nombre de usuario ya está en uso"** (condición §13.2 #3) |
| 9 | Botón retroceso → callback `onVolver` |

**`HomePlaceholderScreenTest.kt`** (nuevo si no existe — condición §13.2 #3):
| # | Caso |
|---|---|
| 1 | Visualiza "Sesión iniciada" y botón "Cerrar sesión" |
| 2 | Nuevo botón "Mi cuenta" dispara `onNavigatePerfil` |

### 7.3 Objetivo de conteo sobre 93 verdes actuales

| Depósito | Antes | Nuevos (estimado) | Después |
|---|---|---|---|
| Unitarios existentes (Fase 1+2) | 93 | — | 93 |
| `MiCuentaViewModelTest` | — | ~12 | +12 |
| `UserRepositoryTest` (MockWebServer) | — | ~13 | +13 |
| `ErrorMapperTest` (nuevas ramas) | — | ~3 | +3 |
| `ValidatorsTest` (formatearFechaISO) | — | ~2 | +2 |
| **Total unitarios** | **93** | **~30** | **~123** |

> **Confirmado en implementación (2026-08-28):** `testDebugUnitTest` reporta
> **123 verdes / 0 fallos** (93 previos + 30 nuevos), reproduciendo el estimado
> exacto.

(androidTest NO suma al `testDebugUnitTest` verde; se compilan y ejecutan con
`connectedDebugAndroidTest` si hay dispositivo.)

## 8. Seguridad (CLAUDE.md §5 aplicado a esta fase)

- **JWT solo en Keystore** — `SesionRepository`/`TokenManagerSesion` ya lo
  garantizan; se reutiliza sin tocar `TokenManager`.
- **Nunca loguear** correo, fecha de nacimiento ni token. `aEraError` no
  loguea bodies; el catch no imprime el perfil.
- **403 ACCOUNT_INACTIVE y 401 UNAUTHORIZED → cerrar sesión local sin
  reintentar** (D-25), regla §5.
- **Validación cliente = retroalimentación inmediata;** el servidor es
  autoridad (§4.8): el 409 se maneja (D-26), el 400 `INVALID_REQUEST` se mapea
  aunque el body del cliente solo manda `nombreUsuario` (D-30).
- Los datos del menor (5 campos) se muestran solo a su dueño autenticado.

## 9. Archivos a crear / modificar

**Crear:**
| Archivo | Descripción |
|---|---|
| `repository/UserRepository.kt` | Interfaz de perfil (D-23) |
| `repository/RemoteUserRepository.kt` | Impl Retrofit con `llamar` (D-23) |
| `ui/perfil/MiCuentaViewModel.kt` | ViewModel (D-23/D-25/D-26) |
| `ui/perfil/MiCuentaUiState.kt` | UiState + eventos + enum |
| `ui/perfil/MiCuentaScreen.kt` | Pantalla (§14.9) |
| `ui/components/SettingsHeader.kt` | Cabecera gris reutilizable (D-28) |
| `ui/components/SettingsCard.kt` | Tarjeta con filas settings (D-28) |
| `test/…/MiCuentaViewModelTest.kt` | Tests unitarios VM |
| `test/…/UserRepositoryTest.kt` | MockWebServer perfil |
| `androidTest/…/MiCuentaScreenTest.kt` | Tests de UI de la pantalla |
| `androidTest/…/HomePlaceholderScreenTest.kt` | Tests de UI del botón "Mi cuenta" (si no existe) |

**Modificar:**
| Archivo | Cambio |
|---|---|
| `utils/EraError.kt` | + `SesionExpirada`, + `PerfilNoEncontrado` (D-30) |
| `utils/ErrorMapper.kt` | + ramas `UNAUTHORIZED`, `NOT_FOUND`, `INVALID_REQUEST` (D-30) |
| `utils/MensajeError.kt` | Ampliar `when` + `mapsTo` (D-30) — obligatorio por exhaustividad |
| `utils/Validators.kt` | + `formatearFechaISO(iso)` (D-31) |
| `ui/theme/Color.kt` | + tokens Settings §14.10 (D-29) |
| `ui/navigation/EraRoutes.kt` | + `PERFIL` (D-24) |
| `ui/navigation/EraNavHost.kt` | + composable `PERFIL`, + `onNavigatePerfil` en home | 
| `ui/login/HomePlaceholderScreen.kt` | + botón "Mi cuenta" + parámetro (D-24) |
| `di/RepositoryModule.kt` | + `@Binds` de `UserRepository` (D-23) |
| `test/…/ErrorMapperTest.kt` | + casos nuevas ramas (D-30) |
| `test/…/ValidatorsTest.kt` | + caso `formatearFechaISO` |

**No se modifica:** `UsersApi.kt`, DTOs `user/*`, `AuthRepository`,
`RemoteAuthRepository`, `SesionRepository`, `TokenManager`, `NetworkModule`.
**No se elimina** ningún archivo.

## 10. Flujo de navegación

```
Login → HomePlaceholderScreen ("Sesión iniciada")
   │  [+ botón "Mi cuenta"] (D-24)
   ▼
Mi cuenta (perfil)
   │  onEntrar → GET /users/me
   │    éxito → muestra 5 campos
   │    Fallo(403|401) → limpiarToken → NavegarALogin (regla §5)
   │  "Editar" → Dialog → "Guardar" → validación cliente → PATCH /users/me
   │    éxito → actualiza username, cierra Dialog
   │    409  → error inline "ya está en uso", Dialog abierto
   │    Fallo(403|401) → limpiarToken → NavegarALogin
   │  ⟵ (retroceso) → popBackStack → HomePlaceholderScreen
```

## 11. Definition of Done de la fase

1. Suite de tests verde: `.\gradlew.bat :app:testDebugUnitTest --console=plain -q`
   → **93 verdes previos + ~30 nuevos** (ViewModel + Repository MockWebServer +
   ErrorMapper + Validators). Regla §4.13: los 93 previos siguen verdes.
2. Compilación limpia: `.\gradlew.bat :app:assembleDebug`.
3. `connectedDebugAndroidTest` (o `assembleDebugAndroidTest` compilado) para
   `MiCuentaScreenTest` si hay emulador/dispositivo.
4. Flujo manual contra backend dev: register → verify → login → home → Mi
   cuenta muestra 5 campos; editar username → 200 refleja el cambio; editar a
   un username en uso → 409 inline; cuenta eliminada en GET/PATCH → cierre de
   sesión local → login.
5. Sin secretos ni PII en logs verificable (inspección logcat en debug);
   el JWT nunca se loguea; correo/fecha nunca se loguean.
6. Reglas §5 cumplidas (403/401 → cerrar sesión local, sin reintentar).
7. Este documento y `CLAUDE.md` §10 actualizados al cerrar (mover a
   "completada", fecha, conteo final). **`decisiones-tecnicas.md` y
   `README.md` no se tocan hasta el cierre.**
8. Doc al cerrar: reproducible desde este acta; no hay cambios de
   `build.gradle.kts` ni `libs.versions.toml` (cero dependencias nuevas).

## 12. Puntos abiertos / requieren palabra del propietario

1. **Título de la ruta y naming:** confirmar `EraRoutes.PERFIL = "perfil"`
   (los demás usan español: `login`, `registro`, `home_placeholder`); la doc
   §15.2 usa `profile` (inglés). Propongo `perfil` por consistencia del grafo.
2. **Mensajes literales:** confirmar textos finales de `SesionExpirada`
   ("Tu sesión expiró…") y `PerfilNoEncontrado` ("No se pudo cargar el perfil").
3. **403 vs 401 en el PATCH:** el backend responde 403 `ACCOUNT_INACTIVE`
   también en PATCH; ¿se muestra algún aviso antes de cerrar sesión o se cierra
   directamente? (D-25 propone cierre directo, siguiendo §5, igual que login).
4. **Prototipo de Mi cuenta ausente:** `docs/prototipos/` está **vacío** (no
   hay JPG/PDF para esta pantalla). Regla §4.11: señalo que no existe prototipo
   y se toma §14.9 como especificación visual. Si el propietario desea anexar
   el JPG antes de implementar, se ajustaría el layout.
5. **`ColorSwitchTrackOn/Off`:** se crean en `Color.kt` por completitud (§14.10)
   aunque Fase 3 no dibuja switches (pertenecen a Ajustes, Fase 10). ¿Se crean
   ahora o se difieren para que no queden sin uso?
6. **Botón "Cambiar avatar" y fila de avatar clickable:** se omiten (Fase 9).
   Confirmar que la tarjeta en Fase 3 muestra solo el avatar estático 100dp
   sin link, como propone D-27.

---

## 13. Aprobación del propietario y del auditor (2026-08-28)

**Veredicto de la auditoría: APROBADO.** Verificado contra el código real,
`decisiones-tecnicas.md` §14.9/§14.10 y el contrato del backend
(`modulo-d-analisis.md`, `modulo-i-analisis.md` §2.2, `MAPA_DEL_REPOSITORIO:289`).
El diseño es correcto, coherente con el patrón de Fase 1/2 y no inventa
capacidades del backend. Correcciones menores aceptadas como condiciones.

### 13.1 Resoluciones de los puntos abiertos (§12)

| # | Punto | Resolución del propietario |
|---|---|---|
| 1 | Ruta `perfil` vs `profile` | **`perfil`** (español, consistente con el grafo) |
| 2 | Mensajes literales | Aceptar "Tu sesión expiró. Vuelve a iniciar sesión" y "No se pudo cargar el perfil" |
| 3 | 403/401 en PATCH | **Cerrar sesión local directo, sin aviso** (regla §5, igual que login) |
| 4 | Prototipo ausente | Usar `decisiones-tecnicas.md` §14.9 como especificación visual; sin JPG por ahora |
| 5 | `ColorSwitchTrackOn/Off` | **NO se crean en Fase 3** (no dejar tokens sin uso). Crear solo: `ColorSettingsHeaderBg` (#767676), `ColorSettingsBackBg` (#F2F2F2), `ColorSettingsBackIcon` (#2C2C2C), `ColorSettingsLabel` (#2D3142), `ColorDivider` (#D8D8D8), `ColorCardBorder` (#E6E6E6) |
| 6 | Omitir "Cambiar avatar" | Confirmado (depende de Fase 9); avatar estático 100dp sin link |

### 13.2 Condiciones de la auditoría a incorporar

1. **D-30 (`INVALID_REQUEST`):** como el cuerpo no trae `details`, definir un
   mensaje por defecto claro ("Solicitud inválida") en el mapper para no mostrar
   "Error de validación".
2. **`formatearFechaISO`** en `Validators.kt` con el mismo `ResolverStyle.STRICT`
   ya usado en el archivo (líneas 21-22).
3. **Tests adicionales (androidTest):**
   - "Cancelar cierra el Dialog sin guardar".
   - Verificación del 409 inline "Este nombre de usuario ya está en uso".
   - Si no existe `HomePlaceholderScreenTest`, crear uno mínimo que valide el
     nuevo botón "Mi cuenta".

### 13.3 Reglas recordadas por la aprobación

- Mapear errores por el campo `error` (central en `ErrorMapper`).
- Actualizar el `when` exhaustivo de `MensajeError.kt` (`mensajeUsuario` y
  `mapsTo`) al añadir `SesionExpirada` y `PerfilNoEncontrado`.
- Reutilizar `Validators.isValidNombreUsuario`, `UsuarioEnUso` y
  `CuentaInactiva` (no duplicar).
- **Cero dependencias nuevas** (no se tocan `build.gradle.kts` ni
  `libs.versions.toml`).
- Al terminar: `.\gradlew.bat :app:testDebugUnitTest --console=plain -q`
  (93 previos + ~30 nuevos verdes) y `.\gradlew.bat :app:assembleDebug`;
  reportar archivos tocados + valores + mensajes para que el propietario
  versione. Sin `git commit`.

---

## 14. Registro de implementación (2026-08-28)

Implementado capa por capa según este acta (regla §4.3), verificando cada capa
antes de avanzar. **Sin `git commit`.**

### 14.1 Archivos creados

| Archivo | Contenido |
|---|---|
| `repository/UserRepository.kt` | Interfaz `obtenerPerfil()` / `actualizarNombreUsuario()` |
| `repository/RemoteUserRepository.kt` | Impl `@Singleton` con wrapper `llamar`+`aEraError` (replicado de `RemoteAuthRepository`) |
| `ui/perfil/MiCuentaViewModel.kt` | `@HiltViewModel`, inyecta `UserRepository` + `SesionRepository` |
| `ui/perfil/MiCuentaUiState.kt` | `MiCuentaUiState` + `MiCuentaEvento` |
| `ui/perfil/MiCuentaScreen.kt` | Pantalla (§14.9) + `MiCuentaContent` + Dialog |
| `ui/components/SettingsHeader.kt` | Cabecera gris Settings reutilizable (D-28) |
| `ui/components/SettingsCard.kt` | Tarjeta + `SettingsCardRow` reutilizables (D-28) |
| `test/…/repository/UserRepositoryTest.kt` | MockWebServer (13 casos) |
| `test/…/ui/perfil/MiCuentaViewModelTest.kt` | VM (12 casos) |
| `androidTest/…/ui/perfil/MiCuentaScreenTest.kt` | UI pantalla |
| `androidTest/…/ui/login/HomePlaceholderScreenTest.kt` | UI botón "Mi cuenta" |

### 14.2 Archivos modificados

| Archivo | Cambio |
|---|---|
| `utils/EraError.kt` | + `SesionExpirada`, + `PerfilNoEncontrado` |
| `utils/ErrorMapper.kt` | + ramas `UNAUTHORIZED`, `NOT_FOUND`, `INVALID_REQUEST` (mensaje por defecto "Solicitud inválida") |
| `utils/MensajeError.kt` | + mensajes y `mapsTo` para `SesionExpirada`/`PerfilNoEncontrado` (when exhaustivo) |
| `utils/Validators.kt` | + `formatearFechaISO` (mismo `ResolverStyle.STRICT`) |
| `ui/theme/Color.kt` | + 6 tokens Settings (§14.10) |
| `ui/navigation/EraRoutes.kt` | + `PERFIL = "perfil"` |
| `ui/navigation/EraNavHost.kt` | + composable `PERFIL` + `onNavigatePerfil` en home |
| `ui/login/HomePlaceholderScreen.kt` | + botón "Mi cuenta" + parámetro `onNavigatePerfil` |
| `di/RepositoryModule.kt` | + `@Binds` de `UserRepository` |
| `test/…/utils/ErrorMapperTest.kt` | + ramas nuevas |
| `test/…/utils/ValidatorsTest.kt` | + `formatearFechaISO` |

### 14.3 Valores y mensajes literales aplicados

| Concepto | Valor |
|---|---|
| Ruta | `EraRoutes.PERFIL = "perfil"` |
| Título pantalla | "Mi Cuenta" |
| `SesionExpirada` | "Tu sesión expiró. Vuelve a iniciar sesión" |
| `PerfilNoEncontrado` | "No se pudo cargar el perfil" |
| `INVALID_REQUEST` por defecto | "Solicitud inválida" |
| 409 inline (reuso) | "Este nombre de usuario ya está en uso" |
| Validación cliente username | "3-60 caracteres, sin espacios" |
| Tokens Settings | `ColorSettingsHeaderBg` #767676, `ColorSettingsBackBg` #F2F2F2, `ColorSettingsBackIcon` #2C2C2C, `ColorSettingsLabel` #2D3142, `ColorDivider` #D8D8D8, `ColorCardBorder` #E6E6E6 |

### 14.4 Verificación

- `.\gradlew.bat :app:testDebugUnitTest --console=plain` → **123 verdes / 0 fallos**.
- `.\gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**.
- `.\gradlew.bat :app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL** (los
  androidTest compilan; su ejecución `connectedDebugAndroidTest` requiere
  emulador/dispositivo según DoD).
- Cero dependencias nuevas (no se tocó `build.gradle.kts` ni
  `libs.versions.toml`).

### 14.5 Pendiente de cierre

- Mover a "completada" en `CLAUDE.md` §10 y actualizar fecha/conteo.
- `decisiones-tecnicas.md` y `README.md` **no se tocan** hasta el cierre (regla
  §10).

### 14.6 Mejora visual (2026-08-28)

Ajuste **solo estético/organizativo** en `ui/components/SettingsCard.kt`,
`ui/components/SettingsHeader.kt` y `ui/perfil/MiCuentaScreen.kt`. Sin cambios
de lógica, datos, navegación, textos, Dialog de edición, `Color.kt` ni
dependencias. `testDebugUnitTest` sigue **123/123 verdes** y `assembleDebug`
BUILD SUCCESSFUL.

**Cambios aplicados:**
- **Filas con icono:** `SettingsCardRow` gana `icono`/`descripcionIcono` (círculo
  40dp `ColorPrimaryPale` + icono 20dp `ColorPrimary`) y `mostrarDivisor`
  (`HorizontalDivider` `ColorDivider`). Cada campo de Mi Cuenta usa su icono
  (Person/Email/Person/DateRange); la acción "Editar" pasa a ser un `TextButton`
  con icono de lápiz (área táctil ≥ 48dp).
- **Avatar montado en la tarjeta:** se reposiciona sobre el borde superior de la
  `SettingsCard` (offset 50dp fuera / 50dp dentro, `zIndex(1)`), con sombra y
  anillo blanco; el `Spacer(48dp)` interno evita que el texto quede cubierto.
- **Elevación:** la tarjeta gana `.shadow(2dp)` sutil manteniendo borde y padding.
- **Título centrado:** en `SettingsHeader`, el título queda centrado en el ancho
  completo de la cabecera (botón de retroceso fijo arriba-izquierda).
- **Espaciado:** se corrige el gap icono↔texto (usa `width`, no `height`) y se
  aumenta el interlineado (label↔valor) y el aire entre filas.

**Desviaciones de diseño (respecto a §13 y §14.9), aceptadas por el propietario:**
1. Label de fila en **`SemiBold`** (16sp) en vez de `Bold` según §14.9.
2. Título de cabecera **centrado en el ancho completo** en vez de junto al botón
   según §13.1.3.
3. `CampoPerfil` (§6.4) **no implementado**: el error de username se modela como
   `String` inline, no como estado por enum de campo.
