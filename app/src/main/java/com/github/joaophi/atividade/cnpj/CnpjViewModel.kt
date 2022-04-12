package com.github.joaophi.atividade.cnpj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

interface ReceitaWS {
    data class Resposta(
        @Json(name = "situacao")
        val situacao: String?,
        @Json(name = "fantasia")
        val fantasia: String?,
        @Json(name = "nome")
        val nome: String?,
        @Json(name = "logradouro")
        val logradouro: String?,
        @Json(name = "status")
        val status: String,
        @Json(name = "message")
        val message: String?,
    )

    @GET("/v1/cnpj/{cnpj}")
    suspend fun getCnpj(@Path("cnpj") cnpj: String): Resposta
}

class CnpjViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.receitaws.com.br/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api: ReceitaWS = retrofit.create()

    private val _cnpj = MutableSharedFlow<Result<ReceitaWS.Resposta>>(replay = 1)
    val cnpj = _cnpj.asSharedFlow()

    fun consultar(cnpj: String) {
        viewModelScope.launch {
            _cnpj.emit(runCatching {
                if (cnpj.isBlank() || cnpj.length != 14)
                    throw Exception("CNPJ inválido")

                api.getCnpj(cnpj)
            })
        }
    }
}