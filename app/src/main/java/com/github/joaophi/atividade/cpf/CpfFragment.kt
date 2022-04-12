package com.github.joaophi.atividade.cpf

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.joaophi.atividade.R
import com.github.joaophi.atividade.databinding.CpfFragmentBinding

class CpfFragment : Fragment(R.layout.cpf_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = CpfFragmentBinding.bind(view)
        binding.root.webViewClient = WebViewClient()
        binding.root.loadUrl("https://servicos.receita.fazenda.gov.br/Servicos/CPF/ConsultaSituacao/ConsultaPublica.asp")
    }
}