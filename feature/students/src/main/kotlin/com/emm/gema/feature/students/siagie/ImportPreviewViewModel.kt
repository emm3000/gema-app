package com.emm.gema.feature.students.siagie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.gradeOrdinalLabel
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.siagie.ApplySiagieImportUseCase
import com.emm.gema.core.domain.siagie.PreviewSiagieImportUseCase
import com.emm.gema.core.domain.siagie.SiagieImportEntry
import com.emm.gema.core.domain.siagie.SiagieImportMissing
import com.emm.gema.core.domain.siagie.SiagieImportPlan
import com.emm.gema.core.domain.siagie.SiagieImportPreview
import com.emm.gema.core.domain.siagie.SiagieImportRejection
import com.emm.gema.core.domain.siagie.SiagieImportResult
import com.emm.gema.core.domain.student.StudentId
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ImportPreviewViewModel(
    private val sectionId: SectionId,
    private val uri: String,
    private val getSection: GetSectionUseCase,
    private val previewImport: PreviewSiagieImportUseCase,
    private val applyImport: ApplySiagieImportUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _state: MutableStateFlow<ImportPreviewUiState> = MutableStateFlow(ImportPreviewUiState())
    val state: StateFlow<ImportPreviewUiState> = _state.asStateFlow()

    private val _effects: Channel<ImportPreviewUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<ImportPreviewUiEffect> = _effects.receiveAsFlow()

    private var sectionTitle: String = ""

    init {
        viewModelScope.launch {
            val section: Section? = getSection(sectionId)
            sectionTitle = section?.title().orEmpty()
            _state.value = _state.value.copy(sectionTitle = sectionTitle)
            load(section)
        }
    }

    fun onIntent(intent: ImportPreviewUiIntent) {
        when (intent) {
            is ImportPreviewUiIntent.GroupToggled -> toggleGroup(intent.group)
            is ImportPreviewUiIntent.WithdrawalToggled -> toggleWithdrawal(intent.studentId, intent.isSelected)
            ImportPreviewUiIntent.ApplyClicked -> apply()
            ImportPreviewUiIntent.CancelClicked -> emit(ImportPreviewUiEffect.NavigateBack)
            ImportPreviewUiIntent.BackClicked -> emit(ImportPreviewUiEffect.NavigateBack)
        }
    }

    private suspend fun load(section: Section?) {
        val preview: SiagieImportPreview = runCatching { previewImport(sectionId, uri) }
            .getOrElse {
                _effects.send(ImportPreviewUiEffect.ShowMessage(ImportPreviewMessage.ImportFailed))
                SiagieImportPreview.Rejected(SiagieImportRejection.NotASiagieTemplate, fileName = _state.value.fileName)
            }
        _state.value = when (preview) {
            is SiagieImportPreview.Ready -> _state.value.readyWith(preview.plan)
            is SiagieImportPreview.Rejected -> _state.value.copy(
                isLoading = false,
                fileName = preview.fileName,
                rejection = rejectionOf(preview.reason, section),
            )
        }
    }

    private fun ImportPreviewUiState.readyWith(plan: SiagieImportPlan): ImportPreviewUiState = copy(
        isLoading = false,
        fileName = plan.fileName,
        rosterSize = plan.rosterSize,
        created = plan.created.map { it.toRow() },
        updated = plan.updated.map { it.toRow() },
        proposedWithdrawals = plan.missing.map { it.toRow() },
    )

    private fun toggleGroup(group: ImportGroup) {
        val expanded: ImportGroup? = _state.value.expandedGroup
        _state.value = _state.value.copy(expandedGroup = if (expanded == group) null else group)
    }

    private fun toggleWithdrawal(studentId: StudentId, isSelected: Boolean) {
        val withdrawals: List<ImportWithdrawalRow> = _state.value.proposedWithdrawals
            .map { row -> if (row.studentId == studentId) row.copy(isSelected = isSelected) else row }
        _state.value = _state.value.copy(proposedWithdrawals = withdrawals)
    }

    private fun apply() {
        if (!_state.value.canApply) return
        _state.value = _state.value.copy(isApplying = true)
        viewModelScope.launch {
            val withdrawals: Set<StudentId> = _state.value.proposedWithdrawals
                .filter { it.isSelected }
                .map { it.studentId }
                .toSet()
            runCatching { applyImport(sectionId, uri, withdrawals, LocalDate.now(clock)) }
                .onSuccess { result: SiagieImportResult -> announce(result) }
                .onFailure { fail(ImportPreviewMessage.ImportFailed) }
        }
    }

    private suspend fun announce(result: SiagieImportResult) {
        when (result) {
            is SiagieImportResult.Applied -> {
                _effects.send(ImportPreviewUiEffect.ShowMessage(result.asMessage()))
                _effects.send(ImportPreviewUiEffect.NavigateBack)
            }

            is SiagieImportResult.Rejected -> {
                _state.value = _state.value.copy(isApplying = false, rejection = rejectionOf(result.reason, null))
            }
        }
    }

    private suspend fun fail(message: ImportPreviewMessage) {
        _state.value = _state.value.copy(isApplying = false)
        _effects.send(ImportPreviewUiEffect.ShowMessage(message))
    }

    private fun rejectionOf(reason: SiagieImportRejection, section: Section?): ImportRejection = when (reason) {
        SiagieImportRejection.NotASiagieTemplate -> ImportRejection(
            reason = ImportRejectionReason.NotASiagieTemplate,
            instruction = ImportInstruction.PICK_ANOTHER_FILE,
            expected = null,
            found = null,
            foundLabel = null,
        )
        SiagieImportRejection.EmptyRoster -> ImportRejection(
            reason = ImportRejectionReason.EmptyRoster,
            instruction = ImportInstruction.PICK_ANOTHER_FILE,
            expected = null,
            found = null,
            foundLabel = null,
        )
        is SiagieImportRejection.MalformedRow -> ImportRejection(
            reason = ImportRejectionReason.MalformedRow(reason.row),
            instruction = ImportInstruction.FIX_FILE,
            expected = null,
            found = null,
            foundLabel = null,
        )
        is SiagieImportRejection.GradeMismatch -> ImportRejection(
            reason = ImportRejectionReason.GradeMismatch,
            instruction = ImportInstruction.PICK_ANOTHER_FILE_OR_OPEN_SECTION,
            expected = sectionTitle.ifEmpty { section?.title().orEmpty() },
            found = gradeOrdinalLabel(reason.found),
            foundLabel = ImportFoundLabel.GRADE,
        )
        is SiagieImportRejection.SectionMismatch -> ImportRejection(
            reason = ImportRejectionReason.SectionMismatch,
            instruction = ImportInstruction.PICK_ANOTHER_FILE_OR_OPEN_SECTION,
            expected = sectionTitle.ifEmpty { section?.title().orEmpty() },
            found = reason.found,
            foundLabel = ImportFoundLabel.SECTION,
        )
    }

    private fun SiagieImportResult.Applied.asMessage(): ImportPreviewMessage =
        ImportPreviewMessage.Applied(created = created, updated = updated, withdrawn = withdrawn)

    private fun emit(effect: ImportPreviewUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun SiagieImportEntry.toRow(): ImportStudentRow = ImportStudentRow(
        studentCode = code.value,
        displayName = fullName,
    )

    private fun SiagieImportMissing.toRow(): ImportWithdrawalRow = ImportWithdrawalRow(
        studentId = studentId,
        displayName = fullName,
        isSelected = true,
    )
}
