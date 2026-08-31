# Fase 7 — Progreso / Sync (Módulo G): Análisis y Diseño

> Documento de análisis para la persistencia local y sincronización de progreso.
> Registra el alcance, la estrategia offline-first, el modelo de datos Room, el catálogo oficial y el plan por capas.
> **Estado:** DISEÑO FINALIZADO (Revisión 2).

---

## 1. Objetivo y Alcance

**Objetivo:** Implementar un sistema de persistencia local robusto que permita jugar sin conexión a internet y sincronizar el progreso de forma transparente con el servidor ERA.

**Alcance:**
- Configuración de **Room Database** (`EraDatabase`).
- Persistencia del **Catálogo Oficial de 20 niveles** (datos estáticos).
- Persistencia del **progreso por usuario** (aislado por `userId`).
- Consumo de endpoints de sincronización (`GET/POST /progress/sync`).
- Lógica de **merge determinista** (servidor es autoridad final, pero acepta avances locales).
- Detección de cambios locales pendientes de sincronizar ("dirty bits").
- Reinicio de progreso local y remoto (`POST /progress/reset`).

---

## 2. Estado Actual (Auditoría)

| Componente | Estado | Notas |
|---|---|---|
| `ProgressApi` | ✅ Implementada | `getProgress()` y `syncProgress()` listos. |
| DTOs de Progreso | ✅ Implementados | `LevelProgress`, `ProgressSyncItem`, etc. |
| Capa `data/` | ❌ Vacía | No existen entidades, DAOs ni Database. |
| Catálogo de Trivia | ✅ Definido | Contenido de los 20 niveles aprobado (ver §4). |
| Sesión | ✅ Funcional | `SesionRepository` permite obtener el token del usuario actual. |
| Errores | ✅ Extensible | `ErrorMapper` y `MensajeError` listos para nuevos casos. |

---

## 3. Contrato API (Módulo G)

Ruta base: `/api/v1/progress`. Autenticación: `Bearer <JWT>`.

| Método | Endpoint | Request Body | Response (200 OK) |
|---|---|---|---|
| `GET` | `/sync` | Ninguno | `ProgressSyncResponse` (Snapshot completo) |
| `POST` | `/sync` | `ProgressSyncRequest` | `ProgressSyncResponse` (Snapshot mergeado) |
| `POST` | `/reset`| `{ "contrasena": "..." }` | `ProgressSyncResponse` (Snapshot vacío) |

### Reglas de Negocio del Servidor:
1. **Merge hacia adelante:** El servidor solo acepta cambios que avancen el estado (`bloqueado < disponible < completado`).
2. **Monotonicidad:** Los intentos totales solo pueden aumentar.
3. **Idempotencia:** Enviar el mismo estado varias veces no produce efectos secundarios negativos.

---

## 4. Catálogo Real (Contenido Definitivo)

El catálogo consta de exactamente 20 niveles. Cada nivel tiene un `orden` (1..20), una `pregunta`, tres `opciones` y un índice de `respuestaCorrecta`.

| Orden | Pregunta | Opciones (A, B, C) | Correcta |
|---|---|---|---|
| 1 | ¿Cuál es la capital de Francia? | Madrid, París, Roma | B (1) |
| 2 | ¿Cuántos lados tiene un triángulo? | Cuatro, Cinco, Tres | C (2) |
| 3 | ¿Cuánto es 6 × 7? | 42, 36, 49 | A (0) |
| 4 | ¿Cuál es el antónimo de complicado? | Difícil, Sencillo, Complejo | B (1) |
| 5 | ¿Cuál es la antepenúltima letra del abecedario? | W, Y, X | C (2) |
| 6 | ¿Cuántas semanas hay en un año? | 48, 52, 60 | B (1) |
| 7 | ¿Cuál es el satélite natural de la Tierra? | Marte, El Sol, La Luna | C (2) |
| 8 | ¿Qué color se obtiene al mezclar rojo y azul? | Verde, Morado, Naranja | B (1) |
| 9 | ¿En qué continente se encuentra el río Amazonas? | África, Europa, América | C (2) |
| 10 | ¿Cuál es el continente más grande del mundo? | África, Asia, Europa | B (1) |
| 11 | ¿Cuántas sílabas tiene la palabra "elefante"? | Tres, Cinco, Cuatro | C (2) |
| 12 | ¿Cuántos años hay en un milenio? | 100, 1000, 10000 | B (1) |
| 13 | ¿Cómo se clasifican los animales que comen carne? | Herbívoros, Omnívoros, Carnívoros | C (2) |
| 14 | ¿Qué fruta es rica en vitamina C? | Manzana, Naranja, Banano | B (1) |
| 15 | ¿En qué año llegó Cristóbal Colón a América? | 1490, 1492, 1500 | B (1) |
| 16 | ¿Qué cultura construyó Machu Picchu? | Maya, Azteca, Inca | C (2) |
| 17 | ¿Tres docenas de manzanas equivalen a cuántas? | 24, 36, 48 | B (1) |
| 18 | ¿En qué país vivieron los faraones? | Grecia, Egipto, Italia | B (1) |
| 19 | ¿Cómo se dice "madera" en inglés? | Stone, Tree, Wood | C (2) |
| 20 | ¿En qué juego se usa el término "jaque mate"? | Damas, Fútbol, Ajedrez | C (2) |

---

## 5. Diseño de Room (Modelo Local)

Se separan los datos estáticos (preguntas) de los datos dinámicos (progreso).

### 5.1 Ubicación Técnica: JSON en `assets`
- **Ubicación:** `app/src/main/assets/trivia_catalog.json`.
- **Estructura del JSON:**
  ```json
  [
    { 
      "orden": 1, 
      "pregunta": "¿Cuál es la capital de Francia?", 
      "opciones": ["Madrid", "París", "Roma"], 
      "respuestaCorrecta": 1 
    },
    ...
  ]
  ```

### 5.2 Entidad `NivelEntity` (Catálogo Estático)
- **Atributos:**
    - `orden: Int` (Primary Key).
    - `pregunta: String`.
    - `opcionA: String`.
    - `opcionB: String`.
    - `opcionC: String`.
    - `respuestaCorrecta: Int` (Índice 0..2).
- **Inmutabilidad:** El catálogo se considera **inmutable** por versión de base de datos. Cualquier cambio en las preguntas requerirá incrementar la versión de la base de datos de Room y ejecutar una migración que repueble la tabla.

### 5.3 Entidad `ProgresoNivelEntity` (Avance Dinámico)
- **Atributos:**
    - `userId: String` (PK).
    - `nivelOrden: Int` (PK).
    - `estadoNivel: String` (`BLOQUEADO`, `DISPONIBLE`, `COMPLETADO`).
    - `intentosTotales: Int`.
    - `intentosFallidosConsecutivos: Int`.
    - `completadoEn: String?` (ISO Date del servidor).
    - `sincronizado: Boolean` (Dirty bit: `true` si coincide con el servidor).

---

## 6. Room + Seed (Estrategia de Inicialización)

### 6.1 Comportamiento de `onCreate()`
- **Instalación nueva:** Al crear la base de datos por primera vez, `RoomDatabase.Callback.onCreate()` leerá el archivo JSON, validará los 20 registros (orden secuencial, no nulos, 3 opciones) e insertará en `NivelEntity`.
- **Base de Datos existente:** `onCreate()` **no se ejecuta**. Los datos de `NivelEntity` persisten entre ejecuciones de la app.
- **Actualización de App:** Si el catálogo cambia en una nueva versión de la app, se debe subir la versión de la BD (`databaseVersion`) y manejar el repoblamiento en `onUpgrade()` o mediante una migración destructiva controlada para `NivelEntity`.

### 6.2 Robustez y Error Handling durante el Seed
- **JSON Corrupto/Inválido:** Si la lectura o el parsing fallan, la creación de la BD fallará. Se debe envolver el proceso en un `try-catch` que registre el error. En caso de fallo crítico en el seed, la app no podrá mostrar niveles (se requiere que el catálogo sea íntegro para jugar).
- **Validación durante el Seed:** Se verificará que cada nivel tenga exactamente 3 opciones y un índice de respuesta válida (0..2).

---

## 7. Forward-Only Merge (Lógica Determinista)

El objetivo es que el progreso nunca retroceda y la sincronización sea robusta ante errores de red.

### 7.1 Reglas de Oro
1.  **Prevalencia de Estado:** `COMPLETADO > DISPONIBLE > BLOQUEADO`.
2.  **Sincronización de Salida (Upload):** Solo se envían registros con `sincronizado = false`.
3.  **Sincronización de Entrada (Download):** La respuesta del servidor (snapshot) se aplica localmente usando lógica `max()`.

### 7.2 Casos de Uso y Ejemplos

| Escenario | Estado Local | Estado Servidor | Acción / Resultado Final |
|---|---|---|---|
| **Local > Servidor** | Nivel 1: `COMPLETADO` | Nivel 1: `DISPONIBLE` | Se envía Local. Servidor actualiza a `COMPLETADO`. Local marca `sincronizado=true`. |
| **Servidor > Local** | Nivel 1: `DISPONIBLE` | Nivel 1: `COMPLETADO` | Download pisa Local. Nivel 1 pasa a `COMPLETADO` localmente. |
| **Local Dirty + Servidor Menor** | Nivel 2: `COMPLETADO` (dirty) | Nivel 2: `BLOQUEADO` | Se envía Local. Prevalece `COMPLETADO`. |
| **Local Dirty + Servidor Mayor** | Nivel 2: `DISPONIBLE` (dirty) | Nivel 2: `COMPLETADO` | Prevalece Servidor (`COMPLETADO`). Local marca `sincronizado=true`. |
| **Igualdad** | Nivel 1: `COMPLETADO` (dirty) | Nivel 1: `COMPLETADO` | Local marca `sincronizado=true`. No hay cambios. |
| **Cambio de Usuario** | — | — | El `userId` en la PK asegura que el progreso del Usuario A no se vea ni se pise con el del Usuario B. |

### 7.3 Flujo de Sincronización (Idempotente)
1.  **Paso 1 (Upload):** Se consultan todos los `ProgresoNivelEntity` con `sincronizado = false`.
2.  **Paso 2 (API):** Se envían al servidor mediante `POST /sync`.
3.  **Paso 3 (Merge):** Al recibir la respuesta (snapshot completo):
    -   Para cada nivel en el snapshot:
        -   `estado = max(local, remoto)`.
        -   `intentosTotales = max(local, remoto)`.
        -   Si `estado_remoto >= estado_local` AND `intentos_remotos >= intentos_locales` -> `sincronizado = true`.
4.  **Error de Red / Reintento:** Si el POST falla, los registros permanecen con `sincronizado = false`. El siguiente disparo (p. ej. al abrir la pantalla de progreso) lo reintentará con los mismos datos.

---

## 8. Plan de implementación por Capas

### Capa 1: Room e Inicialización (Trivia Seed)
- Crear `trivia_catalog.json` en assets con las 20 preguntas.
- Definir `NivelEntity` (con `opcionA/B/C`).
- Definir `ProgresoNivelEntity` con PK compuesta.
- Implementar `RoomDatabase.Callback` con validación de integridad del JSON.

### Capa 2: Repository y Local-First
- Implementar `ProgresoRepository` (Local).
- Lógica de aislamiento por `userId` (obtenido de `SesionRepository`).
- Métodos para actualizar intentos y marcar niveles como completados (fijando `sincronizado = false`).

### Capa 3: Sincronización (Sync Logic)
- Integración con `ProgressApi`.
- Implementación de la lógica de merge detallada en §7.
- Manejo de respuestas 401/403 (cierre de sesión centralizado).

### Capa 4: ViewModel e Integración UI
- `ProgresoViewModel` para alimentar la pantalla de progreso.
- Disparadores de sync automáticos.

---

## 9. Definition of Done (Fase 7)
- Archivo `assets/trivia_catalog.json` contiene las 20 preguntas reales y válidas.
- Room inicializa el catálogo íntegramente en la primera ejecución (Seed exitoso).
- El progreso se guarda localmente (aislado por usuario) y sobrevive al cierre de la app.
- La sincronización resuelve conflictos a favor del estado más avanzado (Forward-only).
- La sincronización es idempotente (reintentos no duplican progreso).
- Suite de tests unitarios e instrumentados al día y en verde.
