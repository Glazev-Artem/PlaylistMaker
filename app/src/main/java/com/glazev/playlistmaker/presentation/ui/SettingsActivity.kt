package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.creator.Creator
import com.glazev.playlistmaker.presentation.viewmodel.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val settingsRoot = findViewById<View>(R.id.settings_root)
        val paddingLeft = settingsRoot.paddingLeft
        val paddingTop = settingsRoot.paddingTop
        val paddingRight = settingsRoot.paddingRight
        val paddingBottom = settingsRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(settingsRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            insets
        }

        viewModel = ViewModelProvider(
            this,
            Creator.provideSettingsViewModelFactory(this)
        )[SettingsViewModel::class.java]

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        viewModel.screenState.observe(this) { state ->
            if (themeSwitcher.isChecked != state.isDarkThemeEnabled) {
                themeSwitcher.isChecked = state.isDarkThemeEnabled
            }
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.onThemeChanged(checked)
        }

        val shareButton = findViewById<FrameLayout>(R.id.share_button)
        shareButton.setOnClickListener {
            viewModel.onShareClicked()
        }

        val supportButton = findViewById<FrameLayout>(R.id.support_button)
        supportButton.setOnClickListener {
            viewModel.onSupportClicked()
        }

        val agreementButton = findViewById<FrameLayout>(R.id.agreement_button)
        agreementButton.setOnClickListener {
            viewModel.onAgreementClicked()
        }
    }
}
