package com.github.joaophi.atividade_login

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val login: String,
    val password: String,
    val name: String,
    val hint: String,
) : Parcelable

private val USERS = listOf(
    User(login = "fabricinho", password = "@moAcl@u", name = "Fabricio", hint = "Paixão"),
    User(login = "pretinho", password = "black", name = "", hint = "Inglês"),
    User(login = "jeanzinho", password = "tut0r1@lDEy0utub3", name = "Jean", hint = "Youtuber"),
)

sealed class LoginState : Parcelable {
    @Parcelize
    object LoggedOff : LoginState()

    @Parcelize
    object LoggingIn : LoginState()

    @Parcelize
    object UserNotFound : LoginState()

    @Parcelize
    data class WrongPassword(val hint: String) : LoginState()

    @Parcelize
    data class LoggedIn(val user: User) : LoginState()
}

class LoginViewModel(handle: SavedStateHandle) : ViewModel() {
    val username by handle.getStateFlow(initialValue = "")
    val password by handle.getStateFlow(initialValue = "")

    private val _login = MutableStateFlow<LoginState>(LoginState.LoggedOff)
    val login = _login.asSharedFlow()

    fun login() {
        viewModelScope.launch {
            _login.emit(LoginState.LoggingIn)

            val login = username.value
            val pass = password.value

            val user = USERS.find { it.login == login }

            password.value = ""
            val state = when {
                user == null -> LoginState.UserNotFound
                user.password != pass -> LoginState.WrongPassword(user.hint)
                else -> LoginState.LoggedIn(user)
            }
            _login.emit(state)
        }
    }

    fun logoff() {
        viewModelScope.launch {
            _login.emit(LoginState.LoggedOff)
        }
    }
}