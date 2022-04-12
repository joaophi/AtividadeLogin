package com.github.joaophi.atividade.cnpj

import android.os.Bundle
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.joaophi.atividade.R
import com.github.joaophi.atividade.databinding.CnpjFragmentBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CnpjFragment : Fragment(R.layout.cnpj_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = CnpjFragmentBinding.bind(view)
        val viewModel: CnpjViewModel by viewModels()

        binding.btnConsultar.setOnClickListener {
            viewModel.consultar(binding.edtCnpj.text?.toString().orEmpty())
        }

        viewModel.cnpj
            .onEach {
                binding.tvResposta.text = buildSpannedString {
                    when {
                        it.isSuccess -> {
                            val resposta = it.getOrThrow()
                            when (resposta.status) {
                                "OK" -> {
                                    bold { append("Situacao:") }
                                    append(resposta.situacao ?: "Erro")
                                    appendLine()
                                    bold { append("Fantasia:") }
                                    append(resposta.fantasia ?: "Erro")
                                    appendLine()
                                    bold { append("Nome:") }
                                    append(resposta.nome ?: "Erro")
                                    appendLine()
                                    bold { append("Logradouro:") }
                                    append(resposta.logradouro ?: "Erro")
                                }
                                "ERROR" -> {
                                    bold { append("Erro:") }
                                    append(resposta.message ?: "Erro")
                                }
                            }
                        }
                        it.isFailure -> {
                            bold { append("Erro:") }
                            append(it.exceptionOrNull()!!.message ?: "Erro")
                        }
                    }
                }
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}