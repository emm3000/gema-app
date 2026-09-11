package com.emm.gema.feature.setup

import com.emm.gema.feature.setup.year.SchoolYearDraft

class SetupDraftStore {

    private var draft: SchoolYearDraft? = null

    fun put(draft: SchoolYearDraft) {
        this.draft = draft
    }

    fun take(): SchoolYearDraft? = draft
}
