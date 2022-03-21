package com.github.joaophi.atividade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.joaophi.atividade.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var configuration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment: NavHostFragment = binding.navHostFragment.getFragment()
        navController = navHostFragment.navController
        configuration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, configuration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(configuration) || super.onSupportNavigateUp()
    }
}