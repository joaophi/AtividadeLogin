package com.github.joaophi.atividade

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: Int,
    val descricao: String,
    val quantidade: Int,
) : Parcelable
