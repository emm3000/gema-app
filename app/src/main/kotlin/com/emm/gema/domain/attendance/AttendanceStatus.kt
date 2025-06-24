package com.emm.gema.domain.attendance

enum class AttendanceStatus(
    val forNetwork: String,
    val nativeValue: Boolean,
    val description: String,
) {

    Present(
        forNetwork = "PRESENT",
        nativeValue = true,
        description = "P",
    ),
    Absent(
        forNetwork = "ABSENT",
        nativeValue = false,
        description = "F",
    );

    fun not(): AttendanceStatus {
        return when (this) {
            Present -> Absent
            Absent -> Present
        }
    }
}