package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.glazev.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val bottomNavigationDestinations = setOf(
        R.id.searchFragment,
        R.id.libraryFragment,
        R.id.settingsFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.main)
        val paddingLeft = rootView.paddingLeft
        val paddingTop = rootView.paddingTop
        val paddingRight = rootView.paddingRight
        val paddingBottom = rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigation.isVisible = destination.id in bottomNavigationDestinations
        }
    }
}
