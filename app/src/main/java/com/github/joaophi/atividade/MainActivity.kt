package com.github.joaophi.atividade

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.joaophi.atividade.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun TextInputEditText.bind(lifecycle: Lifecycle, stateFlow: MutableStateFlow<String>) {
    stateFlow
        .filter { it != text.toString() }
        .onEach(::setText)
        .flowWithLifecycle(lifecycle)
        .launchIn(lifecycle.coroutineScope)
    doAfterTextChanged { stateFlow.value = it.toString() }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtUser.bind(lifecycle, viewModel.username)
        binding.edtPass.bind(lifecycle, viewModel.password)

        binding.login.setOnClickListener { viewModel.login() }

        viewModel.login
            .onEach { state ->
                binding.loading.isVisible = false
                binding.login.isEnabled = true
                binding.tvTexto.setOnClickListener(null)
                binding.tvTexto.isVisible = false

                when (state) {
                    LoginState.LoggedOff -> Unit
                    LoginState.LoggingIn -> {
                        binding.loading.isVisible = true
                        binding.login.isEnabled = false
                    }
                    LoginState.UserNotFound -> {
                        binding.tvTexto.text = "Usuário inexistente"
                        binding.tvTexto.isVisible = true
                    }
                    is LoginState.WrongPassword -> {
                        binding.tvTexto.text = "Senha incorreta! Clique aqui para ver a dica!"
                        binding.tvTexto.isVisible = true
                        binding.tvTexto.setOnClickListener {
                            binding.tvTexto.text = "Dica: ${state.hint}"
                        }
                    }
                    is LoginState.LoggedIn -> {
                        binding.tvTexto.text = "Usuário ${state.user.name} logado!"
                        binding.tvTexto.isVisible = true
                    }
                }
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }
}