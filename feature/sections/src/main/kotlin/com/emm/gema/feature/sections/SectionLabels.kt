package com.emm.gema.feature.sections

import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section

fun Grade.label(): String = "$number°"

fun Section.title(): String = "${grade.label()} $name"
