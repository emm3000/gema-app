package com.emm.gema.core.domain.student

private val siagieOrder: Comparator<Student> = compareBy({ it.fullName }, { it.code.value })

fun List<Student>.orderedByName(): List<Student> = sortedWith(siagieOrder)
