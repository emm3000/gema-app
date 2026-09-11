package com.emm.gema.core.database

import com.emm.gema.core.domain.id.IdGenerator
import java.util.UUID

class UuidIdGenerator : IdGenerator {

    override fun newId(): String = UUID.randomUUID().toString()
}
