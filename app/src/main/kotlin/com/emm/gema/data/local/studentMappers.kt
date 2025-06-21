package com.emm.gema.data.local

import com.emm.gema.domain.student.Student

fun StudentEntity.toDomain() = Student(
    id = id,
    fullName = full_name,
    dni = dni ?: "-",
    email = email ?: "-",
    birthDate = birth_date ?: "-",
    gender = gender ?: "-",
)

fun List<StudentEntity>.toDomain() = map(StudentEntity::toDomain)