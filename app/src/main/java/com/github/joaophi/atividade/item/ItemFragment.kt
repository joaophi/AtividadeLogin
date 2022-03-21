package com.github.joaophi.atividade.item

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.joaophi.atividade.R
import com.github.joaophi.atividade.databinding.FragmentItemBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ItemFragment : Fragment(R.layout.fragment_item) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: ItemViewModel by viewModels()
        val binding = FragmentItemBinding.bind(view)

        binding.btnLimpar.setOnClickListener {
            binding.edtID.text = null
            binding.edtDescricao.text = null
            binding.edtQuantidade.text = null
        }

        binding.btnSalvar.setOnClickListener {
            val id = binding.edtID.text?.toString()?.toIntOrNull()
            val descricao = binding.edtDescricao.text?.toString()
            val quantidade = binding.edtQuantidade.text?.toString()?.toIntOrNull()

            if (descricao.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Descrição inválida", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (quantidade == null || quantidade <= 0) {
                Toast.makeText(requireContext(), "Quantidade inválida", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.salvar(id, descricao, quantidade)
            binding.btnLimpar.callOnClick()
        }

        val adapter = ItemAdapter(
            onClick = {
                binding.edtID.setText("${it.id}")
                binding.edtDescricao.setText(it.descricao)
                binding.edtQuantidade.setText("${it.quantidade}")
            },
            onDelete = { viewModel.excluir(it.id) },
        )
        binding.rvItens.adapter = adapter
        binding.rvItens.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        viewModel.items
            .onEach(adapter::submitList)
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }
}