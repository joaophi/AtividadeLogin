package com.github.joaophi.atividade

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.joaophi.atividade.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                Toast.makeText(this, "Descrição inválida", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (quantidade == null || quantidade <= 0) {
                Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_LONG).show()
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
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        viewModel.items
            .onEach(adapter::submitList)
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }
}