package com.emm.gema.data.network.auth

import android.content.SharedPreferences

class DataStore(private val sharedPreferences: SharedPreferences) {

    private val editor: SharedPreferences.Editor by lazy { sharedPreferences.edit() }

    val token: String
        get() = sharedPreferences.getString(TOKEN_ID, null).orEmpty()

    fun storeToken(token: String) {
        editor.putString(TOKEN_ID, token)
        editor.apply()
    }

    companion object {

        private const val TOKEN_ID = "TOKEN_ID"
    }
}