package com.github.joaophi.atividade.item

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.joaophi.atividade.MeuSQLite
import com.github.joaophi.atividade.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationManager = NotificationManagerCompat.from(application)
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
            val qtd = items.value.count() + 1

            banco.execSQL(
                """
                    INSERT OR REPLACE INTO item (id, descricao, quantidade)
                    VALUES (?, ?, ?)
                """.trimIndent(),
                arrayOf(id, descricao, quantidade),
            )
            _updateItem.emit(Unit)

            if (qtd % 10 == 0) {
                val channelId = "APP"

                val channel = NotificationChannelCompat
                    .Builder(channelId, NotificationManagerCompat.IMPORTANCE_MAX)
                    .build()
                notificationManager.createNotificationChannel(channel)

                val notification = NotificationCompat.Builder(getApplication(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Item $qtd adicionado")
                    .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                    .setContentText("$qtd é multiplo de 10")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build()

                notificationManager.notify(qtd, notification)
            }
        }
    }
}