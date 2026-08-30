# ERA — Instrucciones permanentes del frontend Android

Este archivo es de lectura obligatoria para cualquier asistente de IA que trabaje en
este repositorio. Las reglas de este documento **prevalecen** sobre cualquier
comportamiento por defecto del asistente. Es el equivalente, para el cliente Android,
del `CLAUDE.md` del backend (`BACKEND_ERA`).

---

## 1. Qué es ERA y qué es este repositorio

**ERA (Educación, Repaso y Aprendizaje)** es una app Android nativa de trivia
educativa para niños de básica primaria (**7 a 11 años**), compatible desde
**Android 8.0**, con mediación parental en el registro.

Este repositorio es **solo el cliente Android** (Kotlin, MVVM). Consume vía REST el
backend `BACKEND_ERA` (Ktor + MySQL, ya completo y verificado: 16 endpoints en 6
grupos funcionales). **El frontend no reimplementa lógica de servidor** y **el backend
no sirve contenido de juego** (trivia, FAQ, avatares preestablecidos): eso vive
exclusivamente en el cliente, offline-first, con Room/SQLite.

### Actores del sistema

| Actor | Rol |
|---|---|
| **El menor de edad** | Usuario final que juega. |
| **El acudiente** | Responsable del registro y de autorizar la eliminación de la cuenta. |
| **El servidor / API** | `BACKEND_ERA`, ya construido. Este repo solo lo consume. |

---

## 2. Arquitectura y alcance cerrado del frontend

Arquitectura **offline-first**: el juego, el catálogo de 20 niveles, la FAQ y los
avatares preestablecidos (3 opciones) son **100% locales** (Room/SQLite). El backend
solo entra en juego para: autenticación, verificación por OTP, recuperación de
contraseña, gestión/eliminación de cuenta, sincronización de progreso, envío de
comentarios y avatar personalizado.

El frontend se organiza en los mismos 14 módulos de UX que ya están mapeados a
requisitos (`docs/requisitos-funcionales.md`, REQ-FUN-01…14). De esos 14, **7 hablan
con el backend** y el resto es lógica de cliente. Ver la matriz completa en la
sección 5.

> Si un pedido implica lógica que el backend no expone (ranking en línea, preguntas
> servidas por API, FAQ remota, autenticación social, etc.), **señalarlo antes de
> escribir código** — es un cambio de alcance del backend, no del frontend.

---

## 3. Stack técnico

- **Lenguaje:** Kotlin
- **Plataforma:** Android nativo, `minSdk` 26 (Android 8.0), sin límite superior fijado
- **Sistema de build:** **Gradle** (Kotlin DSL, `build.gradle.kts`) — a diferencia del
  backend, que usa Amper. **No confundir los dos repos.**
- **IDE:** Android Studio (Arctic Fox o superior; soporta Kotlin 2.x y Compose)
- **UI:** **Jetpack Compose** (decidido en Fase 0, 2026-08-17). No se reconsidera por
  módulo.
- **Arquitectura:** MVVM (ViewModel + StateFlow/LiveData)
- **Persistencia local:** Room (progreso, niveles, ajustes, sesión cacheada)
- **Red:** Retrofit + OkHttp (interceptor JWT automático)
- **Serialización:** kotlinx.serialization (mismo serializador que el backend; plugin
  Gradle `kotlinx-serialization` + `kotlinx-serialization-converter` para Retrofit)
- **Credenciales:** Android Keystore / `EncryptedSharedPreferences` para el JWT —
  **nunca** `SharedPreferences` planas
- **Inyección de dependencias:** Hilt (framework DI oficial de Google, requiere plugin
  Gradle + KSP). `@HiltViewModel` para ViewModels, `@Module` + `@InstallIn` para
  wiring de dependencias. Testing con `@TestInstallIn`.
- **Testing:** JUnit + MockWebServer (Retrofit), Compose Testing o Espresso según UI

---

## 4. Reglas permanentes de trabajo

Estas reglas replican, adaptadas al frontend, las del backend — se mantienen porque
ya demostraron funcionar en ese repositorio.

1. **Nunca generar el proyecto completo de una sola vez.** Trabajar módulo por
   módulo, capa por capa (`remote/` → `repository/` → `ui/`), como indica el plan de
   la sección 6.
2. **Nunca pedir ni procesar contraseñas, tokens o secretos reales.** Usar
   placeholders (`<API_BASE_URL>`, `<TEST_PASSWORD>`).
3. **Antes de crear o modificar más de un archivo, presentar un plan y esperar
   confirmación explícita.**
4. **No agregar dependencias sin explicar para qué sirve cada una y sin aprobación
   previa** (editar `build.gradle.kts` + `libs.versions.toml` si se usa Version
   Catalog).
5. **No ejecutar `git commit` ni `git push`.** Se puede *sugerir* el mensaje de
   commit; el propietario del proyecto lo ejecuta.
6. **Explicar el porqué de cada decisión de arquitectura, no solo el qué**
   (especialmente Compose vs XML, Hilt vs manual, y cualquier desviación del plan de
   la sección 6).
7. **Priorizar siempre seguridad y validación de datos.** Esta app la usan niños; el
   registro incluye datos de sus acudientes.
8. **Validar en el cliente todo lo que el backend también valida**, para dar
   retroalimentación inmediata (política de contraseña, formato de correo,
   `nombreUsuario` 3–60 sin espacios, avatar ≤ 2 MB y `jpeg/png/webp`) — pero **sin
   asumir que la validación de cliente reemplaza la del servidor**: todo el manejo de
   errores HTTP de la sección 7 debe implementarse igual.
9. **Nunca implementar eliminación física de datos locales del usuario sin que el
   backend confirme la eliminación de la cuenta.** El flujo de `DELETE /me` es la
   única vía de baja (sección 7).
10. **Avisar antes de codificar si un pedido contradice una regla aprobada de este
    documento o del `CLAUDE.md` del backend.** No asumir una reinterpretación
    silenciosa: plantear el conflicto en una o dos frases y esperar la decisión. Si no
    hay conflicto, implementar sin pedir confirmación extra.
11. **Consultar los prototipos (JPG/PDF) antes de construir cualquier pantalla** que
    tenga un diseño visual asociado. Si un prototipo no existe todavía para una
    pantalla, señalarlo antes de improvisar el layout.
12. **Antes de implementar cualquier módulo, leer el requisito, el caso de uso y la
    historia de usuario correspondientes** en `docs/` (ver matriz de trazabilidad en
    la sección 9). Los criterios de aceptación de `docs/` son la especificación
    vinculante — no derivar el contrato de una pantalla solo del resumen de este
    archivo.
13. **Antes de implementar cualquier pantalla basada en un prototipo, el agente debe
    inspeccionar la estructura existente del proyecto, identificar la arquitectura,
    tecnologías y patrones utilizados, y seguirlos.** No debe introducir nuevas
    librerías, frameworks, patrones arquitectónicos o tecnologías únicamente para
    reproducir características del prototipo. Si considera que una nueva dependencia es
    necesaria, debe justificarla y solicitar aprobación antes de incorporarla.

    **Reglas para el uso de prototipos:**

    a. **El prototipo es una referencia:** Los archivos HTML, CSS y JS representan el
       diseño visual, flujo de navegación y comportamiento esperado. No constituyen la
       implementación final.
    b. **No copiar código:** No reutilizar directamente HTML, CSS o JavaScript en la
       aplicación Android. La funcionalidad debe reinterpretarse e implementarse en
       Kotlin.
    c. **Implementación nativa:** La aplicación debe desarrollarse como una aplicación
       Android nativa utilizando Kotlin y Android Studio, respetando la arquitectura y
       tecnologías definidas en el proyecto.
    d. **Conservar la intención del diseño:** Mantener la estructura visual,
       distribución, componentes, textos, navegación, estados y experiencia de usuario
       del prototipo, adaptándolos a las convenciones de Android.
    e. **Interpretar, no convertir:** Analizar qué representa cada elemento del
       prototipo y utilizar su equivalente apropiado en Android. No realizar
       conversiones automáticas de HTML/CSS/JS a Kotlin.
    f. **Respetar la arquitectura:** El prototipo no define la arquitectura, modelo de
       datos, API, persistencia ni estructura del proyecto. Estas decisiones deben seguir
       la documentación y arquitectura existente.
    g. **No inventar funcionalidades:** No implementar comportamientos presentes en el
       prototipo que contradigan o no estén contemplados en los requisitos del proyecto.
    h. **Prioridad:** En caso de conflicto, prevalecen los requisitos y la arquitectura
       del proyecto sobre el prototipo.
    i. **Adaptación móvil:** El diseño debe adaptarse a diferentes tamaños de pantalla,
       interacción táctil, accesibilidad y demás características propias de Android.
    j. **Antes de implementar:** Revisar primero la estructura existente del proyecto y
       reutilizar sus componentes, patrones y tecnologías antes de crear nuevos.

---

## 5. Manejo de datos personales y de sesión (cliente)

Esta app maneja datos de **menores de edad y de sus acudientes** que ya viajan cifrados
en tránsito hacia el backend, pero el cliente tiene sus propias responsabilidades:

- **JWT de sesión (30 días):** solo en Android Keystore / `EncryptedSharedPreferences`.
  Nunca en `SharedPreferences` planas, nunca en logs, nunca en `Log.d`/`Log.e` con el
  valor completo.
- **Token puente de reseteo (`era-app-reset`, 10 min, single-use):** solo en memoria
  durante el flujo de recuperación; no persistir entre reinicios de la app.
- **Nunca loguear en claro:** correo, cédula del acudiente, fecha de nacimiento,
  contraseñas, código OTP, ni el JWT.
- **Avatar personalizado:** el archivo local temporal (antes de subir) se limpia tras
  el `PUT` exitoso o fallido; no queda huérfano en caché.
- **Logout (REQ-FUN-04):** es responsabilidad **del cliente** — el backend es
  stateless en este endpoint. Al cerrar sesión: borrar el JWT del Keystore y
  considerar limpiar el estado de Room que sea sensible (no necesariamente el
  progreso, que se conserva en servidor).
- **Cuenta eliminada (`403 ACCOUNT_INACTIVE`):** cerrar sesión local inmediatamente,
  sin reintentar la petición que lo disparó.

---

## 6. Plan de fases (orden de implementación)

Mismo principio del backend: **módulo por módulo, capa por capa. Nunca el proyecto
completo de una vez.** Cada fase se diseña, implementa y prueba antes de pasar a la
siguiente, y requiere el plan + confirmación de la regla 3 de la sección 4.

### Fase 0 — Preparación del entorno *(completada — ver sección 10)*

1. Instalar Android Studio (Arctic Fox o superior).
2. Crear el proyecto Gradle nuevo, `minSdk` 26.
3. **Decidir y fijar en este archivo:** Compose vs XML, Hilt vs DI manual,
   kotlinx.serialization vs Moshi.
4. Declarar dependencias base: Retrofit, OkHttp (+ interceptor), Room, la librería de
   serialización elegida, AndroidX Security (Keystore).
5. Definir estructura de paquetes por capas:
   ```
   com.era.app/
     data/        ← Room (entities, DAOs, database)
     remote/      ← Retrofit (API interfaces, DTOs — mismos nombres que el backend)
     repository/  ← orquesta Room + Remote
     ui/          ← pantallas por módulo (viewmodels + composables/views)
     di/          ← inyección de dependencias
     utils/       ← validators, constants
   ```
6. Crear `remote/` con el contrato de la API completo (sección 7 de este documento)
   antes de tocar la primera pantalla.
7. Revisar los prototipos JPG/PDF disponibles y confirmar cuáles pantallas ya tienen
   diseño y cuáles no.

### Fases 1–9 — Módulos (orden y dependencias)

| Fase | Módulo | Endpoints backend | Complejidad | Depende de |
|---|---|---|---|---|
| 1 | Registro + Verificación OTP (A + A.1) — **COMPLETADA** | `register`, `verify-email`, `resend-otp` | Media | Fase 0 |
| 2 | Login (B) — **COMPLETADA** | `login` | Baja | Fase 1 |
| 3 | Perfil / Mi cuenta (D) — **COMPLETADA** | `GET /me`, `PATCH /me` | Baja | Fase 2 |
| 4 | Logout (F) — **COMPLETADA** | `logout` | Muy baja | Fase 2 |
| 5 | Recuperación de contraseña (C) | `password-reset/request\|verify\|confirm` | Media | Fase 2 |
| 6 | Eliminar cuenta (E) | `DELETE /me` | Baja | Fase 3 |
| 7 | Progreso / Sync (G) | `GET/POST /progress/sync` | **Alta** (Room + merge offline-first) | Fase 2 |
| 8 | Comentarios (H) | `POST /feedback/comments` | Baja | Fase 2 |
| 9 | Avatar personalizado (I) | `PUT/GET /users/me/avatar` | Media (permisos + multipart) | Fase 3 |

**Por qué este orden:** registro/verificación es el primer contacto y valida el flujo
de punta a punta; login habilita el JWT que todo lo demás necesita; perfil es la
prueba más simple de que el interceptor JWT funciona; logout es trivial y cierra el
ciclo de sesión; recuperación y eliminación son flujos secundarios pero críticos;
progreso es el módulo más complejo y se deja para cuando Room y el flujo de red ya
están probados; comentarios y avatar son casi independientes del resto.

### Fase 10 — Pantallas transversales (sin backend directo)

Pantalla de carga (lee JWT del Keystore), Sidebar, Pantalla principal, Niveles de
trivia (catálogo local de 20), Juego (cronómetro + scoring), Ajustes (preferencias
locales), FAQ (contenido estático offline). Se implementan al final porque dependen
de que los módulos 1–9 ya expongan sesión y progreso reales, no mocks.

**Criterio de éxito por fase:** tests unitarios de ViewModel/Repository, tests de UI,
y tests de integración con MockWebServer para Retrofit, antes de avanzar.

---

## 7. Contrato de API consumido (resumen — el backend ya está construido)

Base: `/api/v1`. Autenticación: header `Authorization: Bearer <JWT de sesión>` salvo
donde se indique. Errores en formato `ErrorDto` estándar — **mapear siempre por el
campo `error`, nunca por el mensaje** (los textos son deliberadamente genéricos por
anti-enumeración).

| Endpoint | Módulo | Notas clave para el cliente |
|---|---|---|
| `POST /auth/register` | A | 201 + mensaje. Errores de forma (V4–V9) y negocio (unicidad correo/usuario, política contraseña). No hay sesión todavía. |
| `POST /auth/verify-email` | A.1 | OTP fijo `123456` en dev. 3 fallos invalidan el código. |
| `POST /auth/resend-otp` | A.1 | Throttle de 60 s → `429 OTP_RESEND_THROTTLED`. |
| `POST /auth/login` | B | Login por usuario **o** correo. `200 { token }` (JWT 30 días). `401 INVALID_CREDENTIALS` genérico. `423 ACCOUNT_LOCKED` tras 5 fallos → esperar 2 min. `403 ACCOUNT_INACTIVE` si la cuenta fue eliminada. |
| `POST /auth/password-reset/request` | C | Responde `200` siempre, exista o no la cuenta (anti-enumeración). `429` si reenvío < 60 s. |
| `POST /auth/password-reset/verify` | C | Máx. 3 fallos. Éxito → `200 { resetToken }` (JWT puente, 10 min, single-use). |
| `POST /auth/password-reset/confirm` | C | Requiere `resetToken`, no el JWT de sesión. `409 PASSWORD_REUSED` si repite la anterior. |
| `GET /users/me` | D | `200` con **solo 5 campos**: `nombreMenor`, `fechaNacimiento` (ISO), `correo`, `nombreUsuario`, `avatar`. |
| `PATCH /users/me` | D | Body **solo** `{ nombreUsuario }`; cualquier otra clave → `400 INVALID_REQUEST`. `409 CONFLICT` si ya está en uso. 3–60 caracteres, sin espacios. |
| `DELETE /users/me` | E | Body `{ contrasena }` (reverificación). `401` si es incorrecta. Soft delete; tras esto, cerrar sesión local y el correo queda bloqueado para re-registro. |
| `POST /auth/logout` | F | `200` siempre que haya sesión válida. **Stateless: el cliente es responsable de borrar el JWT.** |
| `GET /progress/sync` | G | Snapshot autoritativo: `{ progreso: [...], resumen: { nivelesCompletados, totalNiveles: 20, totalReintentos } }`. El backend no sirve el catálogo de preguntas, solo el estado agregado por nivel. |
| `POST /progress/sync` | G | Sube el acumulado local; el backend mergea "hacia adelante" (nunca retrocede estado) y responde el snapshot ya persistido en la misma llamada (idempotente). Validar en cliente antes de enviar: `orden` 1–20 sin duplicados, `estadoNivel` ∈ `BLOQUEADO/DISPONIBLE/COMPLETADO`. |
| `POST /feedback/comments` | H | Body **solo** `{ contenido }`, máx. 2000 caracteres. `id_usuario` lo resuelve el backend del token — nunca enviarlo. |
| `PUT /users/me/avatar` | I | Multipart, campo `avatar`, ≤ 2 MB, `jpeg/png/webp`. Validar tamaño y tipo **en cliente antes de subir** para no gastar la cuota de red innecesariamente. |
| `GET /users/me/avatar` | I | Devuelve el binario. `404` si el usuario usa un avatar preestablecido (`preset:*`, local) en vez de uno personalizado. Sin URL pública: siempre requiere sesión. |

**Códigos de error a mapear en un solo lugar (interceptor o error-mapper central, no
repetido por pantalla):** `400 VALIDATION_ERROR/INVALID_REQUEST`, `401
INVALID_CREDENTIALS/OTP_INVALID_OR_EXPIRED/RESET_TOKEN_INVALID/UNAUTHORIZED`, `403
ACCOUNT_INACTIVE`, `404 NOT_FOUND`, `409
EMAIL_ALREADY_REGISTERED/EMAIL_LOCKED/PASSWORD_REUSED/CONFLICT`, `423
ACCOUNT_LOCKED`, `429 OTP_RESEND_THROTTLED`, `500 INTERNAL_ERROR`.

> El contrato completo y verificado vive en `README.md` del backend (`BACKEND_ERA`).
> Ante cualquier duda de formato de request/response, **consultar ese archivo antes
> de asumir un DTO** — no inventar campos.

---

## 8. Lo que el frontend NO debe pedirle al backend

- Preguntas de trivia y sus opciones → catálogo local (Room), offline.
- Contenido de la FAQ → local, offline.
- Avatares preestablecidos (3 opciones) → assets locales, lógica de cliente.
- Cálculo de porcentaje de progreso → se calcula en cliente sobre los datos que sí
  vienen del servidor (`nivelesCompletados / 20`).
- Ranking en línea, notificaciones push, autenticación social → **fuera de alcance**;
  si aparecen en un pedido, avisar antes de codificar (regla 10, sección 4).

---

## 9. Documentación y trazabilidad

### Documentación del frontend

| Archivo | Contenido |
|---|---|
| `docs/requisitos-funcionales.md` | REQ-FUN-01 … REQ-FUN-14 |
| `docs/requisitos-no-funcionales.md` | REQ-NF-01 … REQ-NF-06 |
| `docs/casos-de-uso.md` | CU-01 … CU-12 |
| `docs/historias-de-usuario.md` | HU-01 … HU-15 |
| `docs/prototipos/` | JPG/PDF de diseño — **pendiente de anexar** |
| `docs/decisiones-tecnicas.md` | Decisiones de arquitectura y dependencias (justificación completa) |
| `BACKEND_ERA/README.md` | Contrato completo de los 16 endpoints, ya verificado |
| `BACKEND_ERA/CLAUDE.md` | Reglas del backend — consultar ante cualquier duda de contrato o de reglas de negocio compartidas |

### Documentación del backend (BACKEND_ERA)

> **AVISO DE ACCESO RESTRINGIDO:** Estas rutas apuntan al repositorio del backend.
> Solo se debe acceder a estos archivos si es **expresamente necesario** para resolver
> dudas de contrato, reglas de negocio compartidas, o si se **autoriza explícitamente**
> su consulta. No leer por curiosidad o sin un motivo vinculado a una tarea concreta.

**Documentación general del proyecto:**

| Ruta absoluta | Contenido |
|---|---|
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\requisitos-funcionales.md` | REQ-FUN-01 … REQ-FUN-14 (fuente oficial) |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\requisitos-no-funcionales.md` | REQ-NF-01 … REQ-NF-06 (fuente oficial) |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\casos-de-uso.md` | CU-01 … CU-12 (fuente oficial) |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\historias-de-usuario.md` | HU-01 … HU-15 (fuente oficial) |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\ARQUITECTURA_BASE.md` | Arquitectura general del sistema |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\DICCIONARIO_DATOS.md` | Diccionario de datos compartido |

**Análisis de módulos del backend (contrato de endpoints):**

| Ruta absoluta | Módulo |
|---|---|
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-a-analisis.md` | A — Registro + A.1 Verificación OTP |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-b-analisis.md` | B — Login |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-c-analisis.md` | C — Recuperación de contraseña |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-d-analisis.md` | D — Perfil / Mi cuenta |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-f-analisis.md` | F — Logout |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-g-analisis.md` | G — Progreso / Sync |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-h-analisis.md` | H — Comentarios |
| `C:\Users\esalc\IdeaProjects\BACKEND_ERA\docs\modulo-i-analisis.md` | I — Avatar personalizado |

### Matriz de relevancia (requisito → módulo frontend → backend)

| Requisito | Módulo/Pantalla frontend | Backend |
|---|---|---|
| REQ-FUN-01 Registro | Registro (paso 1–2) + verificación OTP | `register`, `verify-email`, `resend-otp` |
| REQ-FUN-02 Login | Login | `login` |
| REQ-FUN-03 Pantalla de carga | Splash / carga | Ninguno directo (techo de 3 s) |
| REQ-FUN-04 Logout | Sidebar → confirmación → logout | `logout` (stateless) |
| REQ-FUN-05 Eliminar cuenta | Ajustes → eliminar cuenta | `DELETE /me` |
| REQ-FUN-06 Mi cuenta | Mi cuenta (editar username + avatar) | `GET/PATCH /me`, `PUT/GET /me/avatar` |
| REQ-FUN-07 Recuperación | Olvidé mi contraseña (3 pasos) | `password-reset/*` |
| REQ-FUN-08 Sidebar | Menú lateral | Ninguno |
| REQ-FUN-09 Pantalla principal | Home | Ninguno |
| REQ-FUN-10 Niveles de trivia | Catálogo de 20 niveles (local) | Solo vía sync (G) |
| REQ-FUN-11 Cronómetro y juego | Juego (local) | Solo vía sync (G) |
| REQ-FUN-12 Progreso | Pantalla de progreso | `GET/POST /progress/sync` |
| REQ-FUN-13 Ajustes | Ajustes (local) | Ninguno (excepto "Sincronizar ahora" → sync) |
| REQ-FUN-14 FAQ y comentarios | FAQ (local) + comentarios | `POST /feedback/comments` |

Para el detalle CU↔HU de cada requisito, ver la tabla equivalente en
`BACKEND_ERA/CLAUDE.md` §8.2 (es la misma trazabilidad, ya que HU/CU son compartidas
entre frontend y backend).

---

## 10. Estado actual de este repositorio

**Fase 0 completada.** **Fase 1 completada.** **Fase 2 completada.**
**Fase 3 completada.** **Fase 4 completada.**

### Fase 0 — Preparación del entorno

- Proyecto Gradle (Kotlin DSL) creado: `minSdk` 26, `targetSdk` 36,
  `compileSdk` 37 (subido el 2026-08-23 por requisito de `androidx.core`
  1.19.0, que exige compilar contra API 37+), Kotlin 2.2, AGP 9.2,
  Version Catalog (`gradle/libs.versions.toml`).
- **Decisiones de Fase 0 fijadas** (ver `docs/decisiones-tecnicas.md`): Compose
  (Material 3), Hilt, kotlinx.serialization, Retrofit 2.12 + OkHttp 4.12, Room 2.8,
  Navigation Compose, Coil 3, Security Crypto.
- Dependencias base declaradas y aprobadas en `app/build.gradle.kts`.
- **Contrato remoto completo (`remote/`):** 5 interfaces Retrofit (`AuthApi`,
  `UsersApi`, `ProgressApi`, `FeedbackApi`, `AvatarApi`) + DTOs que espejan el
  backend con **camelCase nativo sin `@SerialName`**, verificado campo por campo
  contra los DTOs reales del backend (auditoría del 2026-08-23; auth, user,
  progress, feedback, common).
- `utils/TokenManager`: JWT en `EncryptedSharedPreferences` (regla §5 cumplida).
- `remote/JwtInterceptor`: adjunta `Authorization: Bearer <token>` automáticamente.
- `di/NetworkModule` (Hilt): Json + OkHttpClient (timeouts + logging `BASIC`
  solo en DEBUG) + Retrofit + provisión de las 5 APIs.
- **Capa `repository/` iniciada** (2026-08-23): `repository/AuthRepository.kt`
  (interfaz, solo endpoints de Fase 1: `register`, `verify-email`, `resend-otp`),
  `RemoteAuthRepository` (impl Retrofit con wrapper genérico `llamar` +
  `Resultado<T>` sellado `Exito(data)`/`Fallo(EraError)`; re-lanza
  `CancellationException`), `di/RepositoryModule` (`@Binds`).
- **Error-mapper central** (D-02, §7): `utils/EraError.kt` (sealed class de dominio)
  + `utils/ErrorMapper.kt` (mapea por el campo `error` de `ErrorResponse`, nunca por
  mensaje; `IOException` → `ErrorConexion`).
- `ui/theme/`: **tokens ERA implementados** (2026-08-23, decisión D-06 de
  `docs/fase-01-registro-analisis.md`): paleta completa en `Color.kt` (§10.1),
  tipografía Roboto del sistema en `Type.kt` (§10.2), radios + esquema claro
  Material en `Theme.kt` (§10.3); `dynamicColor` desactivado — identidad visual
  fija, sin modo oscuro definido todavía.
- **Tests**: Validators 12, PasswordPolicy 13, ErrorMapper 5,
  AuthRepository (MockWebServer) 12. Deps de test añadidas al catálogo:
  `mockwebserver` y `kotlinx-coroutines-test` (solo `testImplementation`).
- `EraApplication` (`@HiltAndroidApp`) y `MainActivity` (plantilla Compose por
  defecto, sin pantallas reales aún).

### Fase 1 — Registro + Verificación OTP (completada 2026-08-23)

**Plan vinculante:** `docs/fase-01-registro-analisis.md`, decisiones D-01…D-14.

- **Repository:** `register()`, `verifyEmail()`, `resendOtp()` en `AuthRepository`
  + `RemoteAuthRepository`.
- **Errores:** `CorreoRegistrado`, `CorreoBloqueado`, `UsuarioEnUso`, `OtpInvalido`,
  `ReenvioThrottled` + tests `ErrorMapperTest` (MockWebServer).
- **ViewModels:** `RegistroViewModel` (Paso1→Paso2→Paso3), `VerificarEmailViewModel`
  (OTP + countdown delegado a backend D-16).
- **Componentes:** `CompactGreenHeader`, `EraRegButtons`, `InfoBox`, `EraTextField`,
  `StepIndicator`, `EraTextFieldPassword`, `EraCheckbox`.
- **Pantallas:** `RegistroPaso1Screen`, `RegistroPaso2Screen`, `RegistroPaso3Screen`,
  `VerificarEmailScreen`, `RegistroExitosoScreen`.
- **Navegación:** `EraRoutes.REGISTRO_PASO1/2/3`, `VERIFICAR_EMAIL`, `REGISTRO_EXITOSO`,
  `EraNavHost` con transiciones.
- **Tests:** 74 verdes — Validators 12, PasswordPolicy 13, ErrorMapper 5,
  AuthRepository 12, RegistroViewModel 24, Componentes androidTest 8.

### Fase 2 — Login (completada 2026-08-27)

**Plan vinculante:** `docs/fase-02-login-analisis.md`, decisiones D-15…D-21.

- **Repository:** `login()` añadido a `AuthRepository` + `RemoteAuthRepository`.
- **Errores:** `CredencialesInvalidas`, `CuentaBloqueada`, `CuentaInactiva` añadidos
  a `EraError` + mappers en `ErrorMapper.kt` + `MensajeError.kt`.
- **Sesión:** `SesionRepository` (interfaz) + `TokenManagerSesion` (adapter sobre
  `TokenManager`) para permitir testing con fakes.
- **ViewModel:** `LoginViewModel` (@HiltViewModel, `LoginUiState`, `LoginEvento`,
  `CampoLogin`) — patrón idéntico a `RegistroViewModel`.
- **Componentes:** `HeroLogin`, `LoginInputPill`, `LoginButton` (nuevos, no reutilizan
  `EraTextField` por estilo diferente).
- **Pantallas:** `LoginContent` + `LoginScreen` (D-18: post-login → `HomePlaceholderScreen`),
  `HomePlaceholderViewModel` (cerrar sesión).
- **Navegación:** `EraRoutes.LOGIN`, `HOME_PLACEHOLDER`. D-14 (snackbar post-registro)
  vía `savedStateHandle` del `NavBackStackEntry` destino (eliminado el singleton
  `RegistroEstado`).
- **Vector Drawables:** `signo_igual.xml`, `signo_abc123.xml`, `signomas.xml`
  (SVGs reales convertidos desde `C:\Users\esalc\Documents\Mi_proyecto_era_frontend\ERA_app\assets\img\`).
- **Tests:** 93 verdes — Fase 1 (74) + LoginViewModel 11 + AuthRepository login
  (7 MockWebServer) + ErrorMapper login (1).

**Pendiente:**

- Capa `data/` (Room: entities, DAOs, database) — llega con la Fase 7.
- Anexar prototipos JPG/PDF a `docs/prototipos/` (los requisitos funcionales/no
  funcionales, casos de uso e historias de usuario ya están anexados como copias
  sincronizadas desde `BACKEND_ERA/docs`, con nota de procedencia; los diseños de
  pantallas están en `docs/decisiones-tecnicas.md` §10–16).

### Fase 3 — Perfil / Mi cuenta (completada 2026-08-28)

**Plan vinculante:** `docs/fase-03-perfil-analisis.md`, decisiones D-23…D-30.

- **Repository:** `UserRepository` (interfaz: `obtenerPerfil()` /
  `actualizarNombreUsuario()`) + `RemoteUserRepository` (`@Singleton` con wrapper
  `llamar`+`aEraError` replicado de `RemoteAuthRepository`).
- **Errores:** `SesionExpirada`, `PerfilNoEncontrado` añadidos a `EraError` +
  ramas `UNAUTHORIZED`, `NOT_FOUND`, `INVALID_REQUEST` en `ErrorMapper.kt`
  (`INVALID_REQUEST` → "Solicitud inválida") + `when` exhaustivo en
  `MensajeError.kt`. 401/403 del GET y del PATCH cierran sesión local (regla §5).
- **Sesión:** inyecta `SesionRepository` (Fase 2) para el cierre silencioso de
  sesión ante 401/403.
- **ViewModel:** `MiCuentaViewModel` (@HiltViewModel, `MiCuentaUiState`,
  `MiCuentaEvento`); 409 CONFLICT → error inline en el Dialog de username.
- **Componentes (nuevos):** `SettingsHeader` (cabecera gris reutilizable),
  `SettingsCard` + `SettingsCardRow` (tarjeta con filas settings reutilizables
  por Ajustes/FAQ/Eliminar cuenta).
- **Pantallas:** `MiCuentaScreen` + `MiCuentaContent` (`GET /me` → 5 campos;
  Dialog de edición de username). Subtítulos: ruta `EraRoutes.PERFIL = "perfil"`,
  botón "Mi cuenta" en `HomePlaceholderScreen`.
- **Token Settings:** +6 en `Color.kt` (`ColorSettingsHeaderBg`, `ColorSettingsBackBg`,
  `ColorSettingsBackIcon`, `ColorSettingsLabel`, `ColorDivider`, `ColorCardBorder`).
- **Validators:** + `formatearFechaISO(iso)` (mismo `ResolverStyle.STRICT`).
- **DI:** + `@Binds` de `UserRepository` → `RemoteUserRepository`.
- **Tests:** 123 verdes (93 previos + UserRepository MockWebServer 13 +
  MiCuentaViewModel 12 + ErrorMapper 3 + Validators 2). `assembleDebug` y
  `assembleDebugAndroidTest` BUILD SUCCESSFUL.
- **Mejora visual aplicada 2026-08-28** (acta §14.6): filas con icono (círculo
  40dp + divisor), avatar cabalgando el borde superior de la tarjeta (anillo
  blanco + sombra), título de cabecera centrado.
- **Instrumentados ejecutados 2026-08-29:** 45/45 verdes en físico ABR-LX3
  (depuración inalámbrica), incluyendo `MiCuentaScreenTest`, `HomePlaceholderScreenTest`
  y los componentes de Fase 1/2. De paso se corrigieron 2 tests de login para
  ejecución en dispositivo (matcher "Regístrate" por `hasClickAction` + ancestro, y
  `testTag` + `SemanticsActions.SetText` para los campos) y se migró
  `ClickableText` (deprecado) → `Text` + `LinkAnnotation` en `LoginScreen.kt`
  (Fase 1–2).

**Pendiente:**

- Capa `data/` (Room: entities, DAOs, database) — llega con la Fase 7.
- Anexar prototipos JPG/PDF a `docs/prototipos/` (los requisitos funcionales/no
  funcionales, casos de uso e historias de usuario ya están anexados como copias
  sincronizadas desde `BACKEND_ERA/docs`, con nota de procedencia; los diseños de
  pantallas están en `docs/decisiones-tecnicas.md` §10–16).

### Fase 4 — Logout (completada 2026-08-29)

**Plan vinculante:** `docs/fase-04-logout-analisis.md`, decisiones D-32…D-36.

- **Repository:** `logout(): Resultado<MessageResponse>` añadido a `AuthRepository` +
  `RemoteAuthRepository` (reusa el wrapper `llamar { api.logout() }`; `AuthApi` ya lo
  declaraba — no se toca).
- **Semántica D-32 (best-effort):** ante CUALQUIER resultado del POST (200, offline,
  401, 500) se limpia el token local (`limpiarToken()`) y se navega a Login. El backend
  es stateless: "la invalidación del token es responsabilidad del cliente". Validado en
  emulador también con backend apagado.
- **ViewModel:** `HomePlaceholderUiState.kt` (nuevo: `dialogoCierreVisible`/`cerrando` +
  evento `NavegarALogin`) y `HomePlaceholderViewModel` evolucionado (D-34): inyecta
  `AuthRepository` + `SesionRepository`; `onCerrarSesionClick` (anti re-apertura),
  `onCancelarCierre` (no-op si `cerrando`, CA3), `onConfirmarCierre` (anti doble-tap);
  se eliminó `cerrarSesion()`.
- **Pantalla/Navegación (D-35):** diálogo de confirmación en `HomePlaceholderScreen`
  (título 20sp Bold, "¿Deseas cerrar sesión?" 16sp, "Sí, cerrar sesión"/"Cancelar",
  spinner al confirmar, `onDismissRequest` no-op si `cerrando`, 4 testTags); evento→
  navegación en `EraNavHost` con `popUpTo(0){inclusive=true}`.
- **Tests:** 138 verdes (123 previos + AuthRepository logout 5 + HomePlaceholderViewModel
  10). Instrumentados **47/47** en físico ABR-LX3 (2026-08-29): `HomePlaceholderScreenTest`
  reescrito (abre diálogo / cancelar CA3 / confirmar). Cero dependencias nuevas; no se
  tocaron AuthApi, DTOs, ErrorMapper, MensajeError, EraError ni build files (D-36).

**Pendiente:**

- Capa `data/` (Room: entities, DAOs, database) — llega con la Fase 7.
- Anexar prototipos JPG/PDF a `docs/prototipos/` (los requisitos funcionales/no
  funcionales, casos de uso e historias de usuario ya están anexados como copias
  sincronizadas desde `BACKEND_ERA/docs`, con nota de procedencia; los diseños de
  pantallas están en `docs/decisiones-tecnicas.md` §10–16).

Control de versiones: repositorio publicado en GitHub (`ArleySV/frontend_era`,
rama `main`, commit inicial `43d3a0b`).

**Próximo paso:** Fase 5 — Recuperación de contraseña (C,
`password-reset/request|verify|confirm`). Depende de Fase 2 (flujo de correo/OTP ya
validado). Detener para revisión del propietario al completar cada capa.