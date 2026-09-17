package com.emm.gema.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emm.gema.about.AboutRoute
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.activities.evidence.ActivityEvidenceRoute
import com.emm.gema.feature.activities.form.ActivityFormRoute
import com.emm.gema.feature.activities.list.ActivitiesRoute
import com.emm.gema.feature.attendance.day.AttendanceDayRoute
import com.emm.gema.feature.attendance.month.AttendanceMonthRoute
import com.emm.gema.feature.backup.BackupRoute
import com.emm.gema.feature.evaluation.levels.PeriodLevelsRoute
import com.emm.gema.feature.evaluation.worked.WorkedCompetenciesRoute
import com.emm.gema.feature.export.ExportRoute
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
import java.time.LocalDate
import java.time.YearMonth
import org.koin.androidx.compose.koinViewModel

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
                onAttendanceDay = { sectionId, date ->
                    navController.navigate(GemaRoutes.attendanceDayOf(sectionId, date))
                },
                onSchoolYears = { navController.navigate(GemaRoutes.SCHOOL_YEARS) },
                onPeriods = { navController.navigate(GemaRoutes.periodsOf(it)) },
                onNavigateToBackup = { navController.navigate(GemaRoutes.BACKUP) },
                onNavigateToAbout = { navController.navigate(GemaRoutes.ABOUT) },
            )
        }
        composable(GemaRoutes.BACKUP) {
            BackupRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(GemaRoutes.ABOUT) {
            AboutRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(GemaRoutes.SETUP_YEAR) {
            SetupYearRoute(
                onDraftReady = { navController.navigate(GemaRoutes.SETUP_SECTION) },
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
                schoolYearId = SchoolYearId(entry.argument(GemaRoutes.SCHOOL_YEAR_ID)),
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
                schoolYearId = SchoolYearId(entry.argument(GemaRoutes.SCHOOL_YEAR_ID)),
                sectionId = entry.optionalArgument(GemaRoutes.SECTION_ID)?.let(::SectionId),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.SECTION_DETAIL,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            SectionDetailRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                onAttendanceDay = { sectionId, date ->
                    navController.navigate(GemaRoutes.attendanceDayOf(SectionId(sectionId), date))
                },
                onStudents = { navController.navigate(GemaRoutes.studentsOf(it)) },
                onPeriodLevels = { navController.navigate(GemaRoutes.periodLevelsOf(it)) },
                onExport = { navController.navigate(GemaRoutes.exportOf(it)) },
                onActivities = { navController.navigate(GemaRoutes.activitiesOf(it)) },
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
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                date = entry.arguments?.getString(GemaRoutes.DATE)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let(LocalDate::parse),
                onBack = { navController.popBackStack() },
                onMonthlySummary = { sectionId, month ->
                    navController.navigate(GemaRoutes.attendanceMonthOf(SectionId(sectionId), month))
                },
            )
        }
        composable(
            route = GemaRoutes.ATTENDANCE_MONTH,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.MONTH) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            AttendanceMonthRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                month = entry.arguments?.getString(GemaRoutes.MONTH)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let(YearMonth::parse),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.STUDENTS,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            StudentsRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                onStudentForm = { sectionId, studentId ->
                    navController.navigate(GemaRoutes.studentForm(sectionId, studentId))
                },
                onImportPreview = { sectionId, uri ->
                    navController.navigate(GemaRoutes.importPreview(SectionId(sectionId), uri))
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
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                uri = entry.argument(GemaRoutes.URI),
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
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                studentId = entry.optionalArgument(GemaRoutes.STUDENT_ID)?.let(::StudentId),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.SECTION_AREAS,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            SectionAreasRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.PERIOD_LEVELS,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.STUDENT_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(GemaRoutes.COMPETENCY_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            PeriodLevelsRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                studentId = entry.optionalArgument(GemaRoutes.STUDENT_ID)?.let(::StudentId),
                competencyId = entry.optionalArgument(GemaRoutes.COMPETENCY_ID)?.let(::CompetencyId),
                onWorkedCompetencies = { sectionId, periodId, area ->
                    navController.navigate(GemaRoutes.workedCompetenciesOf(sectionId, periodId, area))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.EXPORT,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            ExportRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                onPeriodLevelCell = { sectionId, studentId, competencyId ->
                    navController.navigate(
                        GemaRoutes.periodLevelCellOf(sectionId, studentId, competencyId),
                    )
                },
                onStudents = { navController.navigate(GemaRoutes.studentsOf(it)) },
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
            val area: Area? = parseArea(entry.arguments?.getString(GemaRoutes.AREA))
            if (area == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                WorkedCompetenciesRoute(
                    sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                    periodId = PeriodId(entry.argument(GemaRoutes.PERIOD_ID)),
                    area = area,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(
            route = GemaRoutes.ACTIVITIES,
            arguments = listOf(navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            ActivitiesRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                onActivityEvidence = { navController.navigate(GemaRoutes.activityEvidenceOf(it)) },
                onActivityForm = { sectionId, activityId ->
                    navController.navigate(GemaRoutes.activityForm(sectionId, activityId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.ACTIVITY_FORM,
            arguments = listOf(
                navArgument(GemaRoutes.SECTION_ID) { type = NavType.StringType },
                navArgument(GemaRoutes.ACTIVITY_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            ActivityFormRoute(
                sectionId = SectionId(entry.argument(GemaRoutes.SECTION_ID)),
                activityId = entry.optionalArgument(GemaRoutes.ACTIVITY_ID)?.let(::ActivityId),
                onActivityEvidence = {
                    navController.navigate(GemaRoutes.activityEvidenceOf(it)) {
                        popUpTo(GemaRoutes.ACTIVITIES) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = GemaRoutes.ACTIVITY_EVIDENCE,
            arguments = listOf(navArgument(GemaRoutes.ACTIVITY_ID) { type = NavType.StringType }),
        ) { entry ->
            ActivityEvidenceRoute(
                activityId = ActivityId(entry.argument(GemaRoutes.ACTIVITY_ID)),
                onActivityForm = { sectionId, activityId ->
                    navController.navigate(GemaRoutes.activityForm(sectionId, activityId))
                },
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

internal fun parseArea(raw: String?): Area? = Area.entries.firstOrNull { it.name == raw }

private fun NavBackStackEntry.argument(key: String): String = arguments?.getString(key).orEmpty()

private fun NavBackStackEntry.optionalArgument(key: String): String? =
    arguments?.getString(key)?.takeIf { it.isNotEmpty() }
