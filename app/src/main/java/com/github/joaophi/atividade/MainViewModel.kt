package com.github.joaophi.atividade

import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


class MainViewModel(handle: SavedStateHandle) : ViewModel() {
    val nome by handle.getStateFlow(initialValue = "")

    enum class Sexo(@IdRes val id: Int = -1) {
        MASCULINO(R.id.rbMasculino), FEMININO(R.id.rbFeminino), NAO_SELECIONADO;
    }

    val sexo by handle.getStateFlow(initialValue = Sexo.NAO_SELECIONADO)

    val nascimento by handle.getStateFlow(initialValue = LocalDate.now())

    data class Pessoa(val nome: String, val sexo: Sexo, val nascimento: LocalDate)

    val erros = MutableSharedFlow<Throwable>(extraBufferCapacity = 10)
    val corretos = MutableSharedFlow<Pessoa>(extraBufferCapacity = 10)
    fun salvar() {
        viewModelScope.launch {
            try {
                val nome = nome.value
                if (nome.isBlank())
                    throw Exception("Nome vazio")

                val sexo = sexo.value
                if (sexo == Sexo.NAO_SELECIONADO)
                    throw Exception("Sexo não selecionado")

                val nascimento = nascimento.value
                if (nascimento > LocalDate.now())
                    throw Exception("Nascimento maior que data atual")

                val pessoa = Pessoa(nome, sexo, nascimento)
                corretos.emit(pessoa)
            } catch (ex: Throwable) {
                erros.emit(ex)
            }
        }
    }
}