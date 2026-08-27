package com.era.app.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.era.app.ui.login.LoginPlaceholderScreen
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
            LoginPlaceholderScreen(
                onNavigateToRegistro = {
                    navController.navigate(EraRoutes.REGISTRO)
                },
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
                        navController.navigate(EraRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
