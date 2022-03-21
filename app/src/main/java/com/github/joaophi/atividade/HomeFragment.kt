package com.github.joaophi.atividade

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.joaophi.atividade.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        val navController = findNavController()

        binding.btnListar.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_itemFragment)
        }

        binding.btnCadastrar.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_itemFragment)
        }
    }
}