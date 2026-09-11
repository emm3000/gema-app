package com.emm.gema.di

import com.emm.gema.core.database.GemaDatabase
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.backup.ContentResolverBackupDocuments
import com.emm.gema.core.database.backup.SharedPreferencesBackupSettingsRepository
import com.emm.gema.core.database.schoolyear.SqlDelightSchoolYearRepository
import com.emm.gema.core.domain.backup.BackupDocuments
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.BackupStore
import com.emm.gema.core.domain.backup.CreateBackupUseCase
import com.emm.gema.core.domain.backup.InspectBackupUseCase
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.backup.RestoreBackupUseCase
import com.emm.gema.core.domain.backup.SetReminderThresholdUseCase
import com.emm.gema.core.domain.backup.ValidateBackupUseCase
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.CreateSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.feature.backup.BackupViewModel
import com.emm.gema.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.time.Clock

val appModule: Module = module {
    single<GemaDatabase> { GemaDatabase(androidContext()) }
    single<GemaDb> { get<GemaDatabase>().database }
    single<Clock> { Clock.systemDefaultZone() }
    single<IdGenerator> { UuidIdGenerator() }
    single<SchoolYearRepository> { SqlDelightSchoolYearRepository(get()) }
    single<BackupStore> { get<GemaDatabase>().backupStore }
    single<BackupDocuments> { ContentResolverBackupDocuments(androidContext().contentResolver) }
    single<BackupSettingsRepository> { SharedPreferencesBackupSettingsRepository(androidContext()) }

    factory<CreateSchoolYearUseCase> { CreateSchoolYearUseCase(get(), get()) }
    factory<GetSchoolYearsUseCase> { GetSchoolYearsUseCase(get()) }
    factory<ValidateBackupUseCase> { ValidateBackupUseCase(get<GemaDatabase>().schemaVersion) }
    factory<CreateBackupUseCase> { CreateBackupUseCase(get(), get(), get()) }
    factory<InspectBackupUseCase> { InspectBackupUseCase(get(), get()) }
    factory<RestoreBackupUseCase> { RestoreBackupUseCase(get(), get(), get()) }
    factory<ObserveBackupStatusUseCase> { ObserveBackupStatusUseCase(get(), get()) }
    factory<SetReminderThresholdUseCase> { SetReminderThresholdUseCase(get()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::BackupViewModel)
}
