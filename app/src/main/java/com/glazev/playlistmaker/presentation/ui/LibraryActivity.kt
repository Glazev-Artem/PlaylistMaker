package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.glazev.playlistmaker.R

class LibraryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_library)

        val libraryRoot = findViewById<View>(R.id.library_root)
        val paddingLeft = libraryRoot.paddingLeft
        val paddingTop = libraryRoot.paddingTop
        val paddingRight = libraryRoot.paddingRight
        val paddingBottom = libraryRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(libraryRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            insets
        }
    }
}
