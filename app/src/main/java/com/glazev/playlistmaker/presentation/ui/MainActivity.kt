package com.glazev.playlistmaker.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.glazev.playlistmaker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.main)
        
        // Запоминаем исходные отступы из XML
        val paddingLeft = rootView.paddingLeft
        val paddingTop = rootView.paddingTop
        val paddingRight = rootView.paddingRight
        val paddingBottom = rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Прибавляем системные инсеты к исходным отступам
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            insets
        }

        val searchButton = findViewById<Button>(R.id.button_search)
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val libraryButton = findViewById<Button>(R.id.button_library)
        libraryButton.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        val settingsButton = findViewById<Button>(R.id.button_settings)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
