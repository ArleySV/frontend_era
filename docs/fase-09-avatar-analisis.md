# Fase 9 — Avatar personalizado (Módulo I): Análisis y Diseño

> Documento de análisis para la funcionalidad de avatar personalizado (subida y
> servido de la foto de perfil).
> Registra el alcance, el diseño visual, las decisiones técnicas y el plan de
> implementación.
> **Estado:** APROBADO PARA IMPLEMENTACIÓN (2026-08-31). Las 3 observaciones del
> integrante quedaron resueltas e incorporadas en este documento: obs. 1 → D-59
> (extracción de `AvatarSelector` + dependencias de colores, sección 8); obs. 2 →
> D-57/D-60 (firma pura de `AvatarFileValidator` + inyección de `ContentResolver`, sección 8);
> obs. 3 → §3.2. **Corrección de UI aplicada (2026-09-01, D-63):** el trigger pasa al toque
> directo sobre la foto del avatar y el selector se muestra en modal `AlertDialog` overlay
> (eliminado el link "Cambiar avatar" y el selector inline que se superponían a los campos de
> la tarjeta). Sin cambios de alcance ni dependencias nuevas.

---

## 1. Objetivo

Implementar la edición del avatar de perfil desde **"Mi cuenta"** (REQ-FUN-06 CA4,
CU-06 3a, HU-06): permitir al usuario autenticado subir una foto personalizada
(`PUT /users/me/avatar`, multipart ≤ 2 MB, `jpeg/png/webp`), reemplazar la existente
y volver a los avatares preestablecidos (presets locales, sin archivo en servidor).
Completa la carga del avatar personalizado con Coil que quedó *deferida* de la
Fase 3 (`docs/fase-03-perfil-analisis.md` D-27) y del documento de decisiones
(`docs/decisiones-tecnicas.md` §14.9: "Fase 9 (Module I / D-27 del acta fase-03):
aquí se implementará la carga con Coil de `GET /users/me/avatar` y el link 'Cambiar
avatar'").

## 2. Estado Actual (Auditoría)

| Componente | Hallazgo | Estado |
|---|---|---|
| `AvatarApi` | Ya declara `uploadAvatar(@Part avatar: MultipartBody.Part): Response<Unit>` y `getAvatar(): Response<ResponseBody>`. | ✅ Existe |
| `UsersApi` / `UserRepository` | `obtenerPerfil()/actualizarNombreUsuario()/eliminarCuenta()` ya implementados (Fases 3/6). | ✅ Existe |
| `UserProfile.avatar` | Campo `avatar: String?` — valores `preset:1\|2\|3`, `custom:*` o `null`. | ✅ Existe |
| Presets locales | `avatar_preset_1..3.jpg` en `drawable-nodpi/`. | ✅ Existen |
| `MiCuentaScreen` | Muestra el avatar según D-27 (`preset:*` → drawable local; `null` → iniciales). **Sin** "Cambiar avatar" ni carga `custom:*` con Coil (deferido a esta fase). | ⚠️ A completar |
| Selector de avatar | Solo existe el de **registro** (3 presets, sin botón "+"). Para Mi cuenta hace falta el "+" (decisión §13.9.2 / §14.12). | ⚠️ Extender |
| Coil | Solo `coil-compose` (Coil 3). **Sin** `coil-network-okhttp`. | ⚠️ Ver D-55 |
| Contrato Backend | `PUT/GET /api/v1/users/me/avatar` operativos y verificados (módulo-i-analisis.md). | ✅ Verificado |
| Prototipos JPG/PDF | No hay prototipos anexados en `docs/prototipos/` (pendientes). El diseño del selector y de Mi cuenta está documentado en `decisiones-tecnicas.md` §13.9.2 y §14.9. | ⚠️ Sin prototipo |

> **Nota (regla 11/13):** no existe prototipo JPG/PDF de esta pantalla concreta
> (la carpeta `docs/prototipos/` sigue vacía). Se adopta como referencia de diseño el
> especificado en `docs/decisiones-tecnicas.md`: el **avatar-selector** (§13.9.2,
> 3 presets + botón "+" solo en "Mi cuenta") y la tarjeta de **Mi cuenta** (§14.9,
> avatar 100dp con anillo + sombra). **Corrección D-63:** el token visual "link 'Cambiar
> avatar'" del §14.9, al sobreponerse a los campos en el flujo real, se reemplaza por el
> toque en la foto + selector `AlertDialog`. No se introducen nuevas
> librerías/patrones para reproducir el prototipo (regla 13): se reutiliza el
> selector de presets del registro (Fase 1) y los componentes `SettingsCard`/
> `SettingsCardRow` (Fase 3).

## 3. Requisitos / Casos de uso / Historias de usuario cubiertos

| Fuente | Ref. | Criterio / detalle |
|---|---|---|
| REQ-FUN-06 | CA4 | El avatar puede cambiarse seleccionando una imagen de la galería interna o cargando una desde el almacenamiento del dispositivo. |
| REQ-FUN-06 | CA5 | Solo `avatar` y `nombre de usuario` son editables; cualquier otro campo enviado se ignora. |
| CU-06 | 3a | El usuario puede cambiar su avatar seleccionando una imagen de la galería interna o del dispositivo. |
| HU-06 | — | ...editar mi nombre de usuario y avatar, para mantener actualizada mi identidad. |
| REQ-FUN-06 / CU-12 | Nota | El avatar personalizado **no** forma parte de la sincronización (CU-12); solo progreso, niveles, reintentos y comentarios. |
| `CLAUDE.md` §5 / módulo I §5 | — | La foto de un menor es dato sensible: GET siempre autenticado, sin URL pública, `Cache-Control: private, no-store`, **cero logs del binario**, y el archivo local temporal se limpia tras el `PUT` (exitoso o fallido). |

## 4. Contrato de API (verificado contra `BACKEND_ERA/docs/modulo-i-analisis.md` y `BACKEND_ERA/README.md`)

### 4.1 Subida — `PUT /api/v1/users/me/avatar` (multipart)

- **Autenticación:** `Authorization: Bearer <JWT sesión>` (adjunta el `JwtInterceptor`).
- **Request:** `multipart/form-data` con **una única parte** de archivo llamada `avatar`.
  - **Importantísimo (regla de contrato del backend, módulo I §3.1): la parte debe incluir
    `filename`**. El servidor Ktor entrega `FileItem` solo si `Content-Disposition` trae
    `filename`; sin él responde `400 "Se requiere un archivo."` sin recuperar el binario.
    Con OkHttp/Retrofit el `MultipartBody.Part.createFormData("avatar", filename, body)` añade
    `filename` automáticamente.
  - Validación cliente (regla 8) **antes de subir**: tamaño ≤ 2 MB y extensión/MIME en
    `jpeg/png/webp` — para no gastar cuota de red ni provocar 400 innecesario.
- **Response de éxito — 200 OK:**
  ```json
  { "message": "Avatar actualizado con éxito." }
  ```
- **Errores (mapear por el campo `error`, §7 cliente):**
  | Status | `error` | Cliente |
  |---|---|---|
  | 400 | `VALIDATION_ERROR` (con `details`: "Se requiere un archivo.", "Formato no permitido: jpeg, png o webp.", "Máximo 2 MB.") | mostrar `mensajeUsuario()` |
  | 401 | `UNAUTHORIZED` | cerrar sesión local (regla §5) → `SesionExpirada` |
  | 403 | `ACCOUNT_INACTIVE` | cerrar sesión local (regla §5) → `CuentaInactiva` |
  | 500 | `INTERNAL_ERROR` | error genérico |

### 4.2 Descarga — `GET /api/v1/users/me/avatar`

- **Autenticación:** obligatoria (sin URL pública).
- **Response de éxito — 200 OK:** binario (imagen). Headers `Content-Type` real,
  `Cache-Control: private, no-store`, `X-Content-Type-Options: nosniff`.
- **Errores:**
  | Status | `error` | Cliente |
  |---|---|---|
  | 404 | `NOT_FOUND` | el perfil usa `preset:*`/`null`, o el archivo no existe → **no es un error de la app**: el cliente solo pide el binario cuando `avatar` es `custom:*` (ver D-54). |
  | 401 | `UNAUTHORIZED` | cerrar sesión (regla §5) → `SesionExpirada` |
  | 403 | `ACCOUNT_INACTIVE` | cerrar sesión (regla §5) → `CuentaInactiva` |

> **Discrepancia anotada:** el resumen del `README.md` del backend dice
> `Content-Disposition: attachment` mientras el `modulo-i-analisis.md` §3.2 dice
> `inline; filename="avatar.<ext>"`. Irrelevante para el cliente (no usamos el
> `Content-Disposition` para mostrar la imagen; solo los bytes). No bloquea.

### 4.3 Formato del campo `avatar` (`UserProfile`)

| Valor | Significado | Rendering en cliente |
|---|---|---|
| `preset:1\|2\|3` | Avatar preestablecido local (sin archivo en servidor) | `painterResource(R.drawable.avatar_preset_<n>)` |
| `custom:<uuid>.<ext>` | Foto personalizada (archivo en servidor) | bytes de `GET /avatar` → Coil |
| `null` | Sin avatar | iniciales en 32sp Bold `ColorPrimary` |

## 5. Hallazgos Frontend/Backend

- **El GET /avatar requiere sesión** (sin URL pública). Al estar `AvatarApi.getAvatar()`
  dentro del `JwtInterceptor`, Retrofit ya adjunta el token automáticamente.
- **Coil 3 sin módulo de red:** `coil-compose` cargaría una URL sin adjuntar el JWT.
  Para no añadir `coil-network-okhttp` (dependencia nueva, regla 4) ni exponer el token,
  la estrategia es **obtener los bytes vía Retrofit** y pasarlos a Coil como modelo
  `ByteArray` (que Coil 3 soporta de forma nativa, sin red) — ver D-55.
- **Validación doble en cliente:** tamaño (regla 8: el backend fuerza ≤ 2 MB y whitelist
  `jpeg/png/webp` con magic bytes) + extensión/MIME. Se valida el tamaño leyendo el
  `Uri` sin cargar a memoria (ContentResolver + OpenAssetFileDescriptor), y el tipo
  por la extensión del nombre del archivo (heurística de cliente; el servidor re-verifica
  con magic bytes como autoridad).
- **El `PUT` no expone el nuevo valor** (`custom:*`) en la respuesta (mínimo privilegio).
  El cliente refresca vía un `GET /me` posterior (round-trip) o usa los bytes en memoria
  (D-54/D-56).

## 6. Diseño propuesto por capas (`remote/` → `repository/` → `ui/`)

### Capa 1 — `remote/` (mínimo, `AvatarApi` ya existe)

- **`AvatarApi` — ajuste de firma por D-62** (ver nota inferior). `uploadAvatar` y `getAvatar`
  declaran **tipo directo** (`uploadAvatar: Unit`, `getAvatar: ResponseBody`) en lugar de
  `Response<...>`.
- **DTOs — sin cambios.** No hay DTO serializable para el multipart (el backend lee por
  partes); la respuesta reutiliza `MessageResponse` (ya existe, `common/`).
- **`UserRepository.obtenerAvatarBytes()`** no toca `remote/dto` — ver Capa 2.

> **D-62 (2026-08-31, trazado en implementación):** el plan inicial (líneas 138-139) asumía
> `AvatarApi.getAvatar(): Response<ResponseBody>`. Con Retrofit, un tipo de retorno
> `Response<T>` **no lanza `HttpException`**: el código HTTP queda envuelto en la `Response`
> y el wrapper `llamar` + `aEraError` (patrón del proyecto) nunca llegaría a mapear los
> errores 400/401/403/404/500. Se corrigió la firma a **tipo directo** (`Unit` / `ResponseBody`)
> para que Retrofit lance `HttpException` en códigos de error, igual que `RemoteFeedbackRepository`
> con `sendComment`. El `404` del GET se mapea a `PerfilNoEncontrado` sin cerrar sesión (D-54).
> Los tests `AvatarRepositoryTest` (MockWebServer) cubren estos códigos.


### Capa 2 — `repository/`

Nuevo repositorio específico de avatar (cohesión con el patrón Feedback): **`AvatarRepository`**
(interfaz) + **`RemoteAvatarRepository`** (impl, `@Singleton`). Métodos:

- `suspend fun subirAvatar(uriBytes: ByteArray, filename: String, mimeType: String): Resultado<Unit>`
  - Construye `MultipartBody.Part.createFormData("avatar", filename, body.toRequestBody(mimeType))`.
  - Reutiliza el wrapper `llamar` + `aEraError` (mismo patrón que `RemoteUserRepository` /
    `RemoteFeedbackRepository`).
  - Ante 401/403 → `sesionRepository.limpiarToken()` (regla §5).
  - **No loguear el binario ni el `filename` real** (Regla de Oro del Módulo I §5).
- `suspend fun obtenerAvatarBytes(): Resultado<ByteArray>`
  - `llamar { api.getAvatar().bytes() }` (por D-62 Retrofit lanza `HttpException` en errores;
    `ResponseBody.bytes()` devuelve el binario).
  - Ante 401/403 → cierra sesión. El 404 `NOT_FOUND` **no** se cierra sesión: el cliente
    no debe llamar al GET si `avatar` no es `custom:*` (D-54), y si llega 404 es un caso
    defensivo que no amerita cerrar sesión.

La validación de tamaño/formato vive en un helper puro y testeable en JVM, sin dependencias
Android (`utils/AvatarFileValidator`, ver D-57): es el composable que lee la imagen quien lo
invoca (sobrecarga de bytes o de tamaño) y pasa el `Resultado` ya validado al ViewModel.

**DI (`RepositoryModule.kt`):** añadir `@Binds Abstract fun bindAvatarRepository(impl: RemoteAvatarRepository): AvatarRepository`.

### Capa 3 — `ui/`

#### 3.1 ViewModel — extender `MiCuentaViewModel`

Se **extiende el `MiCuentaViewModel` existente** (no se crea un VM nuevo): la edición de
avatar vive dentro de la misma pantalla "Mi cuenta". Se añade:

- Estados en `MiCuentaUiState`: `selectorAvatarAbierto: Boolean`, `subiendoAvatar: Boolean`,
  `bytesAvatarPersonalizado: ByteArray?`, `errorAvatar: EraError?` (y limpieza de campos).
- `onCambiarAvatarClick()` — abre el selector de avatar (reutiliza el dialogo/selector).
- `onSeleccionarPreset(id: Int)` — **vuelta a preset local**: no hay endpoint para borrar la
  foto personalizada (el PATCH de username no toca `avatar`); por tanto, "volver a preset"
  **solo es local** y no se persiste en servidor — ver D-58.
- `onSeleccionarFoto`/`onAvatarSeleccionado(validacion: Resultado<ArchivoAvatar>)` — el
  composable ya leyó del `Uri` y validó (tamaño/tipo, D-57); entrega el `Resultado` ya
  validado; en éxito el VM sube con `avatarRepository.subirAvatar(...)`; al éxito refresca
  el perfil (round-trip `GET /me`) y guarda `bytesAvatarPersonalizado` para el pre-render
  inmediato (D-56).
- `onCerrarSelector()`, `onLimpiarErrorAvatar()`.
- Manejo de 401/403 → `cerrarSesionPorReglaCinco()` (reutiliza el existente).

#### 3.2 UI — `MiCuentaScreen.kt`

- **Trigger de apertura (D-63):** el avatar 100dp de `AvatarPerfil` es **clickable**
  (`tag="avatarTrigger"`). El toque directo sobre la foto abre el selector. **No** existe un
  texto/enlace aparte "Cambiar avatar": el diseño original heredado del prototipo (§14.9,
  deferido por D-27 de Fase 3) fue corregido tras evidencia de bug de UI en emulador (el link
  sobrepuesto a "Nombre del menor") — ver D-63.
- **Selector de avatar en modal overlay (D-63):** se muestra como `AlertDialog` (Material 3)
  sobre toda la pantalla, con scrim que oscurece el resto del contenido; **no** se inserta
  inline en el flujo del scroll (el diseño original con `offset(y=112.dp)` se superponía a los
  campos de la tarjeta). Contiene el componente `AvatarSelector` (fila de 4 ítems de 49dp con
  gap 8dp — los **3 presets** reutilizando el `AvatarSelector` visual del registro (D-59) +
  el botón **"+"** (`ColorAvatarPlusBg` + icono `ColorAvatarPlusIcon` de `Color.kt`)) y un
  botón "Cerrar" (`onCerrarSelector`). Visible cuando `MiCuentaUiState.selectorAvatarAbierto`.
  Al cerrarse o seleccionar un preset el `AlertDialog` desaparece sin dejar residuos sobre el
  layout.
  - Sel preset → border 2.5dp `ColorPrimary` + halo sombra teal.
  - Sel "+" → lanza el picker de imagen del sistema
    (`rememberLauncherForActivityResult`, `ActivityResultContracts.PickVisualMedia`).
- **Errores (`errorAvatar`):** ya no se dibujan inline bajo la tarjeta; se muestran como
  `Snackbar` en el host de `MiCuentaScreen` y se limpian con `onLimpiarErrorAvatar()`.
- **Carga del avatar personalizado en `AvatarPerfil`:** si `avatar` es `custom:*`, se muestra
  con `AsyncImage` (Coil) usando como modelo los `bytesAvatarPersonalizado` (o los bytes
  obtenidos al entrar con `custom:*`). Presets/iniciales siguen como hoy.
- Limpieza del archivo local temporal tras el `PUT` (exitoso o fallido) — regla §5.

> **Resolución obs. 3 (acceso al selector de avatar) — reemplazada por D-63 (2026-09-01):**
> el requisito original era la visibilidad **"siempre"** de un link "Cambiar avatar" bajo el
> avatar. En pruebas en emulador se evidenció un bug de UI: el link (y el selector inline en
> `offset(y=112.dp)`) se superponían a "Nombre del menor"/"Correo electrónico", dejando el
> texto ilegible. **Decisión D-63:** el trigger pasa a ser el **toque directo sobre la foto
> del avatar** (`AvatarPerfil` clickable, `tag="avatarTrigger"`, visible **en cuanto el perfil
> esté cargado**, con `avatar` `preset:*`, `custom:*` o `null` — conserva REQ-FUN-06 CA4/CA5),
> y el selector se implementa como **`AlertDialog` overlay** que no afecta el layout. Con esto
> los tests de UI/VM `CambiarAvatarTest` verifican: con perfil cargado, `avatarTrigger` existe
> y al pulsarlo `MiCuentaUiState.selectorAvatarAbierto == true`; **sin** perfil cargado
> (`perfil == null`) el `avatarTrigger` NO está presente; y con `selectorAvatarAbierto == true`
> el modal muestra presets y "+" en un overlay sin superposiciones.

#### 3.3 Navegación

- No hay nueva ruta de navegación: la edición de avatar ocurre en la pantalla "Mi cuenta"
  existente (`EraRoutes.PERFIL`). El toque sobre el avatar no navega a otra pantalla: abre el
  `AlertDialog` modal del selector sobre la misma pantalla.

## 7. Dependencias nuevas

**Ninguna.** Justificación explícita:

- `coil-compose` (Coil 3) ya está declarado (`libs.versions.toml`) y se reutiliza para el
  render de los bytes (`AsyncImage` con modelo `ByteArray`).
- **No** se añade `coil-network-okhttp`: la imagen se obtiene por Retrofit (que ya adjunta
  el JWT vía `JwtInterceptor`) y se pasa a Coil como `ByteArray` — sin dependencia nueva
  (regla 4).
- **No** se añade `activity-compose` de más ni librería de picker: se usa
  `rememberLauncherForActivityResult` con `ActivityResultContracts` / `PickVisualMedia` del
  propio AndroidX (ya disponible en el stack; `activity-compose` 1.13 ya declarada).
- `MultipartBody.Part` / `RequestBody` / `MediaType` vienen de OkHttp (ya declarado).

## 8. Decisiones técnicas (continúa D-53 → D-54…)

| ID | Decisión | Justificación |
|---|---|---|
| **D-54** | El cliente solo pide `GET /avatar` cuando `UserProfile.avatar` es `custom:*`; si es `preset:*`/`null` se dibuja el asset local / iniciales (no se hace el GET). | El backend devuelve `404 NOT_FOUND` precisamente cuando NO hay foto personalizada (módulo I §4.1); pedir el binario con `preset:*` sería un error de contrato. El campo `avatar` es la fuente autoritativa (D-27 de Fase 3). |
| **D-55** | La foto personalizada se obtiene con Retrofit (`GET /avatar`, el `JwtInterceptor` adjunta el token) y se muestra con Coil pasando los **bytes** (`ByteArray`) como modelo, no una URL. | Coil 3 sin `coil-network-okhttp` no adjuntaría el `Authorization` a una URL, y el GET no tiene URL pública (requiere sesión). Traer los bytes por Retrofit evita exponer el token y **no requiere dependencia nueva** (regla 4): Coil 3 renderiza `ByteArray` de forma nativa. |
| **D-56** | Tras un `PUT` exitoso, el cliente usa **los bytes ya en memoria** para el render inmediato y además refresca el perfil con `GET /me` (round-trip) para sincronizar el nuevo `avatar: custom:*`. | El `PUT` no devuelve el nuevo valor (mínimo privilegio, módulo I §3.1). Los bytes en memoria dan UX inmediata; el `GET /me` actualiza el valor `custom:*` persistido (fuente autoritativa). |
| **D-57** | Validación de avatar en un helper puro `utils/AvatarFileValidator` (tamaño ≤ 2 MB, formato `jpeg/png/webp`) usado en ViewModel **y** repositorio. | Regla 8: validar en cliente todo lo que el backend valida (≤ 2 MB y whitelist), sin reemplazar la validación de servidor (magic bytes). Centralizarlo evita duplicar y facilita tests unitarios puros. |
| **D-58** | "Volver a un preset" es **local y no se persiste** (máximo: se limpia el `bytesAvatarPersonalizado` y se dibuja el drawable). | El backend **no expone** un endpoint para borrar/resetear la foto personalizada a preset (el `PATCH /me` no toca `avatar`; el reset a preset está fuera de alcance del Módulo I §2.4). Implementar esa lógica sería un cambio de alcance del backend (aviso, regla 10): se señalará al Auditor/equipo. El `avatar` del servidor seguirá `custom:*` tras "volver a preset" hasta que exista ese endpoint. |
| **D-59** | El selector de avatar ampliado se implementa como evolución reutilizable: se extrae la lógica visual del `AvatarSelector` de presets (Fase 1, `RegistroPaso2Screen`) a un componente compartido, y en "Mi cuenta" se le añade el botón "+" y el estado de selección por `avatar` actual. | Regla 13j: reutilizar componentes/patrones existentes antes de crear nuevos. El selector del registro NO muestra "+" (el backend no acepta foto custom sin sesión), coherente con §13.9.2/§14.12. |
| **D-60** | La subida usa `rememberLauncherForActivityResult` con `ActivityResultContracts.PickVisualMedia` (o `GetContent` como fallback) y lee las bytes vía `ContentResolver`. | Mecanismo estándar de AndroidX para seleccionar imagen de galería/almacenamiento (REQ-FUN-06 CA4), sin permisos peligrosos en Android 13+ y sin dependencias nuevas. |
| **D-61** | Cero logs del contenido del avatar: ni el binario, ni el `filename`, ni la URI. Solo se loguean (si acaso) códigos/estados genéricos sin datos. | Regla de Oro del Módulo I §5 y CLAUDE.md §5: la foto de un menor es dato sensible; el `filename` del cliente es potencialmente identificativo. |
| **D-63** | **Corrección de UI de Fase 9 (2026-09-01):** (a) el trigger de apertura del selector pasa de un link "Cambiar avatar" bajo la foto al **toque directo sobre `AvatarPerfil`** (clickable, `tag="avatarTrigger"`); (b) el selector se muestra como **`AlertDialog` overlay** (scrim sobre toda la pantalla, botón "Cerrar") en lugar de inline en el flujo del scroll; (c) `errorAvatar` se reporta por **`Snackbar`** y no inline bajo la tarjeta. | Evidencia en emulador: el link (en `offset(y=112.dp)`) y el selector inline se superponían a "Nombre del menor"/"Correo electrónico" (texto ilegible y contenido tapado). El `AlertDialog` no altera el layout ni deja residuos; el toque en la foto es el patrón esperado por REQ-FUN-06 CA4/CA5 (diseño §14.9). Mantiene la visibilidad "en cuanto hay perfil cargado" (obs. 3) sin enlace adicional. |

> **Resolución obs. 1 (D-57 & D-60 — firma del validador y flujo del picker):**
> - **Firma pura** (sin instrumentación, permite tests unitarios JVM, **no suspendida**):
>   `object AvatarFileValidator` con dos sobrecargas no suspendidas:
>   1. `fun validar(bytes: ByteArray, filename: String?, mimeType: String?): Resultado<ArchivoAvatar>`,
>      donde `ArchivoAvatar(bytes: ByteArray, filename: String?, mimeType: String?)`.
>      - Valida `mimeType`: solo acepta `image/jpeg`, `image/png`, `image/webp` (whitelist
>        regla 8); si no, `EraError.Validacion("Formato no soportado")`.
>      - Valida `bytes.size`: si `> MAX_BYTES_AVATAR (2 MB = 2 * 1024 * 1024)` →
>        `EraError.Validacion("... >2 MB")`. Es **fallo descriptivo**, no excepción (regla 3).
>      - Solo tras pasar ambas acepta y devuelve `Resultado.Exito(ArchivoAvatar)`.
>   2. `fun validar(size: Long, mimeType: String?): Resultado<ArchivoAvatar>` (sobrecarga para
>      validar **sin bytes**, añadida 2026-08-31 en la revisión de Fase 9): valida por tamaño y
>      MIME cuando el archivo supera el límite. En ese caso **no se lee el binario** (D-57) y
>      **no se fabrica un `ByteArray` sintético de 2 MB** (optimización de memoria); el "éxito"
>      teórico con `ByteArray(0)` no se da, porque un tamaño > MAX siempre produce fallo.
> - **Inyección de `ContentResolver` (refactor aprobado por el Auditor 2026-08-31):** el
>   plan original fijaba `validar(uri: Uri, resolver: ContentResolver)`, pero el proyecto de
>   test **no tiene Robolectric ni MockK** y la regla 4 prohíbe añadir dependencias sin
>   aprobación, lo que impedía testear el validador/VM en JVM con un `ContentResolver` real.
>   Desviación técnica de integración aprobada: **la lectura del `Uri`→bytes ocurre en el
>   composable** (capa UI, donde `LocalContext.current.contentResolver` está disponible,
>   mismo patrón que `ProgresoScreen.kt:50`) usando `openAssetFileDescriptor` (tamaño vía
>   `length` **sin cargar binario**) → `openInputStream` (bytes solo si el tamaño está dentro
>   del límite) → `OpenableColumns.DISPLAY_NAME` (filename) → tipo de `getType(uri)`. El
>   composable valida con `AvatarFileValidator` (sobrecarga de bytes, o de tamaño si el
>   archivo excede el límite) y pasa el `Resultado<ArchivoAvatar>` ya validado al ViewModel.
>   Con esto **no se inyecta `ContentResolver` al ViewModel** y el flujo de negocio (manejo
>   del resultado de validación → subida → refresh) es testeable en JVM puro.
> - **ViewModel:** `onAvatarSeleccionado(validacion: Resultado<ArchivoAvatar>)` — el VM ya no
>   valida bytes (eso ocurre en la capa que lee la imagen); ante `Fallo` setea `errorAvatar` y
>   cierra el selector; ante `Exito` llama a `subirAvatar(bytes, filename, mime)`.
> - **Flujo completo (D-63):** tap en la **foto del avatar** (`avatarTrigger`) → abre el
>   `AlertDialog` modal con `AvatarSelector` → tap "+" (D-59)
>   → `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia)` devuelve
>   `Uri?` (nulo si el usuario cancela → no hace nada) → el composable lee del `Uri` y valida
>   → pasa el `Resultado` a `onAvatarSeleccionado(...)` → `PUT` → refresh `GET /me`. Errores
>   de validación se muestran vía `errorAvatar` → `Snackbar`, sin navegación extra y sin
>   residuos en el layout.
> - **Testabilidad del validador (JVM, sin emulador):** el test llama `validar` con
>   bytearrays/filenames/mimes (y con la sobrecarga de `size`) preparados → asevera cada rama
>   (exitoso, >2 MB, mime no soportado). El VM se testea con fakes de repositorio (patrón
>   `MiCuentaViewModelTest`). Coherente con la regla de no añadir dependencias nuevas.

> **Resolución obs. 2 (D-59 — viabilidad de extracción confirmada):**
> - Verificado en el código: `AvatarSelector` es una `private fun` del propio
>   `RegistroPaso2Screen.kt` (línea 273) y su **único llamador** es esa misma pantalla
>   (línea 194). No hay otros usos en el árbol de UI → la extracción es **segura** y no
>   rompe el registro mientras se conserve la misma firma visual y el mismo contrato de
>   datos (lista de presets + `onSeleccionar(presetId)`).
> - **Firma del componente compartido** `ui/components/avatar/AvatarSelector.kt`:
>   `@Composable fun AvatarSelector(presets: List<Int>, seleccionado: Int?, onSeleccionar: (Int) -> Unit, mostrarMas: Boolean = false, onMas: (() -> Unit)? = null)`.
>   En el registro se invoca con `mostrarMas = false` y `onMas = null` → comportamiento
>   byte-for-byte igual al actual (regresión visual cero). En "Mi cuenta" se invoca con
>   `mostrarMas = true` + `onMas` para abrir el picker (D-60).
> - **Dependencias de colores** (todas ya en `ui/theme/Color.kt`, coherentes con el patrón
>   `SettingsCard` de Fase 3 de leer colores del tema y referenciar tokens por nombre):
>   `ColorAvatarPreset1`, `ColorAvatarPreset2`, `ColorAvatarPreset3` (fondos de presets),
>   `ColorAvatarBorderDefault` (borde no seleccionado), `ColorPrimary` (borde seleccionado
>   2.5dp + halo), `ColorAvatarPlusBg` + `ColorAvatarPlusIcon` (botón "+"). El componente
>   extraído lee estos tokens de `MaterialTheme` igual que hoy, sin introducir colores duros
>   ni dependencias nuevas.

## 9. Lista de archivos a crear / modificar

### Crear
| Archivo | Capa | Contenido |
|---|---|---|
| `app/src/main/java/com/era/app/repository/AvatarRepository.kt` | repository | Interfaz: `subirAvatar(bytes, filename, mime)`, `obtenerAvatarBytes()`. |
| `app/src/main/java/com/era/app/repository/RemoteAvatarRepository.kt` | repository | Impl Retrofit (wrapper `llamar` + `aEraError`, limpieza sesión ante 401/403, multipart con `filename`). |
| `app/src/main/java/com/era/app/utils/AvatarFileValidator.kt` | utils | Helper puro de datos (JVM, sin dependencias Android): `validar(bytes, filename, mime)` y sobrecarga `validar(size, mime)` (sin bytes) → `Resultado<ArchivoAvatar>` (≤ 2 MB, `jpeg/png/webp`). |
| `app/src/main/java/com/era/app/ui/components/avatar/AvatarSelector.kt` *(o dentro del paquete perfil)* | ui | Selector reutilizable 3 presets + botón "+" (D-59). |
| `app/src/test/java/com/era/app/repository/AvatarRepositoryTest.kt` | test | Tests MockWebServer del repositorio. |
| `app/src/test/java/com/era/app/utils/AvatarFileValidatorTest.kt` | test | Tests del validador. |
| `app/src/androidTest/java/com/era/app/ui/perfil/CambiarAvatarTest.kt` *(opcional/instrumentado)* | androidTest | Prueba de interacción de "Cambiar avatar". |

### Modificar
| Archivo | Cambio |
|---|---|
| `app/src/main/java/com/era/app/di/RepositoryModule.kt` | `@Binds` de `AvatarRepository` → `RemoteAvatarRepository`. |
| `app/src/main/java/com/era/app/ui/perfil/MiCuentaUiState.kt` | Añadir `selectorAvatarAbierto`, `subiendoAvatar`, `bytesAvatarPersonalizado`, `errorAvatar` (y limpieza). |
| `app/src/main/java/com/era/app/ui/perfil/MiCuentaViewModel.kt` | Inyectar `AvatarRepository`; métodos: `onCambiarAvatarClick`, `onSeleccionarPreset`, `onAvatarSeleccionado(Resultado<ArchivoAvatar>)`, `onCerrarSelector`, `onLimpiarErrorAvatar`, carga de `custom:*` al entrar. |
| `app/src/main/java/com/era/app/ui/perfil/MiCuentaScreen.kt` | Avatar clickable (`avatarTrigger`); reemplazo de `AvatarPerfil` para `custom:*` con Coil (D-55); selector en `AlertDialog` modal (D-63); integración del picker de imagen (D-60); `errorAvatar` → snackbar. |
| `app/src/test/java/com/era/app/ui/perfil/MiCuentaViewModelTest.kt` | Fakes de `AvatarRepository` + nuevos tests del flujo avatar. |
| `app/src/main/java/com/era/app/remote/api/AvatarApi.kt` | D-62: firmas a tipo directo (`uploadAvatar(): Unit`, `getAvatar(): ResponseBody`) en lugar de `Response<T>`. |

### Sin cambios (confirmado)
- `remote/dto/*` — no hay DTO nuevo (respuesta `MessageResponse` ya existe).
- `build.gradle.kts` / `libs.versions.toml` — **sin dependencias nuevas** (D-61/§7).
- `EraRoutes.kt` / `EraNavHost.kt` — no hay nueva ruta (edicción dentro de "Mi cuenta").
- `ErrorMapper.kt` / `EraError.kt` / `MensajeError.kt` — **sin cambios**: los códigos
  `VALIDATION_ERROR` (→ `Validacion`), `UNAUTHORIZED` (→ `SesionExpirada`),
  `ACCOUNT_INACTIVE` (→ `CuentaInactiva`), `NOT_FOUND` (→ `PerfilNoEncontrado`),
  `INTERNAL_ERROR` (→ `ErrorServidor`) ya están mapeados (D-02/D-03).

## 10. Testing previsto

1. **Unitarios `AvatarFileValidatorTest`:** acepta ≤ 2 MB `jpeg/png/webp`; rechaza > 2 MB;
   rechaza formato no permitido (`gif`, `pdf`, sin extensión); valida MIME/extensiones.
2. **`AvatarRepositoryTest` (MockWebServer):**
   - `subirAvatar` éxito → `Resultado.Exito(Unit)`; verifica que el request multipart envía
     la parte con `filename` presente (contrato backend, §4.1).
   - `subirAvatar` 400 `VALIDATION_ERROR` → `Fallo(Validacion)`; 401 → `SesionExpirada` +
     limpia token; 403 → `CuentaInactiva` + limpia token; 500 → `ErrorServidor`.
   - `obtenerAvatarBytes` 200 → `Exito(bytes)`; 401/403 → limpia sesión; 404 → `Fallo(PerfilNoEncontrado)` **sin** limpiar sesión (D-54).
3. **`MiCuentaViewModelTest`:** abrir/cerrar selector; seleccionar preset (local, D-58);
   seleccionar foto pasando validator (tamaño/tipo); `subirAvatar` éxito → refresco perfil +
   bytes en memoria (D-56); fallo de red → `errorAvatar`; 401/403 → `NavegarALogin`.
4. **Instrumentado (`androidTest` `CambiarAvatarTest`):** con perfil cargado el
   `avatarTrigger` está visible y al pulsarlo abre el selector (modal); **sin** perfil el
   `avatarTrigger` no existe; con `selectorAvatarAbierto=true` el modal muestra presets y "+"
   (overlay); selección de preset marca el callback; el flujo "+" invoca el picker (el "1, 2,
   3" de `AvatarSelector` no debe quedar residual sobre el resto de la pantalla).

**Criterio de éxito de la fase (regla §6):** tests unitarios de VM/Repository, tests de UI y
tests de integración con MockWebServer antes de avanzar; **0 regresiones** en los 198 unitarios
y 69 instrumentados previos.

## 11. Definition of Done (Fase 9)

- Foto personalizada subible con `PUT /users/me/avatar` (multipart con `filename`, ≤ 2 MB,
  `jpeg/png/webp`) validada en cliente antes de subir.
- Foto personalizada visible en "Mi cuenta" (Carga con Coil de `GET /avatar`, autenticado).
- Selector de avatar en "Mi cuenta" accesible por toque en la foto, en **modal overlay**
  (sin superposiciones ni residuos en el layout) con 3 presets + botón "+" (D-63, diseño
  §13.9.2).
- Manejo de 401/403 (cierre local de sesión) y de 404 defensivo.
- Cero logs del binario/`filename`/URI; archivo temporal local limpio tras el `PUT`.
- Sin dependencias nuevas.
- Documentada la limitación D-58 (volver a preset es local, no persiste) para Auditor/equipo.

## 12. Bloqueo / límite de alcance a reportar

- **Volver a preset no persiste en servidor (D-58):** el backend no expone un endpoint para
  borrar la foto personalizada y volver a `preset:*`. Implementar esa persistencia sería un
  cambio de backend, fuera del alcance del frontend (regla 10). Se deja la opción local
  (visual) y se reporta al Auditor/equipo para decidir si se solicita el endpoint al backend
  o se acepta la limitación.
