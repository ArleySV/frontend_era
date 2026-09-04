package com.era.app.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.era.app.ui.components.EraIcons
import com.era.app.ui.components.layout.EraDrawer
import com.era.app.ui.components.layout.EraDrawerItem
import com.era.app.ui.home.HomeEvento
import com.era.app.ui.home.HomeScreen
import com.era.app.ui.home.HomeViewModel
import com.era.app.ui.login.LoginScreen
import com.era.app.ui.perfil.EliminarCuentaScreen
import com.era.app.ui.perfil.EliminarCuentaViewModel
import com.era.app.ui.perfil.MiCuentaScreen
import com.era.app.ui.progreso.ProgresoScreen
import com.era.app.ui.progreso.ProgresoViewModel
import com.era.app.ui.faq.FaqScreen
import com.era.app.ui.faq.FaqViewModel
import com.era.app.ui.recuperacion.RecuperacionPaso1Screen
import com.era.app.ui.recuperacion.RecuperacionPaso2Screen
import com.era.app.ui.recuperacion.RecuperacionPaso3Screen
import com.era.app.ui.recuperacion.RecuperacionViewModel
import com.era.app.ui.register.RegistroPaso1Screen
import com.era.app.ui.splash.SplashScreen
import com.era.app.ui.register.RegistroPaso2Screen
import com.era.app.ui.register.RegistroPaso3Screen
import com.era.app.ui.register.RegistroViewModel
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import kotlinx.coroutines.launch

@Composable
fun EraNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = EraRoutes.SPLASH,
        modifier = modifier,
    ) {
        composable(EraRoutes.SPLASH) {
            SplashScreen(
                onNavegarAHome = { route ->
                    navController.navigate(route) {
                        popUpTo(EraRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavegarALogin = {
                    navController.navigate(EraRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(EraRoutes.LOGIN) {
            LoginScreen(
                onNavigateToHome = { navController.navigate(EraRoutes.HOME) },
                onNavigateToRegistro = { navController.navigate(EraRoutes.REGISTRO) },
                onNavigateARecuperacion = { navController.navigate(EraRoutes.RECUPERACION) },
                snackbarHostState = snackbarHostState,
                backStackEntry = navController.getBackStackEntry(EraRoutes.LOGIN),
            )
        }

        composable(EraRoutes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsState()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                vm.eventos.collect { evento ->
                    when (evento) {
                        is HomeEvento.NavegarALogin ->
                            navController.navigate(EraRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                    }
                }
            }

            val items = listOf(
                EraDrawerItem("perfil", EraIcons.AccountCircle, "Mi cuenta"),
                EraDrawerItem("progreso", EraIcons.Assessment, "Progreso"),
                EraDrawerItem("separador", EraIcons.Assessment, ""),
                EraDrawerItem("ajustes", EraIcons.Settings, "Ajustes", habilitado = false),
                EraDrawerItem("faq", EraIcons.Help, "FAQ"),
                EraDrawerItem("separador", EraIcons.Assessment, ""),
                EraDrawerItem("cerrar_sesion", EraIcons.Logout, "Cerrar sesión"),
            )

            EraDrawer(
                drawerState = drawerState,
                nombre = uiState.nombreMenor,
                correo = uiState.correo,
                avatar = uiState.avatar,
                cargandoPerfil = uiState.cargandoPerfil,
                items = items,
                onItemClick = { id ->
                    when (id) {
                        "perfil" -> navController.navigate(EraRoutes.PERFIL)
                        "progreso" -> navController.navigate(EraRoutes.PROGRESO)
                        "faq" -> navController.navigate(EraRoutes.FAQ)
                        else -> scope.launch { drawerState.close() }
                    }
                },
                onCerrarSesionClick = vm::onCerrarSesionClick,
                bytesAvatarCustom = uiState.bytesAvatarCustom,
            ) {
                HomeScreen(
                    nombreMenor = uiState.nombreMenor,
                    cargandoPerfil = uiState.cargandoPerfil,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavegarNiveles = { /* ruta NIVELES llega en S3 */ },
                )
            }

            if (uiState.dialogoCierreVisible) {
                DialogoCierreSesion(
                    cerrando = uiState.cerrando,
                    onCancelar = vm::onCancelarCierre,
                    onConfirmar = vm::onConfirmarCierre,
                )
            }
        }

        composable(EraRoutes.PERFIL) {
            MiCuentaScreen(
                onVolver = { navController.popBackStack() },
                onNavegarALogin = {
                    navController.navigate(EraRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavegarAEliminarCuenta = {
                    navController.navigate(EraRoutes.ELIMINAR_CUENTA)
                }
            )
        }

        composable(EraRoutes.ELIMINAR_CUENTA) {
            val vm: EliminarCuentaViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                vm.eventos.collect { evento ->
                    when (evento) {
                        is com.era.app.ui.perfil.EliminarCuentaEvento.NavegarALogin -> {
                            navController.navigate(EraRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
            EliminarCuentaScreen(
                vm = vm,
                onVolver = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(EraRoutes.PROGRESO) {
            val vm: ProgresoViewModel = hiltViewModel()
            ProgresoScreen(
                vm = vm,
                onVolver = { navController.popBackStack() },
                onSesionExpirada = {
                    navController.navigate(EraRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }

        composable(EraRoutes.FAQ) {
            val vm: FaqViewModel = hiltViewModel()
            FaqScreen(
                vm = vm,
                onVolver = { navController.popBackStack() },
                onSesionExpirada = {
                    navController.navigate(EraRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }

        navigation(
            route = EraRoutes.REGISTRO,
            startDestination = EraRoutes.REGISTRO_PASO1,
        ) {
            composable(EraRoutes.REGISTRO_PASO1) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.REGISTRO)
                }
                val vm: RegistroViewModel = hiltViewModel(parentEntry)
                RegistroPaso1Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onCancelar = {
                        vm.cancelar()
                        navController.popBackStack()
                    },
                    onNavegarAPaso2 = {
                        navController.navigate(EraRoutes.REGISTRO_PASO2)
                    },
                )
            }

            composable(EraRoutes.REGISTRO_PASO2) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.REGISTRO)
                }
                val vm: RegistroViewModel = hiltViewModel(parentEntry)
                RegistroPaso2Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onAtras = { navController.popBackStack() },
                    onNavegarAPaso3 = {
                        navController.navigate(EraRoutes.REGISTRO_PASO3)
                    },
                )
            }

            composable(EraRoutes.REGISTRO_PASO3) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.REGISTRO)
                }
                val vm: RegistroViewModel = hiltViewModel(parentEntry)
                RegistroPaso3Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onRegistroExitoso = {
                        navController.getBackStackEntry(EraRoutes.LOGIN)
                            .savedStateHandle["registro_exitoso"] = true
                        navController.navigate(EraRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        navigation(
            route = EraRoutes.RECUPERACION,
            startDestination = EraRoutes.RECUPERACION_PASO1,
        ) {
            composable(EraRoutes.RECUPERACION_PASO1) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.RECUPERACION)
                }
                val vm: RecuperacionViewModel = hiltViewModel(parentEntry)
                RecuperacionPaso1Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onVolverAlLogin = {
                        vm.cancelar()
                        navController.popBackStack()
                    },
                    onNavegarAPaso2 = {
                        navController.navigate(EraRoutes.RECUPERACION_PASO2)
                    },
                )
            }

            composable(EraRoutes.RECUPERACION_PASO2) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.RECUPERACION)
                }
                val vm: RecuperacionViewModel = hiltViewModel(parentEntry)
                RecuperacionPaso2Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onAtras = { navController.popBackStack() },
                    onNavegarAPaso3 = {
                        navController.navigate(EraRoutes.RECUPERACION_PASO3)
                    },
                    onReiniciarFlujo = {
                        navController.popBackStack(EraRoutes.RECUPERACION_PASO1, inclusive = false)
                    },
                )
            }

            composable(EraRoutes.RECUPERACION_PASO3) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(EraRoutes.RECUPERACION)
                }
                val vm: RecuperacionViewModel = hiltViewModel(parentEntry)
                RecuperacionPaso3Screen(
                    vm = vm,
                    snackbarHostState = snackbarHostState,
                    onReiniciarFlujo = {
                        navController.popBackStack(EraRoutes.RECUPERACION_PASO1, inclusive = false)
                    },
                    onRecuperacionExitosa = {
                        navController.getBackStackEntry(EraRoutes.LOGIN)
                            .savedStateHandle["recuperacion_exitosa"] = true
                        navController.navigate(EraRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DialogoCierreSesion(
    cerrando: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Cerrar sesión",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "¿Deseas cerrar sesión?",
                fontSize = 16.sp,
            )
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !cerrando) {
                Text(
                    text = "Cancelar",
                    color = ColorPrimary,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !cerrando) {
                if (cerrando) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp,
                        color = ColorPrimary,
                    )
                } else {
                    Text(
                        text = "Sí, cerrar sesión",
                        color = ColorError,
                        fontSize = 16.sp,
                    )
                }
            }
        },
    )
}
