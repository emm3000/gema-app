package com.emm.gema.feat.dashboard.attendance

enum class AttendanceStatus(
    val forNetwork: String,
    val checkboxValue: Boolean,
) {

    Present(
        forNetwork = "PRESENT",
        checkboxValue = true,
    ),
    Absent(
        forNetwork = "ABSENT",
        checkboxValue = false,
    );

    fun not(): AttendanceStatus {
        return when (this) {
            Present -> Absent
            Absent -> Present
        }
    }
}