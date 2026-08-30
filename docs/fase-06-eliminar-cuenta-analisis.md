# Fase 6 — Eliminar cuenta (Módulo E): Análisis y Diseño

> Documento de análisis para la funcionalidad de eliminación de cuenta.
> Registra el alcance, el diseño visual, las decisiones técnicas y el plan de implementación.
> **Estado:** **COMPLETADA (2026-08-30).** La funcionalidad de eliminación, el
> diálogo de confirmación, la limpieza de token y la integración en Mi Cuenta
> están implementados y verificados con tests.

---

## 1. Trazabilidad

| Elemento | Referencia |
|---|---|
| Requisito funcional | REQ-FUN-11 (Eliminar cuenta, prioridad **Baja/Media**) |
| Caso de Uso | CU-13 (Eliminar cuenta) |
| Historia de Usuario | HU-11 |
| Endpoints backend | `DELETE /users/me` |
| Fuente visual | `decisiones-tecnicas.md` §13.2.6, §13.3.2, §13.9.4, §14.11, §16.3 |

## 2. Alcance

**Incluye:**
- Pantalla `EliminarCuentaScreen` con cabecera gris de ajustes.
- Validación de contraseña obligatoria para habilitar el botón de borrado.
- Diálogo de confirmación final antes de ejecutar la acción destructiva.
- Extensión de `UserRepository` para incluir `eliminarCuenta(contrasena)`.
- Manejo de cierre de sesión local (limpieza de token) tras éxito.
- Navegación al Login con limpieza de backstack tras la eliminación.
- Gestión de errores: 401 (contraseña incorrecta) mostrado inline.

**Fuera de alcance:**
- Recuperación de cuenta eliminada (el backend hace soft delete, pero la app no provee reactivación).
- Eliminación de datos locales en Room (se abordará en Fase 7 de limpieza general).

## 3. Estado previo aprovechado

| Componente | Archivo | Estado |
|---|---|---|
| `UsersApi.deleteAccount` | `remote/api/UsersApi.kt` | ✅ Ya existe. |
| `DeleteAccountRequest` | `remote/dto/user/DeleteAccountRequest.kt` | ✅ Ya existe. |
| `SesionRepository.limpiarToken()` | `repository/SesionRepository.kt` | ✅ Ya existe. |
| `ErrorMapper` (401 -> `CredencialesInvalidas`) | `utils/ErrorMapper.kt` | ✅ Reutilizable. |
| `EraTextField` (Password) | `ui/components/EraTextField.kt` | ✅ Reutilizable. |
| `EraRegPrimaryButton` / `EraRegSecondaryButton` | `ui/components/EraRegButtons.kt` | ✅ Reutilizable para el footer. |

## 4. Decisiones de diseño (D-46…)

### D-46 — Pantalla con cabecera gris y tarjeta de advertencia
Se seguirá el patrón visual de Ajustes y Mi Cuenta:
- Cabecera gris (`ColorSettingsHeaderBg`) con título "Eliminar cuenta".
- Tarjeta blanca superpuesta con bordes redondeados (24dp).
- Icono de advertencia (`Warning`) en color rojo (`ColorError`).
- Texto descriptivo enfatizando la permanencia de la acción.

### D-47 — Confirmación en dos pasos (Input + Dialog)
1. El usuario **debe** escribir su contraseña actual. El botón "Eliminar mi cuenta" permanece deshabilitado hasta que el campo no esté vacío.
2. Al pulsar el botón, no se llama directamente al API, sino que se muestra un `AlertDialog` de confirmación final ("¿Confirmas que deseas eliminar tu cuenta...?").
3. Solo al confirmar en el diálogo se procede con la llamada al backend.

### D-48 — Cierre de sesión atómico tras éxito
Si `eliminarCuenta` devuelve éxito (200 OK):
1. Se llama a `sesionRepository.limpiarToken()`.
2. Se emite un evento de navegación `NavegarALogin`.
3. El `EraNavHost` ejecuta `navController.navigate(EraRoutes.LOGIN) { popUpTo(0) { inclusive = true } }`.

## 5. Arquitectura Propuesta

### 5.1 Capa Data / Repository
- Modificar `UserRepository.kt` para añadir `suspend fun eliminarCuenta(contrasena: String): Resultado<Unit>`.
- Implementar en `RemoteUserRepository.kt` usando el wrapper `llamar { api.deleteAccount(DeleteAccountRequest(contrasena)) }`.

### 5.2 Capa UI (ViewModel)
- `EliminarCuentaViewModel` (@HiltViewModel):
    - Inyecta `UserRepository` y `SesionRepository`.
    - `UiState`: `contrasena`, `contrasenaVisible`, `cargando`, `errorGeneral`.
    - `onEliminarClick()`: Muestra el diálogo de confirmación.
    - `confirmarEliminacion()`: Llama al repositorio, si hay éxito limpia token y emite evento de éxito.

### 5.3 Capa UI (Screen)
- `EliminarCuentaScreen`:
    - Estructura con `Scaffold` y cabecera personalizada.
    - `EraTextField` para contraseña con toggle de visibilidad.
    - `AlertDialog` de confirmación.

## 6. Plan de implementación

1. **Repository:** Añadir método `eliminarCuenta` a la interfaz y su implementación remota.
2. **ViewModel:** Crear `EliminarCuentaViewModel`, `UiState` y `Evento`.
3. **Navegación:** Añadir `ELIMINAR_CUENTA` a `EraRoutes` y configurar la ruta en `EraNavHost`.
4. **UI:** Implementar `EliminarCuentaScreen` siguiendo los specs de diseño.
5. **Tests:**
    - Test unitario de `EliminarCuentaViewModel`.
    - Test de integración en `UserRepositoryTest`.
    - AndroidTest para la pantalla (verificar deshabilitado de botón y aparición de diálogo).

## 7. Verificación de Done (DoD)
- El botón de eliminar cuenta en Ajustes navega a la nueva pantalla.
- La eliminación exitosa borra el token local y vuelve al Login.
- Si la contraseña es incorrecta (401), se muestra el error "Correo/usuario o contraseña incorrectos" (o similar mapeado).
- No es posible eliminar sin ingresar texto en el campo de contraseña.
- Los tests existentes siguen pasando (169 unitarios + 57 instrumentados).

---

## 8. Registro de implementación (2026-08-30)

### Capa 1: Repository (2026-08-30)
- `UserRepository` extendido con `eliminarCuenta(contrasena)`.
- `RemoteUserRepository` implementado.
- Tests unitarios en `UserRepositoryTest` (200 OK, 401, 403).

### Capa 2: ViewModel y Navegación (2026-08-30)
- `EliminarCuentaViewModel` creado con manejo de contraseña y carga.
- `EliminarCuentaUiState` y `Evento` definidos.
- Ruta `ELIMINAR_CUENTA` añadida a `EraRoutes`.
- Grafo de navegación actualizado en `EraNavHost`.

### Capa 3: UI e Integración (2026-08-30)
- `EliminarCuentaScreen` implementada con patrón destructivo.
- Pantalla "Mi Cuenta" ajustada para integrar la sección de Seguridad dentro del recuadro principal.
- Tests instrumentados actualizados y verdes (60/60).
