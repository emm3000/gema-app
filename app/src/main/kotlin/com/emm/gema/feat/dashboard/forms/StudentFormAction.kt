package com.emm.gema.feat.dashboard.forms

sealed interface StudentFormAction {

    class NameChanged(val name: String): StudentFormAction

    class EmailChanged(val email: String): StudentFormAction

    class DniChanged(val dni: String): StudentFormAction

    class SexChanged(val sex: String): StudentFormAction

    object Save: StudentFormAction
}