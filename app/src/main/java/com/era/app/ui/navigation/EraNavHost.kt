package com.era.app.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.era.app.ui.login.HomePlaceholderEvento
import com.era.app.ui.login.HomePlaceholderScreen
import com.era.app.ui.login.HomePlaceholderViewModel
import com.era.app.ui.login.LoginScreen
import com.era.app.ui.perfil.EliminarCuentaScreen
import com.era.app.ui.perfil.EliminarCuentaViewModel
import com.era.app.ui.perfil.MiCuentaScreen
import com.era.app.ui.recuperacion.RecuperacionPaso1Screen
import com.era.app.ui.recuperacion.RecuperacionPaso2Screen
import com.era.app.ui.recuperacion.RecuperacionPaso3Screen
import com.era.app.ui.recuperacion.RecuperacionViewModel
import com.era.app.ui.register.RegistroPaso1Screen
import com.era.app.ui.register.RegistroPaso2Screen
import com.era.app.ui.register.RegistroPaso3Screen
import com.era.app.ui.register.RegistroViewModel

@Composable
fun EraNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = EraRoutes.LOGIN,
        modifier = modifier,
    ) {
        composable(EraRoutes.LOGIN) {
            LoginScreen(
                onNavigateToHome = { navController.navigate(EraRoutes.HOME_PLACEHOLDER) },
                onNavigateToRegistro = { navController.navigate(EraRoutes.REGISTRO) },
                onNavigateARecuperacion = { navController.navigate(EraRoutes.RECUPERACION) },
                snackbarHostState = snackbarHostState,
                backStackEntry = navController.getBackStackEntry(EraRoutes.LOGIN),
            )
        }

        composable(EraRoutes.HOME_PLACEHOLDER) {
            val vm: HomePlaceholderViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsState()
            LaunchedEffect(Unit) {
                vm.eventos.collect { evento ->
                    when (evento) {
                        is HomePlaceholderEvento.NavegarALogin ->
                            navController.navigate(EraRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                    }
                }
            }
            HomePlaceholderScreen(
                onNavigatePerfil = { navController.navigate(EraRoutes.PERFIL) },
                onCerrarSesion = vm::onCerrarSesionClick,
                dialogoCierreVisible = uiState.dialogoCierreVisible,
                cerrando = uiState.cerrando,
                onCancelarCierre = vm::onCancelarCierre,
                onConfirmarCierre = vm::onConfirmarCierre,
            )
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
