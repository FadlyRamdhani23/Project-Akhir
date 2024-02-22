package com.tugasakhir.udmrputra.ui

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityHomeBinding
import com.tugasakhir.udmrputra.databinding.FragmentDashboardBinding
import com.tugasakhir.udmrputra.ui.onboarding.OnboardingActivity

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.bottomAppBar)
        val navView: BottomNavigationView = binding.bottomNavigationView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_dashboard -> {
                    // Handle navigation to dashboard
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                else -> false
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val fab: FloatingActionButton = findViewById(R.id.fab_add_activity)
        fab.setOnClickListener {
            showFabOptions()
        }
    }

    private fun showFabOptions() {
        val fab: FloatingActionButton = findViewById(R.id.fab_add_activity)
        val popupMenu = PopupMenu(this, fab)
        popupMenu.menuInflater.inflate(R.menu.fab_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_1 -> {
                    val intent = Intent(this, FragmentDashboardBinding::class.java)
                    startActivity(intent)
                    true
                }
                R.id.option_2 -> {
                    val intent = Intent(this, OnboardingActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}