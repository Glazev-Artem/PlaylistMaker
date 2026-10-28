package com.glazev.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.presentation.models.NewPlaylistEvent
import com.glazev.playlistmaker.presentation.models.NewPlaylistScreenState
import com.glazev.playlistmaker.presentation.viewmodel.NewPlaylistViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment(R.layout.fragment_new_playlist) {
    private val viewModel: NewPlaylistViewModel by viewModel()
    private var views: NewPlaylistViews? = null
    private var exitDialog: AlertDialog? = null
    private var displayedCover: String? = null

    private val pickCover = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Некоторые провайдеры предоставляют только временное разрешение.
            }
            viewModel.onCoverSelected(uri.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val boundViews = NewPlaylistViews(
            cover = view.findViewById(R.id.playlist_cover),
            nameLayout = view.findViewById(R.id.playlist_name_layout),
            name = view.findViewById(R.id.playlist_name),
            descriptionLayout = view.findViewById(R.id.playlist_description_layout),
            description = view.findViewById(R.id.playlist_description),
            create = view.findViewById(R.id.create_playlist_button)
        )
        views = boundViews
        boundViews.name.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
        boundViews.description.doAfterTextChanged {
            viewModel.onDescriptionChanged(it?.toString().orEmpty())
        }
        boundViews.cover.setOnClickListener {
            pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        boundViews.create.setOnClickListener { viewModel.onCreateClicked() }
        view.findViewById<ImageView>(R.id.new_playlist_back).setOnClickListener {
            viewModel.onBackRequested()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = viewModel.onBackRequested()
            }
        )
        viewModel.screenState.observe(viewLifecycleOwner, ::render)
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (val content = event.getContentIfNotHandled()) {
                NewPlaylistEvent.Close -> closeScreen()
                is NewPlaylistEvent.Created -> {
                    Toast.makeText(context, getString(R.string.playlist_created, content.name), Toast.LENGTH_SHORT).show()
                    closeScreen()
                }
                NewPlaylistEvent.SaveError -> {
                    Toast.makeText(context, R.string.playlist_save_error, Toast.LENGTH_SHORT).show()
                }
                null -> Unit
            }
        }
    }

    private fun render(state: NewPlaylistScreenState) {
        val boundViews = views ?: return
        if (boundViews.name.text.toString() != state.name) boundViews.name.setText(state.name)
        if (boundViews.description.text.toString() != state.description) {
            boundViews.description.setText(state.description)
        }
        updateHintColor(boundViews.nameLayout, state.name)
        updateHintColor(boundViews.descriptionLayout, state.description)
        boundViews.name.isEnabled = !state.isSaving
        boundViews.description.isEnabled = !state.isSaving
        boundViews.cover.isEnabled = !state.isSaving
        boundViews.create.isEnabled = state.isCreateEnabled
        if (displayedCover != state.coverUri) {
            displayedCover = state.coverUri
            Glide.with(this)
                .load(state.coverUri?.let(Uri::parse))
                .placeholder(R.drawable.playlist_cover_placeholder)
                .error(R.drawable.playlist_cover_placeholder)
                .centerCrop()
                .into(boundViews.cover)
        }
        if (state.showExitConfirmation && exitDialog == null) {
            exitDialog = MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistExitDialog)
                .setTitle(R.string.playlist_exit_title)
                .setMessage(R.string.playlist_exit_message)
                .setNegativeButton(R.string.playlist_exit_cancel) { _, _ -> viewModel.onExitCancelled() }
                .setPositiveButton(R.string.playlist_exit_confirm) { _, _ -> viewModel.onExitConfirmed() }
                .setOnCancelListener { viewModel.onExitCancelled() }
                .create()
                .also { dialog ->
                    dialog.setOnDismissListener { exitDialog = null }
                    dialog.show()
                }
        }
    }

    private fun updateHintColor(layout: TextInputLayout, text: String) {
        val attribute = if (text.isEmpty()) R.attr.settingsHeaderTextColor else R.attr.settingsIconTint
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attribute, typedValue, true)
        val color = if (typedValue.resourceId != 0) {
            ContextCompat.getColor(requireContext(), typedValue.resourceId)
        } else {
            typedValue.data
        }
        layout.defaultHintTextColor = ColorStateList.valueOf(color)
    }

    private fun closeScreen() {
        val keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        keyboard?.hideSoftInputFromWindow(view?.windowToken, 0)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        exitDialog?.setOnDismissListener(null)
        exitDialog?.dismiss()
        exitDialog = null
        views = null
        displayedCover = null
        super.onDestroyView()
    }

    private data class NewPlaylistViews(
        val cover: ShapeableImageView,
        val nameLayout: TextInputLayout,
        val name: TextInputEditText,
        val descriptionLayout: TextInputLayout,
        val description: TextInputEditText,
        val create: MaterialButton
    )
}
