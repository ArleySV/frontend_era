# Fase 1 — Registro + Verificación OTP: Análisis y Diseño

> Documento de análisis del módulo frontend. Registra el alcance, el diseño,
> las decisiones aprobadas (D-xx) y las correcciones al diseño heredado del
> prototipo HTML. Cualquier cambio posterior al diseño se registra en este archivo.
> **Estado:** Diseño aprobado por el propietario (2026-08-23) — implementación pendiente de arranque.

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-01 (Registro de usuario) |
| Requisitos no funcionales asociados | REQ-NF-02 (seguridad), REQ-NF-03 (usabilidad), REQ-NF-06 (portabilidad minSdk 26) |
| Casos de uso | CU-01 (Registrarse), CU-11 (Verificar código — include) |
| Historias de usuario | HU-01, HU-15 |
| Endpoints backend consumidos | `POST /auth/register`, `POST /auth/verify-email`, `POST /auth/resend-otp` |

## 2. Alcance

**Incluye:**
- 3 pantallas de registro (datos del menor/acudiente → cuenta → verificación OTP).
- Envío único de `register` con todos los datos; verificación y reenvío de OTP.
- Validaciones completas en cliente (el backend sigue siendo autoridad).
- Fundaciones que la fase necesita: tokens de tema, error-mapper central,
  navegación base, componentes compartidos, capa `repository/`.
- Tests unitarios e integración por capa (criterio de éxito de CLAUDE.md §6).

**Fuera de alcance:** login real (Fase 2 — solo placeholder de navegación),
avatar personalizado con subida (Fase 9), persistencia Room (Fase 7).

## 3. Estado previo aprovechado

Ya existe y **no se reescribe** (regla §4.13): `AuthApi` (Retrofit, suspend),
`RegisterRequest`/`VerifyEmailRequest`/`ResendOtpRequest`/respuestas (kotlinx.serialization,
snake_case vía `@SerialName`), `JwtInterceptor`, `TokenManager`, `NetworkModule`
(Hilt), recursos `drawable-nodpi/avatar_preset_1..3.jpg`.

## 4. Flujo de navegación

```
login (placeholder Fase 2)
   │  "Regístrate"
   ▼
Registro 1/3 ──Continuar──► Registro 2/3 ──Continuar──► [POST /auth/register]
   │                           │                              éxito
 Cancelar                     Atrás                            ▼
   ▼                           ▼                        Registro 3/3 (OTP)
 login                       paso 1                     [verify-email | resend-otp]
                                                            │ éxito
                                                            ▼
                                                       login + snackbar
                                                       "Cuenta verificada"
```

Reglas: `Cancelar` (paso 1) y `Atrás` (paso 2) conservan datos ya ingresados
(REQ-FUN-01: "sin perder los datos ya ingresados"). En el paso 3 el botón
atrás del sistema queda deshabilitado tras enviar el registro (CU-01/CU-11);
la única salida es verificar o volver a `login` mediante enlace explícito.

## 5. Pantallas (tokens en `decisiones-tecnicas.md` §10, componentes §13)

### 5.1 Paso 1 — Datos de usuario (`register/step1`)
Cabecera compacta verde ("Registro - Paso 1 de 3 / Datos de usuario") +
StepIndicator (punto 1 activo). Campos: nombres del menor*, fecha de nacimiento*
(DD/MM/AAAA, teclado numérico), nombres del acudiente*, cédula del acudiente*
(numérica, máx. 15). Footer fijo: `btn-reg-secondary` Cancelar + `btn-reg-primary`
Continuar. Solo validación local — no hay red en este paso (§14.5).

### 5.2 Paso 2 — Configurar cuenta (`register/step2`)
Misma cabecera + StepIndicator (punto 2). Campos: correo principal*, nombre de
usuario* (máx. 60, contador), selector de avatar (3 presets locales, 49dp, borde
2.5dp `ColorPrimary` al seleccionado; **sin** botón "+": requiere sesión, Fase 9),
contraseña* (ojo toggle) y confirmar*. Hint de reglas + info-box (§13.9.1).
Footer: Atrás + Continuar. **Al pulsar Continuar se envía `POST /auth/register`**
(D-03): errores 409/400 se muestran inline aquí, donde son corregibles.

### 5.3 Paso 3 — Verificación de correo (`register/step3`)
Cabecera + StepIndicator (punto 3). Icono sobre (122dp), "Código enviado a
<correo>", input OTP (6 dígitos numéricos), botón pill "Verificar código",
enlace "Reenviar código" con countdown de 60 s (D-10), info-box de expiración
(10 min / máx. 3 fallos). Éxito → `login` con snackbar de confirmación (D-04).
OTP de desarrollo: `123456` (solo ambiente dev).

## 6. Validaciones de cliente (fuente: REQ-FUN-01 + corrección 2026-08-23)

| Campo | Reglas | Feedback |
|---|---|---|
| Nombres del menor / acudiente | No vacío; solo letras y espacios | Error 12sp bajo campo |
| Fecha nacimiento | Fecha válida de calendario; edad calculada dinámicamente entre 7 y 11 años | Error bajo campo |
| Cédula acudiente | No vacía; ≤15 dígitos numéricos (backend acepta 6–20 alfanum.; restricción UX intencional) | Error bajo campo |
| Correo | Formato válido (regex pragmática); obligatorio | Error bajo campo |
| Nombre de usuario | 3–60 caracteres, sin espacios | Contador + error |
| Contraseña | ≥8; mayúscula; **minúscula**; número; símbolo `!@#$%^&*`; ≠ username; sin contener nombre del menor/acudiente | Checklist visual + error |
| Confirmar contraseña | Coincide exactamente | Error bajo campo |
| Avatar | Uno seleccionado (preset 1–3) | Error si falta |
| Código OTP | Exactamente 6 dígitos numéricos | Error bajo campo |

La fecha se muestra DD/MM/AAAA y se envía al backend en formato ISO (`AAAA-MM-DD`),
mismo criterio que `GET /users/me`. Verificar contra `BACKEND_ERA/README.md`
durante la prueba de integración antes de asumir otro formato.

## 7. Contrato de red consumido

| Llamada | Momento | Request | Respuesta esperada |
|---|---|---|---|
| `POST /auth/register` | Continuar del paso 2 | `RegisterRequest` completo (snake_case) | 201 + `MessageResponse` |
| `POST /auth/verify-email` | Verificar código (paso 3) | `{ correo, codigo }` | 200 + `MessageResponse`; cuenta activa |
| `POST /auth/resend-otp` | Reenviar (paso 3) | `{ correo }` | 200 + mensaje; throttle 60 s |

Sin sesión JWT en toda la fase (ninguna llamada usa el interceptor de sesión;
`JwtInterceptor` solo adjunta token si existe — no existe durante registro).

## 8. Errores HTTP → `EraError` → UI (mapeo central, D-02)

| HTTP + campo `error` | `EraError` | Mensaje/UI |
|---|---|---|
| 400 `VALIDATION_ERROR` | `Validacion` | Mostrar detalle genérico inline |
| 409 `EMAIL_ALREADY_REGISTERED` | `CorreoRegistrado` | Inline en correo (paso 2): "este correo ya tiene una cuenta" |
| 409 `EMAIL_LOCKED` | `CorreoBloqueado` | "correo no reutilizable (cuenta eliminada)" |
| 409 `CONFLICT` | `UsuarioEnUso` | Inline en username (paso 2) |
| 401 `OTP_INVALID_OR_EXPIRED` | `OtpInvalido` | Inline en paso 3; aviso de 3 fallos |
| 429 `OTP_RESEND_THROTTLED` | `ReenvioThrottled` | Snackbar + mantener countdown |
| 500 `INTERNAL_ERROR` | `ErrorServidor` | Snackbar "intenta más tarde" |
| `IOException` / timeout | `ErrorConexion` | Mensaje temporal; **estado conservado** (REQ-FUN-01 CA5) |
| Otros | `Desconocido` | Genérico; loguear código sin PII |

Mapear siempre por el **campo `error`**, nunca por texto (anti-enumeración, §7).

## 9. Decisiones de diseño aprobadas

| ID | Decisión | Justificación |
|---|---|---|
| D-01 | Un único `RegistroViewModel` compartido por los 3 pasos, scoped al grafo `register` | El POST es único con todos los datos; conserva estado al navegar atrás (REQ-FUN-01) |
| D-02 | `EraError` sealed class + mapper central por campo `error` | Un solo lugar de mapeo (§7); pantallas solo consumen tipos |
| D-03 | El `register` se dispara al continuar del paso 2→3 | Los 409 (unicidad) se corrigen donde ocurren; paso 3 solo OTP |
| D-04 | Fin del flujo → **Login** con snackbar "Cuenta verificada" | Tras registro NO hay JWT; corregido desde prototipo (ver §10) |
| D-05 | Logging OkHttp nivel `BASIC` en debug | `BODY` escribiría contraseñas/OTP en logcat (violación §5) |
| D-06 | Tokens ERA en `Color.kt`/`Type.kt`/`Theme.kt`; `dynamicColor=false` | La plantilla default (Purple/Pink) no es la marca; dynamic color rompería identidad en Android 12+ |
| D-07 | Componentes compartidos en `ui/components/`: cabecera verde compacta, StepIndicator, input estilo registro, botones reg, InfoBox | Reinterpretación nativa de §13 (regla §4.13), reutilizados por Fase 5 (recuperación) |
| D-08 | Fecha DD/MM/AAAA teclado numérico + cálculo dinámico de edad 7–11 | REQ-FUN-01 CA3; feedback inmediato, backend es autoridad |
| D-09 | Avatares = 3 drawables locales (`avatar_preset_1..3`) | Backend acepta solo `preset:1\|2\|3`; "+" solo en Mi cuenta (Fase 9) |
| D-10 | Reenvío OTP con countdown 60 s sincronizado al throttle | Evita 429 innecesarios; coincide con CU-11 4a |
| D-11 | UiState inmutable (`StateFlow`) + eventos one-shot (`Channel`) | Navegación/snackbars sin duplicarse en recomposición |
| D-12 | Tests: JUnit + `coroutines-test` (VM/validators) + MockWebServer (repository) | Criterio de éxito por fase (§6) |
| D-13 | Política de contraseña según REQ-FUN-01 CA2 completa | Corrección sobre diseño heredado (ver §10) |
| D-14 | Placeholder `LoginScreen` mínimo como destino de navegación | El grafo necesita la ruta desde Fase 1; se reemplaza en Fase 2 |

## 10. Correcciones registradas al diseño heredado del prototipo

1. **Fin del flujo → Login (no Home).** §14.7 heredó "→ Home" del prototipo HTML,
   pero el contrato (§7) no entrega sesión tras registro. Corregido 2026-08-23 con
   aprobación del propietario. Registrado también en `decisiones-tecnicas.md` §14.
2. **Política de contraseña incompleta.** §14.6 omitía minúsculas y la exclusión
   de datos personales (REQ-FUN-01 CA2). Corregido en `decisiones-tecnicas.md`
   §14 y aplicado en este análisis (D-13).

## 11. Seguridad (CLAUDE.md §5 aplicado a esta fase)

- Nunca loguear contraseña, OTP, correo ni cédula; logging `BASIC` (D-05).
- La fase no maneja JWT (no hay sesión); `TokenManager` no se toca.
- Validación de cliente = retroalimentación inmediata; nunca sustituye la del
  servidor: todos los errores HTTP de §8 se implementan igual.
- Datos del acudiente viajan cifrados en tránsito (HTTPS) hacia el backend.

## 12. Archivos a crear / modificar

**Modificar:** `app/build.gradle.kts` + `gradle/libs.versions.toml` (solo deps de
test autorizadas: `mockwebserver`, `kotlinx-coroutines-test`), `ui/theme/Color.kt`,
`ui/theme/Type.kt`, `ui/theme/Theme.kt`, `di/NetworkModule.kt` (logging BASIC),
`MainActivity.kt` (NavHost).

**Crear:**
```
utils/EraError.kt            ← sealed class de dominio
utils/ErrorMapper.kt         ← HTTP+campo error → EraError
utils/Validators.kt          ← funciones puras de validación
repository/AuthRepository.kt ← interfaz
repository/RemoteAuthRepository.kt
di/RepositoryModule.kt       ← binding Hilt
ui/navigation/EraNavHost.kt  ← rutas login + register/step1..3
ui/components/CompactGreenHeader.kt, StepIndicator.kt,
    EraTextField.kt, EraRegButtons.kt, InfoBox.kt
ui/register/RegistroViewModel.kt, RegistroUiState.kt,
    RegistroPaso1Screen.kt, RegistroPaso2Screen.kt, RegistroPaso3Screen.kt
ui/login/LoginPlaceholderScreen.kt
test/…/ValidatorsTest.kt, RegistroViewModelTest.kt
androidTest|test/…/AuthRepositoryTest.kt (MockWebServer)
```

## 13. Definition of Done de la fase

1. Suite de tests verde: Validators, ViewModel, Repository (MockWebServer cubre
   201/400/409/429/401/IOException).
2. Compilación limpia (`assembleDebug`).
3. Flujo manual contra backend dev (OTP `123456`): registro completo hasta
   snackbar en login; datos conservados ante fallo de conexión (CA5).
4. Sin secretos ni PII en logs verificable (inspección logcat en debug).
5. Este documento y `CLAUDE.md` §10 actualizados con lo implementado.

## 14. Puntos abiertos

- Ninguno bloqueante. Nota: si el usuario abandona en paso 3 sin verificar, la
  cuenta queda inactiva y el correo ocupado (409 `EMAIL_ALREADY_REGISTERED` en un
  reintento) — comportamiento del backend, se documenta en la UI del info-box.
