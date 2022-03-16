package com.github.joaophi.atividade

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val banco = MeuSQLite(application, name = "aplicacaodb").writableDatabase

    private val _updateItem = MutableSharedFlow<Unit>()
    val items = _updateItem
        .onStart { emit(Unit) }
        .map {
            val columns = arrayOf("id", "descricao", "quantidade")
            val cursor = banco.query("item", columns, null, null, null, null, null)

            buildList {
                while (cursor.moveToNext()) {
                    val item = Item(
                        id = cursor.getInt(0),
                        descricao = cursor.getString(1),
                        quantidade = cursor.getInt(2),
                    )
                    add(item)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = emptyList())

    init {
        _updateItem.tryEmit(Unit)
    }

    fun excluir(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            banco.execSQL(
                """
                    DELETE FROM item
                    WHERE id = ?
                """.trimIndent(),
                arrayOf(id),
            )
            _updateItem.emit(Unit)
        }
    }

    fun salvar(id: Int?, descricao: String, quantidade: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            banco.execSQL(
                """
                    INSERT OR REPLACE INTO item (id, descricao, quantidade)
                    VALUES (?, ?, ?)
                """.trimIndent(),
                arrayOf(id, descricao, quantidade),
            )
            _updateItem.emit(Unit)
        }
    }
}