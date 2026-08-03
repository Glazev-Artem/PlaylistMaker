package com.glazev.playlistmaker.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(
    private val createViewModel: () -> ViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = createViewModel()
        require(modelClass.isInstance(viewModel)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return viewModel as T
    }
}
