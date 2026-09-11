package com.emm.gema.di

import com.emm.gema.core.database.GemaDatabase
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.attendance.SqlDelightAttendanceRepository
import com.emm.gema.core.database.backup.ContentResolverBackupDocuments
import com.emm.gema.core.database.backup.SharedPreferencesBackupSettingsRepository
import com.emm.gema.core.database.schoolyear.SqlDelightActiveSchoolYearRepository
import com.emm.gema.core.database.schoolyear.SqlDelightPeriodRepository
import com.emm.gema.core.database.schoolyear.SqlDelightSchoolYearRepository
import com.emm.gema.core.database.curriculum.SqlDelightCompetencyRepository
import com.emm.gema.core.database.curriculum.SqlDelightWorkedCompetencyRepository
import com.emm.gema.core.database.evaluation.SqlDelightPeriodLevelRepository
import com.emm.gema.core.database.section.SqlDelightSectionAreaRepository
import com.emm.gema.core.database.section.SqlDelightSectionRepository
import com.emm.gema.core.database.setup.SqlDelightSetupRepository
import com.emm.gema.core.database.siagie.CacheSiagieExportStore
import com.emm.gema.core.database.siagie.ContentResolverSiagieDocuments
import com.emm.gema.core.database.siagie.SqlDelightSiagieImportStore
import com.emm.gema.core.database.student.SqlDelightStudentRepository
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.attendance.CountAttendanceDaysUseCase
import com.emm.gema.core.domain.attendance.ExportMonthlyAttendanceUseCase
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.GetMonthlyAttendanceSummaryUseCase
import com.emm.gema.core.domain.attendance.RecordAttendanceUseCase
import com.emm.gema.core.domain.backup.BackupDocuments
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.BackupStore
import com.emm.gema.core.domain.backup.CreateBackupUseCase
import com.emm.gema.core.domain.backup.InspectBackupUseCase
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.backup.RestoreBackupUseCase
import com.emm.gema.core.domain.backup.SetReminderThresholdUseCase
import com.emm.gema.core.domain.backup.ValidateBackupUseCase
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.GetAreaRecordedLevelCountsUseCase
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelCountUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.export.ExportGradesUseCase
import com.emm.gema.core.domain.export.GetGradesExportPlanUseCase
import com.emm.gema.core.domain.export.GetGradesTemplateNameUseCase
import com.emm.gema.core.domain.export.SiagieExportStore
import com.emm.gema.core.domain.evaluation.GetPeriodLevelUseCase
import com.emm.gema.core.domain.evaluation.GetRecordedLevelCountsUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.evaluation.SavePeriodLevelUseCase
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.SwitchSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.UpdatePeriodsUseCase
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionCountsUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.section.SetAreaVisibilityUseCase
import com.emm.gema.core.domain.section.UpdateSectionUseCase
import com.emm.gema.core.domain.siagie.ApplySiagieImportUseCase
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.siagie.PreviewSiagieImportUseCase
import com.emm.gema.core.domain.siagie.SiagieDocuments
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.siagie.SiagieImportPlanner
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.siagie.SiagieRosterReader
import com.emm.gema.core.domain.setup.CompleteSetupUseCase
import com.emm.gema.core.domain.setup.SetupRepository
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import com.emm.gema.core.domain.student.GetStudentUseCase
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.SaveStudentUseCase
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.WithdrawStudentUseCase
import com.emm.gema.core.siagie.XlsxMonthlyAttendanceWriter
import com.emm.gema.core.siagie.XlsxSiagieGradesWriter
import com.emm.gema.core.siagie.XlsxSiagieRosterReader
import com.emm.gema.feature.backup.BackupViewModel
import com.emm.gema.home.HomeViewModel
import com.emm.gema.navigation.StartDestinationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File
import java.time.Clock

private const val EXPORTS_DIRECTORY: String = "exports"

val appModule: Module = module {
    single<GemaDatabase> { GemaDatabase(androidContext()) }
    single<GemaDb> { get<GemaDatabase>().database }
    single<Clock> { Clock.systemDefaultZone() }
    single<IdGenerator> { UuidIdGenerator() }
    single<SchoolYearRepository> { SqlDelightSchoolYearRepository(get()) }
    single<PeriodRepository> { SqlDelightPeriodRepository(get()) }
    single<SectionRepository> { SqlDelightSectionRepository(get()) }
    single<SectionAreaRepository> { SqlDelightSectionAreaRepository(get()) }
    single<ActiveSchoolYearRepository> { SqlDelightActiveSchoolYearRepository(get()) }
    single<SetupRepository> { SqlDelightSetupRepository(get()) }
    single<CompetencyRepository> { SqlDelightCompetencyRepository(get()) }
    single<WorkedCompetencyRepository> { SqlDelightWorkedCompetencyRepository(get()) }
    single<StudentRepository> { SqlDelightStudentRepository(get()) }
    single<SiagieImportStore> { SqlDelightSiagieImportStore(get()) }
    single<SiagieDocuments> { ContentResolverSiagieDocuments(androidContext().contentResolver) }
    single<SiagieRosterReader> { XlsxSiagieRosterReader() }
    single<SiagieGradesWriter> { XlsxSiagieGradesWriter() }
    single<SiagieExportStore> { CacheSiagieExportStore(File(androidContext().cacheDir, EXPORTS_DIRECTORY)) }
    single<PeriodLevelRepository> { SqlDelightPeriodLevelRepository(get()) }
    single<AttendanceRepository> { SqlDelightAttendanceRepository(get()) }
    single<MonthlyAttendanceExporter> {
        XlsxMonthlyAttendanceWriter(get<SiagieDocuments>(), androidContext().cacheDir.resolve("attendance-exports"))
    }
    single<BackupStore> { get<GemaDatabase>().backupStore }
    single<BackupDocuments> { ContentResolverBackupDocuments(androidContext().contentResolver) }
    single<BackupSettingsRepository> { SharedPreferencesBackupSettingsRepository(androidContext()) }

    factory<CompleteSetupUseCase> { CompleteSetupUseCase(get(), get()) }
    factory<GetSchoolYearsUseCase> { GetSchoolYearsUseCase(get()) }
    factory<GetActiveSchoolYearUseCase> { GetActiveSchoolYearUseCase(get(), get()) }
    factory<SwitchSchoolYearUseCase> { SwitchSchoolYearUseCase(get()) }
    factory<GetPeriodsUseCase> { GetPeriodsUseCase(get()) }
    factory<GetPeriodUseCase> { GetPeriodUseCase(get()) }
    factory<GetCurrentPeriodUseCase> { GetCurrentPeriodUseCase(get(), get()) }
    factory<UpdatePeriodsUseCase> { UpdatePeriodsUseCase(get(), get()) }
    factory<GetSchoolYearUseCase> { GetSchoolYearUseCase(get()) }
    factory<CreateSectionUseCase> { CreateSectionUseCase(get(), get()) }
    factory<UpdateSectionUseCase> { UpdateSectionUseCase(get()) }
    factory<DeleteSectionUseCase> { DeleteSectionUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory<GetSectionsUseCase> { GetSectionsUseCase(get()) }
    factory<GetSectionAreasUseCase> { GetSectionAreasUseCase(get()) }
    factory<GetSectionCountsUseCase> { GetSectionCountsUseCase(get()) }
    factory<GetSectionUseCase> { GetSectionUseCase(get()) }
    factory<SetAreaVisibilityUseCase> { SetAreaVisibilityUseCase(get()) }
    factory<SeedCurriculumUseCase> { SeedCurriculumUseCase(get()) }
    factory<GetPeriodCompetenciesUseCase> { GetPeriodCompetenciesUseCase(get(), get()) }
    factory<SetCompetencyWorkedUseCase> { SetCompetencyWorkedUseCase(get()) }
    factory<GetPeriodLevelGridUseCase> { GetPeriodLevelGridUseCase(get(), get(), get()) }
    factory<GetPeriodLevelUseCase> { GetPeriodLevelUseCase(get()) }
    factory<SavePeriodLevelUseCase> { SavePeriodLevelUseCase(get()) }
    factory<GetRecordedLevelCountsUseCase> { GetRecordedLevelCountsUseCase(get()) }
    factory<GetAreaRecordedLevelCountsUseCase> { GetAreaRecordedLevelCountsUseCase(get()) }
    factory<GetPeriodLevelCountUseCase> { GetPeriodLevelCountUseCase(get()) }
    factory<GetMissingPeriodLevelCountUseCase> {
        GetMissingPeriodLevelCountUseCase(get(), get(), get(), get())
    }
    factory<SaveStudentUseCase> { SaveStudentUseCase(get(), get()) }
    factory<SiagieImportPlanner> { SiagieImportPlanner(get(), get(), get(), get()) }
    factory<PreviewSiagieImportUseCase> { PreviewSiagieImportUseCase(get()) }
    factory<ApplySiagieImportUseCase> { ApplySiagieImportUseCase(get(), get(), get(), get(), get()) }
    factory<GetGradesTemplateNameUseCase> { GetGradesTemplateNameUseCase(get()) }
    factory<GetGradesExportPlanUseCase> { GetGradesExportPlanUseCase(get(), get()) }
    factory<ExportGradesUseCase> { ExportGradesUseCase(get(), get(), get(), get()) }
    factory<GetStudentsUseCase> { GetStudentsUseCase(get()) }
    factory<GetStudentUseCase> { GetStudentUseCase(get()) }
    factory<GetStudentCountsUseCase> { GetStudentCountsUseCase(get()) }
    factory<WithdrawStudentUseCase> { WithdrawStudentUseCase(get()) }
    factory<ReactivateStudentUseCase> { ReactivateStudentUseCase(get()) }
    factory<GetAttendanceDayUseCase> { GetAttendanceDayUseCase(get(), get()) }
    factory<RecordAttendanceUseCase> { RecordAttendanceUseCase(get(), get()) }
    factory<CountAttendanceDaysUseCase> { CountAttendanceDaysUseCase(get()) }
    factory<GetMonthlyAttendanceSummaryUseCase> { GetMonthlyAttendanceSummaryUseCase(get(), get()) }
    factory<ExportMonthlyAttendanceUseCase> { ExportMonthlyAttendanceUseCase(get(), get(), get()) }
    factory<ValidateBackupUseCase> { ValidateBackupUseCase(get<GemaDatabase>().schemaVersion) }
    factory<CreateBackupUseCase> { CreateBackupUseCase(get(), get(), get()) }
    factory<InspectBackupUseCase> { InspectBackupUseCase(get(), get()) }
    factory<RestoreBackupUseCase> { RestoreBackupUseCase(get(), get(), get()) }
    factory<ObserveBackupStatusUseCase> { ObserveBackupStatusUseCase(get(), get()) }
    factory<SetReminderThresholdUseCase> { SetReminderThresholdUseCase(get()) }

    viewModel { StartDestinationViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModelOf(::BackupViewModel)
}
