package com.emm.gema.core.domain.section

private val gradeOrdinals: List<String> = listOf("1ro", "2do", "3ro", "4to", "5to", "6to")

fun gradeOrdinalLabel(number: Int): String = gradeOrdinals[number - 1]

fun Grade.label(): String = gradeOrdinalLabel(number)

fun Section.title(): String = "${grade.label()} $name"
