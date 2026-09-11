package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class BackupFileNameTest {

    @Test
    fun `a backup file is named after the moment it was created`() {
        val name: String = backupFileName(LocalDateTime.of(2026, 9, 10, 14, 32))

        assertThat(name).isEqualTo("gema-20260910-1432.gema")
    }

    @Test
    fun `two backups created in the same minute share a name`() {
        val first: String = backupFileName(LocalDateTime.of(2026, 9, 10, 14, 32, 1))
        val second: String = backupFileName(LocalDateTime.of(2026, 9, 10, 14, 32, 59))

        assertThat(first).isEqualTo(second)
    }
}
