# Fase 10 — Pantallas transversales: Análisis y Diseño

> Documento de análisis para las pantallas que atraviesan toda la aplicación
> autenticada: **Splash, Sidebar (menú lateral), Home, Niveles de trivia, Juego,
> Ajustes** y la **integración de FAQ** en la navegación.
>
> Registra el alcance, la trazabilidad, el diseño visual, las decisiones técnicas
> (D-64…) y el plan de implementación.
> **Estado:** **APROBADO** por el auditor (2026-09-01, correcciones y F-1 Opción A
> incorporadas). Observaciones **O-1**, **O-2**, **O-3** cerradas (§7.1).
> Autorizado el inicio de **S1 (Splash)**.

---

## 1. Objetivo

Completar el recorrido principal de la app autenticada, dejando operativo el flujo
completo **Splash → Home → (Niveles → Juego) → Progreso / Ajustes / FAQ / Mi cuenta**,
cerrando así los requisitos REQ-FUN-03, 08, 09, 10, 11, 13 y 14 (y enrutando el ya
implementado REQ-FUN-12 Progreso desde el sidebar).

No se implementa código nuevo en la API: el backend ya expone los endpoints
necesarios (`GET /users/profile`, `POST /progress/sync`, `POST /progress/submit`,
`DELETE /users/account`, `POST /feedback/comments`) verificados en fases previas.

## 2. Estado actual (Auditoría)

| Componente | Hallazgo | Estado |
|---|---|---|
| `Splash` | No existe pantalla de carga; `startDestination = LOGIN` fijo en `EraNavHost.kt:43`. **No hay chequeo de sesión persistente** (REQ-FUN-02 CA1 / HU-02 CA1). Diseño aprobado (D-65/F-2). | ✅ Diseñado (D-65) |
| `SesionRepository` | API síncrona lista: `tieneToken()`, `obtenerToken()`, `limpiarToken()`, `guardarCorreo()`, etc. (`repository/SesionRepository.kt`). | ✅ Existe |
| `Sidebar` / Drawer | No existe ningún `ModalNavigationDrawer`/`DrawerSheet` en el proyecto. El diseño está especificado en `decisiones-tecnicas.md` §13.5 y §14.8. | ⚠️ Falta |
| `Home` | `HomePlaceholderScreen.kt` es un placeholder real ("Home placeholder - Fase 10", `ui/login/HomePlaceholderScreen.kt:56`) con 4 botones y el diálogo de cierre de sesión. | ⚠️ Reemplazar |
| `Niveles` / `Juego` | No existen las pantallas. **La capa de datos ya está completa** (Fase 7): `NivelDao.obtenerTodos()`, `ProgresoDao.obtenerTodoConProgreso()`, `RoomProgresoRepository.obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>>`, `registrarResultado(orden, exito)` (forward-only, auto-desbloqueo) y catálogo de **20 preguntas** sembrado en Room desde `assets/trivia_catalog.json`. | ⚠️ Falta UI |
| `Progreso` | Pantalla, ViewModel y sync ya implementados (Fase 7). `ProgresoEvento` en `EraNavHost.kt:116`. | ✅ Existe |
| Cabecera de `Progreso` (cover) | **Desviación detectada (O-3):** implementada con `SettingsHeader` gris (`ProgresoScreen.kt:91`), mientras el diseño §14.15 define cabecera con imagen de fondo. | ⚠️ **Resuelta (O-3):** Progreso → `img_progress.svg` (convertido a VectorDrawable XML en S5). Niveles → 20 imágenes individuales (`img_study1.jpg`…`img_study20.jpg`) en `res/drawable-nodpi/`. Regla: las 20 imágenes deben existir antes de codificar S3. |
| `FAQ` | Implementada (Fase 8): cabecera gris + acordeón offline (`assets/faq.json`, 8 preguntas) + formulario `POST /feedback/comments`. Ruta `faq` existe. | ✅ Existe |
| `Mi cuenta`/`Eliminar cuenta` | Implementadas (Fases 3/6): cabecera gris, tarjeta, avatar, diálogo de confirmación. | ✅ Existe |
| `Ajustes` | No existe pantalla. Diseño especificado en `decisiones-tecnicas.md` §14.10. **DataStore aprobado** (O-1, §14.10). Sin soporte de modo oscuro (`Theme.kt` solo usa `lightColorScheme()`). | ⚠️ Falta |
| Tema | Todos los tokens necesarios existen (`ColorTriviaBg/Text/Btn`, `ColorSoonBg/Title/Icon`, `ColorQuizBgTop/Bottom`, `ColorNivel*`, `ColorCorrecta`, `ColorIncorrecta`, `ColorSettings*`, `ColorCardBorder`, `ColorDivider`, `ColorSettingsLabel`…). **Faltan** `ColorSwitchTrackOn/Off` (§14.10: `#2D3142`, mismo color ambos estados). | ⚠️ Menor |
| Iconografía | Solo `androidx.compose.material:material-icons-core`. `EraIcons` expone `EmailOutline/LockOutline/Visibility/VisibilityOff`. Iconos del sidebar (`account_circle`, `assessment`, `settings`, `help`, `logout`) no todos están en `icons-core`. | ⚠️ D-74 resuelta (vectores XML propios, §9.1) |
| Componentes reutilizables | `SettingsHeader`, `SettingsCard`, `SettingsCardRow`, `AvatarSelector`, `AvatarPerfil` (privado, `MiCuentaScreen.kt:350`), `EraTextField`, `EraRegPrimary/SecondaryButton`, `HeroLogin`, `StepIndicator`, `InfoBox`, `CompactGreenHeader`. | ✅ Existen |
| Prototipos | `docs/prototipos/` vacía. Adoptado `decisiones-tecnicas.md §13-§16` como referencia (excepción regla 11, M-1). | ⚠️ Sin prototipo |

> **Nota (regla 13):** no existen prototipos JPG/PDF de estas pantallas. Se adopta
> como referencia de diseño lo especificado en `docs/decisiones-tecnicas.md`
> (§13.5 Sidebar, §13.4.2 cronómetro, §13.6.3 card-nivel, §13.7 quiz, §13.8 overlays,
> §14.8 Home+Sidebar, §14.10 Ajustes, §14.12 FAQ, §14.13 Niveles, §14.14 Juego,
> §15 Navegación). Donde el diseño se distancia del prototipo web se anota
> explícitamente (p. ej. "Pregunta X de 20" → "Nivel X de 20", D-69). No se
> introducen librerías nuevas salvo las listadas y aprobadas en §8.
>
> **Excepción a regla 11 (M-1):** `docs/prototipos/` sigue vacía (pendiente de
> anexar desde Fase 0). Se adopta `docs/decisiones-tecnicas.md §13-§16` como
> referencia de diseño **vinculante**, conforme a la regla 11h (prioridad:
> requisitos y arquitectura del proyecto sobre el prototipo).

## 3. Requisitos / Casos de uso / Historias de usuario cubiertos

| Fuente | Ref. | Criterio / detalle | Cubierto por |
|---|---|---|---|
| REQ-FUN-02 | CA1 | Sesión persistente: si ya inició sesión y no cerró, abre directo a la pantalla principal. | Splash (D-65) |
| REQ-FUN-03 | CA1-CA4 | Pantalla de carga ≤ 3 s (1 s con caché), sin controles, sin navegar atrás, error → reintentar o cerrar sesión. | Splash (D-65) |
| REQ-FUN-04 | CA1-CA4 | Cerrar sesión: confirmación, invalidar token, redirigir a login, conservar datos. | Sidebar (D-66) |
| REQ-FUN-08 | CA1-CA3 | Menú lateral solo desde pantalla principal; cada opción redirige; cerrar sesión con confirmación. | Sidebar (D-66) |
| REQ-FUN-09 | CA1-CA4 | Home tras autenticación; botón trivia → niveles (20); "Próximamente" visible e inactivo; hamburguesa. | Home (D-67) |
| REQ-FUN-10 | CA1-CA6 | 20 niveles consecutivos; solo nivel 1 disponible al inicio; auto-desbloqueo; bloqueados no clicables; estado persistido/sincronizado; 1 pregunta con 3 opciones; imagen opcional; menú Salir/Continuar. | Niveles (D-68) |
| REQ-FUN-11 | CA1-CA5 | Cronómetro 10 s automático; detener al responder; pausa 60 s tras 2 fallos; 3 s de resultado; reintentos registrados. | Juego (D-69) |
| REQ-FUN-12 | CA1-CA3 | Progreso: niveles/20, barra animada, reintentos acumulados. **(Ya implementado F7)** — integrar al sidebar. | Sidebar |
| REQ-FUN-13 | CA1-CA5 | Preferencias locales aplicadas de inmediato; modo oscuro global; tamaño de texto global; sync con indicador; eliminar cuenta. | Ajustes (D-70/D-71) |
| REQ-FUN-14 | CA1-CA4 | FAQ desde el menú lateral; botón enviar deshabilitado si vacío; confirmación. **(Ya implementado F8)** — integrar al sidebar. | Sidebar (D-72) |
| REQ-NF-01 | — | Carga de pantalla ≤ 2 s; peticiones ≤ 3 s. | Splash, Home |
| CU-02 / CU-04 | — | Jugar nivel; iniciar sesión con sesión persistente. | Juego / Splash |
| CU-05 / CU-08 / CU-09 / CU-10 / CU-12 | — | Cerrar sesión; consultar progreso; configurar ajustes (incl. sync CU-12/REQ-FUN-13 CA4); FAQ/comentario. | Sidebar / Ajustes |

## 4. Integración con módulos previos (Fases 1-9)

| Fase | Aporte reutilizado aquí |
|---|---|
| 1 Registro | `EraRegButtons`, `StepIndicator`, selector de presets (base de `AvatarSelector`). |
| 2 Login | `LoginScreen`, `HeroLogin`, patrón `Channel` de eventos de navegación, `LoginEvento.NavegarAHome`. |
| 3 Mi cuenta | `SettingsHeader`/`SettingsCard`/`SettingsCardRow`, `AvatarPerfil` (patrón avatar con iniciales), `UserRepository.obtenerPerfil()`, tokens de color settings. |
| 4 Logout | Cambio a `EraRoutes.LOGIN` con `popUpTo(0){inclusive=true}` + diálogo de confirmación (se reubica en el drawer). |
| 5 Recuperación | Patrón de sub-grafos anidados (no aplica). |
| 6 Eliminar cuenta | Enlace desde Ajustes → `EliminarCuentaScreen`. |
| 7 Progreso/Sync | Toda la capa de datos de trivia (`RoomProgresoRepository`), catálogo 20 niveles y `ProgresoScreen` (enrutada por sidebar). |
| 8 FAQ | `FaqScreen`/`FaqViewModel` (enrutada por sidebar, D-72). |
| 9 Avatar | Patrón avatares presets + subida Módulo I (avatar del sidebar). |

## 5. Navegación objetivo

### 5.1 Rutas (`ui/navigation/EraRoutes.kt`)

Se **mantienen** las constantes String actuales (no migración a `@Serializable`;
cambio opcional fuera de esta fase) y se **añaden/renombran**:

| Constante | Valor | Nota |
|---|---|---|
| `SPLASH` | `"splash"` | **Nueva**. `startDestination` del grafo (D-65). |
| `HOME` | `"home"` | **Renombra** `HOME_PLACEHOLDER` (elimina el placeholder). |
| `NIVELES` | `"niveles"` | **Nueva**. |
| `JUEGO_NIVEL` | `"juego/{nivelOrden}"` | **Nueva**. `nivelOrden: Int` (`navArgument`, tipo Int). Ruta base `JUEGO = "juego"`. |
| `AJUSTES` | `"ajustes"` | **Nueva**. |
| `PERFIL`, `PROGRESO`, `FAQ`, `ELIMINAR_CUENTA`, `REGISTRO…`, `RECUPERACION…`, `LOGIN` | — | Se mantienen. |

**Nomenclatura (F-1):** todas las rutas nuevas usan **español snake_case**, consistente
con el código actual (`eliminar_cuenta`, `progreso`, `faq`). `coming-soon` queda eliminado
y sustituido por Home + card "Próximamente" inactiva (§6.3).

> **F-1 (Opción A aplicada):** `decisiones-tecnicas.md §15.2/§15.3` se actualizó a
> español (`levels`→`niveles`, `game/{levelId}`→`juego/{nivelOrden}`, `settings`→`ajustes`,
> `delete-account`→`eliminar_cuenta`, `profile`→`perfil`, `progress`→`progreso`);
> `coming-soon` eliminado de la tabla y `splash` añadido como `startDestination`.
> Consistencia total entre la documentación y esta fase.

### 5.2 Grafo

```
                    ┌──────────┐
                    │  Splash  │ startDestination
                    └────┬─────┘
           sin token ────┴─────── con token
                 ▼                    ▼
            ┌────────┐          ┌──────────────────┐
            │ Login  │          │ Home (en drawer) │ ◄── hamburguesa abre drawer
            └────────┘          └───────┬──────────┘
            │ (login ok,              │ Jugar
            ▼  popUpTo 0)             ▼
   (Registro/Recuperar)         ┌──────────┐      ┌─────────────────────┐
                                │ Niveles  │ ───► │ Juego/{nivelOrden}  │ (X → overlay
                                └────┬─────┘ ◄─── │ Continuar/Reiniciar/Salir)
                                     │            └─────────────────────┘
   Drawer: Mi cuenta → PERFIL        │ (Correo para las pantallas autenticadas)
   Drawer: Progreso  → PROGRESO ─────┘
   Drawer: Ajustes   → AJUSTES ──► ELIMINAR_CUENTA
   Drawer: FAQ       → FAQ
   Drawer: Cerrar sesión → LOGIN (popUpTo(0){inclusive=true})
```

Regla de autenticación (patrón existente): toda navegación de entrada/salida de
sesión usa `popUpTo(0) { inclusive = true }` para limpiar el backstack.

## 6. Pantallas

### 6.1 Splash (D-65)

**Decisión:** añadir ruta `splash` como `startDestination`. `SplashScreen` +
`SplashViewModel` deciden la ruta inicial consultando `sesionRepository.tieneToken()`
**local y síncronamente** (sin red): con token → `HOME`; sin token → `LOGIN`.

**Diseño:** fondo `ColorPrimary`, marca "ERA", frase motivacional/educativa alternada
cada visita (lista corta hardcodeada), spinner sutil. Sin controles interactivos
(REQ-FUN-03 CA3).

**Comportamiento:**
- `LaunchedEffect(Unit)` en el screen colecta `SplashEvento.NavegarAHome` /
  `NavegarALogin`; ambas navegan con `popUpTo(SPLASH) { inclusive = true }` → no hay
  vuelta atrás (CA3).
- Transición mínima ~1 s (transición agradable) y máx 3 s (REQ-NF-01); con datos en
  caché se permite 1 s (CA2). La carga es local (EncryptedSharedPreferences + Room);
  no se espera red.
- Fallo de lectura local (raro) → estado error con **Reintentar** y **Cerrar sesión**
  (CA4). `limpiarToken()` + `popUpTo(0)` incluidos.

**Capas:**
- `ui/splash/SplashScreen.kt` (nuevo)
- `ui/splash/SplashViewModel.kt` (nuevo; inyecta `SesionRepository`)
- Modifica: `EraRoutes.kt`, `EraNavHost.kt` (startDestination), `LoginViewModel`
  (mantener; el evento `NavegarAHome` pasa a apuntar a `HOME` vía constante).

### 6.2 Sidebar / menú lateral (D-66)

**Decisión:** `ModalNavigationDrawer` de Material3 envolviendo **solo el Home**
(REQ-FUN-08 CA1). Las pantallas secundarias (Niveles, Juego, Progreso, Mi cuenta,
Ajustes, FAQ, Eliminar cuenta) no muestran drawer; usan su propia cabecera con botón
atrás (patrón vigente).

**Diseño (§13.5 / §14.8):** ancho 70% (max 289dp), overlay 50% negro
(`scrimColor = Color.Black.copy(alpha = 0.5f)`).

- **Cabecera:** fondo `ColorPrimary`, alto mínimo 220dp, padding 32/20/24; avatar
  circular 80dp con iniciales (patrón `AvatarPerfil`) o imagen Coil si `custom:*`;
  nombre 20sp Medium blanco; correo 16sp Regular blanco; frase "ERA - Educación,
  Repaso y Aprendizaje" 16sp 85% blanca.
- **Items** (alto min 44dp, padding 12/20, icono 28dp + texto 20sp, gap 20dp):

| # | Item | Icono (ver D-74) | Destino |
|---|---|---|---|
| 1 | Mi cuenta | `account_circle` | `PERFIL` |
| 2 | Progreso | `assessment` | `PROGRESO` |
| | — separador — | | |
| 3 | Ajustes | `settings` | `AJUSTES` |
| 4 | Preguntas frecuentes | `help` | `FAQ` |
| | — separador — | | |
| 5 | Cerrar sesión | `logout` | Confirmación → `LOGIN` (icono rojo `ColorError`) |

**Cerrar sesión (REQ-FUN-04):** `AlertDialog` "¿Deseas cerrar sesión?" con "Sí,
cerrar sesión" / "Cancelar". Al confirmar: `sesionRepository.limpiarToken()` →
`navigate(LOGIN) { popUpTo(0) { inclusive = true } }`. Mueve el diálogo hoy alojado en
`HomePlaceholderViewModel`.

**Capas:**
- `ui/components/layout/EraDrawer.kt` (nuevo: `DrawerContent` + drawer state)
- `ui/home/HomeViewModel.kt` (asume `HomePlaceholderViewModel`: `dialogoCierreVisible`,
  `cerrando`, `onCerrarSesion`)
- Modifica: `EraRoutes.kt`, `EraNavHost.kt`.

### 6.3 Home (D-67)

**Decisión:** nueva `HomeScreen` reemplazando `HomePlaceholderScreen` (se elimina).
Contenida en el drawer ✔. `HomeViewModel` reutiliza el existente añadiendo el perfil.

**Diseño (§14.8):**
- **Hero** `ColorPrimary`, alto mínimo 300dp; botón hamburguesa **54dp**
  arriba-izquierda (abre drawer); saludo **"¡Hola! {nombre}" 32sp Bold** blanco
  (diferente peso al hero de login) + subtítulo "Nos alegra tenerte de nuevo por aquí"
  20sp Regular blanco.
- **Cuerpo** (padding 24/16, gap 20dp):
  - Card **Trivia Escolar**: fondo `ColorTriviaBg` (#9CFFDB), radio 30dp, icono
    56dp `ColorPrimary`, título 24sp Bold `ColorTriviaText` "Trivia Escolar",
    subtítulo "Cultura general - Nivel primaria", botón pill **"Jugar"** (fondo
    `ColorTriviaBtn` #128A5D, texto blanco 18sp Bold, alto 48dp, min-width 140dp) →
    `NIVELES`.
  - Card **Próximamente** (`ColorSoonBg` #D9D9D9, icono reloj `ColorSoonIcon`, título
    `ColorSoonTitle` "Próximamente", sin botón) — visible, sin acción (REQ-FUN-09 CA3).
- **Perfil:** nombre y avatar para la cabecera del drawer se cargan con
  `UserRepository.obtenerPerfil()` (GET /users/profile). **Offline-first:** si la
  petición falla (sin red), el saludo y el avatar caen a estado genérico sin bloquear
  la pantalla (REQ-NF-01); no hay caché local de perfil (anotado como mejora futura).

**Capas:**
- `ui/home/HomeScreen.kt` (nuevo), `ui/home/HomeViewModel.kt` (renombrado desde
  `login/HomePlaceholderViewModel.kt`; se añade `nombreUsuario/cargandoPerfil`)
- `ui/layout/EraDrawer.kt` (envoltura del Home)
- Modifica: `EraNavHost.kt` (ruta `home`), elimina `ui/login/HomePlaceholderScreen.kt`
  y su ViewModel/estado. Actualizar tests que referencien `home_placeholder`.

### 6.4 Niveles de trivia (D-68)

**Decisión:** `NivelesScreen` + `NivelesViewModel` alimentados por
`ProgresoRepository.obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>>`
(JOIN reactivo de Fase 7, sin consultas nuevas).

**Diseño (§13.6.3 / §14.13):**
- **Cabecera con imagen** (§13.1.4): alto min 300dp, título "Trivia primaria"
  24-28sp Bold, botón atrás (flecha en círculo) → `HOME`. **Asset (O-3, resuelto):**
  las 20 imágenes individuales `img_study1.jpg`…`img_study20.jpg`, una por nivel
  (sin repetir), ubicadas en `app/src/main/res/drawable-nodpi/`. **Regla de bloqueo:**
  S3 no se inicia hasta que las 20 imágenes existan. **Diseño de cabecera pendiente
  de concretar:** la imagen de cada nivel se usa en la cabecera cover de la pantalla
  Niveles (p. ej. la imagen del nivel disponible/foco) **o** en cada `card-nivel` /
  navegación al nivel — se define en la implementación S3 sin cambiar el arquitectura.
- **Lista:** 20 items (scroll, gap 14dp) tipo `card-nivel`:
  - Icono circular 44dp con estado; nombre del nivel 18sp Bold ("Nivel 1: Animales");
    estado 14sp según color.
  - Estados y tokens ya existentes: **Completado** (#DDF7EA/#1E9E63, check ✓),
    **Disponible** (#DCEBFB/#2E7FD6, play ▶), **Bloqueado** (#ECECEC/#D5D5D5,
    candado, texto #8A8A8A).
- **Interacción:** clic solo en Disponible (→ `JUEGO_NIVEL/{orden}`) y Completado
  (→ overlay con **Continuar**/siguiente disponible o **Reiniciar**/re-jugar el
  nivel); Bloqueado no es clicable (CA3). El auto-desbloqueo lo resuelve
  `registrarResultado` (forward-only, Fase 7).
- Menú "Salir/Continuar" (REQ-FUN-10 CA6) se materializa en el **overlay-menu-nivel**
  del juego (D-69) y en el clic sobre un nivel completado.

**Capas:**
- `ui/niveles/NivelesScreen.kt`, `ui/niveles/NivelesViewModel.kt` (nuevos)
- Modifica: `EraRoutes.kt`, `EraNavHost.kt`.

### 6.5 Juego de nivel / Quiz (D-69)

**Decisión:** ruta `juego/{nivelOrden}` (Int). `JuegoScreen` + `JuegoViewModel`
(scoped a la ruta con `hiltViewModel()` + `SavedStateHandle` para `nivelOrden`).
Máquina de estados de la partida: `CARGANDO / JUGANDO / RESULTADO / PAUSA / MENU`.

**Diseño (§13.7 / §13.8 / §14.14):**
- Fondo gradiente `ColorQuizBgTop (#1F6F63)` → `ColorQuizBgBottom (#2F9E8F)`.
- **Cabecera quiz:** botón X 44dp (círculo, borde blanco translúcido) → overlay
  **menu-nivel**; cronómetro circular 56dp (anillo regresivo antihorario, número 16sp
  Bold blanco, rojo `ColorIncorrecta` ≤ 3 s); badge nivel (pill blanco, 14sp Bold
  `ColorPrimaryDark`).
- **Cuerpo:** imagen ilustrativa por nivel (máx 200dp, radio 16dp) usando la
  imagen del nivel actual `img_study{nivelOrden}.jpg` desde
  `res/drawable-nodpi/` (O-3, alineada con la asignación de Niveles); si la imagen
  concreta no está disponible, se reserva el espacio, indicador
  **"Nivel X de 20"** (*adaptación:* el prototipo dice "Pregunta X de 20" pero
  `NivelEntity` es **1 pregunta por nivel**), texto de pregunta 26sp Bold blanco
  (line-height 1.3), y 3 opciones pill blancas (texto 17sp Bold, alto min 56dp,
  gap 16dp, sombra).
- **Resultado (§13.7.3):** bottom sheet blanco (esquinas 24dp) 3 s con título
  verde/rojo según correcta/incorrecta y **mensaje aleatorio** de las listas
  `FRASES_FELICITACION` (correcto) o `FRASES_MOTIVACION` (incorrecto):
  - Correcto → avanzar automáticamente al **siguiente nivel disponible** (auto-continue);
    si no hay más, volver a `NIVELES`.
  - Incorrecto → reiniciar la **misma** pregunta (reintentos ilimitados);
    **no se muestra la respuesta correcta** al fallar.
  - **2 fallos consecutivos → overlay Pausa** (§13.8.2): emoji 🧘 56sp,
    "Estírate y respira." 24sp Bold, **ventana verde claro (`ColorVerdeClaro`)**
    con frase aleatoria de `FRASES_SABIAS` ("¿Sabías que...?"),
    círculo 84dp con cuenta regresiva 60 s (borde 3dp blanco, 30sp Bold).
    Al llegar a 0 → cierra overlay, resetea racha, reinicia la pregunta (REQ-FUN-11 CA3).
- **Overlay menu-nivel (§13.8.1):** modal centrado (fondo oscuro 55%, tarjeta gris
  `ColorSurface`, radio 24dp, max 340dp) con 3 botones pill: **Continuar** (texto
  `ColorNivelCompletado`), **Reiniciar** (texto `ColorNivelBloqueado`) y **Salir**
  (texto `ColorError`) → `NIVELES`.

**Lógica (REQ-FUN-11):**
- Cronómetro **15 s automático, no pausable** (CA1): `LaunchedEffect` con decremento
  cada 1000 ms; tocar 0 → se procesa como incorrecta sin respuesta.
- Al responder, el cronómetro se **detiene** de inmediato (CA2) y se evalúa.
- Cada resolución llama `RoomProgresoRepository.registrarResultado(orden, exito)`:
  actualiza `estadoNivel` (forward-only → auto-desbloqueo CA2), `intentosTotales`,
  `intentosFallidosConsecutivos` y `completadoEn`; todo persiste y es sincronizable.
- **Auto-continue (REQ-FUN-10):** tras acierto, si el siguiente nivel existe y está
  disponible/completado, navega automáticamente a `JUEGO/{siguienteOrden}`;
  si es el nivel 20 o el siguiente está bloqueado, vuelve a `NIVELES`.
- `intentosFallidosConsecutivos` se resetea al superar el nivel o al salir (CA3) —
  gestionado por `registrarResultado`.
- Back físico/gesto durante JUGANDO → equivale a "Salir" del overlay (confirmación
  del sistema con mensaje "¿Salir del nivel?").
- **Mensajes alternados:** las frases de felicitación e incorrectas se seleccionan
  aleatoriamente de las listas `FRASES_FELICITACION` y `FRASES_MOTIVACION`
  (20 frases cada una, pensadas para niños de 7 años en adelante).

**Capas:**
- `ui/juego/JuegoScreen.kt`, `ui/juego/JuegoViewModel.kt`, `JuegoUiState.kt` (nuevos/modificados).
- `JuegoEvento` integrado en `JuegoUiState.kt`: `NavegarANiveles(orden: Int)` y `VolverANiveles`.
- Constantes de frases (`FRASES_FELICITACION`, `FRASES_MOTIVACION`, `FRASES_SABIAS`) como companion object en `JuegoViewModel`.
- Componentes quiz nuevos: `quiz-header`, `opcion-respuesta`, `resultado-sheet`,
  `overlay-menu-nivel`, `overlay-pausa`, `cronometro-circular`
  (`ui/components/quiz/`, `ui/components/juego/`) — reutilizando tokens existentes.
- Modifica: `EraRoutes.kt`, `EraNavHost.kt` (ruta con argumento Int).

### 6.6 Ajustes (D-70 / D-71)

**Decisión:** `AjustesScreen` + `AjustesViewModel`. Cabecera **gris Settings**
(§14.10), tarjeta contenedora con 6 filas. No usa drawer (REQ-FUN-08 CA1).

**Filas (§14.10, alto min 44dp, divisor 1dp `ColorDivider`):**

| # | Fila | Control | Acción |
|---|---|---|---|
| 1 | Efectos de sonido | Switch | persistir preferencia |
| 2 | Música de fondo | Switch | persistir preferencia |
| 3 | Modo oscuro | Switch | aplica tema global (D-71) |
| 4 | Tamaño de texto | valor + "›" ("Mediano") | Dialog selector Pequeño/Mediano/Grande (D-71) |
| 5 | Sincronizar ahora | texto `ColorPrimary` | `sincronizarConServidor()` + indicador + snackbar OK/error (REQ-FUN-13 CA4) |
| 6 | Eliminar cuenta | texto `ColorError` | → `ELIMINAR_CUENTA` (REQ-FUN-13 CA5, REQ-FUN-05) |

- Switch: pill ~56×32dp, track `ColorSwitchTrackOn/Off` (#2D3142, mismo en ambos
  estados — **token a añadir** en `Color.kt`), thumb blanco solo cambia de posición
  (§13.9.3).
- Persistencia: **preferencias locales** → D-70a (**DataStore**, cerrada: aprobada en §7.1 O-1 / §8).
- `data/prefs/PreferenciasRepository.kt` (nuevo; **DataStore**, D-70a/O-1 cerrada), `Sincronizador`/sync reutiliza `ProgresoRepository`.

**Capas:**
- `ui/ajustes/AjustesScreen.kt`, `ui/ajustes/AjustesViewModel.kt`,
  `ui/ajustes/AjustesUiState.kt` (+ eventos) (nuevos)
- `ui/components/settings/SettingsSwitchRow.kt` (fila con switch) (nuevo) — reutiliza
  `SettingsCardRow`/`SettingsCard` donde aplique.
- `data/prefs/PreferenciasRepository.kt` (nuevo; **DataStore**, D-70a/O-1 cerrada), `Sincronizador`/sync reutiliza `ProgresoRepository`.
- Modifica: `Color.kt` (2 tokens), `Theme.kt`+`MainActivity`/App (D-71).

### 6.7 FAQ — integración en navegación (D-72)

**Decisión:** la pantalla FAQ existe (Fase 8) y cumple REQ-FUN-14. La única
integración pendiente es **enrutarla desde el drawer** ("Preguntas frecuentes" →
`faq`). Cero cambios funcionales; solo se verifica que "atrás" vuelva al Home.

## 7. Decisiones técnicas (registro D-64…)

| # | Decisión | Resumen | Fecha |
|---|---|---|---|
| D-64 | **Documentación de la fase** | Un único archivo de análisis (este), consistente con el patrón de fases previas (un doc por fase). El **trabajo se divide por pantalla** a efectos de commits/tests (ver §10), pero **no** se crean subdocumentos por capas: cada pantalla documenta sus capas (UI/ViewModel/Repositorio) dentro de su sección. Si durante la implementación un screen requiere profundidad (p. ej. Juego), sus decisiones se amplían aquí o en su sección sin fragmentar el doc. | 2026-09-01 |
| D-65 | **Splash y ruta inicial** | `startDestination = splash`; `SplashViewModel` decide con `sesionRepository.tieneToken()` (local/síncrono): token → `home`, sin token → `login`, ambos con `popUpTo(splash){inclusive}`. Sin red. Reintentar/cerrar sesión en fallo local (REQ-FUN-03). | 2026-09-01 |
| D-66 | **Sidebar solo en Home** | `ModalNavigationDrawer` M3 (ancho 70%, max 289dp, scrim 50%) envolviendo únicamente `HomeScreen` (REQ-FUN-08 CA1). Cerrar sesión: diálogo + `limpiarToken()` + login con `popUpTo(0)`. | 2026-09-01 |
| D-67 | **Home real** | Reemplaza `HomePlaceholderScreen`. Hero verde + saludo + card Trivia Escolar (→ Niveles) + card Próximamente (inactiva). Perfil por `GET /users/profile` con fallback offline-genérico. | 2026-09-01 |
| D-68 | **Niveles** | UI sobre `obtenerNivelesConProgreso()` (Flow JOIN de Fase 7). Estados con tokens existentes; cabecera con imagen usando las 20 imágenes individuales `img_study1.jpg`…`img_study20.jpg` (O-3, cerrada). Bloqueante S3: las 20 imágenes en `res/drawable-nodpi/` antes de codificar. | 2026-09-01 |
| D-69 | **Juego / Quiz** | 1 pregunta por nivel (catálogo), cronómetro 10 s, `registrarResultado` persiste y desbloquea; overlays menu-nivel y pausa 60 s; resultado 3 s. Adaptación de etiqueta "Pregunta X de 20" → "Nivel X de 20". | 2026-09-01 |
| D-70 | **Ajustes** | 6 filas según §14.10; añadir tokens `ColorSwitchTrackOn/Off`. Enlace Eliminar cuenta → flujo existente. | 2026-09-01 |
| D-70a | **Persistencia de preferencias** | **Cerrada (DataStore):** decisión preexistente en `decisiones-tecnicas.md §14.10`. DataStore aprobado para preferencias de Ajustes. EncryptedSharedPreferences se reserva solo para el JWT. | 2026-09-01 |
| D-71 | **Modo oscuro + tamaño de texto global** | `ERATheme(darkTheme, textScale)` con `darkColorScheme()` y `CompositionLocal` de escala (0.9/1.0/1.15). Estado leído en el arranque y aplicado en toda la app (REQ-FUN-13 CA2/CA3). Auditoría de colores hardcodeados → tokenizar. | 2026-09-01 |
| D-72 | **FAQ wiring** | FAQ ya cumple REQ-FUN-14; solo se añade al drawer (item 4). | 2026-09-01 |
| D-73 | **Orden de implementación y versionado** | Sub-fases S1..S6 (ver §10); una sub-fase por commit; el usuario ejecuta los `git add/commit` con coautores (regla 5). | 2026-09-01 |
| D-74 | **Iconografía del sidebar** | **Cerrada (vectores XML propios):** decisión preexistente en `decisiones-tecnicas.md §9.1`. `material-icons-extended` NO se agrega. Iconos faltantes (`assessment`, `logout`, etc.) se definen como vectores XML propios en `ui/components/EraIcons.kt` (precedente Fase 2). | 2026-09-01 |

### 7.1 Observaciones resueltas por el auditor

#### O-1 — Persistencia de preferencias de Ajustes (D-70a) — RESUELTA ✅

**DECISIÓN CERRADA:**
*Decisión preexistente en `decisiones-tecnicas.md §14.10`. DataStore aprobado para
preferencias de Ajustes. EncryptedSharedPreferences se reserva solo para el JWT
(dominio de sesión, no compite).*

Contexto histórico del análisis (candidatos evaluados y descartados por la decisión
preexistente):

**Opción A — `androidx.datastore:datastore-preferences` (nueva dependencia)**

*Características:* biblioteca oficial Jetpack; Preferences DataStore (pares clave-valor
con tipos seguros); lectura/escritura asíncrona con corrutinas; expone `Flow<T>` reactivo;
persistencia local en archivo `preferences.preferences_pb` (Protobuf).

*Ventajas:* idiomática para config (caso exacto de Ajustes: sonido/música/modo oscuro/tamaño
de texto); **lectura observable** → el tema global (D-71) reacciona a cambios sin propagación
manual; sin bloqueos del hilo principal; recomendada por Google sobre `SharedPreferences`.

*Desventajas:* dependencia nueva (regla 4 → ya aprobada por la decisión preexistente);
conviven dos repositorios de persistencia local (Room + DataStore) — justificable por
dominio distinto (datos de juego vs configuración).

**Opción B — `EncryptedSharedPreferences` existente (sin dependencia)** *(descartada)

*Características:* ya integrada (`security-crypto`), usada por `TokenManager` para el JWT;
API síncrona; cifrada con `MasterKey AES256_GCM`.

*Desventajas (por qué se descarta):* **síncrona** (bloquea el hilo en cada
escritura/lectura → posible jank en las filas de Switch); **sin observabilidad reactiva**
→ D-71 tendría que propagar el cambio de modo oscuro manualmente (estado fragmentado);
guardar config no sensible en un store cifrado es sobredirecto y ensucia el test de
persistencia; no es el patrón recomendado para settings.

#### O-2 — Iconografía del sidebar (D-74) — RESUELTA ✅

**DECISIÓN CERRADA:**
*Decisión preexistente en `decisiones-tecnicas.md §9.1`. `material-icons-extended`
NO se agrega. Los iconos `account_circle`, `assessment`, `settings`, `help`,
`logout` que no estén en `material-icons-core` se definen como vectores XML
propios en `ui/components/EraIcons.kt` (precedente Fase 2: `signo_igual.xml`,
`signo_abc123.xml`, `signomas.svg`).*

Contexto histórico (candidatos evaluados): `material-icons-core` (único incluido) **no**
trae `assessment` (Progreso) ni `logout` (Cerrar sesión); `material-icons-extended` aporta
fidelidad total a Material pero es dependencia nueva y "talla grande" para 2 iconos — se
descarta en favor de vectores XML propios, consistentes con el precedente auditado de Fase 2.

#### O-3 — Asset de cabecera (Niveles / Progreso) — RESUELTA ✅

Repositorio local de assets:
`C:\Users\esalc\Documents\Mi_proyecto_era_frontend\ERA_app\assets\img\` con candidatos:
`img_progress.svg` (191 KB), `img_study.jpg` (56 KB), `img_math.jpg` (52 KB), más
decorativos (`decor_prot_05.svg`, `img_villa.svg`…) y gifs (`gif_gok.gif` 272 KB,
`img_estudygif.gif` 500 KB — los gifs no se usan como cabecera: requieren el módulo GIF
de Coil, no incluido).

**Hallazgo:** `ProgresoScreen` (implementada en Fase 7) usa `SettingsHeader` gris
(`ProgresoScreen.kt:91`), **no** la cabecera con imagen cover que especifica §14.15.

**Asignación aprobada:**
- **Niveles** usa `img_study1.jpg`…`img_study20.jpg` (una por nivel, sin repetir).
- **Progreso** usa `img_progress.svg`.

**Regla de bloqueo S3:**
*Para codificar las pantallas de los niveles ya se deben contar con las 20 imágenes
(`img_study1.jpg … img_study20.jpg`) ubicadas en `app/src/main/res/drawable-nodpi/`.
Hasta que no estén, S3 no se inicia.*

**Nota técnica:** los JPG se empaquetan en `res/drawable-nodpi/` y se cargan con
`painterResource` (precedente Fase 2). `img_progress.svg` se convierte a VectorDrawable
XML (`res/drawable-nodpi/ic_progress_cover.xml`).

---

## 8. Dependencias / aprobaciones requeridas (regla 4)

| Dependencia | Motivo | Requerida por | Estado |
|---|---|---|---|
| `androidx.datastore:datastore-preferences` | Persistencia reactiva de preferencias de Ajustes | D-70a / **O-1 Resuelta** | ✅ Aprobada (decisión preexistente §14.10) |
| `material-icons-extended` | Iconos faltantes del sidebar | D-74 / **O-2 Resuelta** | ❌ NO se agrega (vectores XML propios, §9.1) |

> **O-1 y O-2 resueltas por el auditor** (2026-09-01): DataStore aprobado por la
> decisión preexistente de `decisiones-tecnicas.md §14.10`; `material-icons-extended`
> descartado por `§9.1` (iconos propios en `EraIcons.kt`). No requieren nueva aprobación.

Sin otras dependencias: cronómetro y overlays se implementan con Compose puro; cargas
de imagen con Coil ya integrado.

## 9. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Modo oscuro global toca pantallas con colores hardcodeados (hex en componentes) | Bugs visuales | Auditoría de hex → tokens durante S5; `darkColorScheme()` mapea tokens; revisión por pantalla en tests instrumentados. |
| Cambio de `startDestination` a Splash rompe tests de navegación existentes (login directo) | Suite roja | Los instrumentados de login/registro pasan a arrancar su flujo desde la ruta explícita (`navigate`) o con testTags; garantizar que el arranque esté desacoplado del Splash en tests. |
| Home requiere perfil (red) en modo offline | Saludo sin nombre | Fallback genérico sin bloqueo (REQ-NF-01). |
| `registrarResultado` forward-only: repetir un nivel completado no provoca regresión | Progreso no decrece (diseñado) | Aceptado por diseño; documentado en Fase 7. |
| 1 pregunta por nivel → quiz corto | Desmonta el concepto "Pregunta X de 20" | Sustituido por "Nivel X de 20"; consistente con el catálogo oficial (20 niveles/1 pregunta). |
| Cabeceras con imagen (Niveles/Progreso) — O-3 resuelta | Pantallas sin la identidad del prototipo | Niveles → `img_study1.jpg`…`img_study20.jpg` (bloqueante S3: las 20 en `res/drawable-nodpi/`); Progreso → `img_progress.svg` → VectorDrawable XML. Si falta alguna imagen, S3 se bloquea (regla fijada en §7.1). |

## 10. Plan de implementación (por sub-fase y commit)

| Sub-fase | Contenido | Decisiones | Verificación |
|---|---|---|---|
| S1 | Splash + ruta inicial (`HOME_PLACEHOLDER` se mantiene; renombrado → `HOME` en S2) | D-65 | Unitarios `SplashViewModelTest`; instrumentado arranque con/ sin token; suite completa |
| S2 | Drawer + Home real (elimina placeholder) + renombrado `HOME_PLACEHOLDER` → `HOME` | D-66, D-67 | `HomeViewModelTest` (perfil, logout); instrumentado drawer/items; tests de login actualizados |
| S3 | Niveles (**bloqueante:** las 20 imágenes `img_study1.jpg`…`img_study20.jpg` en `res/drawable-nodpi/` antes de iniciar; ver O-3) | D-68 | `NivelesViewModelTest` (estados, clics); instrumentado niveles |
| S4 | Juego | D-69 | `JuegoViewModelTest` (cronómetro, correcta/incorrecta, 2 fallos→pausa, desbloqueo); instrumentado quiz/overlays |
| S5 | Ajustes + tema oscuro/escala | D-70, D-70a, D-71 | `AjustesViewModelTest` + `PreferenciasRepositoryTest`; instrumentado ajustes y modos |
| S6 | FAQ wiring + pase final | D-72 | Verificación de toda la suite (unitarios + instrumentados) y builds |

**DoD por sub-fase:** `assembleDebug` + `assembleDebugAndroidTest` +
`testDebugUnitTest` en verde; actualización de `app/CLAUDE.md` (funcionalidades,
matriz §9, contador unitarios/instrumentados) y `README.md`; el usuario ejecuta el
commit con los coautores tras revisar `git status`/`git diff`.

**Estimación de suite nueva (orientativa):** 5 ViewModels nuevos ≈ 40-60 unitarios;
instrumentados por pantalla ≈ 18-25. Total proyectado ≈ 265-290 unitarios.

## 11. Definición de Hecho (DoD) global

- [ ] Splash respeta REQ-FUN-03 y dirige por sesión persistente (REQ-FUN-02 CA1).
- [ ] Drawer disponible solo desde Home, con los 5 items y cierre de sesión con
      confirmación (REQ-FUN-08, REQ-FUN-04).
- [ ] Home con saludo, Trivia Escolar → Niveles y "Próximamente" inerte.
- [ ] Niveles: 20 items con estados, solo disponibles/completados clicables,
      desbloqueo automático persistido (REQ-FUN-10).
- [ ] Juego: cronómetro 10 s, resultado 3 s, pausa 60 s tras 2 fallos, menú
      continuar/reiniciar/salir, sincronizable (REQ-FUN-11, CU-02).
- [ ] Ajustes: 6 filas operativas con persistencia local, modo oscuro y escala de
      texto globales, sync con indicador, enlace a eliminar cuenta (REQ-FUN-13).
- [ ] FAQ accesible desde el drawer (REQ-FUN-14).
- [ ] Suite completa en verde; docs (CLAUDE/README) actualizados; versionado por
      sub-fase con coautores.

> **Excepción a regla 11 (M-1):** `docs/prototipos/` sigue vacía (pendiente de
> anexar desde Fase 0). Se adopta `docs/decisiones-tecnicas.md §13-§16` como
> referencia de diseño vinculante, conforme a la regla 11h.