# FRONTEND_ERA — Cliente Android de ERA

**ERA (Educación, Repaso y Aprendizaje)** es una aplicación Android nativa de trivia
educativa para niños de básica primaria (**7 a 11 años**), compatible desde
**Android 8.0** (`minSdk` 26), con mediación parental en el registro y autorización
del acudiente para eliminar la cuenta.

Este repositorio es **solo el cliente Android**. Consume vía REST al backend
`BACKEND_ERA` (Ktor + MySQL, ya completo y verificado: 16 endpoints en 6 grupos
funcionales). El backend **no sirve contenido de juego**: las preguntas de trivia,
la FAQ y los avatares preestablecidos viven exclusivamente en el cliente.

---

## Arquitectura

**Offline-first + MVVM.** El juego, el catálogo de 20 niveles, la FAQ y los 3
avatares preestablecidos son 100% locales (Room/SQLite). El backend solo participa
en: autenticación, verificación por OTP, recuperación de contraseña, gestión/
eliminación de cuenta, sincronización de progreso, envío de comentarios y avatar
personalizado.

De los 14 módulos de UX (REQ-FUN-01…14), **7 hablan con el backend** y el resto es
lógica de cliente. Ver la matriz completa en `app/CLAUDE.md` §9.

## Stack técnico

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2 |
| UI | Jetpack Compose (Material 3) |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| DI | Hilt |
| Red | Retrofit 2.12 + OkHttp 4.12 (+ interceptor JWT automático) |
| Serialización | kotlinx.serialization (mismo serializador que el backend) |
| Persistencia local | Room 2.8 (progreso, niveles, ajustes, sesión cacheada) |
| Seguridad | Android Keystore / `EncryptedSharedPreferences` para el JWT |
| Navegación | Navigation Compose 2.9 |
| Imágenes | Coil 3 |
| Testing | JUnit, MockWebServer (Retrofit), Compose Testing / Espresso |

Las decisiones completas (qué se eligió, por qué y qué se descartó) están en
[`docs/decisiones-tecnicas.md`](docs/decisiones-tecnicas.md).

## Estructura del proyecto

```
com.era.app/
  data/        ← Room (entities, DAOs, database) — pendiente (Fase 7)
  remote/      ← Retrofit: API interfaces + DTOs (mismos nombres que el backend)
    api/       ← AuthApi, UsersApi, ProgressApi, FeedbackApi, AvatarApi
    dto/       ← auth, user, progress, feedback, common
    JwtInterceptor.kt
  repository/  ← orquesta Room + Remote — pendiente
  ui/          ← theme implementado; pantallas por módulo — pendiente
  di/          ← NetworkModule (Hilt)
  utils/       ← TokenManager (JWT cifrado); validators — pendiente
```

## Estado actual

**Fase 0, 1, 2, 3, 4, 5, 6, 7 y 8 completadas.** Detalle del avance fase por fase:

| Fase | Módulo | Estado |
|---|---|---|
| 0 | Preparación del entorno | ✅ Completada |
| 1 | Registro + Verificación OTP | ✅ Completada (2026-08-23) |
| 2 | Login | ✅ Completada (2026-08-27) |
| 3 | Perfil / Mi cuenta | ✅ Completada (2026-08-28) |
| 4 | Logout | ✅ Completada (2026-08-29) |
| 5 | Recuperación de contraseña | ✅ Completada (2026-08-30) |
| 6 | Eliminar cuenta | ✅ Completada (2026-08-30) |
| 7 | Progreso / Sync (Room + merge offline-first) | ✅ Completada (2026-08-30) |
| 8 | FAQ + Comentarios | ✅ Completada (2026-08-30) |
| 9 | Avatar personalizado | ✅ Completada (2026-08-31; fix UI D-63 2026-09-01) — siguiente: Fase 10 |
| 10 | Pantallas transversales (Splash, Sidebar, Home, Niveles, Juego, Ajustes, FAQ) | ⬜ Pendiente |

Implementado hasta ahora:

- Proyecto Gradle (Kotlin DSL) con todas las dependencias base declaradas.
- Contrato remoto completo: 5 interfaces Retrofit y ~25 DTOs que espejan el backend,
  verificados contra los DTOs reales (camelCase nativo sin `@SerialName`,
  auditoría 2026-08-23).
- `TokenManager`: JWT y correo del usuario en `EncryptedSharedPreferences` (nunca en texto plano).
- `JwtInterceptor`: adjunta `Authorization: Bearer <token>` automáticamente.
- `NetworkModule` (Hilt): OkHttpClient + Retrofit + Json + provisión de las 5 APIs;
  logging HTTP nivel `BASIC` en debug (nunca loguear cuerpos con contraseña/OTP).
- Tema Compose: tokens ERA aplicados (paleta, tipografía y radios de
  `docs/decisiones-tecnicas.md` §10; `dynamicColor` desactivado).
- **Fase 1 — Registro + Verificación OTP:** pantallas (`RegistroPaso1/2/3`,
  `VerificarEmail`, `RegistroExitoso`), ViewModels, componentes COMMON y navegación;
  74 tests verdes.
- **Fase 2 — Login:** pantalla de login, `LoginViewModel`, `HeroLogin` con los 3 SVGs
  decorativos (`signo_igual`, `signo_abc123`, `signomas`), `LoginInputPill`, `LoginButton`;
  93 tests verdes.
- **Fase 3 — Perfil / Mi cuenta:** pantalla "Mi Cuenta" (`MiCuentaScreen`) con `GET /me`
  (5 campos) y Dialog de edición de username (`PATCH /me`, 409 inline); `MiCuentaViewModel`,
  `UserRepository` + `RemoteUserRepository`; componentes reutilizables `SettingsHeader`,
  `SettingsCard`/`SettingsCardRow`; manejo de 401/403 (cierre silencioso de sesión) y
  404/`INVALID_REQUEST`; mejora visual 2026-08-28 (filas con icono+divisor, avatar sobre la
  tarjeta, título centrado); **123 tests verdes** (unitarios) e instrumentados **45/45** en
  físico ABR-LX3 (2026-08-29).
- **Fase 4 — Logout:** cierre de sesión con diálogo de confirmación ("¿Deseas cerrar sesión?"
  → "Sí, cerrar sesión"/"Cancelar", con anti doble-tap y diálogo no-descartable mientras
  confirma; `popUpTo(0){inclusive=true}`); semántica best-effort D-32 (se limpia el token
  local y se navega a Login ante CUALQUIER resultado del POST, validado también con backend
  apagado); `AuthRepository.logout()` en repository y `HomePlaceholderUiState` +
  `HomePlaceholderViewModel` evolucionado (AuthRepository + SesionRepository inyectados);
  **138 tests verdes** (unitarios) e instrumentados **47/47** en físico ABR-LX3 (2026-08-29).
- **Fase 5 — Recuperación de contraseña:** flujo de 3 pasos (Email -> OTP -> Nueva clave),
  `resetToken` solo en memoria (D-37), anti-enumeración en UI (D-42); **169 tests verdes**
  (2026-08-30).
- **Fase 6 — Eliminar cuenta:** pantalla `EliminarCuentaScreen` con re-verificación de
  contraseña, diálogo de confirmación, limpieza de sesión atómica e integración en "Mi Cuenta"
  (Sección Seguridad integrada en tarjeta principal); **181 tests verdes** (unitarios) e
  instrumentados **60/60** en físico ABR-LX3 (2026-08-30).
- **Fase 7 — Progreso / Sync:** Room configurado (`EraDatabase`), catálogo oficial de 20
  niveles (seed JSON), lógica offline-first con merge determinista, aislamiento por usuario
  (PK por correo) y pantalla de progreso con barra animada; **189 tests verdes** (unitarios)
  e instrumentados **64/64** en físico ABR-LX3 (2026-08-30).
- **Fase 8 — FAQ + Comentarios:** Sección informativa offline (8 preguntas) con tarjetas
  expandibles y canal de sugerencias online (máx 2000 caracteres); implementación de
  recuperación automática de Keystore en `TokenManager`; **198 tests verdes** (unitarios)
  e instrumentados **69/69** en físico ABR-LX3 (2026-08-30). *Revisión 2026-08-31: guard
  de longitud de comentario a 2000 exacto (sin estado residual 2001–2100), fallo de lectura
  de FAQ local ya no se mapea a `ErrorConexion` (se muestra texto de error en pantalla), y
  trazado en CLAUDE.md del cambio de alcance `MasterKeys`→`MasterKey.Builder` en
  `TokenManager`.*
- **Fase 9 — Avatar personalizado (Módulo I):** implementación **completada (2026-08-31)**.
  Subida por picker (`PickVisualMedia` con fallback) y presets locales (D-58), validación
  pura `AvatarFileValidator` (≤ 2 MB, `jpeg/png/webp`), descarga `GET /users/me/avatar`
  renderizada con Coil (D-55), `AvatarSelector` compartido (D-59), cero logs de binario/
  filename (D-61). Se trazó **D-62**: la firma de `AvatarApi` pasó a tipo directo
  (`uploadAvatar(): Unit`, `getAvatar(): ResponseBody`) porque `Response<T>` no lanza
  `HttpException` y rompía el patrón `llamar`+`aEraError`. **226 tests verdes** (unitarios,
  +28 nuevos: `AvatarFileValidatorTest`, `AvatarRepositoryTest` MockWebServer,
  `MiCuentaViewModelTest`). Instrumentados `CambiarAvatarTest` (7) pendientes de ejecutar
  en dispositivo.
  **Corrección de UI (2026-09-01, D-63):** el trigger pasó del link "Cambiar avatar" al
  **toque directo sobre la foto** (`avatarTrigger`) y el selector se muestra como
  **`AlertDialog` modal overlay** (título "Elegir un buen avatar", presets + "+", botón
  "Cerrar") — en el emulador el link y el selector inline se superponían a "Nombre del
  menor"/"Correo electrónico". `errorAvatar` ahora se reporta por `Snackbar`. Solo se tocó
  la capa UI y el `CambiarAvatarTest`; build y 226 unitarios BUILD SUCCESSFUL.

## Compilar y ejecutar

Requisitos: Android Studio (Narwhal o superior recomendado), JDK 17, SDK 37
(compileSdk; targetSdk 36).

```bash
# Windows
gradlew.bat assembleDebug

# Linux/macOS
./gradlew assembleDebug
```

> **Configuración:** `NetworkModule.BASE_URL` apunta al backend local de
> `BACKEND_ERA` (`http://192.168.20.64:8080/api/v1/`, host en la LAN).

OTP de desarrollo del backend: código fijo `123456` (verificar correo y pasos 2/3
de recuperación de contraseña).

## Documentación

| Archivo | Contenido |
|---|---|
| [`app/CLAUDE.md`](app/CLAUDE.md) | Reglas permanentes de trabajo, contrato de API, matriz de trazabilidad y estado del repo (**lectura obligatoria**) |
| [`docs/decisiones-tecnicas.md`](docs/decisiones-tecnicas.md) | Decisiones de arquitectura y dependencias + diseño visual completo (tokens, componentes y las 16 pantallas) |
| [`docs/requisitos-funcionales.md`](docs/requisitos-funcionales.md) | REQ-FUN-01…14 (copia sincronizada del backend) |
| [`docs/requisitos-no-funcionales.md`](docs/requisitos-no-funcionales.md) | REQ-NF-01…06 (copia sincronizada del backend) |
| [`docs/casos-de-uso.md`](docs/casos-de-uso.md) | CU-01…12 (copia sincronizada del backend) |
| [`docs/historias-de-usuario.md`](docs/historias-de-usuario.md) | HU-01…15 (copia sincronizada del backend) |
| [`docs/fase-01-registro-analisis.md`](docs/fase-01-registro-analisis.md) | Análisis y diseño de la Fase 1 — Registro + Verificación OTP |
| [`docs/fase-02-login-analisis.md`](docs/fase-02-login-analisis.md) | Análisis y diseño de la Fase 2 — Login |
| [`docs/fase-03-perfil-analisis.md`](docs/fase-03-perfil-analisis.md) | Análisis y diseño de la Fase 3 — Perfil / Mi cuenta |
| `docs/prototipos/` | JPG/PDF de diseño — *pendiente de anexar* (diseño documentado en `decisiones-tecnicas.md`) |

El contrato completo de los 16 endpoints vive en el `README.md` del backend
(`BACKEND_ERA`). Ante cualquier duda de formato de request/response, consultar ese
archivo antes de asumir un DTO.

## Reglas de trabajo

Cualquier contribución (humana o de IA) debe seguir las reglas de
[`app/CLAUDE.md`](app/CLAUDE.md) §4. Resumen:

1. Trabajar módulo por módulo, capa por capa (`remote/` → `repository/` → `ui/`).
   Nunca generar el proyecto completo de una sola vez.
2. Plan previo + confirmación explícita antes de crear o modificar más de un archivo.
3. No agregar dependencias sin justificación y aprobación previa.
4. No ejecutar `git commit` ni `git push` (el propietario del proyecto lo hace).
5. JWT solo cifrado; nunca loguear datos personales ni tokens.
6. Validar en cliente lo que el backend también valida, sin reemplazar su validación.
7. Consultar prototipos y requisitos antes de construir cualquier pantalla.
