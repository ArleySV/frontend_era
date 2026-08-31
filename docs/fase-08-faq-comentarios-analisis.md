# Fase 8 — FAQ y Comentarios (Módulo H): Análisis y Diseño

> Documento de análisis para la funcionalidad de preguntas frecuentes y envío de sugerencias.
> Registra el alcance, el diseño visual, las decisiones técnicas y el plan de implementación.
> **Estado:** DISEÑO FINALIZADO (Revisión 2).

---

## 1. Objetivo

Implementar una sección informativa (FAQ) disponible de forma offline y un canal de comunicación unidireccional (Comentarios) para que los usuarios envíen retroalimentación al equipo de ERA.

## 2. Estado Actual (Auditoría)

| Componente | Hallazgo | Estado |
|---|---|---|
| `FeedbackApi` | Posee el método `sendComment(@Body request: CommentRequest)`. | ✅ Existe |
| `CommentRequest` | DTO con campo `contenido: String`. | ✅ Existe |
| `MessageResponse` | DTO de respuesta genérica con `message: String`. | ✅ Existe |
| Contrato Backend | `POST /api/v1/feedback/comments` recibe y persiste el comentario. | ✅ Verificado |
| Almacenamiento FAQ | No existe definición técnica ni contenido en el repositorio. | ❌ Falta |
| Pantalla FAQ | No implementada. | ❌ Falta |

## 3. Hallazgos Frontend/Backend

- **Backend:** Es **stateless** para las FAQ (no las sirve). Solo recibe comentarios. No hay mecanismo de respuesta (unidireccional).
- **Aislamiento:** El backend asocia el comentario al usuario mediante el JWT (claim `sub`).
- **Validación:** El servidor limita a 2000 caracteres. El cliente debe validar esto antes de enviar.
- **Offline:** Las FAQ deben funcionar sin internet. El envío de comentarios requiere conexión.

## 4. Flujo de Comentarios y Manejo de Errores

### 4.1 Ciclo de vida del borrador
**Decisión (D-53): El borrador del comentario reside únicamente en la memoria del ViewModel.**
- **Conexión perdida:** Si el envío falla (p.ej. `ErrorConexion`), el texto **permanece** en el `uiState` del `FaqViewModel`. El usuario puede pulsar "Enviar" nuevamente tras recuperar red.
- **Cierre de pantalla/app:** Si el usuario sale de la pantalla de FAQ o cierra la aplicación, el borrador **se pierde**. 
- **Justificación:** No se utiliza Room para comentarios para evitar acumular PII (Datos Personales) localmente de forma innecesaria, cumpliendo con el principio de mínimo privilegio de `CLAUDE.md`.

### 4.2 Lógica de envío
1. El usuario escribe en un `EraTextField` multi-línea.
2. El botón "Enviar" se habilita solo si: `contenido.isNotBlank()` AND `contenido.length <= 2000`.
3. Al pulsar "Enviar", se dispara `feedbackRepository.enviarComentario(contenido.trim())`.

## 5. Contrato API (Verificado contra Backend ERA)

`POST /api/v1/feedback/comments`
- **Autenticación:** Obligatoria vía `Bearer <JWT>`.
- **Request Body:** `{ "contenido": "..." }`
- **Validación Servidor:** `400 VALIDATION_ERROR` si está vacío o supera 2000 caracteres.
- **Aislamiento:** `403 ACCOUNT_INACTIVE` si la cuenta está eliminada.
- **Response (200 OK):** `{ "message": "Comentario enviado con éxito." }`

## 6. Preguntas Frecuentes (FAQ)

### 6.1 Contenido Oficial y Verificación
He revisado las respuestas contra el estado real del proyecto:

1. **¿Cómo empiezo a jugar?**
   Una vez que inicies sesión, presiona el botón "Trivia" en la pantalla principal para ver los 20 niveles. ¡Toca el Nivel 1 para comenzar!
2. **¿Qué pasa si me equivoco en una pregunta?**
   Tienes intentos ilimitados. Si fallas 2 veces seguidas, el sistema te pedirá un breve descanso de 60 segundos antes de reintentar. (Verificado: REQ-FUN-11).
3. **¿Para qué edades está recomendada la aplicación?**
   Está diseñada para niños de básica primaria, idealmente entre **7 y 11 años**. (Verificado: README).
4. **¿Cómo se garantizan la seguridad y privacidad de mi hijo?**
   El registro requiere mediación parental y los datos sensibles (como el JWT) se guardan cifrados en el Keystore del dispositivo. (Verificado: CLAUDE.md §5).
5. **¿La aplicación contiene anuncios o compras integradas?**
   No. ERA es una herramienta educativa gratuita y segura, sin publicidad ni cargos extra. (Verificado: REQ-FUN-13).
6. **¿La aplicación tiene posibilidad de escalar en un futuro?**
   Sí, el diseño permite añadir nuevos niveles o temáticas mediante actualizaciones del catálogo local.
7. **¿Qué hago si se me olvida la contraseña?**
   Usa la opción "¿Olvidaste tu contraseña?" en el Login. Recibirás un código de 6 dígitos en tu correo para crear una nueva clave. (Verificado: Fase 5).
8. **¿Cómo eliminar mi cuenta y qué ocurre con mis datos?**
   Puedes hacerlo desde Ajustes. Tus datos se desactivarán de forma permanente y tu correo no podrá usarse para nuevos registros por seguridad. (Verificado: REQ-FUN-05).

### 6.2 Diseño de Almacenamiento (FAQ JSON)
- **Ubicación:** `app/src/main/assets/faq.json`.
- **Estructura Definitiva:**
  ```json
  [
    { 
      "id": 1, 
      "pregunta": "...", 
      "respuesta": "..." 
    }
  ]
  ```
- **Orden:** El orden de presentación será el definido por el `id` (ascendente).

## 7. UI/UX

- **Componentes:**
    - `SettingsHeader` con título "Ayuda y Comentarios".
    - `SettingsCard` para mostrar la lista de FAQ. Se usará un estilo de lista simple: pregunta en negrita y respuesta debajo.
    - Área de texto para Comentarios: `EraTextField` con `minLines = 3` y contador de caracteres visible.
- **Colores:** Se usará `ColorPrimary` para el botón de envío y `ColorError` para advertencias.

## 8. Arquitectura

- **Repository:** `FeedbackRepository` (interfaz) + `RemoteFeedbackRepository` (impl).
- **ViewModel:** `FaqViewModel` gestionará la carga del asset y el estado del borrador.
- **UI:** `FaqScreen` (Compose).

## 9. División por Capas

### Capa 1: FAQ y Repositorio
- Crear `assets/faq.json`.
- Implementar `FeedbackRepository` y `RemoteFeedbackRepository`.
- **Justificación:** Establece la base de datos estática y el canal de red.

### Capa 2: Lógica de Negocio (ViewModel)
- Implementar `FaqViewModel` y `FaqUiState`.
- Lógica de lectura de assets y validación de longitud de comentario.

### Capa 3: Interfaz de Usuario e Integración
- Crear `FaqScreen`.
- Integrar navegación en `EraNavHost` y enlace en `HomePlaceholderScreen`.

## 10. Testing

1. **Unitarios:**
   - Carga correcta de JSON desde assets.
   - Habilitación del botón "Enviar" (lógica de 1-2000 caracteres).
   - Mapeo de errores 401/403 (limpieza de sesión).
2. **Instrumentados:**
   - Visualización de las 8 FAQ en la pantalla.
   - Validación de que el texto del comentario se borra tras éxito.

## 11. Definition of Done (Fase 8)
- `faq.json` integrado con contenido oficial.
- FAQ legibles sin conexión a internet.
- Comentarios enviables con límite de 2000 caracteres.
- Borrador conservado en memoria ante error de red.
- Sin logs de contenido de comentarios (Regla de Oro).
- 0 regresiones en tests unitarios previos.
