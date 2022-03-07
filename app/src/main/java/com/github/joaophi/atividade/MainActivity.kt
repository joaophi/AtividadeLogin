package com.github.joaophi.atividade

import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import com.github.joaophi.atividade.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

fun EditText.bind(lifecycle: Lifecycle, stateFlow: MutableStateFlow<String>) {
    stateFlow
        .filter { it != text?.toString() }
        .onEach(::setText)
        .flowWithLifecycle(lifecycle)
        .launchIn(lifecycle.coroutineScope)
    doAfterTextChanged { stateFlow.value = it.toString() }
}

fun RadioGroup.bindSexo(lifecycle: Lifecycle, stateFlow: MutableStateFlow<MainViewModel.Sexo>) {
    val map = MainViewModel.Sexo.values()
        .associateBy(MainViewModel.Sexo::id)
        .withDefault { MainViewModel.Sexo.NAO_SELECIONADO }
    stateFlow
        .filter { it.id != checkedRadioButtonId }
        .onEach { check(it.id) }
        .flowWithLifecycle(lifecycle)
        .launchIn(lifecycle.coroutineScope)
    setOnCheckedChangeListener { _, checkedId -> stateFlow.value = map.getValue(checkedId) }
}

fun DatePicker.bind(lifecycle: Lifecycle, stateFlow: MutableStateFlow<LocalDate>) {
    val date = stateFlow.value
    init(date.year, date.monthValue.dec(), date.dayOfMonth) { _, year, month, day ->
        stateFlow.value = LocalDate.of(year, month.inc(), day)
    }
    stateFlow
        .filter { it != LocalDate.of(year, month.inc(), dayOfMonth) }
        .onEach { updateDate(it.year, it.monthValue.dec(), it.dayOfMonth) }
        .flowWithLifecycle(lifecycle)
        .launchIn(lifecycle.coroutineScope)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtNome.bind(lifecycle, viewModel.nome)
        binding.rgSexo.bindSexo(lifecycle, viewModel.sexo)
        binding.dpNascimento.bind(lifecycle, viewModel.nascimento)
        binding.btnSalvar.setOnClickListener { viewModel.salvar() }

        viewModel.erros
            .onEach { Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_LONG).show() }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycle.coroutineScope)

        viewModel.corretos
            .onEach { Toast.makeText(this, "Pessoa: $it", Toast.LENGTH_LONG).show() }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycle.coroutineScope)
    }
}