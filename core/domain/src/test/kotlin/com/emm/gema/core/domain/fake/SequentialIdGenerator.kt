package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.id.IdGenerator
import java.util.concurrent.atomic.AtomicInteger

class SequentialIdGenerator(private val prefix: String = "id") : IdGenerator {

    private val counter = AtomicInteger()

    override fun newId(): String = "$prefix-${counter.incrementAndGet()}"
}
