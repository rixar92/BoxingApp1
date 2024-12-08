package com.knockoutgym.boxingapp1

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.knockoutgym.boxingapp1.presentation.data.CrearClaseScreen
import com.knockoutgym.boxingapp1.presentation.data.MisReservasScreen
import com.knockoutgym.boxingapp1.presentation.data.ModificarClaseScreen
import com.knockoutgym.boxingapp1.presentation.data.ReservaScreen
import com.knockoutgym.boxingapp1.presentation.data.UserProfileScreen
import com.knockoutgym.boxingapp1.presentation.data.reservarClase
import com.knockoutgym.boxingapp1.presentation.home.AdminHomeScreen
import com.knockoutgym.boxingapp1.presentation.home.UserHomeScreen
import com.knockoutgym.boxingapp1.presentation.initial.InitialScreen
import com.knockoutgym.boxingapp1.presentation.initial.SplashScreen
import com.knockoutgym.boxingapp1.presentation.login.LoginScreen
import com.knockoutgym.boxingapp1.presentation.register.RegisterScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    NavHost(
        navController = navHostController,
        startDestination = "splash"
    ) {
        // Pantalla de carga inicial
        composable("splash") {
            SplashScreen(
                navHostController = navHostController,
                auth = auth
            )
        }

        // Pantalla inicial
        composable("initial") {
            InitialScreen(
                navigateToLogin = { navHostController.navigate("login") },
                navigateToRegister = { navHostController.navigate("register") }
            )
        }

        // Pantalla de inicio de sesión
        composable("login") {
            LoginScreen(auth) {
                navHostController.navigate("splash") {
                    popUpTo("initial") {
                        inclusive = true
                    }
                }
            }
        }

        // Pantalla de registro
        composable("register") {
            RegisterScreen(auth) {
                navHostController.navigate("splash") {
                    popUpTo("initial") {
                        inclusive = true
                    }
                }
            }
        }

        // Pantalla principal para administradores
        composable("admin_home") {
            AdminHomeScreen(
                onSignOut = {
                    auth.signOut()
                    navHostController.navigate("initial") {
                        popUpTo("admin_home") {
                            inclusive = true
                        }
                    }
                },
                onClassClick = { selectedClass ->
                    val horariosPorDia = selectedClass.horariosPorDia.entries.joinToString(";") { "${it.key},${it.value.joinToString("|")}" }
                    val route = "reserva/${selectedClass.id}/${selectedClass.nombre}/$horariosPorDia"
                    navHostController.navigate(route)
                },
                onCreateNewClassClick = {
                    navHostController.navigate("crear_clase")
                },
                onModifyClassClick = { claseId ->
                    navHostController.navigate("modificar_clase/$claseId")
                }
            )
        }

        // Pantalla principal para usuarios
        composable("user_home") {
            val userId = auth.currentUser?.uid ?: ""
            val context = LocalContext.current

            UserHomeScreen(
                onMyAccountClick = {
                    navHostController.navigate("user_profile")
                },
                onClassClick = { selectedClass ->
                    val horariosPorDia = selectedClass.horariosPorDia.entries.joinToString(";") { "${it.key},${it.value.joinToString("|")}" }
                    val route = "reserva/${selectedClass.id}/${selectedClass.nombre}/$horariosPorDia"
                    navHostController.navigate(route)
                },
                onMyReservationsClick = {
                    if (userId.isNotEmpty()) {
                        navHostController.navigate("mis_reservas/$userId")
                    } else {
                        Toast.makeText(
                            context,
                            "Usuario no autenticado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        // Pantalla de perfil de usuario
        composable("user_profile") {
            UserProfileScreen(
                auth = auth,
                onSignOut = {
                    auth.signOut()
                    navHostController.navigate("initial") {
                        popUpTo("user_home") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Pantalla para crear una nueva clase (solo admin)
        composable("crear_clase") {
            CrearClaseScreen(
                onClaseCreated = {
                    navHostController.popBackStack()
                }
            )
        }

        // Pantalla de reserva con el `claseId` real
        composable(
            "reserva/{claseId}/{claseNombre}/{horariosPorDia}",
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType },
                navArgument("claseNombre") { type = NavType.StringType },
                navArgument("horariosPorDia") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId")
            val claseNombre = backStackEntry.arguments?.getString("claseNombre")
            val horariosString = backStackEntry.arguments?.getString("horariosPorDia")

            val horariosPorDia = horariosString?.split(";")?.associate { item ->
                val splitData = item.split(",")
                if (splitData.size == 2) {
                    val dia = splitData[0]
                    val horarios = splitData[1]
                    dia to horarios.split("|")
                } else {
                    "" to listOf()
                }
            } ?: mapOf()

            if (claseId != null && claseNombre != null && horariosPorDia.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: ""

                // Ahora onReservar es una función suspend que devuelve Result<String>
                // No necesitamos coroutineScope.launch aquí, ya que se llama desde ReservaScreen en una corrutina.
                ReservaScreen(
                    claseId = claseId,
                    claseNombre = claseNombre,
                    horarios = horariosPorDia,
                    onReservar = { diaSeleccionado, horarioSeleccionado ->
                        reservarClase(
                            userId = userId,
                            claseId = claseId,
                            claseNombre = claseNombre,
                            fecha = diaSeleccionado,
                            horario = horarioSeleccionado
                        )
                    }
                )
            } else {
                Text("Error: Clase no encontrada")
            }
        }

        // Pantalla de modificación de clase
        composable(
            "modificar_clase/{claseId}",
            arguments = listOf(navArgument("claseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId")
            if (claseId != null) {
                ModificarClaseScreen(
                    claseId = claseId,
                    onClaseModified = {
                        navHostController.popBackStack()
                    }
                )
            } else {
                Text("Error: Clase no encontrada")
            }
        }

        // Nueva ruta para Mis Reservas
        composable(
            "mis_reservas/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                MisReservasScreen(
                    userId = userId,
                    onBack = { navHostController.popBackStack() }
                )
            } else {
                Text("Error: Usuario no encontrado")
            }
        }
    }
}






