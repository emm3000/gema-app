package com.emm.gema.core.domain.section

fun Grade.label(): String = "$number°"

fun Section.title(): String = "${grade.label()} $name"
