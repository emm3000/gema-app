package com.emm.gema.domain.attendance

enum class AttendanceStatus(
    val forNetwork: String,
    val nativeValue: Boolean,
) {

    Present(
        forNetwork = "PRESENT",
        nativeValue = true,
    ),
    Absent(
        forNetwork = "ABSENT",
        nativeValue = false,
    );

    fun not(): AttendanceStatus {
        return when (this) {
            Present -> Absent
            Absent -> Present
        }
    }
}