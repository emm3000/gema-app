package com.emm.gema.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emm.gema.core.domain.section.Area
import com.emm.gema.feature.attendance.day.AttendanceDayRoute
import com.emm.gema.feature.backup.BackupRoute
import com.emm.gema.feature.evaluation.levels.PeriodLevelsRoute
import com.emm.gema.feature.evaluation.worked.WorkedCompetenciesRoute
import com.emm.gema.feature.sections.areas.SectionAreasRoute
import com.emm.gema.feature.sections.detail.SectionDetailRoute
import com.emm.gema.feature.sections.form.SectionFormRoute
import com.emm.gema.feature.setup.periods.PeriodsRoute
import com.emm.gema.feature.setup.section.SetupSectionRoute
import com.emm.gema.feature.setup.year.SetupYearRoute
import com.emm.gema.feature.setup.years.SchoolYearsRoute
import com.emm.gema.feature.students.form.StudentFormRoute
import com.emm.gema.feature.students.list.StudentsRoute
import com.emm.gema.feature.students.siagie.ImportPreviewRoute
import com.emm.gema.home.HomeRoute
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun GemaNavHost(
    modifier: Modifier = Modifier,
    viewModel: StartDestinationViewModel = koinViewModel(),
) {
    val startDestination: State<String?> = viewModel.startDestination.collectAsStateWithLifecycle()
    val navController: NavHostController = rememberNavController()
    val destination: String = startDestination.value ?: return

    NavHost(
        navController = navController,
        startDestination = destination,
        modifier = modifier,
    ) {
        composable(GemaRoutes.HOME) {
            HomeRoute(
                onSectionForm = { schoolYearId, sectionId ->
                    navController.navigate(GemaRoutes.sectionForm(schoolYearId, sectionId))
                },
                onSectionDetail = { navController.navigate(GemaRoutes.sectionDetailOf(it)) },
                onAttendanceDay = { navController.navigate(GemaRoutes.attendanceDayOf(it, null)) },
                onSchoolYears = { navController.navigate(GemaRoutes.SCHOOL_YEARS) },
                onPeriods = { navController.navigate(GemaRoutes.periodsOf(it)) },
                onNavigateToBackup = { navController.navigate(GemaRoutes.BACKUP) },
            )
        }
        composable(GemaRoutes.BACKUP) {
            BackupRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(GemaRoutes.SETUP_YEAR) {
            SetupYearRoute(
                onDraftReady = { navController.navigate(GemaRoutes.SETUP_SECTION) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(GemaRoutes.SETUP_SECTION) {
            SetupSectionRoute(
                onFinished = { navController.toHome() },
                onAreaSelection = { sectionId ->
                    navController.toHome()
                    navController.navigate(GemaRoutes.sectionAreasOf(sectionId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(GemaRoutes.SCHOOL_YEARS) {
            SchoolYearsRoute(
                onPeriods = { navController.navigate(GemaRoutes.periodsOf(it)) },
                onAddYear = { navController.navigate(GemaRoutes.SETUP_YEAR) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.PERIODS,
            arguments = listOf(navArgument(GemaRoutes.SCHOOL_YEAR_ID) { type = NavType.StringType }),
        ) { entry ->
            PeriodsRoute(
                schoolYearId = entry.arguments?.getString(GemaRoutes.SCHOOL_YEAR_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.SECTION_FORM,
            arguments = listOf(
                navArgument(GemaRoutes.SCHOOL_YEAR_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.SECTION_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            SectionFormRoute(
                schoolYearId = entry.arguments?.getString(GemaRoutes.SCHOOL_YEAR_ID).orEmpty(),
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID)?.takeIf { it.isNotEmpty() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.SECTION_DETAIL,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            SectionDetailRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                onAttendanceDay = { sectionId, date ->
                    navController.navigate(GemaRoutes.attendanceDayOf(sectionId, date))
                },
                onStudents = { navController.navigate(GemaRoutes.studentsOf(it)) },
                onPeriodLevels = { navController.navigate(GemaRoutes.periodLevelsOf(it)) },
                onSectionAreas = { navController.navigate(GemaRoutes.sectionAreasOf(it)) },
                onSectionForm = { schoolYearId, sectionId ->
                    navController.navigate(GemaRoutes.sectionForm(schoolYearId, sectionId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.ATTENDANCE_DAY,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.DATE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            AttendanceDayRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                date = entry.arguments?.getString(GemaRoutes.DATE)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let(LocalDate::parse),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.STUDENTS,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            StudentsRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                onStudentForm = { sectionId, studentId ->
                    navController.navigate(GemaRoutes.studentForm(sectionId, studentId))
                },
                onImportPreview = { sectionId, uri ->
                    navController.navigate(GemaRoutes.importPreview(sectionId, uri))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.IMPORT_PREVIEW,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.URI) { type = NavType.StringType },
            ),
        ) { entry ->
            ImportPreviewRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                uri = entry.arguments?.getString(GemaRoutes.URI).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.STUDENT_FORM,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.STUDENT_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            StudentFormRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                studentId = entry.arguments?.getString(GemaRoutes.STUDENT_ID)?.takeIf { it.isNotEmpty() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.SECTION_AREAS,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            SectionAreasRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.PERIOD_LEVELS,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            PeriodLevelsRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                onWorkedCompetencies = { sectionId, periodId, area ->
                    navController.navigate(GemaRoutes.workedCompetenciesOf(sectionId, periodId, area))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.WORKED_COMPETENCIES,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.PERIOD_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.AREA) { type = NavType.StringType },
            ),
        ) { entry ->
            WorkedCompetenciesRoute(
                sectionId = entry.arguments?.getString(GemaRoutes.SECTION_ID).orEmpty(),
                periodId = entry.arguments?.getString(GemaRoutes.PERIOD_ID).orEmpty(),
                area = Area.valueOf(entry.arguments?.getString(GemaRoutes.AREA).orEmpty()),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.toHome() {
    navigate(GemaRoutes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}
