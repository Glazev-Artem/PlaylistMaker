package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.presentation.viewmodel.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val themeSwitcher = view.findViewById<SwitchMaterial>(R.id.themeSwitcher)
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            if (themeSwitcher.isChecked != state.isDarkThemeEnabled) {
                themeSwitcher.isChecked = state.isDarkThemeEnabled
            }
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.onThemeChanged(checked)
        }

        view.findViewById<FrameLayout>(R.id.share_button).setOnClickListener {
            viewModel.onShareClicked()
        }

        view.findViewById<FrameLayout>(R.id.support_button).setOnClickListener {
            viewModel.onSupportClicked()
        }

        view.findViewById<FrameLayout>(R.id.agreement_button).setOnClickListener {
            viewModel.onAgreementClicked()
        }
    }
}
