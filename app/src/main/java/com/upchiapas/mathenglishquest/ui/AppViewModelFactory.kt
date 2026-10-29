package com.upchiapas.mathenglishquest.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.repository.DadoProgressRepository
import com.upchiapas.mathenglishquest.di.AppContainer
import com.upchiapas.mathenglishquest.ui.screens.auth.LoginViewModel
import com.upchiapas.mathenglishquest.ui.screens.clase.ClassViewModel
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterStudentViewModel
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterTeacherViewModel
import com.upchiapas.mathenglishquest.ui.screens.dado.DadoViewModel
import com.upchiapas.mathenglishquest.ui.screens.home.HomeViewModel
import com.upchiapas.mathenglishquest.ui.screens.portfolio.PortfolioViewModel
import com.upchiapas.mathenglishquest.ui.screens.portfolio.StudentPortfolioViewModel
import com.upchiapas.mathenglishquest.ui.screens.splash.SplashViewModel

/** Crea los ViewModels que necesitan repositorios (los que no, se crean con viewModel() normal). */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val auth = container.authRepository
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(auth)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(auth)
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(auth)
            modelClass.isAssignableFrom(RegisterStudentViewModel::class.java) -> RegisterStudentViewModel(auth)
            modelClass.isAssignableFrom(RegisterTeacherViewModel::class.java) -> RegisterTeacherViewModel(auth)
            modelClass.isAssignableFrom(DadoViewModel::class.java) -> DadoViewModel(
                // El progreso se guarda por correo; quien entró "sin correo" comparte la clave de invitado
                userKeyProvider = { auth.currentUser()?.email ?: DadoProgressRepository.GUEST_KEY },
                progressStore = container.dadoProgressRepository,
                // La foto del procedimiento solo se exige a estudiantes con cuenta (no a invitados ni maestros)
                evidenceEligibleProvider = {
                    val user = auth.currentUser()
                    user != null && user.role == UserRole.ESTUDIANTE && user.email != null
                },
                evidenceSaver = container.evidenceRepository
            )
            modelClass.isAssignableFrom(PortfolioViewModel::class.java) -> PortfolioViewModel(
                entries = container.evidenceRepository.observeMine(),
                fileResolver = container.evidenceRepository::fileFor
            )
            modelClass.isAssignableFrom(ClassViewModel::class.java) -> ClassViewModel(container.classRepository)
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(auth)
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        } as T
    }
}

@Composable
fun rememberAppViewModelFactory(): AppViewModelFactory {
    val context = LocalContext.current.applicationContext
    return remember { AppViewModelFactory(AppContainer.getInstance(context)) }
}

/** El portafolio de un estudiante necesita saber de cuál: por eso tiene su propia fábrica. */
class StudentPortfolioViewModelFactory(
    private val container: AppContainer,
    private val studentId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StudentPortfolioViewModel(studentId, container.classRepository) as T
}

@Composable
fun rememberStudentPortfolioViewModelFactory(studentId: Int): StudentPortfolioViewModelFactory {
    val context = LocalContext.current.applicationContext
    return remember(studentId) { StudentPortfolioViewModelFactory(AppContainer.getInstance(context), studentId) }
}
