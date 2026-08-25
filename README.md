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

**Fase 0 completada.** Detalle del avance fase por fase:

| Fase | Módulo | Estado |
|---|---|---|
| 0 | Preparación del entorno | ✅ Completada |
| 1 | Registro + Verificación OTP | ⬜ Pendiente — **siguiente paso** |
| 2 | Login | ⬜ Pendiente |
| 3 | Perfil / Mi cuenta | ⬜ Pendiente |
| 4 | Logout | ⬜ Pendiente |
| 5 | Recuperación de contraseña | ⬜ Pendiente |
| 6 | Eliminar cuenta | ⬜ Pendiente |
| 7 | Progreso / Sync (Room + merge offline-first) | ⬜ Pendiente |
| 8 | Comentarios | ⬜ Pendiente |
| 9 | Avatar personalizado | ⬜ Pendiente |
| 10 | Pantallas transversales (Splash, Sidebar, Home, Niveles, Juego, Ajustes, FAQ) | ⬜ Pendiente |

Implementado hasta ahora:

- Proyecto Gradle (Kotlin DSL) con todas las dependencias base declaradas.
- Contrato remoto completo: 5 interfaces Retrofit y ~25 DTOs que espejan el backend,
  verificados contra los DTOs reales (camelCase nativo sin `@SerialName`,
  auditoría 2026-08-23).
- `TokenManager`: JWT en `EncryptedSharedPreferences` (nunca en texto plano).
- `JwtInterceptor`: adjunta `Authorization: Bearer <token>` automáticamente.
- `NetworkModule` (Hilt): OkHttpClient + Retrofit + Json + provisión de las 5 APIs;
  logging HTTP nivel `BASIC` en debug (nunca loguear cuerpos con contraseña/OTP).
- Tema Compose: tokens ERA aplicados (paleta, tipografía y radios de
  `docs/decisiones-tecnicas.md` §10; `dynamicColor` desactivado).

## Compilar y ejecutar

Requisitos: Android Studio (Narwhal o superior recomendado), JDK 17, SDK 37
(compileSdk; targetSdk 36).

```bash
# Windows
gradlew.bat assembleDebug

# Linux/macOS
./gradlew assembleDebug
```

> **Configuración pendiente:** `NetworkModule.BASE_URL` apunta a un placeholder
> (`https://era-backend.example.com/api/v1/`). Reemplazarlo por la URL real del
> backend `BACKEND_ERA` antes de probar contra servidor.

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
