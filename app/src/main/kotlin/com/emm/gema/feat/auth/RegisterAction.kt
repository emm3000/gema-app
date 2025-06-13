package com.emm.gema.feat.auth

sealed interface RegisterAction {

    class OnNameChange(val value: String) : RegisterAction

    class OnEmailChange(val value: String) : RegisterAction

    class OnPasswordChange(val value: String) : RegisterAction

    class OnConfirmPasswordChange(val value: String) : RegisterAction

    class OnCheckedChange(val value: Boolean) : RegisterAction

    data object Register : RegisterAction
}