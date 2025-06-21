package com.emm.gema.data.local

import com.emm.gema.domain.student.Student

fun StudentEntity.toDomain() = Student(
    id = id,
    fullName = fullName,
    dni = dni ?: "-",
    email = email ?: "-",
    birthDate = birthDate ?: "-",
    gender = gender ?: "-",
)

fun List<StudentEntity>.toDomain() = map(StudentEntity::toDomain)