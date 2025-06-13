package com.emm.gema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

object GemaRoutes {

    @Serializable
    object PreLogin

    @Serializable
    object Login

    @Serializable
    object Register

    @Serializable
    object Dashboard

    @Serializable
    object CreateCourse
}

sealed interface DashboardRoutes {

    val title: String

    val icon: ImageVector

    @Serializable
    data object Dashboard : DashboardRoutes {
        override val title: String get() = "Inicio"
        override val icon: ImageVector get() = Icons.Default.Home
    }

    @Serializable
    data object Courses : DashboardRoutes {
        override val title: String get() = "Cursos"
        override val icon: ImageVector get() = Icons.Default.School
    }

    @Serializable
    data object Attendance : DashboardRoutes {
        override val title: String get() = "Asistencia"
        override val icon: ImageVector get() = Icons.Default.Check
    }

    @Serializable
    data object Evaluations : DashboardRoutes {
        override val title: String get() = "Evaluaciones"
        override val icon: ImageVector get() = Icons.Default.Edit
    }

    @Serializable
    data object Settings : DashboardRoutes {
        override val title: String get() = "Perfil"
        override val icon: ImageVector get() = Icons.Default.Settings
    }
}

val hhRoutes: List<DashboardRoutes> = listOf(
    DashboardRoutes.Dashboard,
    DashboardRoutes.Courses,
    DashboardRoutes.Attendance,
    DashboardRoutes.Evaluations,
    DashboardRoutes.Settings,
)