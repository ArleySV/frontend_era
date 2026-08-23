# ERA Frontend — Decisiones Tecnicas

> Registro de decisiones de arquitectura y dependencias del frontend Android.
> Cada decision incluye: que se eligio, por que, y que se descarto.

## 1. UI Framework — Jetpack Compose

- **Elegido:** Jetpack Compose (Material3)
- **Descartado:** XML tradicional
- **Por que:** UI declarativa moderna, testing nativo con Compose Testing,
  integracion directa con ViewModel y Navigation Compose. Estandar Android 2024+.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 2. Inyeccion de dependencias — Hilt

- **Elegido:** Hilt (framework DI oficial de Google)
- **Descartado:** DI manual, Dagger, Koin
- **Por que:**
  - Requerido por normativa institucional (uso de framework DI obligatorio)
  - Estandar Android, nativo con Jetpack (`@HiltViewModel`, `@InstallIn`)
  - Integracion directa con Navigation Compose (`hiltNavigationCompose`)
  - Testing robusto con `@TestInstallIn`
  - Curva de aprendizaje menor que Dagger, mas type-safe que Koin
- **Descarte de DI manual:** Aunque era consistente con el backend, la normativa
  exige framework. Hilt es la evolucion natural de Dagger con menos boilerplate.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 3. Serializacion — kotlinx.serialization

- **Elegido:** kotlinx.serialization
- **Descartado:** Moshi
- **Por que:**
  - Mismo serializador que el backend (Ktor usa `kotlinx.json`)
  - Integracion nativa con Room (`@Serializable` como `@TypeConverter`)
  - Integracion nativa con Retrofit (`converter-kotlinx-serialization`)
  - Kotlin-first, soporte oficial JetBrains
  - Moshi es legado de la era Java, requiere kapt/KSP adicional
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 4. Dependencias de red — Retrofit 2.12.0 + OkHttp 4.12.0

- **Retrofit:** Cliente HTTP tipado para los 16 endpoints del backend
- **OkHttp:** Transporte HTTP subyacente + interceptor JWT + logging
- **Por que Retrofit 2.12.0:** Ultima estable de la rama 2.x (may 2025).
  Incluye soporte nativo para kotlinx.serialization. Retrofit 3.0 existe
  pero es demasiado nuevo (may 2025) y puede tener breaking changes.
- **Por que OkHttp 4.12.0:** Compatible con Retrofit 2.x (dependencia transitiva).
  OkHttp 5.x es Kotlin Multiplatform y puede causar conflictos con Retrofit 2.x.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 5. Persistencia local — Room 2.8.4

- **Elegido:** Room 2.8.4 (rama 2.x estable)
- **Descartado:** Room 3.0 (alpha, cambia paquete a `room3`)
- **Por que:**
  - Ultima estable de la rama 2.x (nov 2025)
  - Room 3.0 alpha cambia el paquete a `androidx.room3`, incompatible con
    tutoriales y documentacion existente. Se migrara cuando sea estable.
  - Usa KSP (no kapt) para annotation processing — mas rapido.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 6. Seguridad — AndroidX Security Crypto 1.1.0-alpha06

- **Por que:** El CLAUDE.md §5 exige JWT solo en Android Keystore /
  `EncryptedSharedPreferences`. Esta libreria provee la implementacion.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 7. Navegacion — Navigation Compose 2.9.0

- **Por que:** 14 modulos de UX requieren navegacion centralizada.
  Navigation Compose es el estandar para Compose, tipa las rutas y
  soporta argumentos entre pantallas.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 8. Imagenes — Coil 3.0.4

- **Por que:** Libreria estandar para imagenes en Compose (reemplaza
  Glide/Picasso). Se usa para mostrar avatar personalizado del backend.
- **Fecha de decision:** 2026-08-17 (Fase 0)

## 9. Coroutines — kotlinx-coroutines-android 1.10.2

- **Por que:** Room, Retrofit y ViewModels usan coroutines. Provee
  `viewModelScope` y `Dispatchers.IO`.
- **Fecha de decision:** 2026-08-17 (Fase 0)

---

# Diseno visual y pantallas

> Extraido del prototipo HTML/CSS/JS (index.html + css/styles.css + js/script.js).
> Proposito: documentar el diseno para no depender del repositorio externo.
> El prototipo es referencia — la implementacion es 100% Jetpack Compose nativo.

## 10. Design Tokens

### 10.1 Paleta de colores

Colores base — definir en `ui/theme/Color.kt`:

| Token                  | Valor HEX   | Uso                                                 |
|------------------------|-------------|-----------------------------------------------------|
| `ColorPrimary`         | `#037373`   | Verde teal principal — fondos hero, botones, marca   |
| `ColorPrimaryDark`     | `#025F5F`   | Hover/pressed del primario                           |
| `ColorPrimaryLight`    | `#69BFA0`   | Boton secundario de registro, avatar placeholder     |
| `ColorPrimaryPale`     | `#A9D9CB`   | Fondos de inputs de registro                         |
| `ColorSurface`         | `#D9D9D9`   | Panel gris (login), tarjetas                         |
| `ColorSurfaceWhite`    | `#FFFFFF`   | Fondos blancos                                       |
| `ColorTextDark`        | `#000000`   | Texto principal                                      |
| `ColorTextBody`        | `#303030`   | Texto de botones secundarios                         |
| `ColorTextMuted`       | `#5F6368`   | Placeholders, texto secundario                       |
| `ColorTextWhite`       | `#FFFFFF`   | Texto sobre fondo verde                              |
| `ColorError`           | `#E24B4A`   | Errores, "cerrar sesion", "reiniciar progreso"       |
| `ColorBorderInfo`      | `#037373`   | Bordes de cajas informativas                         |
| `ColorAvatarPlusBg`    | `#F5F5F5`   | fondo boton "+" agregar avatar                       |
| `ColorAvatarPlusIcon`  | `#757575`   | Icono "+"                                            |
| `ColorTriviaBg`        | `#9CFFDB`   | Tarjeta "Trivia Escolar" (home)                      |
| `ColorTriviaText`      | `#0071C7`   | Texto de tarjeta Trivia                              |
| `ColorTriviaBtn`       | `#128A5D`   | Boton "Jugar"                                        |
| `ColorTriviaBtnHover`  | `#0E7049`   | Pressed del boton "Jugar"                            |
| `ColorSoonBg`          | `#D9D9D9`   | Tarjeta "Proximamente" (home)                        |
| `ColorSoonIcon`        | `#4A4A4A`   | Icono tarjeta proximamente                           |
| `ColorSoonTitle`       | `#2C2C2C`   | Titulo tarjeta proximamente                          |
| `ColorQuizBgTop`       | `#1F6F63`   | Gradiente superior pantalla de juego                 |
| `ColorQuizBgBottom`    | `#2F9E8F`   | Gradiente inferior pantalla de juego                 |
| `ColorNivelCompletado` | `#1E9E63`   | Nivel verde (completado)                             |
| `ColorNivelDisponible` | `#2E7FD6`   | Nivel azul (disponible)                              |
| `ColorNivelBloqueado`  | `#E08A3C`   | Nivel naranja (bloqueado)                            |
| `ColorCorrecta`        | `#34C77B`   | Opcion de respuesta correcta                         |
| `ColorIncorrecta`      | `#E5534B`   | Opcion de respuesta incorrecta                       |

Colores derivados de tarjetas de nivel (usar directo):

| Estado       | Fondo      | Borde              | Texto              |
|--------------|------------|--------------------|--------------------|
| Completado   | `#DDF7EA`  | `ColorNivelCompletado` | `ColorNivelCompletado` |
| Disponible   | `#DCEBFB`  | `ColorNivelDisponible` | `ColorNivelDisponible` |
| Bloqueado    | `#ECECEC`  | `#D5D5D5`          | `#8A8A8A`          |

### 10.2 Tipografia

Familia: **Roboto** (fuente del sistema Android, no requiere fuente custom).
Pesos: Light (300), Regular (400), Medium (500), Bold (700).

| Estilo          | Tamano | Peso   | Uso                                                       |
|-----------------|--------|--------|------------------------------------------------------------|
| `HeroTitle`     | 32sp   | Light  | "Bienvenidos!" (login) — en Home el peso es Bold            |
| `FormTitle`     | 36sp   | Bold   | "Inicio de sesion"                                         |
| `HeroRegTitle`  | 20sp   | Bold   | Titulos de cabecera en pantallas de registro               |
| `BodyBase`      | 16sp   | Reg/Med| Texto base, inputs, botones                               |
| `Small`         | 14sp   | Reg/Med| Textos secundarios, estados                                |
| `XSmall`        | 12sp   | Medium | Mensajes de error bajo campos                               |

Tamanios puntuales adicionales:
- 18sp: opciones de quiz, boton "Jugar", subtitulo home
- 20sp: subtitulo home, nombre en sidebar, texto verificacion
- 22–36sp: titulos de pantalla
- 26sp: pregunta de quiz
- 40sp: numero grande en progreso

### 10.3 Formas y radios

| Token           | Valor     | Uso                                                   |
|-----------------|-----------|-------------------------------------------------------|
| `RadiusPill`    | 25.5dp    | Inputs de login, boton primario, opciones de quiz      |
| `RadiusPanel`   | 30dp      | Esquinas superiores del panel gris de login            |
| `RadiusInputReg`| 10dp      | Inputs de las pantallas de registro                    |
| `RadiusBtnReg`  | 8dp       | Botones "Atrás/Continuar/Validar"                      |
| `RadiusCard`    | 30dp      | Tarjetas de modo de juego (home)                       |

### 10.4 Reglas tactiles y espaciados

- Tamano minimo tactil: **44dp** en todos los botones/iconos interactivos.
- Ancho de pantalla de diseno: 412dp (usar como `maxWidth` en layouts si se corre en tablet).
- Elevaciones/sombras: usar `Modifier.shadow()` sutil (aprox. 4–10dp) en botones primarios y tarjetas. Sombras con tinte del color del componente (ej. sombra del boton primario teintada en `ColorPrimary` al 40% opacidad).

### 10.5 Iconografia

- Sidebar: Material Icons Extended (`account_circle`, `assessment`, `settings`, `help`, `logout`).
- Flechas, ojo (mostrar/ocultar contrasena), sobre, candado, reloj, X, check: `Icons.Outlined.*` de Material o SVGs importados como Vector Drawables.
- SVGs decorativos (`signo_igual.svg`, `signo_abc123.svg`, `signomas.svg`): importar como Vector Drawables, posicionarse absolutos en hero de login.

---

## 11. Catalogo de pantallas

| #  | Pantalla                    | Modulo REQ-FUN | Prototipo HTML | Nota                             |
|----|-----------------------------|----------------|----------------|----------------------------------|
| 1  | Login                       | B              | Si             | Autenticacion principal          |
| 2  | Recuperar contrasena (1/3)  | C              | Si             | Solicitud de recuperacion        |
| 3  | Recuperar contrasena (2/3)  | C              | No             | Verificacion OTP                 |
| 4  | Recuperar contrasena (3/3)  | C              | No             | Nueva contrasena                 |
| 5  | Registro (1/3)              | A              | Si             | Datos basicos                    |
| 6  | Registro (2/3)              | A              | Si             | Seleccion de avatar + contrasena |
| 7  | Registro (3/3)              | A              | Si             | Verificacion OTP del registro    |
| 8  | Home + Sidebar              | —              | Si             | Dashboard principal              |
| 9  | Mi cuenta                   | D              | No             | Perfil + editar usuario/avatar    |
| 10 | Ajustes                     | —              | No             | Preferencias de la app           |
| 11 | Eliminar cuenta             | D              | No             | Confirmacion de eliminacion      |
| 12 | FAQ                         | H              | No             | FAQ local + formulario comentarios|
| 13 | Niveles de trivia           | —              | Si             | Seleccion de nivel               |
| 14 | Juego / Pregunta            | —              | Si             | Pantalla de trivia               |
| 15 | Progreso                    | —              | Si             | Estadisticas del usuario         |
| 16 | Proximamente                | —              | Si             | Placeholder de modulos futuros   |

---

## 12. Layout base

### 12.1 Marco de la app (`app-frame`)

Todas las pantallas caben dentro de un marco simulado de 412px de ancho
(vista movil estandar). Las pantallas tienen scroll interno si el contenido
excede el viewport.

### 12.2 Sistema de navegacion

El prototipo usa un sistema tipo SPA (Single Page Application):
- Una sola `index.html` con multiples `section#screen-*` ocultos/mostrados
- Sidebar persistente visible en todas las pantallas autenticadas
- Sidebar se oculta en pantallas de login/registro/recuperacion

En Compose: Navigation Compose con grafo de rutas tipadas. Sidebar se
implementa como `ModalDrawer` o `NavigationRail` segun el breakpoint.

### 12.3 Breakpoints moviles

- **Movil:** < 600px (diseño principal, 412px de referencia)
- **Tablet:** 600px - 1024px (paneles mas amplios, grid de 2 columnas)
- **Desktop:** > 1024px (layout expandido)

---

## 13. Componentes reutilizables

> Todos los componentes usan los tokens de §10. Los nombres de color en el codigo
> corresponden a `Color.kt`. Las medidas en `dp` (equivalentes a `px` del prototipo
> en un frame de 412dp).

### 13.1 Cabeceras

#### 13.1.1 Hero login (`hero-login`)

- Fondo `ColorPrimary` (#037373), alto minimo 283dp
- Padding 44/28/90/28 (top/h/bottom/h)
- 3 SVGs decorativos posicionados absolutos (no interactivos):
  - `signo_igual`: arriba-izquierda, ~140dp ancho
  - `signo_abc123`: arriba-derecha, ~170dp ancho
  - `signomas`: abajo-derecha, ~140dp ancho
- Titulo "Bienvenidos!" — 32sp **Light**, `ColorTextWhite`, margin-top ~130dp
- Subtitulo — 16sp Light, `ColorTextWhite` 85%, max-width ~215dp, line-height 1.5
- **Uso:** Solo pantalla Login

```
┌─────────────────────────────────────────┐
│  ≈≈  (signo_igual)      (signo_abc)    │
│                                         │
│                                         │
│  ¡Bienvenidos!                          │  32sp Light, blanco
│  ERA - Educación, Repaso y Aprendizaje  │  16sp Light, blanco 85%
│                                   (+)   │
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  panel gris se superpone
└─────────────────────────────────────────┘
```

#### 13.1.2 Cabecera compacta verde (`header-compact-green`)

- Fondo `ColorPrimary`, alto minimo 104dp, padding 28/24/20
- Titulo: 20sp Bold, `ColorTextWhite`
- Subtitulo: 16sp Regular, `ColorTextWhite`
- **Uso:** Registro (3 pasos), Recuperar contrasena (3 pasos)

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │
│  Registro - Paso 1 de 3                 │  20sp Bold, blanco
│  Datos de usuario                       │  16sp Regular, blanco
└─────────────────────────────────────────┘
```

#### 13.1.3 Cabecera gris Settings (`header-settings`)

- Fondo `ColorSettingsHeaderBg` (#767676), alto ~230dp, color plano (sin imagen ni gradiente)
- Boton retroceso: circulo 64dp, fondo `ColorSettingsBackBg` (#F2F2F2), icono flecha 24dp `ColorSettingsBackIcon` (#2C2C2C), margen 24dp desde borde izquierdo y arriba
- Titulo: 34-36sp Bold, `ColorTextWhite`, centrado verticalmente con el boton de retroceso
- **Uso:** Ajustes, Mi cuenta, FAQ, Eliminar cuenta

```
┌─────────────────────────────────────────┐
│  ┌────┐                                 │
│  │ ←  │  Título                         │  fondo #767676
│  └────┘                                 │  boton: circulo 64dp #F2F2F2
│                                         │  titulo: 34-36sp Bold blanco
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto ~230dp
└─────────────────────────────────────────┘
```

#### 13.1.4 Cabecera con imagen de fondo (`header-image`)

- Imagen decorativa a pantalla completa (cover), alto minimo 300dp
- Contenido alineado abajo
- Boton retroceso: flecha en circulo (blanco)
- Titulo: 24-28sp Bold, `ColorTextWhite`
- **Uso:** Niveles de trivia, Progreso

```
┌─────────────────────────────────────────┐
│  ←  (flecha en circulo)                │
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  imagen cover
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  min 300dp
│   Título                                │  24-28sp Bold, blanco
└─────────────────────────────────────────┘
```

---

### 13.2 Botones

#### 13.2.1 Boton primario pill (`btn-primary-pill`)

- Fondo `ColorPrimary`, texto `ColorTextWhite` 16sp Bold
- Radio 25.5dp (pill shape), alto minimo 44dp
- Sombra teintada en `ColorPrimary` al 40% opacidad (~4-10dp)
- Padding horizontal 32dp
- **Hover/pressed:** fondo `ColorPrimaryDark`
- **Uso:** "Iniciar sesion", "Enviar enlace", "Verificar codigo", "Restablecer", "Jugar"

```
┌─────────────────────────────────┐
│       Iniciar sesión            │  pill, fondo #037373
└─────────────────────────────────┘  texto blanco 16sp Bold
```

#### 13.2.2 Boton secundario pill (`btn-secondary-pill`)

- Fondo `ColorSurfaceWhite`, borde 2px solid `ColorPrimary`, texto `ColorPrimary` 16sp
- Radio 25.5dp (pill shape), alto minimo 44dp
- **Hover:** fondo `ColorPrimaryPale`
- **Uso:** "Crear una cuenta", "Volver al login"

```
┌─────────────────────────────────┐
│      Crear una cuenta           │  pill, fondo blanco
└─────────────────────────────────┘  borde 2px #037373
```

#### 13.2.3 Boton registro primario (`btn-reg-primary`)

- Fondo `ColorPrimary`, texto `ColorTextWhite` 16sp Bold
- Radio 8dp, alto 44dp, ancho ~170dp
- Icono flecha derecha (+8dp gap)
- Sombra teintada en `ColorPrimary`
- **Uso:** Footer de registro: "Continuar", "Validar"

```
┌───────────────────────────┐
│    → Continuar            │  radio 8dp, fondo #037373
└───────────────────────────┘  texto blanco 16sp Bold
```

#### 13.2.4 Boton registro secundario (`btn-reg-secondary`)

- Fondo `ColorPrimaryLight`, texto `ColorTextBody` 16sp
- Radio 8dp, alto 44dp
- Icono izquierda (+8dp gap)
- **Uso:** Footer de registro: "Cancelar", "Atras"

```
┌───────────────────────────┐
│  ✕ Cancelar               │  radio 8dp, fondo #69BFA0
└───────────────────────────┘  texto #303030 16sp
```

#### 13.2.5 Boton outline (`btn-outline`)

- Fondo transparente, borde 2px `ColorError`, texto `ColorError` 16sp Medium
- Radio 25.5dp (pill), alto minimo 44dp, ancho completo
- **Uso:** "Reiniciar mi progreso" (Progreso), "Cancelar" (Eliminar cuenta)

```
┌─────────────────────────────────┐
│     Reiniciar mi progreso       │  pill, borde 2px #E24B4A
└─────────────────────────────────┘  texto rojo 16sp Medium
```

#### 13.2.6 Boton danger (`btn-danger`)

- Fondo `ColorError`, texto `ColorTextWhite` 16sp Bold
- Radio 25.5dp (pill), alto minimo 44dp, ancho completo
- Sombra roja teintada
- **Deshabilitado:** opaco 50%, sin interaccion
- **Uso:** "Eliminar mi cuenta" (Eliminar cuenta)

```
┌─────────────────────────────────┐
│      Eliminar mi cuenta         │  pill, fondo #E24B4A
└─────────────────────────────────┘  texto blanco 16sp Bold
```

---

### 13.3 Inputs

#### 13.3.1 Input login pill (`input-login`)

- Radio 25.5dp (pill), fondo `ColorSurfaceWhite`, alto 51dp, ancho max 276dp centrado
- Borde 1px `ColorSurface`
- Padding horizontal 16dp, texto 16sp Medium `ColorTextMuted`
- Icono izquierda (16dp margen), icono derecho opcional (ojo, 44dp toggle)
- **Invalido:** borde `ColorError`, hint error 12sp Medium `ColorError` debajo
- **Uso:** Login (email, contrasena)

```
┌─────────────────────────────────┐
│  ✉  ID/E-mail                   │  pill, fondo blanco
└─────────────────────────────────┘  51dp alto, max 276dp
```

#### 13.3.2 Input registro (`input-reg`)

- Radio 10dp, fondo `ColorPrimaryPale` (#A9D9CB), alto 53dp, ancho max 358dp
- Padding horizontal 16dp, texto 16sp Regular
- Label arriba: 16sp Regular, `ColorTextDark`, asterisco rojo si obligatorio
- Hint error debajo: 12sp Medium `ColorError`
- **Uso:** Registro (todos los pasos), Recuperar contrasena, Eliminar cuenta (contrasena)

```
  Correo electrónico *                   label 16sp, asterisco rojo
  ┌─────────────────────────────────┐
  │  ✉  correo@ejemplo.com          │  radio 10dp, fondo #A9D9CB
  └─────────────────────────────────┘  53dp alto, max 358dp
```

---

### 13.4 Indicadores

#### 13.4.1 Step indicator (`step-indicator`)

- 3 puntos circulares de 8dp, centrados, gap 8dp
- Color `ColorPrimary` — activo: opacidad 100%, inactivo: opacidad 30%
- **Uso:** Registro (3 pasos), Recuperar contrasena (3 pasos)

```
    ● ─── ○ ─── ○      paso 1 activo
    ○ ─── ● ─── ○      paso 2 activo
    ○ ─── ○ ─── ●      paso 3 activo
```

#### 13.4.2 Cronometro circular (`cronometro-circular`)

- 56dp diametro, anillo de progreso SVG que se vacia en sentido antihorario
- Numero en el centro: 16sp Bold `ColorTextWhite`
- Anillo verde `ColorPrimary` normal, cambia a `ColorIncorrecta` cuando quedan <=3 segundos
- **Uso:** Cabecera del quiz (pantalla Juego)

```
      ┌──────┐
     /   10   \        56dp, anillo SVG regresivo
    │    s    │        numero 16sp Bold blanco
     \       /         rojo <=3 segundos
      └──────┘
```

#### 13.4.3 Contador de caracteres (`char-counter`)

- Texto "0 / 2000", 12sp, color gris `ColorTextMuted`
- Alineado a la derecha debajo del textarea
- **Uso:** Formulario de comentarios (FAQ)

---

### 13.5 Sidebar

Implementar como `ModalNavigationDrawer` de Compose.

- Ancho: 70% de pantalla (max 289dp)
- Desliza desde la izquierda
- Overlay oscuro semitransparente (50% negro) detras

**Header del drawer:**
- Fondo `ColorPrimary`, alto minimo 220dp, contenido alineado abajo, padding 32/20/24
- Avatar circular 80dp, fondo `ColorPrimaryLight`, iniciales 32sp Bold `ColorPrimary` (o imagen si tiene avatar)
- Nombre: 20sp Medium `ColorTextWhite`
- Email: 16sp Regular `ColorTextWhite`
- Frase marca: 16sp Regular `ColorTextWhite` 85% opacidad

**Items del menu:**
- Lista, fondo blanco, alto minimo 44dp, padding 12/20
- Icono 28dp + texto 20sp Regular, gap 20dp entre icono y texto
- Separadores entre grupos

| # | Item                   | Icono            | Color texto    |
|---|------------------------|------------------|----------------|
| 1 | Mi cuenta              | `account_circle` | `ColorTextDark`|
| 2 | Progreso               | `assessment`     | `ColorTextDark`|
|   | — separador —          |                  |                |
| 3 | Ajustes                | `settings`       | `ColorTextDark`|
| 4 | Preguntas frecuentes   | `help`           | `ColorTextDark`|
|   | — separador —          |                  |                |
| 5 | Cerrar sesion          | `logout`         | `ColorError`   |

```
┌────────────────────────────┐
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  fondo ColorPrimary
│▓▓▓  ┌────────┐  ▓▓▓▓▓▓▓▓▓▓│  avatar 80dp
│▓▓▓  │  MA    │  ▓▓▓▓▓▓▓▓▓▓│  fondo ColorPrimaryLight
│▓▓▓  └────────┘  ▓▓▓▓▓▓▓▓▓▓│
│▓▓  Maria Lopez              │  20sp Medium, blanco
│▓▓  maria@ejemplo.com        │  16sp Regular, blanco
│  ERA - Educación, Repaso    │  16sp Regular, 85%
│     y Aprendizaje            │
├────────────────────────────┤
│  👤 Mi cuenta               │  28dp icono + 20sp texto
│  📊 Progreso                │  44dp alto min
│  ────────────────────────   │  separador
│  ⚙ Ajustes                  │
│  ❓ Preguntas frecuentes     │
│  ────────────────────────   │  separador
│  🚪 Cerrar sesión           │  texto rojo ColorError
└────────────────────────────┘
```

---

### 13.6 Tarjetas

#### 13.6.1 Tarjeta contenedora Settings (`card-settings`)

- Fondo blanco, borde 1dp `ColorCardBorder` (#E6E6E6), radio 24dp
- Sin sombra pronunciada (o muy sutil)
- Superpuesta al final de la cabecera gris
- Padding interno 24dp
- Lista vertical de filas separadas por divisor 1dp `ColorDivider` (#D8D8D8)
- **Uso:** Ajustes, Mi cuenta, FAQ, Eliminar cuenta

```
┌─────────────────────────────────┐
│  ┌─────────────────────────┐    │  borde 1dp #E6E6E6
│  │  Fila 1                 │    │  radio 24dp
│  │  ─────────────────────  │    │  padding 24dp
│  │  Fila 2                 │    │  divisor 1dp #D8D8D8
│  │  ─────────────────────  │    │
│  │  Fila 3                 │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
```

#### 13.6.2 Fila settings (`fila-settings`)

- Alto minimo 44dp, padding vertical ~16dp
- Layout horizontal: label izquierda + valor/switch derecha
- Label: 18sp Bold, `ColorSettingsLabel` (#2D3142)
- Valor: 18sp Regular, negro
- Separada por divisor 1dp `ColorDivider`
- **Uso:** Ajustes (switches, selector tamano texto), Mi cuenta (campos)

```
  Efectos de sonido      [====○]    label Bold + Switch
  ─────────────────────────────
  Tamaño de texto     Mediano >     label Bold + valor + flecha
```

#### 13.6.3 Tarjeta nivel (`card-nivel`)

- Alto flexible, padding 16/20, radio 18dp, borde 1.5dp
- Layout horizontal: icono circular 44dp izquierda + columna texto derecha
- Nombre nivel: 18sp Bold
- Estado: 14sp, color segun estado
- **Clicable** (solo completado y disponible)

| Estado       | Fondo      | Borde      | Icono fondo | Icono      | Texto estado |
|--------------|------------|------------|-------------|------------|--------------|
| Completado   | `#DDF7EA`  | `#1E9E63`  | `#1E9E63`   | Check (✓)  | `#1E9E63`    |
| Disponible   | `#DCEBFB`  | `#2E7FD6`  | `#2E7FD6`   | Play (▶)   | `#2E7FD6`    |
| Bloqueado    | `#ECECEC`  | `#D5D5D5`  | `#E08A3C`   | Candado    | `#8A8A8A`    |

```
┌─────────────────────────────────┐
│  ┌────┐  Nivel 1: Animales      │  nombre 18sp Bold
│  │ ✓  │  Completado             │  estado 14sp
│  └────┘  (44dp circular)       │
└─────────────────────────────────┘
```

#### 13.6.4 TarjetaTrivia Escolar (`card-trivia`)

- Fondo `ColorTriviaBg` (#9CFFDB), radio 30dp (`RadiusCard`), padding 32/24/28
- Min-height 230dp, contenido centrado
- Icono 56dp `ColorPrimary`
- Titulo: 24sp Bold, `ColorTriviaText` (#0071C7)
- Subtitulo: 16sp Regular, `ColorTriviaText`
- Boton "Jugar": pill, fondo `ColorTriviaBtn` (#128A5D), texto blanco 18sp Bold, alto 48dp, min-width 140dp, padding horizontal 32dp, sombra verde
- **Hover pressed:** `ColorTriviaBtnHover` (#0E7049)

```
┌─────────────────────────────────┐
│           🎯 (56dp)             │  icono ColorPrimary
│                                 │
│       Trivia Escolar            │  24sp Bold, #0071C7
│   Cultura general - Nivel       │  16sp Regular, #0071C7
│             primaria            │
│                                 │
│          [   Jugar   ]          │  pill, fondo #128A5D
│                                 │  18sp Bold blanco
└─────────────────────────────────┘
```

#### 13.6.5 Tarjeta Proximamente (`card-soon`)

- Mismo layout que Trivia Escolar
- Fondo `ColorSoonBg` (#D9D9D9), radio 30dp
- Icono reloj 46-56dp `ColorSoonIcon` (#4A4A4A)
- Titulo: 24sp Bold, `ColorSoonTitle` (#2C2C2C)
- Subtitulo: 16sp Regular
- Sin boton de accion

```
┌─────────────────────────────────┐
│           ⏰ (46-56dp)          │  icono #4A4A4A
│                                 │
│       Próximamente              │  24sp Bold, #2C2C2C
│   Nuevo modo de juego en ERA    │  16sp Regular
│                                 │
│                                 │  sin boton
└─────────────────────────────────┘
```

#### 13.6.6 Tarjeta progreso reintentos (`card-reintentos`)

- Fondo `#ECECEC`, radio 18dp, padding 28/24, centrada
- Numero: 40sp Bold, `ColorNivelCompletado`
- Label: "Reintentos totales" 16sp Bold `ColorTextDark`

```
┌─────────────────────────────────┐
│          42                     │  40sp Bold, #1E9E63
│     Reintentos totales          │  16sp Bold, negro
└─────────────────────────────────┘
```

#### 13.6.7 Tarjeta progreso niveles (`card-niveles-progreso`)

- Fondo `#ECECEC`, radio 18dp, padding 28/24, alineacion izquierda
- Label: "Niveles completados" 16sp Bold
- Fraccion: "X / 20 niveles" 14sp gris `ColorTextMuted`, alineada derecha
- Barra de progreso: alto 12dp, radio 6dp, fondo `#D5D5D5`, relleno `ColorNivelCompletado` animado

```
┌─────────────────────────────────┐
│  Niveles completados    8 / 20  │  label Bold + fraccion
│  ━━━━━━━━━━━━━░░░░░░░░░░░░░░░  │  barra 12dp, relleno verde
└─────────────────────────────────┘
```

---

### 13.7 Quiz

#### 13.7.1 Cabecera quiz (`quiz-header`)

- Fondo gradiente: `ColorQuizBgTop` (#1F6F63) a `ColorQuizBgBottom` (#2F9E8F)
- Layout horizontal: boton X izquierda, cronometro centro, badge nivel derecha
- Boton X: circulo 44dp, borde blanco translucido
- Badge nivel: pill blanca, 14sp Bold `ColorPrimaryDark`

```
┌─────────────────────────────────────────┐
│  ✕ (44dp)   ┌──┐   Nivel 3            │
│  circulo     │10│   pill blanca         │
│  borde       │s │   14sp Bold           │
│  blanco      └──┘   ColorPrimaryDark    │
│  translucido  cronometro                │
│              circular 56dp              │
└─────────────────────────────────────────┘
```

#### 13.7.2 Opcion respuesta (`quiz-opcion`)

- Lista vertical, gap 16dp
- Pill, fondo `ColorSurfaceWhite`, texto negro 17sp Bold mayusculas
- Alto minimo 56dp, padding 14/20, sombra
- **Correcta:** fondo `ColorCorrecta` (#34C77B), texto blanco
- **Incorrecta:** fondo `ColorIncorrecta` (#E5534B), texto blanco
- Tras responder: botones deshabilitados

```
┌─────────────────────────────────┐
│          BALLENA                │  pill blanco, texto 17sp Bold
└─────────────────────────────────┘  56dp alto min, sombra

┌─────────────────────────────────┐
│     ✔ BALLENA (correcta)       │  fondo #34C77B, texto blanco
└─────────────────────────────────┘

┌─────────────────────────────────┐
│     ✕ ELEFANTE (incorrecta)    │  fondo #E5534B, texto blanco
└─────────────────────────────────┘
```

#### 13.7.3 Bottom sheet resultado (`quiz-resultado`)

- Desliza desde abajo tras responder
- Fondo blanco, esquinas superiores 24dp, padding 24/28/32
- Handle centrado arriba
- Titulo: 26sp Bold, verde `ColorCorrecta` o rojo `ColorIncorrecta`
- Mensaje: 16sp Regular, `ColorTextMuted`
- Se muestra 3 segundos, luego avanza automaticamente

```
┌─────────────────────────────────┐
│  ━━━━━━━━━ (handle) ━━━━━━━━━  │
│                                 │
│  Correcto / Incorrecto          │  26sp Bold, verde o rojo
│  Mensaje motivacional...        │  16sp Regular, gris
│                                 │
└─────────────────────────────────┘
```

---

### 13.8 Overlays

#### 13.8.1 Menu nivel (`overlay-menu-nivel`)

- Modal centrado sobre fondo oscuro 55%
- Tarjeta gris `ColorSurface` (#D9D9D9), radio 24dp, padding 28/24, ancho max 340dp
- Imagen ilustrativa arriba (radio 14dp, alto 90-160dp)
- 3 botones apilados (gap 12dp), pill, fondo blanco, 18sp Bold, alto min 44dp:
  - "Continuar": texto `ColorNivelCompletado` → cierra menu
  - "Reiniciar": texto `ColorNivelBloqueado` → reinicia pregunta
  - "Salir": texto `ColorError` → vuelve a Niveles

```
┌─────────────────────────────────┐
│  ┌─────────────────────────┐    │
│  │       [imagen]           │    │  radio 14dp, 90-160dp
│  └─────────────────────────┘    │
│  [     Continuar     ]         │  pill blanco, texto verde
│  [     Reiniciar     ]         │  pill blanco, texto naranja
│  [      Salir        ]         │  pill blanco, texto rojo
└─────────────────────────────────┘
```

#### 13.8.2 Overlay pausa (`overlay-pausa`)

- Fondo casi opaco `rgba(2,51,51,0.94)`
- Emoji 🧘 56sp, titulo "Estirate y respira." 24sp Bold blanco
- Subtitulo "Tomate un momento." 16sp blanco 90%
- Circulo cuenta regresiva 60s: 84dp diametro, borde 3dp blanco, numero 30sp Bold blanco

```
┌─────────────────────────────────────────┐
│              🧘  56sp                   │
│                                         │
│     Estírate y respira.                 │  24sp Bold, blanco
│     Tómate un momento.                  │  16sp, blanco 90%
│                                         │
│           ┌────────┐                    │  circulo 84dp
│           │   60   │                    │  borde 3dp blanco
│           └────────┘                    │  30sp Bold blanco
└─────────────────────────────────────────┘
```

---

### 13.9 Componentes de formulario

#### 13.9.1 Info box (`info-box`)

- Borde 1dp `ColorBorderInfo` (`ColorPrimary`), radio 6dp, padding 12/14
- Texto 14sp, `ColorTextDark`
- **Uso:** Register (reglas contrasena), Recuperar contrasena (expiracion OTP)

```
┌─────────────────────────────────┐
│ La contraseña no puede contener │  borde 1dp #037373
│ tu nombre...                    │  radio 6dp, 14sp
└─────────────────────────────────┘
```

#### 13.9.2 Avatar selector (`avatar-selector`)

- Fila de circulos seleccionables, gap 8dp, centrados
- Circulos: 49dp diametro
- 3 avatares predefinidos (imagen) + 1 boton "+" (solo visible en "Mi cuenta", NO en registro — el backend requiere sesión para subir foto custom)
- Avatar seleccionado: borde 2.5dp `ColorPrimary` + halo sombra teal
- **Uso:** Registro paso 2, Mi cuenta (cambiar avatar)

```
┌────┐ ┌────┐ ┌────┐ ┌────┐
│ av1│ │ av2│ │ av3│ │ +  │  49dp c/u, gap 8dp
└────┘ └────┘ └────┘ └────┘  "+": fondo #F5F5F5, icono #757575
```

#### 13.9.3 Switch (`switch-settings`)

- Pill ~56x32dp
- Track: `ColorSwitchTrackOn/Off` (#2D3142) — mismo color ambos estados
- Thumb circular blanco — solo cambia posicion (izq/derecha)
- **Uso:** Ajustes (sonido, musica, modo oscuro)

```
  [====○]  desactivado (thumb izq)
  [====●]  activado (thumb der)
```

#### 13.9.4 Dialog (`dialog-confirm`)

- Modal centrado sobre fondo oscuro 55%
- Tarjeta blanca, radio 16dp, padding 24dp
- Titulo: 20sp Bold
- Mensaje: 16sp Regular
- 2 botones al fondo (gap 12dp): Cancelar (outline) + Accion (primary o danger)
- **Uso:** Confirmar eliminar cuenta, editar nombre, reiniciar progreso

```
┌─────────────────────────────────┐
│  Título                         │  20sp Bold
│                                 │
│  Mensaje de confirmación...     │  16sp Regular
│                                 │
│       [Cancelar]  [Eliminar]    │  gap 12dp
└─────────────────────────────────┘
```

---

### 13.10 Feedback

#### 13.10.1 Snackbar (`snackbar`)

- Fondo `#2C2C2C`, texto blanco 14sp Medium, radio 14dp
- Centrado abajo, aparece/desaparece con fade+slide
- Duracion ~3 segundos
- **Uso:** Nivel bloqueado, sync exitoso, comentario enviado, error generico

```
┌─────────────────────────────────┐
│  Debes completar el nivel       │  fondo #2C2C2C
│  anterior                       │  texto blanco 14sp Medium
└─────────────────────────────────┘
```

#### 13.10.2 Mensaje de estado (`mensaje-estado`)

- Caja con fondo tintado, texto 14sp
- Exito: fondo verde claro, texto verde
- Error: fondo rojo claro, texto rojo
- **Uso:** Recuperar contrasena (exito/error), Login (credenciales invalidas)

---

### 13.11 Texto

Estilos tipograficos del sistema (Roboto, fuente del sistema Android):

| Estilo          | Tamano | Peso   | Color              | Uso                                          |
|-----------------|--------|--------|--------------------|----------------------------------------------|
| `HeroTitle`     | 32sp   | Light  | `ColorTextWhite`   | Login "Bienvenidos!"                         |
| `HeroTitleBold` | 32sp   | Bold   | `ColorTextWhite`   | Home "Hola!"                                 |
| `FormTitle`     | 36sp   | Bold   | `ColorPrimary`     | Login "Inicio de sesion"                     |
| `ScreenTitle`   | 34-36sp| Bold   | `ColorTextWhite`   | Cabeceras Settings                           |
| `HeaderTitle`   | 20sp   | Bold   | `ColorTextWhite`   | Cabeceras compactas, registro                |
| `CardTitle`     | 24sp   | Bold   | Varia              | Titulos de tarjetas (Trivia, Niveles, etc.)  |
| `BodyBold`      | 18sp   | Bold   | `ColorSettingsLabel`| Filas settings, opcion quiz                  |
| `BodyBase`      | 16sp   | Regular| `ColorTextDark`    | Texto base, labels, contenido                |
| `BodyMedium`    | 16sp   | Medium | `ColorTextDark`    | Links, placeholders activos                  |
| `Subtitle`      | 16sp   | Regular| `ColorTextMuted`   | Subtitulos, descripciones                    |
| `Small`         | 14sp   | Regular| `ColorTextMuted`   | Estados, contadores, hints                   |
| `SmallMedium`   | 14sp   | Medium | Varia              | Mensajes de error, estados de nivel          |
| `XSmall`        | 12sp   | Medium | `ColorError`       | Hints de error bajo campos                   |

---

## 14. Pantallas documentadas

> **Verificación cruzada (2026-08-18):** Este catálogo fue verificado contra el código
> real del backend: DTOs (`LoginRequestDto`, `PasswordResetConfirmRequestDto`,
> `RegisterRequestDto`, `ActualizarUsuarioRequestDto`), controllers (`AuthController`,
> `UsuarioController`, `AvatarController`, `ProgressController`, `FeedbackController`),
> routes (`AuthRoutes`, `UserRoutes`, `ProgressRoutes`, `FeedbackRoutes`), validadores
> (`Validators.kt`) y documentación de análisis (`docs/modulo-*-analisis.md`). Las
> discrepancias encontradas fueron corregidas: campo de login (`usuarioOCorreo`),
> transporte de token en password-reset (body, no header), longitud de username (60, no 30),
> avatar durante registro (solo presets), y navegación del sidebar (rutas reales, no
> "Próximamente"). Los colores y dimensiones son estimaciones visuales del prototipo HTML;
> no existe archivo de diseño (Figma/Sketch) como fuente de verdad.

### 14.1 Login

**Fondo general:** `ColorPrimary` (#037373).

#### Seccion superior (Hero) — altura minima 283dp, padding 44/28/90/28 (top/h/bottom/h)

```
┌─────────────────────────────────────────┐
│  ≈≈  (signo_igual)      (signo_abc)    │  SVGs decorativos posicionados
│                                         │  absolutos, no interactivos
│                                         │
│  ¡Bienvenidos!                          │  32sp Light, blanco, margin-top ~130dp
│  ERA - Educacion, Repaso y Aprendizaje  │  16sp Light, blanco, max-width ~215dp
│                                   (+)   │  signomas.svg abajo-derecha
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  ← panel gris se superpone aqui
```

- 3 imagenes decorativas SVG flotantes, posicion absoluta:
  - `signo_igual` (rayas): arriba-izquierda, ~140dp ancho
  - `signo_abc123`: arriba-derecha, ~170dp ancho
  - `signomas` (+): abajo-derecha, ~140dp ancho
- Titulo "Bienvenidos!" — 32sp, peso **Light**, blanco, margin-top ~130dp
- Subtitulo "ERA - Educacion, Repaso y Aprendizaje" — 16sp, Light, blanco, max-width ~215dp, line-height 1.5

#### Panel de formulario (tarjeta gris superpuesta)

```
┌─────────────────────────────────────────┐
│                                         │
│     Inicio de sesion                    │  36sp Bold, ColorPrimary, centrado
│                                         │  margin-bottom 36dp
│  ┌─────────────────────────────────┐    │
│  │ ✉  ID/E-mail                    │    │  pill, fondo blanco, alto 51dp
│  └─────────────────────────────────┘    │  max-width 276dp, centrado
│                                         │  icono sobre izq (16dp margen)
│  ┌─────────────────────────────────┐    │  16sp Medium, ColorTextMuted
│  │ 🔒  Contraseña            👁   │    │  pill, icono candado izq
│  └─────────────────────────────────┘    │  icono ojo der (boton 44dp)
│                                         │
│      ¿Olvidaste la contraseña?          │  16sp Medium, ColorPrimary, centrado
│                                         │  area tactil min 44dp
│      [   Iniciar sesión   ]             │  pill, max-width 276dp, alto 51dp
│                                         │  fondo ColorPrimary, texto blanco
│                                         │  16sp Bold, sombra teal
│                                         │
│  ¿No tienes cuenta? Regístrate          │  16sp, centrado
│                                         │  "Regístrate" en Bold + ColorPrimary
│                                         │
└─────────────────────────────────────────┘
```

- Fondo `ColorSurface` (#D9D9D9), esquinas superiores redondeadas 30dp (`RadiusPanel`)
- Se superpone al hero con offset negativo ~-42dp, sombra sutil hacia arriba
- Padding: 40dp arriba, 28dp lados, 52dp abajo

**Campo email/ID:**
- Pill (radio 25.5dp), fondo blanco, alto 51dp, max-width 276dp centrado
- Icono de sobre a la izquierda (16dp margen)
- Placeholder "ID/E-mail", texto 16sp Medium, color `ColorTextMuted`

**Campo contrasena:**
- Mismo estilo pill
- Icono de candado a la izquierda
- Icono de ojo a la derecha (boton 44dp para mostrar/ocultar)
- Placeholder "Contrasena"

**Link "Olvidaste la contrasena?":**
- Centrado, 16sp Medium, color `ColorPrimary`
- Area tactil minima 44dp

**Boton primario "Iniciar sesion":**
- Pill, max-width 276dp, alto 51dp
- Fondo `ColorPrimary`, texto blanco 16sp Bold
- Sombra teal (teintada en `ColorPrimary` al 40% opacidad)

**Texto inferior:**
- "¿No tienes cuenta? **Registrate**" — centrado, 16sp
- "Registrate" en negrita y color `ColorPrimary`

**Mensaje de error de login (si aplica):**
- Texto centrado 14sp Medium, color `ColorError`
- Ubicacion: arriba del formulario

**Validaciones:**
- Email: no vacio, formato valido
- Contrasena: no vacia

**Navegacion:**
- "Iniciar sesion" → Home (si credenciales validas)
- "Olvidaste la contrasena?" → Recuperar contrasena paso 1/3
- "Registrate" → Registro paso 1/3
- "Iniciar sesion" → valida y navega a Home

**Backend:** `POST /auth/login` con `usuarioOCorreo` + `contrasena`

**Errores:** Credenciales invalidas (401), usuario no verificado (403)

### 14.2 Recuperar contrasena — Paso 1/3

**Fondo:** blanco.

#### Cabecera compacta verde

- Fondo `ColorPrimary`, alto minimo 104dp, padding 28/24/20 (top/h/bottom)
- Titulo "Recuperar contrasena" — 20sp Bold, blanco
- Subtitulo "Te enviamos un enlace a tu correo" — 16sp Regular, blanco

#### Cuerpo (padding 32/28/60)

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  cabecera verde
│  Recuperar contraseña                   │  20sp Bold, blanco
│  Te enviamos un enlace a tu correo      │  16sp Regular, blanco
├─────────────────────────────────────────┤
│                                         │
│         ┌──────────┐                    │
│         │  ✉ 52dp  │                    │  circulo 100dp diametro
│         └──────────┘                    │  fondo ColorPrimary 12% opacidad
│                                         │  icono sobre 52dp ColorPrimary
│   ¿Olvidaste tu contraseña?             │  24sp Bold, ColorPrimary, centrado
│                                         │
│   Descripcion del proceso...            │  16sp Regular, ColorTextMuted
│   (max-width 310dp, line-height 1.6)    │  centrada
│                                         │
│   ┌─ Mensaje de estado (opcional) ─┐    │  caja fondo tintado, 14sp
│   │  Exito/Error                  │    │
│   └───────────────────────────────┘    │
│                                         │
│   Correo electrónico *                  │  label 16sp visible
│   ┌─────────────────────────────────┐    │
│   │ ✉  correo@ejemplo.com          │    │  input estilo registro
│   └─────────────────────────────────┘    │  RadiusInputReg (10dp)
│                                         │
│      [    Enviar enlace    ]             │  pill, max-width ~358dp
│                                         │  fondo ColorPrimary
│                                         │
│      ← Volver al inicio de sesión       │  centrado
│                                         │
└─────────────────────────────────────────┘
```

**Validaciones:**
- Email: no vacio, formato valido

**Navegacion:**
- "Enviar enlace" → Recuperar contrasena paso 2/3 (exito) / error inline
- "Volver al inicio de sesion" → Login

**Backend:** `POST /auth/password-reset/request` con `correo`

**Errores:** Email no registrado (404)

### 14.3 Recuperar contrasena — Paso 2/3

**Prototipo:** No existe en HTML (nuevo)

```
┌─────────────────────────────────┐
│         Logo NS (grande)        │  reg-hero
├─────────────────────────────────┤
│  Titulo: "Verificar codigo"     │  page-text center
│  step indicator: ①──●──②──③   │
├─────────────────────────────────┤
│  [Codigo de verificacion]       │  reg-input (6 digitos)
├─────────────────────────────────┤
│  [    Verificar codigo    ]     │  btn-primary
│  [ Reenviar codigo ]           │  link
└─────────────────────────────────┘
```

- **Tokens:** Hero verde, step indicator, boton pill
- **Validaciones:** Codigo no vacio, 6 digitos numericos
- **Navegacion:**
  - "Verificar codigo" → Recuperar contrasena paso 3/3
  - "Reenviar codigo" → reenvia OTP, permanece en pantalla
- **Backend:** `POST /auth/password-reset/verify` con `correo` + `codigo` (devuelve `resetToken` en la respuesta, que se usa en el paso 3)
- **Nota:** El usuario ingresa el codigo recibido por email

### 14.4 Recuperar contrasena — Paso 3/3

**Prototipo:** No existe en HTML (nuevo)

```
┌─────────────────────────────────┐
│         Logo NS (grande)        │  reg-hero
├─────────────────────────────────┤
│  Titulo: "Nueva contrasena"     │  page-text center
│  step indicator: ①──②──●──③   │
├─────────────────────────────────┤
│  [Nueva contrasena]             │  reg-input (password)
│  [Confirmar contrasena]         │  reg-input (password)
│  ℹ  La contrasena debe...      │  info-box
├─────────────────────────────────┤
│  [  Restablecer  ]              │  btn-primary
│  [ Volver al login ]            │  btn-secondary
└─────────────────────────────────┘
```

- **Tokens:** Hero verde, info-box azul, boton pill
- **Validaciones:** Misma contrasena en ambos campos, cumple requisitos (8+ chars, mayuscula, numero, especial)
- **Navegacion:**
  - "Restablecer" → Login (con mensaje de exito)
  - "Volver al login" → Login
- **Backend:** `POST /auth/password-reset/confirm` con `resetToken` + `nuevaContrasena` + `confirmarContrasena` en el body (el token se obtiene del paso anterior, NO se envía como header Authorization)

### 14.5 Registro — Paso 1/3

**Fondo:** blanco.

#### Patron comun a las 3 pantallas de registro

- **Cabecera verde compacta** (igual que pantalla 2): titulo "Registro - Paso N de 3" (20sp Bold), subtitulo descriptivo (16sp Regular).
- **Indicador de pasos**: 3 puntos circulares de 8dp, centrados, gap 8dp, color `ColorPrimary` — el punto activo con opacidad 100%, los demas con 30%.
- **Cuerpo blanco**, padding 20/24/100(bottom, para dejar espacio al footer fijo).
- **Campo de registro estandar** (`reg-input`): ancho max 358dp, alto 53dp, fondo `ColorPrimaryPale` (#A9D9CB), radio 10dp, padding horizontal 16dp, texto 16sp Regular. Label arriba (16sp Regular, negro) con asterisco rojo si es obligatorio. Hint de error debajo en 12sp Medium rojo.
- **Footer fijo inferior**: fondo blanco, borde superior sutil, 2 botones lado a lado (gap 16dp):
  - Secundario ("Cancelar"/"Atras"): fondo `ColorPrimaryLight`, texto `ColorTextBody`, radio 8dp, alto 44dp, icono + texto.
  - Primario ("Continuar"/"Validar"): fondo `ColorPrimary`, texto blanco, radio 8dp, alto 44dp, icono + texto, sombra teal.

#### 14.5.1 Paso 1 de 3 — Datos de usuario

**Cabecera:** titulo "Registro - Paso 1 de 3", subtitulo "Datos de usuario".

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  cabecera verde
│  Registro - Paso 1 de 3                 │  20sp Bold, blanco
│  Datos de usuario                       │  16sp Regular, blanco
├─────────────────────────────────────────┤
│         ● ─── ○ ─── ○                   │  step indicator (8dp c/u)
├─────────────────────────────────────────┤
│                                         │
│   INFORMACIÓN DEL MENOR                 │  16sp Medium, mayusculas, centrado
│                                         │
│   Nombres completos del menor *         │  label 16sp, asterisco rojo
│   ┌─────────────────────────────────┐    │
│   │                                 │    │  358dp max, 53dp alto
│   └─────────────────────────────────┘    │  fondo ColorPrimaryPale, radio 10dp
│                                         │
│   Fecha de nacimiento *                 │  label 16sp, formato DD/MM/AAAA
│   ┌─────────────────────────────────┐    │  teclado numerico
│   │  DD/MM/AAAA                     │    │
│   └─────────────────────────────────┘    │
│                                         │
│   DATOS DEL ACUDIENTE                   │  16sp Medium, mayusculas, centrado
│                                         │
│   Nombres del acudiente *               │  label 16sp
│   ┌─────────────────────────────────┐    │
│   │                                 │    │
│   └─────────────────────────────────┘    │
│                                         │
│   Cédula del acudiente *                │  label 16sp, max 15 digitos
│   ┌─────────────────────────────────┐    │  teclado numerico
│   │                                 │    │
│   └─────────────────────────────────┘    │
│                                         │
├─────────────────────────────────────────┤  footer fijo
│  [  ✕ Cancelar  ]  [  → Continuar  ]    │  gap 16dp
└─────────────────────────────────────────┘
```

**Validaciones:**
- Nombres del menor: no vacio
- Fecha de nacimiento: no vacia, formato DD/MM/AAAA
- Nombres del acudiente: no vacio
- Cedula del acudiente: no vacia, max 15 digitos numericos
  - **Nota backend:** El backend acepta 6–20 caracteres alfanuméricos (`Validators.isValidCedula`). La UI restringe a 15 dígitos numéricos como decisión de UX para simplificar la experiencia del usuario (menor de edad). El backend es más permisivo; esto es intencional.

**Navegacion:**
- "Continuar" → Registro paso 2/3
- "Cancelar" → Login

**Backend:** Solo validacion local — el POST se hace en paso 3/3

**Notas:** La fecha de nacimiento se almacena para validacion de edad (7-11 anios)

### 14.6 Registro — Paso 2/3

#### 14.6.1 Paso 2 de 3 — Configurar cuenta

**Cabecera:** titulo "Registro - Paso 2 de 3", subtitulo "Configura tu cuenta".

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  cabecera verde
│  Registro - Paso 2 de 3                 │  20sp Bold, blanco
│  Configura tu cuenta                    │  16sp Regular, blanco
├─────────────────────────────────────────┤
│         ○ ─── ● ─── ○                   │  step indicator
├─────────────────────────────────────────┤
│                                         │
│   Correo principal *                    │  label 16sp
│   ┌─────────────────────────────────┐    │
│   │  ✉  correo@ejemplo.com          │    │  358dp max, 53dp alto
│   └─────────────────────────────────┘    │  fondo ColorPrimaryPale, radio 10dp
│                                         │
│   Nombre de usuario *                   │  label 16sp, max 60 caracteres
│   ┌─────────────────────────────────┐    │
│   │  @usuario                       │    │
│   └─────────────────────────────────┘    │
│                                         │
│   Elige un avatar                       │  16sp Medium, centrado
│                                         │
│   ┌────┐ ┌────┐ ┌────┐ ┌────┐           │  49dp diametro c/u, gap 8dp
│   │ av1│ │ av2│ │ av3│ │ +  │           │  RadioGroup visual
│   └────┘ └────┘ └────┘ └────┘           │  "+": fondo #F5F5F5, icono #757575
│                                         │  borde 1.5dp #E0E0E0
│   Avatar seleccionado:                  │  borde 2.5dp ColorPrimary
│   borde 2.5dp + halo teal               │  + halo sombra teal
│                                         │
│   Contraseña *                          │  label 16sp
│   ┌─────────────────────────────────┐    │
│   │ 🔒  ••••••••              👁   │    │  tipo password
│   └─────────────────────────────────┘    │  icono ojo der (44dp toggle)
│                                         │
│   Confirmar contraseña *                │  label 16sp
│   ┌─────────────────────────────────┐    │
│   │ 🔒  ••••••••              👁   │    │
│   └─────────────────────────────────┘    │
│                                         │
│   Mín. 8 caracteres, mayúscula,         │  12sp Medium, hint reglas
│   número y símbolo                      │
│                                         │
│   ┌─────────────────────────────────┐    │  info-box
│   │ La contraseña no puede contener │    │  borde 1dp ColorPrimary
│   │ tu nombre, ser una palabra del  │    │  radio 6dp, padding 12/14
│   │ diccionario ni ser igual al     │    │  14sp
│   │ usuario                         │    │
│   └─────────────────────────────────┘    │
│                                         │
├─────────────────────────────────────────┤  footer fijo
│  [  ✕ Atrás  ]   [  → Continuar  ]     │  gap 16dp
└─────────────────────────────────────────┘
```

**Validaciones:**
- Correo: no vacio, formato valido, no registrado
- Nombre de usuario: no vacio, 3–60 caracteres (`Validators.USERNAME_MIN_LENGTH=3`, `USERNAME_MAX_LENGTH=60`), sin espacios, unico
- Avatar: seleccionado (obligatorio)
- Contrasena: min 8 caracteres, 1 mayuscula, 1 numero, 1 simbolo (!@#$%^&*)
- Confirmar contrasena: debe coincidir
- El "+" NO aplica durante el registro: el backend requiere sesión autenticada (`session-jwt`) para subir foto custom (`PUT /users/me/avatar`). Solo se muestran los 3 presets.
- **Nota backend:** `RegisterRequestDto.avatar` solo acepta `preset:1|2|3` o `null` (`AvatarPreset.kt`). La foto personalizada solo es posible desde "Mi cuenta" tras iniciar sesión.

**Navegacion:**
- "Continuar" → Registro paso 3/3
- "Atras" → Registro paso 1/3

**Backend:** POST provisional con avatar + contrasena (registro completo se envia en paso 3)

**Notas:** Los 3 avatares predefinidos se obtienen del backend. La foto personalizada solo esta disponible desde "Mi cuenta" (requiere sesion activa).

### 14.7 Registro — Paso 3/3

#### 14.7.1 Paso 3 de 3 — Verificacion de correo

**Cabecera:** titulo "Registro - Paso 3 de 3", subtitulo "Verifica tu correo".

```
┌─────────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  cabecera verde
│  Registro - Paso 3 de 3                 │  20sp Bold, blanco
│  Verifica tu correo                     │  16sp Regular, blanco
├─────────────────────────────────────────┤
│         ○ ─── ○ ─── ●                   │  step indicator
├─────────────────────────────────────────┤
│                                         │
│         ┌────────────┐                  │  circulo 122dp diametro
│         │  ✉ 60dp    │                  │  fondo #BDBDBD
│         └────────────┘                  │  icono sobre blanco 60dp
│                                         │
│   Código enviado a                      │  20sp Regular, centrado
│   correo@ejemplo.com                    │  20sp Bold, centrado
│                                         │
│   ┌─────────────────────────────────┐    │
│   │    ingresa el código de 6       │    │  input numerico, 6 digitos
│   │         dígitos                 │    │  max 358dp, centrado
│   └─────────────────────────────────┘    │
│                                         │
│      [   Verificar código   ]            │  pill, ~160dp ancho
│                                         │  fondo ColorPrimary
│      Reenviar código                     │  link de texto
│                                         │
│   ┌─────────────────────────────────┐    │  info-box
│   │ El código expira en 10 minutos. │    │  borde 1dp ColorPrimary
│   │ Si no lo recibes, revisa spam   │    │  radio 6dp
│   │ o reenvíalo                     │    │
│   └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

**Validaciones:**
- Codigo: no vacio, 6 digitos numericos

**Navegacion:**
- "Verificar codigo" → Home (registro exitoso)
- "Reenviar codigo" → reenvia OTP, permanece en pantalla
- No hay boton "Atras" — el usuario no puede volver al paso 2 tras enviar el registro

**Backend:** `POST /auth/register` con todos los datos del paso 1+2, luego `POST /auth/verify-email` con el codigo

**Errores:** Codigo invalido (400), codigo expirado (410)

**Navegacion registro completa:** Paso1 → Paso2 → Paso3 → Home (tras verificar). "Cancelar" en paso 1 → Login. "Atras" en paso 2 → Paso 1.

### 14.8 Home + Sidebar

**Fondo:** blanco.

#### Sidebar (menu lateral / Navigation Drawer)

Usar `ModalNavigationDrawer` de Compose, ancho 70% de pantalla (max 289dp), desliza desde la izquierda, con overlay oscuro semitransparente (50% negro) detras.

**Header del drawer:**
- Fondo `ColorPrimary`, alto minimo 220dp, contenido alineado abajo, padding 32/20/24
- Avatar circular 80dp, fondo `ColorPrimaryLight`, iniciales del usuario en 32sp Bold color `ColorPrimary` (o imagen si tiene avatar)
- Nombre del usuario: 20sp Medium blanco
- Email: 16sp Regular blanco
- Frase de marca: "ERA - Educacion, Repaso y Aprendizaje" 16sp Regular blanco 85% opacidad

```
┌────────────────────────────┐
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  fondo ColorPrimary
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto min 220dp
│▓▓▓  ┌────────┐  ▓▓▓▓▓▓▓▓▓▓│
│▓▓▓  │  MA    │  ▓▓▓▓▓▓▓▓▓▓│  avatar 80dp circular
│▓▓▓  └────────┘  ▓▓▓▓▓▓▓▓▓▓│  fondo ColorPrimaryLight
│▓▓  Maria Lopez              │  20sp Medium, blanco
│▓▓  maria@ejemplo.com        │  16sp Regular, blanco
│▓  ERA - Educación, Repaso   │  16sp Regular, 85% opacidad
│     y Aprendizaje            │
├────────────────────────────┤
│  👤 Mi cuenta               │  icono 28dp + texto 20sp
│  📊 Progreso                │  alto min 44dp, padding 12/20
│  ────────────────────────   │  separador
│  ⚙ Ajustes                  │
│  ❓ Preguntas frecuentes     │
│  ────────────────────────   │  separador
│  🚪 Cerrar sesión           │  color rojo ColorError
└────────────────────────────┘
```

**Items del menu** (lista, fondo blanco, alto min 44dp, padding 12/20, icono 28dp + texto 20sp Regular, gap 20dp entre icono y texto):

| # | Item                   | Icono            | Navega a                  | Nota              |
|---|------------------------|------------------|---------------------------|-------------------|
| 1 | Mi cuenta              | `account_circle` | Pantalla "Mi cuenta"      |                   |
| 2 | Progreso               | `assessment`     | Pantalla de Progreso      |                   |
|   | — separador —          |                  |                           |                   |
| 3 | Ajustes                | `settings`       | Pantalla "Ajustes"        |                   |
| 4 | Preguntas frecuentes   | `help`           | Pantalla "FAQ"            |                   |
|   | — separador —          |                  |                           |                   |
| 5 | Cerrar sesion          | `logout`         | Login                     | Icono rojo        |

**Nota:** REQ-FUN-08 CA2 confirma que cada opción del menú redirige a la pantalla correspondiente. El "Próximamente" aplica solo a la tarjeta de la pantalla principal (REQ-FUN-09 CA3).

#### Hero superior

- Fondo `ColorPrimary`, altura minima 300dp, imagen decorativa de fondo (patron repetido), contenido alineado abajo
- Boton hamburguesa (54dp) arriba-izquierda para abrir el drawer

```
┌─────────────────────────────────────────┐
│  ☰ (54dp)                               │  boton hamburguesa
│                                         │  fondo ColorPrimary
│                                         │  min-height 300dp
│                                         │  imagen decorativa de fondo
│                                         │  contenido alineado abajo
│   ¡Hola!                                │  32sp Bold, blanco
│   Nos alegra tenerte de nuevo            │  20sp Regular, blanco
│   por aquí                              │
└─────────────────────────────────────────┘
```

- Titulo "Hola!" — 32sp **Bold**, blanco (nota: diferente peso al hero de login que es Light)
- Subtitulo "Nos alegra tenerte de nuevo por aqui" — 20sp Regular, blanco

#### Cuerpo — Modos de juego

Fondo blanco, padding 24/16, lista vertical con gap 20dp entre tarjetas.

**Tarjeta "Trivia Escolar" (disponible):**

```
┌─────────────────────────────────────────┐
│                                         │
│           🎯 (56dp, ColorPrimary)       │
│                                         │
│       Trivia Escolar                    │  24sp Bold, #0071C7
│   Cultura general - Nivel primaria      │  16sp Regular, #0071C7
│                                         │
│          [   Jugar   ]                  │  pill, fondo #128A5D
│                                         │  texto blanco 18sp Bold
│                                         │  alto 48dp, min-width 140dp
│                                         │  padding horizontal 32dp
│                                         │  sombra verde
└─────────────────────────────────────────┘
```

- Fondo `#9CFFDB`, radio 30dp (`RadiusCard`), padding 32/24/28, min-height 230dp
- Contenido centrado
- Icono 56dp color `ColorPrimary`
- Titulo "Trivia Escolar" — 24sp Bold, `#0071C7`
- Subtitulo "Cultura general - Nivel primaria" — 16sp Regular, `#0071C7`
- Boton "Jugar": pill, fondo `#128A5D`, texto blanco 18sp Bold, alto 48dp, min-width 140dp, padding horizontal 32dp, sombra verde

**Tarjeta "Proximamente" (bloqueada):**

```
┌─────────────────────────────────────────┐
│                                         │
│           ⏰ (46-56dp, #4A4A4A)         │
│                                         │
│       Próximamente                      │  24sp Bold, #2C2C2C
│   Nuevo modo de juego en ERA            │  16sp Regular
│                                         │
│                                         │  sin boton de accion
└─────────────────────────────────────────┘
```

- Mismo layout que Trivia Escolar
- Fondo gris `#D9D9D9` (`ColorSoonBg`)
- Icono de reloj 46-56dp color `#4A4A4A` (`ColorSoonIcon`)
- Titulo "Proximamente" — 24sp Bold, `#2C2C2C` (`ColorSoonTitle`)
- Subtitulo "Nuevo modo de juego en ERA"
- Sin boton de accion

**Backend:** `GET /users/profile` para datos del usuario, `GET /progress/summary` para resumen

### 14.9 Mi cuenta

> Usa cabecera gris Settings (misma que Ajustes) — es una de las dos pantallas con este
> tratamiento. El backend solo expone 5 campos via `GET /users/me` y solo permite editar
> el username (`PATCH`) y el avatar (`PUT`). La cedula del acudiente y otros datos
> internos nunca se exponen.

**Fondo general:** blanco.

#### Cabecera (Top Bar)

- Mismo estilo que Ajustes: fondo `ColorSettingsHeaderBg` (#767676), alto ~230dp, color plano
- Boton retroceso: circulo 64dp, fondo `ColorSettingsBackBg`, icono `ColorSettingsBackIcon`
- Titulo "Mi Cuenta": 34-36sp Bold blanco

```
┌─────────────────────────────────────────┐
│  ← (circulo 64dp)     Mi Cuenta        │  cabecera gris Settings
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto ~230dp, fondo #767676
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│
└─────────────────────────────────────────┘
```

#### Tarjeta contenedora

- Mismo estilo que Ajustes: borde 1dp `ColorCardBorder`, radio 24dp, padding 24dp
- Superpuesta al final de la cabecera

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │
│  │         ┌────────┐              │    │
│  │         │ Avatar │              │    │  avatar 100dp circular
│  │         └────────┘              │    │  fondo ColorPrimaryLight
│  │         Cambiar avatar          │    │  link texto 14sp ColorPrimary
│  │  ─────────────────────────────  │    │
│  │                                 │    │
│  │  Nombre del menor               │    │  16sp Bold, ColorSettingsLabel
│  │  Maria Lopez                    │    │  18sp Regular, negro
│  │  ─────────────────────────────  │    │  solo lectura
│  │                                 │    │
│  │  Correo electronico             │    │  16sp Bold, ColorSettingsLabel
│  │  maria@ejemplo.com              │    │  18sp Regular, negro
│  │  ─────────────────────────────  │    │  solo lectura
│  │                                 │    │
│  │  Nombre de usuario              │    │  16sp Bold, ColorSettingsLabel
│  │  @maria_lopez          [Editar] │    │  18sp Regular + link "Editar"
│  │  ─────────────────────────────  │    │  solo el usuario es editable
│  │                                 │    │
│  │  Fecha de nacimiento            │    │  16sp Bold, ColorSettingsLabel
│  │  15/03/2015                     │    │  18sp Regular, negro
│  │                                 │    │  solo lectura
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

**Avatar:**
- 100dp circular, fondo `ColorPrimaryLight`
- Si el usuario tiene avatar personalizado: imagen cargada con Coil desde `GET /users/avatar`
- Si no: iniciales del usuario en 32sp Bold `ColorPrimary`
- Link "Cambiar avatar" debajo: 14sp, color `ColorPrimary`, clicable
- Al tocar: abre selector de avatar (mismo patron del registro paso 2)

**Campos:**
- **Nombre del menor:** solo lectura (no editable desde la app)
- **Correo electronico:** solo lectura
- **Nombre de usuario:** editable — link "Editar" a la derecha
  - Abre Dialog con campo de texto (mismo estilo reg-input), boton "Guardar"
  - Validacion: 3-60 caracteres, sin espacios, unico
  - Backend: `PATCH /users/me` con `{ "nombreUsuario": "nuevo_nombre" }`
  - Exito: actualiza el nombre en pantalla
  - Error: muestra error inline en el Dialog
- **Fecha de nacimiento:** solo lectura, formato DD/MM/AAAA

**Backend:**
- `GET /users/profile` — carga los 5 campos
- `PATCH /users/me` — edita solo username
- `PUT /users/avatar` — cambia avatar (Module I)

**Navegacion:**
- Boton retroceso → Home
- Sidebar se oculta en esta pantalla

### 14.10 Ajustes

> Esta pantalla reemplaza el placeholder generico "Proximamente" que hoy usa el sidebar
> para "Ajustes" en el prototipo web. Usa una cabecera **gris neutro** (no verde), distinta
> al resto de la app — es la unica pantalla con este tratamiento.

**Fondo general:** blanco.

#### Colores nuevos para esta pantalla (agregar a `Color.kt`)

| Token                    | Valor HEX | Uso                                                        |
|--------------------------|-----------|------------------------------------------------------------|
| `ColorSettingsHeaderBg`  | `#767676` | Fondo de la cabecera de Ajustes                            |
| `ColorSettingsBackBg`    | `#F2F2F2` | Fondo del boton circular de retroceso                      |
| `ColorSettingsBackIcon`  | `#2C2C2C` | Icono de flecha "atras"                                    |
| `ColorSettingsLabel`     | `#2D3142` | Texto de las etiquetas (gris-azulado oscuro / "slate")     |
| `ColorSwitchTrackOn/Off` | `#2D3142` | Fondo (track) de los switches — mismo color ambos estados  |
| `ColorDivider`           | `#D8D8D8` | Lineas separadoras entre filas                             |
| `ColorCardBorder`        | `#E6E6E6` | Borde de la tarjeta contenedora                             |
| Reutilizar `ColorPrimary`| `#037373` | Texto "Sincronizar ahora"                                  |
| Reutilizar `ColorError`  | `#E24B4A` | Texto "Eliminar cuenta"                                    |

#### Cabecera (Top Bar)

- Fondo `ColorSettingsHeaderBg` (gris neutro, **no** teal), altura ~230dp
- Bloque de color plano (sin imagen decorativa ni gradiente)
- Boton de retroceso: circulo 64dp, fondo `ColorSettingsBackBg`, icono flecha izquierda 24dp color `ColorSettingsBackIcon`, centrado verticalmente, margen ~24dp desde borde izquierdo y ~24dp desde arriba
- Titulo "Ajustes": 34-36sp, **Bold**, color blanco, alineado a la misma altura vertical que el boton de retroceso, margen izquierdo ~24dp

```
┌─────────────────────────────────────────┐
│                                         │
│  ┌────┐                                 │
│  │ ←  │  Ajustes                        │  fondo ColorSettingsHeaderBg (#767676)
│  └────┘                                 │  boton retroceso: circulo 64dp
│    ↕ 24dp margen                        │  fondo #F2F2F2, icono #2C2C2C
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  titulo: 34-36sp Bold blanco
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto ~230dp, color plano
└─────────────────────────────────────────┘
```

#### Tarjeta contenedora

- Card blanca flotando sobre el fondo blanco: borde 1dp `ColorCardBorder`, radio 24dp
- Sin sombra pronunciada (o muy sutil)
- Posicion: superpuesta parcialmente al final de la cabecera gris
- Padding interno: ~24dp en todos los lados
- Lista vertical de filas, separadas por divisor horizontal 1dp `ColorDivider`
- Ultima fila ("Eliminar cuenta") sin divisor debajo

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │  tarjeta contenedora
│  │                                 │    │  fondo blanco, borde 1dp #E6E6E6
│  │  Efectos de sonido      [====○] │    │  radio 24dp
│  │  ─────────────────────────────  │    │  padding 24dp
│  │  Música de fondo        [====○] │    │
│  │  ─────────────────────────────  │    │  divisor 1dp #D8D8D8
│  │  Modo oscuro            [○====] │    │
│  │  ─────────────────────────────  │    │
│  │  Tamaño de texto     Mediano >  │    │
│  │  ─────────────────────────────  │    │
│  │  Sincronizar ahora              │    │
│  │  ─────────────────────────────  │    │
│  │  Eliminar cuenta                │    │  sin divisor debajo
│  │                                 │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

#### Filas de la lista (cada una alto minimo 44dp, padding vertical ~16dp)

**1. Efectos de sonido** — fila con `Switch`:
- Label: 18sp, **Bold**, color `ColorSettingsLabel`, alineado a la izquierda
- `Switch` a la derecha: pill ~56x32dp, track color `ColorSettingsLabel` (mismo tono oscuro sea cual sea el estado), thumb circular blanco
- Estado **activado** en este ejemplo (thumb a la derecha)

**2. Musica de fondo** — misma estructura, estado **activado**

**3. Modo oscuro** — misma estructura, estado **desactivado** (thumb a la izquierda, track del mismo color oscuro — el diseno no usa un track mas claro para el estado "off", solo cambia la posicion del thumb)

**4. Tamano de texto** — fila de navegacion/seleccion (no switch):
- Label a la izquierda: 18sp Bold, color `ColorSettingsLabel`
- Valor actual a la derecha: "Mediano", 18sp **Regular** (no bold), color negro/gris oscuro, alineado a la derecha
- Debe abrir un selector (`Dialog` o `BottomSheet`) con opciones Pequeno/Mediano/Grande al tocar la fila

**5. Sincronizar ahora** — fila de accion tipo link/boton de texto:
- Texto 18sp **Bold**, color `ColorPrimary` (#037373), alineado a la izquierda
- Sin icono ni valor a la derecha
- Toda la fila es clicable (min. 44dp de alto)

**6. Eliminar cuenta** — fila de accion destructiva, ultima de la lista (sin divisor debajo):
- Texto 18sp **Bold**, color `ColorError` (#E24B4A), alineado a la izquierda
- Debe disparar un dialogo de confirmacion antes de ejecutar la accion (buena practica para accion destructiva)

**Persistencia:** Preferencias locales (DataStore, no Room — son config, no datos)

**Navegacion:**
- "Sincronizar ahora" → ejecuta sync, muestra toast/snackbar de resultado
- "Eliminar cuenta" → Eliminar cuenta (confirmacion)
- Boton retroceso → Home
- Sidebar se oculta en esta pantalla

**Backend:** `GET /users/profile` para cargar preferencias, `POST /progress/sync` para sincronizar, `DELETE /users/account` para eliminar

### 14.11 Eliminar cuenta

> Usa cabecera gris Settings (mismo estilo que Ajustes, Mi cuenta y FAQ). Requiere
> contrasena para confirmar (medida de seguridad del backend) y dialogo de confirmacion
> final antes de ejecutar. El backend realiza soft delete — nunca borra fisicamente la fila.

**Fondo general:** blanco.

#### Cabecera (Top Bar)

- Mismo estilo que Ajustes: fondo `ColorSettingsHeaderBg` (#767676), alto ~230dp, color plano
- Boton retroceso: circulo 64dp, fondo `ColorSettingsBackBg`, icono `ColorSettingsBackIcon`
- Titulo "Eliminar cuenta": 34-36sp Bold blanco

```
┌─────────────────────────────────────────┐
│  ← (circulo 64dp)     Eliminar         │  cabecera gris Settings
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto ~230dp, fondo #767676
│    cuenta                              │
└─────────────────────────────────────────┘
```

#### Tarjeta contenedora

- Mismo estilo que Ajustes: borde 1dp `ColorCardBorder`, radio 24dp, padding 24dp
- Superpuesta al final de la cabecera

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │
│  │                                 │    │
│  │         ⚠  (48dp)              │    │  icono advertencia ColorError
│  │                                 │    │
│  │  ¿Estás seguro que deseas       │    │  20sp Bold, ColorSettingsLabel
│  │  eliminar tu cuenta?            │    │  centrado
│  │                                 │    │
│  │  Esta acción es permanente y    │    │  16sp Regular, ColorTextMuted
│  │  no se podrá deshacer. Se      │    │  centrado, max-width 310dp
│  │  eliminarán todos tus datos,    │    │  line-height 1.5
│  │  progreso y avatar.             │    │
│  │                                 │    │
│  │  ─────────────────────────────  │    │  divisor #D8D8D8
│  │                                 │    │
│  │  Contraseña *                   │    │  label 16sp, ColorSettingsLabel
│  │  ┌─────────────────────────┐    │    │
│  │  │ 🔒  ••••••••      👁   │    │    │  input password, estilo registro
│  │  └─────────────────────────┘    │    │  fondo ColorPrimaryPale, radio 10dp
│  │                                 │    │  icono ojo der (44dp toggle)
│  │  └─ contraseña incorrecta  ┘   │    │  hint error 12sp ColorError
│  │                                 │    │  (solo si backend retorna 401)
│  │  ─────────────────────────────  │    │
│  │                                 │    │
│  │  [    Eliminar mi cuenta    ]   │    │  pill, fondo ColorError (#E24B4A)
│  │                                 │    │  texto blanco 16sp Bold
│  │                                 │    │  deshabilitado si contrasena vacia
│  │                                 │    │
│  │  [       Cancelar          ]   │    │  pill, fondo blanco
│  │                                 │    │  borde 2dp ColorSettingsLabel
│  │                                 │    │  texto ColorSettingsLabel 16sp
│  │                                 │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

#### Dialog de confirmacion final

- Aparece al tocar "Eliminar mi cuenta" (despues de ingresar contrasena)
- Modal centrado sobre fondo oscuro 55%

```
┌─────────────────────────────────┐
│                                 │
│  Eliminar cuenta                │  20sp Bold, ColorError
│                                 │
│  ¿Confirmas que deseas          │  16sp Regular, ColorTextDark
│  eliminar tu cuenta de forma    │
│  permanente?                    │
│                                 │
│       [Cancelar]  [Eliminar]    │  Cancelar: borde | Eliminar: fondo ColorError
│                                 │
└─────────────────────────────────┘
```

#### Flujo completo

1. Usuario llega desde Ajustes → "Eliminar cuenta"
2. Ve cabecera gris + tarjeta con advertencia + campo contrasena
3. Ingresa contrasena → boton "Eliminar mi cuenta" se habilita
4. Toca "Eliminar" → aparece Dialog de confirmacion final
5. Toca "Eliminar" en el Dialog → `DELETE /users/account` con `{ "contrasena": "..." }`
6. Exito: limpia JWT (TokenManager), navega a Login
7. Error 401: muestra "Contrasena incorrecta" debajo del input
8. "Cancelar" en cualquier momento → vuelve a Ajustes

**Seguridad:**
- Contrasena obligatoria (medida de seguridad del backend)
- Dialog de confirmacion final (doble verificacion)
- Boton deshabilitado si contrasena vacia

**Backend:** `DELETE /users/me` con `{ "contrasena": "..." }`
- Exito: `200 { "message": "Cuenta eliminada. Tus datos se conservan." }`
- Error: `401 { "error": "INVALID_CREDENTIALS" }`
- Soft delete: cuenta queda con estado `eliminado`, email bloqueado para re-registro

**Navegacion:**
- "Eliminar mi cuenta" (exitoso) → Login (limpia JWT)
- "Cancelar" → Ajustes
- Boton retroceso → Ajustes

### 14.12 FAQ

> Pantalla combinada: preguntas frecuentes (contenido local) + formulario de envio de
> comentarios (backend Module H). Usa cabecera gris Settings (misma que Ajustes y Mi cuenta).
> El backend es write-only para comentarios — no hay endpoint GET para leerlos.

**Fondo general:** blanco.

#### Cabecera (Top Bar)

- Mismo estilo que Ajustes: fondo `ColorSettingsHeaderBg` (#767676), alto ~230dp, color plano
- Boton retroceso: circulo 64dp, fondo `ColorSettingsBackBg`, icono `ColorSettingsBackIcon`
- Titulo "Preguntas frecuentes": 34-36sp Bold blanco

```
┌─────────────────────────────────────────┐
│  ← (circulo 64dp)     Preguntas        │  cabecera gris Settings
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto ~230dp, fondo #767676
│    frecuentes                          │
└─────────────────────────────────────────┘
```

#### Seccion 1: Preguntas frecuentes (contenido local)

- Items colapsables (acordeon), solo 1 abierto a la vez
- Contenido hardcoded (no hay endpoint de FAQ en el backend)

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │
│  │  ¿Cómo juego una trivia?   [▼] │    │  item colapsable
│  │  Responde las preguntas...      │    │  16sp Bold ColorSettingsLabel
│  ├─────────────────────────────────┤    │  respuesta: 14sp Regular
│  │  ¿Qué son los niveles?    [▼] │    │
│  ├─────────────────────────────────┤    │
│  │  ¿Cómo cambio mi avatar?  [▼] │    │
│  ├─────────────────────────────────┤    │
│  │  ¿Cómo recupero mi pass?  [▼] │    │
│  ├─────────────────────────────────┤    │
│  │  ¿Cómo envío un comentario? [▼]│    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

**Preguntas ejemplo (hardcoded):**
1. "¿Como juego una trivia?" — Responde las preguntas seleccionando la opcion correcta antes de que se agote el tiempo...
2. "¿Que son los niveles?" — Los niveles son etapas de aprendizaje. Debes completar cada nivel para desbloquear el siguiente...
3. "¿Como cambio mi avatar?" — Ve a Mi cuenta y pulsa "Cambiar avatar". Puedes elegir uno predefinido o subir una foto...
4. "¿Como recupero mi contrasena?" — En la pantalla de login, pulsa "Olvidaste tu contrasena" y sigue los pasos...
5. "¿Como envio un comentario?" — En esta misma pantalla, desplazate hacia abajo y escribe tu comentario en el formulario...

#### Seccion 2: Formulario de comentario (backend)

- Despues de las FAQ, separador visual
- Textarea para enviar comentario/sugerencia al backend

```
┌─────────────────────────────────────────┐
│  ─────────── separators ──────────────  │
│                                         │
│  Envíanos tu comentario                 │  18sp Bold, ColorSettingsLabel
│                                         │
│  ┌─────────────────────────────────┐    │
│  │                         │    │  textarea
│  │  Escribe aquí tu        │    │  fondo ColorPrimaryPale
│  │  comentario...          │    │  radio 10dp
│  │                         │    │  min 120dp alto
│  └─────────────────────────────────┘    │  max 2000 caracteres
│  0 / 2000                              │  contador 12sp gris
│                                         │
│  [    Enviar comentario    ]            │  pill, fondo ColorPrimary
│                                         │  deshabilitado si vacio
│                                         │  texto blanco 16sp Bold
└─────────────────────────────────────────┘
```

**Textarea:**
- Fondo `ColorPrimaryPale`, radio 10dp, padding 16dp
- Min 120dp alto, max 2000 caracteres
- Placeholder "Escribe aqui tu comentario..." en `ColorTextMuted`
- Contador de caracteres "0 / 2000" debajo, 12sp gris

**Boton "Enviar comentario":**
- Pill, fondo `ColorPrimary`, texto blanco 16sp Bold
- Deshabilitado (opaco 50%) si el textarea esta vacio
- Alto min 44dp, ancho completo

**Comportamiento:**
- Al enviar: valida que no este vacio (max 2000 chars)
- Exito: muestra snackbar "Comentario enviado con exito", limpia el campo
- Error: muestra error inline debajo del textarea
- Reintentar el mismo POST duplica el comentario (backend no deduplica)

**Backend:** `POST /feedback/comments` con `{ "contenido": "..." }`
- El usuario se obtiene del JWT automaticamente (no se envia en el body)
- Respuesta: `{ "message": "Comentario enviado con exito." }`

**Navegacion:**
- Boton retroceso → Home
- Sidebar se oculta en esta pantalla

### 14.13 Niveles de trivia

#### Cabecera con imagen de fondo

- Altura minima 300dp, imagen decorativa a pantalla completa (cover), contenido alineado abajo
- Boton de retroceso (flecha en circulo) arriba-izquierda
- Titulo "Trivia primaria" — 24-28sp Bold, blanco

```
┌─────────────────────────────────────────┐
│  ←  (flecha en circulo)                │  boton retroceso
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  imagen decorativa (cover)
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto min 300dp
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│
│   Trivia primaria                      │  24-28sp Bold, blanco
└─────────────────────────────────────────┘
```

#### Lista de niveles

- Fondo blanco, padding 24/20/32, scroll vertical, gap 14dp entre items
- 20 niveles maximos

#### Item de nivel (fila clicable)

- Alto flexible, padding 16/20, radio 18dp, borde 1.5dp
- Layout horizontal: icono circular 44dp a la izquierda + columna de texto (nombre del nivel 18sp Bold + estado 14sp muted)

```
┌─────────────────────────────────────────┐
│  ┌────┐  Nivel 1: Animales              │  nombre 18sp Bold
│  │ ✓  │  Completado                     │  estado 14sp muted
│  └────┘  (44dp circular)               │
└─────────────────────────────────────────┘
```

**Colores por estado:**

| Estado       | Fondo      | Borde      | Icono circular fondo | Icono         | Texto estado   |
|--------------|------------|------------|----------------------|---------------|----------------|
| Completado   | `#DDF7EA`  | `#1E9E63`  | `#1E9E63`            | Check (✓)     | `#1E9E63`      |
| Disponible   | `#DCEBFB`  | `#2E7FD6`  | `#2E7FD6`            | Numero/Play   | `#2E7FD6`      |
| Bloqueado    | `#ECECEC`  | `#D5D5D5`  | `#E08A3C`            | Candado (🔒)  | `#8A8A8A`      |

**Comportamiento:**
- Solo niveles disponibles y completados son clickeables
- Niveles bloqueados: no clickeables, solo muestran aviso Snackbar

#### Aviso flotante (Snackbar)

- Al tocar nivel bloqueado: fondo `#2C2C2C`, texto blanco 14sp Medium, radio 14dp, centrado abajo
- Animacion: fade + slide (aparece/desaparece)
- Mensaje: "Debes completar el nivel anterior para desbloquear este nivel"

**Navegacion:**
- Nivel disponible/completado → Juego / Pregunta
- Boton retroceso → Home
- Sidebar se oculta en esta pantalla

**Backend:** `GET /progress/levels` para obtener estado de cada nivel

### 14.14 Juego de nivel (Quiz + cronometro)

**Fondo:** gradiente vertical de `#1F6F63` (arriba) a `#2F9E8F` (abajo).

#### Cabecera del quiz

```
┌─────────────────────────────────────────┐
│  ✕ (44dp)   ┌──┐   Nivel 3            │
│  circulo     │10│   (pildora blanca)    │
│  borde       │s │   14sp Bold           │
│  blanco      └──┘   ColorPrimaryDark    │
│  translucido  cronometro                │
│              circular                   │
│              56dp                       │
└─────────────────────────────────────────┘
```

- Boton "X" cerrar: circulo 44dp, borde blanco translucido — abre menu de nivel
- Cronometro circular: 56dp, anillo de progreso SVG que se vacia en sentido antihorario
  - Numero en el centro: 16sp Bold blanco
  - Anillo cambia a rojo `ColorIncorrecta` cuando quedan <=3 segundos
- Badge del numero de nivel: pill blanca, texto 14sp Bold `ColorPrimaryDark`

**Regla de negocio:** cronometro de 10 segundos por pregunta, inicia automatico, no se puede pausar. Si llega a 0 se procesa como respuesta incorrecta.

#### Cuerpo

```
┌─────────────────────────────────────────┐
│                                         │
│  ┌─────────────────────────────────┐    │  imagen ilustrativa (opcional)
│  │         [imagen]                │    │  ancho completo, max 200dp
│  └─────────────────────────────────┘    │  radio 16dp
│                                         │
│  Pregunta X de 20                       │  14sp, blanco 85% opacidad
│                                         │
│  ¿Cuál es el mamífero que más vive?     │  26sp Bold, blanco
│                                         │  line-height 1.3
│                                         │
│  ┌─────────────────────────────────┐    │
│  │          BALLENA                │    │  pill blanco
│  └─────────────────────────────────┘    │  texto negro 17sp Bold mayusculas
│  ┌─────────────────────────────────┐    │  alto min 56dp
│  │         ELEFANTE                │    │  padding 14/20
│  └─────────────────────────────────┘    │  sombra
│  ┌─────────────────────────────────┐    │  gap 16dp entre opciones
│  │          JIRAFA                 │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │           LEON                  │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

- Imagen ilustrativa de la pregunta (opcional): ancho completo, max alto 200dp, radio 16dp
- Contador "Pregunta X de 20" — 14sp, blanco 85% opacidad
- Pregunta — 26sp Bold blanco, line-height 1.3

**Opciones de respuesta:**
- Lista vertical, gap 16dp
- Cada boton pill: fondo blanco, texto negro 17sp Bold mayusculas, alto min 56dp, padding 14/20, sombra
- Al responder: opcion correcta → verde `ColorCorrecta`, incorrecta seleccionada → rojo `ColorIncorrecta`, ambas con texto blanco
- Botones quedan deshabilitados tras responder

#### Banner de resultado (bottom sheet)

- Aparece deslizando desde abajo tras responder
- Fondo blanco, esquinas superiores 24dp, padding 24/28/32

```
┌─────────────────────────────────────────┐
│  ━━━━━━━━━ (handle) ━━━━━━━━━          │
│                                         │
│  Correcto / Incorrecto                  │  26sp Bold
│                                         │  verde si correcto
│                                         │  rojo si incorrecto
│  Mensaje motivacional...                │  16sp Regular, gris oscuro
│                                         │
└─────────────────────────────────────────┘
```

**Comportamiento:**
- Se muestra 3 segundos
- Si es correcto: avanza automaticamente al siguiente nivel (o vuelve al mapa de niveles si no hay mas)
- Si es incorrecto: reinicia la pregunta
- **Excepcion:** 2 fallos consecutivos → activa pausa de descanso

#### Overlay — Menu de nivel (al tocar "X")

- Modal centrado sobre fondo oscuro 55%
- Tarjeta gris `#D9D9D9`, radio 24dp, padding 28/24, ancho max 340dp

```
┌─────────────────────────────────┐
│                                 │
│  ┌─────────────────────────┐   │  imagen ilustrativa
│  │       [imagen]           │   │  radio 14dp, alto 90-160dp
│  └─────────────────────────┘   │
│                                 │
│  [     Continuar     ]         │  pill blanco, texto verde
│  [     Reiniciar     ]         │  pill blanco, texto naranja
│  [      Salir        ]         │  pill blanco, texto rojo
│                                 │
└─────────────────────────────────┘
```

- 3 botones apilados (gap 12dp), pill, fondo blanco, 18sp Bold, alto min 44dp:
  - "Continuar" (texto verde `ColorNivelCompletado`) → cierra el menu
  - "Reiniciar" (texto naranja `ColorNivelBloqueado`) → reinicia la pregunta actual
  - "Salir" (texto rojo `ColorError`) → vuelve a la pantalla de Niveles

#### Overlay — Pausa "Estirate y respira"

- Se activa tras 2 fallos consecutivos en el mismo nivel
- Fondo casi opaco `rgba(2,51,51,0.94)`

```
┌─────────────────────────────────────────┐
│                                         │
│              🧘  56sp                   │
│                                         │
│     Estírate y respira.                 │  24sp Bold, blanco
│     Tómate un momento.                  │  16sp, blanco 90%
│                                         │
│           ┌────────┐                    │
│           │   60   │                    │  circulo 84dp
│           └────────┘                    │  borde 3dp blanco
│                                         │  numero 30sp Bold blanco
│                                         │
└─────────────────────────────────────────┘
```

- Emoji 🧘 56sp, titulo "Estirate y respira." 24sp Bold blanco
- Subtitulo "Tomate un momento." 16sp blanco 90%
- Circulo con cuenta regresiva de 60 segundos: 84dp diametro, borde 3dp blanco, numero 30sp Bold blanco
- Al llegar a 0: cierra el overlay, resetea la racha de fallos, reinicia la pregunta automaticamente

**Backend:** `POST /progress/submit` con respuestas al finalizar la trivia

### 14.15 Progreso

#### Cabecera con imagen de fondo

- Mismo patron que Niveles: imagen de fondo a pantalla completa, boton atras (flecha en circulo), titulo "Progreso"

```
┌─────────────────────────────────────────┐
│  ←  (flecha en circulo)                │
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  imagen decorativa (cover)
│▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓│  alto min 300dp
│   Progreso                              │  24-28sp Bold, blanco
└─────────────────────────────────────────┘
```

#### Cuerpo

- Fondo blanco, padding 32/24, gap 24dp entre tarjetas

```
┌─────────────────────────────────────────┐
│                                         │
│  ┌─────────────────────────────────┐    │  tarjeta "Reintentos totales"
│  │          42                      │    │  fondo #ECECEC, radio 18dp
│  │     Reintentos totales          │    │  padding 28/24, centrada
│  └─────────────────────────────────┘    │  numero: 40sp Bold ColorNivelCompletado
│                                         │  label: 16sp Bold negro
│  ┌─────────────────────────────────┐    │  tarjeta "Niveles completados"
│  │  Niveles completados    8 / 20  │    │  fondo #ECECEC, radio 18dp
│  │  ━━━━━━━━━━━━━░░░░░░░░░░░░░░░  │    │  padding 28/24
│  └─────────────────────────────────┘    │  label 16sp Bold izquierda
│                                         │  fraccion 14sp gris derecha
│                                         │  barra: alto 12dp, radio 6dp
│                                         │  fondo #D5D5D5
│                                         │  relleno ColorNivelCompletado animado
│                                         │  ancho segun % completado
│                                         │
│                                         │
│                                         │
│  ┌─────────────────────────────────┐    │  boton outline
│  │     Reiniciar mi progreso       │    │  borde 2dp rojo, sin relleno
│  └─────────────────────────────────┘    │  texto rojo 16sp Medium
│                                         │  pill, alto min 44dp
│                                         │  ancho completo
│                                         │  pide confirmacion antes de borrar
└─────────────────────────────────────────┘
```

**Tarjeta "Reintentos totales":**
- Fondo `#ECECEC`, radio 18dp, padding 28/24, centrada
- Numero grande: 40sp Bold, color `ColorNivelCompletado`
- Label: "Reintentos totales" 16sp Bold negro

**Tarjeta "Niveles completados":**
- Mismo fondo/radio, alineacion izquierda
- Label "Niveles completados" 16sp Bold
- Barra de progreso: alto 12dp, radio 6dp, fondo `#D5D5D5`, relleno `ColorNivelCompletado` animado (ancho segun % completado)
- Fraccion "X / 20 niveles" alineada a la derecha, 14sp, gris muted

**Boton "Reiniciar mi progreso":**
- Outline (borde 2dp rojo, sin relleno), texto rojo 16sp Medium
- Pill, alto min 44dp, ancho completo, al final de la pantalla
- Al presionar: pide confirmacion antes de borrar (dialogo de confirmacion)
- **Nota:** El prototipo web no confirma, pero se agrega confirmacion como buena practica en la version nativa

**Backend:** `GET /progress/summary` para estadisticas. **Pendiente: endpoint de reinicio de progreso** — ver §17.

### 14.16 Proximamente

**Prototipo:** `section#screen-proximamente` (index.html)

```
┌─────────────────────────────────┐
│ ≡  Logo                   NS   │  header-green
├─────────────────────────────────┤
│                                 │
│         🚧                      │
│  "Proximamente"                 │  page-text center
│  "Este modulo esta en          │  page-text center
│   desarrollo..."               │
│                                 │
└─────────────────────────────────┘
```

- **Tokens:** Header verde, centrado vertical
- **Uso:** Placeholder para modulos que aun no existen (Ranking, Logros, Tienda, etc.)
- **Navegacion:** Sidebar accesible desde ≡
- **Backend:** Sin llamadas a API

---

## 15. Navegacion

### 15.1 Grafo de navegacion

```
                        ┌──────────┐
                        │  Login   │
                        └────┬─────┘
           ┌─────────────────┼─────────────────┐
           │                 │                 │
           ▼                 ▼                 ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │   Registro   │  │  Recuperar   │  │     Home     │
   │   (1/3→2/3   │  │  contrasena  │  │   + Sidebar  │
   │    →3/3)     │  │  (1/3→2/3    │  └──────┬───────┘
   └──────┬───────┘  │   →3/3)      │         │
          │          └──────┬───────┘         │
          ▼                 ▼          ┌──────┼──────┐
   ┌──────────────┐  ┌──────────┐     │      │      │
   │     Home     │  │  Login   │     ▼      ▼      ▼
   │   + Sidebar  │  │(exito)   │  ┌──────┐┌─────┐┌─────┐
   └──────────────┘  └──────────┘  │ Mi   ││Ajust││FAQ  │
                                   │cuenta││es   ││     │
                                   └──────┘└──┬──┘└─────┘
                                              │
                                              ▼
                                       ┌──────────┐
                                       │ Eliminar │
                                       │ cuenta   │
                                       └──────────┘

   Home + Sidebar ──→ Niveles ──→ Juego ──→ Resumen
   Home + Sidebar ──→ Progreso
```

### 15.2 Rutas tipadas (Compose Navigation)

| Ruta                    | Parametros         | Pantalla                |
|-------------------------|--------------------|-------------------------|
| `login`                 | —                  | Login                   |
| `register/step1`        | —                  | Registro 1/3            |
| `register/step2`        | —                  | Registro 2/3            |
| `register/step3`        | —                  | Registro 3/3            |
| `recover/step1`         | —                  | Recuperar 1/3           |
| `recover/step2`         | —                  | Recuperar 2/3           |
| `recover/step3`         | —                  | Recuperar 3/3           |
| `home`                  | —                  | Home                    |
| `profile`               | —                  | Mi cuenta               |
| `settings`              | —                  | Ajustes                 |
| `delete-account`        | —                  | Eliminar cuenta         |
| `faq`                   | —                  | FAQ                     |
| `levels`                | —                  | Niveles de trivia       |
| `game/{levelId}`        | levelId: Int       | Juego / Pregunta        |
| `progress`              | —                  | Progreso                |
| `coming-soon`           | —                  | Proximamente            |

### 15.3 Sidebar — rutas de navegacion

| Item              | Ruta destino   | Color barra  | Categoria |
|-------------------|----------------|--------------|-----------|
| Mi cuenta         | `profile`      | Verde        | Nav       |
| Progreso          | `progress`     | Verde        | Nav       |
| Ajustes           | `settings`     | Verde        | Nav       |
| FAQ               | `faq`          | Gris         | Support   |
| Cerrar Sesion     | `login` (limpia JWT) | Gris  | Exit      |

---

## 16. Decisiones de diseno para pantallas faltantes

### 16.1 Mi cuenta — Decision

- **Estructura:** Cabecera gris Settings + tarjeta contenedora con avatar 100dp, 4 campos (nombre menor, correo, usuario, fecha nacimiento) y links de accion
- **Por que este diseno:**
  - Cabecera gris: misma identidad visual que Ajustes — es una de las dos pantallas "de configuracion" de la app
  - Solo username editable: el backend (`PATCH /users/me`) solo permite cambiar `nombreUsuario`. Cedula del acudiente, nombre del acudiente y otros datos internos nunca se exponen (decision de seguridad D-4 del backend)
  - Avatar editable via Module I (`PUT /users/avatar`)
  - Campos de solo lectura: nombreMenor, correo, fechaNacimiento — el backend los devuelve pero no permite editarlos
- **Componentes reutilizados:** cabecera gris Settings, tarjeta contenedora (borde + radio 24dp), Dialog para editar username, selector de avatar
- **Diferencia del prototipo:** No existia en el prototipo HTML; se diseno segun los endpoints del backend (modulo D) y las restricciones de seguridad
- **Fecha de decision:** 2026-08-17 (Fase 0) — actualizado 2026-08-17 con diseno detallado

### 16.2 Ajustes — Decision

- **Estructura:** Lista de preferencias con toggles (sonido, musica, modo oscuro) y selector de tamano de texto. Seccion de "Sincronizar ahora" y enlace a "Eliminar cuenta"
- **Por que este diseno:** El usuario corrigio la interpretacion inicial — Ajustes NO es perfil, es configuracion de la app. Header es GRIS, no verde. Las preferencias se almacenan localmente (DataStore)
- **Componentes reutilizados:** header-gris (nuevo, variante del header), screen-title, toggle switches
- **Exclusion:** "Mi cuenta" y "FAQ" NO estan dentro de Ajustes — son items separados del sidebar
- **Fecha de decision:** 2026-08-17 (Fase 0)

### 16.3 Eliminar cuenta — Decision

- **Estructura:** Cabecera gris Settings + tarjeta contenedora con icono de advertencia, texto de aviso, campo contrasena, botones Eliminar (rojo) y Cancelar, mas dialogo de confirmacion final
- **Por que este diseno:**
  - Cabecera gris: misma identidad visual que Ajustes, Mi cuenta y FAQ (pantallas "de configuracion")
  - Contrasena obligatoria: el backend (`DELETE /users/me`) exige re-verificacion de contrasena como medida de seguridad
  - Dialogo de confirmacion final: doble verificacion antes de ejecutar una accion destructiva (buena practica nativa, no existia en el prototipo web)
  - Soft delete: el backend nunca borra fisicamente la fila — la cuenta queda con estado `eliminado`, el email se bloquea para re-registro
  - Boton deshabilitado si contrasena vacia: previene envios accidentales
- **Componentes reutilizados:** cabecera gris Settings, tarjeta contenedora (borde + radio 24dp), input password estilo registro (fondo ColorPrimaryPale, icono ojo toggle), pill buttons
- **Diferencia del prototipo:** El prototipo web no tenia esta pantalla; se diseno segun el Module E del backend con mejoras de UX nativas (dialogo confirmacion)
- **Backend:** `DELETE /users/me` con `{ "contrasena": "..." }` → soft delete
- **Fecha de decision:** 2026-08-17 (Fase 0) — actualizado 2026-08-17 con diseno detallado

### 16.4 FAQ — Decision

- **Estructura:** Cabecera gris Settings + 2 secciones: FAQ local (items colapsables) + formulario de comentario (textarea + boton enviar)
- **Por que este diseno:**
  - FAQ es contenido local/offline — no hay endpoint de FAQ en el backend; el contenido es fijo y no cambia frecuentemente
  - El backend Module H provee `POST /feedback/comments` para enviar comentarios/sugerencias — es write-only (no hay GET para leerlos)
  - Cabecera gris: misma identidad visual que Ajustes y Mi cuenta (pantallas "de configuracion")
  - Items colapsables (acordeon): patron estandar para FAQ, solo 1 abierto a la vez
  - Formulario de comentario: textarea con max 2000 chars, contador, boton deshabilitado si vacio
- **Componentes reutilizados:** cabecera gris Settings, tarjeta contenedora, items colapsables, textarea, pill button
- **Contenido FAQ:** 5 preguntas hardcoded cubriendo: como jugar, niveles, avatar, recuperar contrasena, enviar comentarios
- **Backend:** `POST /feedback/comments` — el usuario se obtiene del JWT automaticamente
- **Fecha de decision:** 2026-08-17 (Fase 0) — actualizado 2026-08-17 con seccion de comentarios

### 16.5 Recuperar contrasena paso 2/3 — Decision

- **Estructura:** Hero verde + step indicator + input de 6 digitos + boton "Verificar" + link "Reenviar codigo"
- **Por que este diseno:** Consistente con el flujo de registro paso 3/3 (mismo patron de verificacion OTP). El backend expone `POST /auth/password-reset/verify`
- **Componentes reutilizados:** reg-hero, step-indicator, reg-input, btn-primary
- **Diferencia del prototipo:** No existia en el prototipo; se diseño segun el modulo C del backend
- **Fecha de decision:** 2026-08-17 (Fase 0)

### 16.6 Recuperar contrasena paso 3/3 — Decision

- **Estructura:** Hero verde + step indicator + 2 inputs (nueva contrasena + confirmar) + info-box + boton "Restablecer"
- **Por que este diseno:** Consistente con el registro paso 2/3 (mismo patron de contrasena + info-box). El backend expone `POST /auth/password-reset/confirm`
- **Componentes reutilizados:** reg-hero, step-indicator, reg-input, info-box, btn-primary
- **Diferencia del prototipo:** No existia en el prototipo; se diseño segun el modulo C del backend
- **Fecha de decision:** 2026-08-17 (Fase 0)

---

## 17. Endpoint de reinicio de progreso

> **Estado:** Implementado y verificado en el backend (`BACKEND_ERA`, 2026-08-22).
> **Propósito:** Dar soporte al botón "Reiniciar mi progreso" de la pantalla de Progreso
> (§14.15).

### 17.1 Endpoint

```
POST /api/v1/progress/reset
Content-Type: application/json
Authorization: Bearer <session-token>

{ "contrasena": "MiPassword123!" }
```

**Comportamiento:**
1. Requiere sesión autenticada (`session-jwt`) — mismo patrón que `GET/POST /progress/sync`
2. Requiere `contrasena` en el body (reverificación, patrón D-3 del Módulo E)
3. Borra todos los `intento` y `progreso_usuario` del usuario
4. Inserta nivel 1 como `disponible`
5. Devuelve `200` con `ProgresoSyncResponseDto` (snapshot post-reset)

### 17.2 Tablas involucradas

| Tabla | Qué hacer | Por qué |
|---|---|---|
| `intento` | DELETE todas las filas del usuario | FK RESTRICT exige este orden |
| `progreso_usuario` | DELETE + INSERT nivel 1 | Reset completo del avance |
| `nivel` | Solo lectura (obtener id de nivel 1) | Catálogo compartido |

### 17.3 Seguridad

- Requiere sesión (`session-jwt`) — sin token → 401 `UNAUTHORIZED`
- Requiere `contrasena` no vacía — vacía → 400 `VALIDATION_ERROR`
- Contraseña incorrecta → 401 `INVALID_CREDENTIALS` (mensaje genérico)
- Cuenta eliminada → 403 `ACCOUNT_INACTIVE`
- Anti-carrera: `FOR UPDATE` → bcrypt fuera de transacción → segunda transacción con guarda
- El frontend **debe** mostrar dialogo de confirmación antes de enviar

### 17.4 Respuesta

```json
{
  "progreso": [
    { "orden": 1, "estadoNivel": "disponible", "intentosTotales": 0, "completadoEn": null, "ultimaInteraccion": "..." }
  ],
  "resumen": { "nivelesCompletados": 0, "totalNiveles": 20, "totalReintentos": 0 }
}
```

### 17.5 Archivos relevantes del backend

| Archivo | Propósito |
|---|---|
| `routes/ProgressRoutes.kt:36` | `post("/reset")` dentro de `authenticate("session-jwt")` |
| `controllers/ProgressController.kt:115` | `reiniciarProgreso(call)` — validación de forma |
| `services/ProgressSyncService.kt:143` | `reiniciarProgreso()` — anti-carrera + reset atómico |
| `repositories/ProgresoRepository.kt` | `deleteByUsuario()` + `ensureNivel1Disponible()` |
| `repositories/ExposedProgresoRepository.kt:81` | Implementación real con DELETE + INSERT |
| `repositories/FakeProgresoRepository.kt:62` | Fake para tests unitarios |
| `models/dto/ReiniciarProgresoRequestDto.kt` | `{ contrasena: String = "" }` |
| `models/entities/IntentionTable.kt` | Mapeo mínimo de `intento` (solo DELETE) |

### 17.6 Tests

- **Service tests** (`ProgressSyncServiceTest`): 7 tests — happy path, nivel 1 previo, nivel 1 sin fila, cuenta eliminada, contraseña incorrecta, usuario inexistente, 2 transacciones lock+reset
- **Route tests** (`ProgressControllerTest`): 8 tests — 401 sin token, 401 token reseteo, 200 happy path, 400 campo ausente, 400 vacía, 403 cuenta eliminada, 401 contraseña incorrecta, 400 sin campo contrasena
