package com.inky.fitnesscalendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inky.fitnesscalendar.di.appContext
import com.inky.fitnesscalendar.ui.ImportView
import com.inky.fitnesscalendar.ui.components.AppFrame
import com.inky.fitnesscalendar.ui.util.AppContextProviders
import com.inky.fitnesscalendar.view_model.ImportViewModel
import java.io.FileNotFoundException

private const val TAG = "ImportActivity"

class ImportActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels {
        val app = appContext
        viewModelFactory { initializer { ImportViewModel(app) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.closeActivity = {
            finish()
        }

        if (savedInstanceState == null) {
            val uris = when (intent.action) {
                Intent.ACTION_SEND -> handleSendIntent()
                Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleIntent()
                Intent.ACTION_VIEW -> handleViewIntent()
                else -> throw RuntimeException("Unexpected action ${intent.action}")
            }

            Log.i(TAG, "Received: $uris")

            val files = try {
                uris.mapNotNull { contentResolver.openFileDescriptor(it, "r") }
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "File not found: $e")
                finish()
                return
            }

            if (files.isEmpty()) {
                Log.e(TAG, "No valid files in the intent")
                finish()
                return
            }

            viewModel.loadFiles(files)
        }

        setContent {
            AppFrame {
                AppContextProviders(repository = viewModel.dbRepository) {
                    ImportView(viewModel)
                }
            }
        }
    }

    private fun handleSendIntent(): List<Uri> {
        @Suppress("DEPRECATION") val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            ?: return emptyList()

        return listOf(uri)
    }

    private fun handleSendMultipleIntent(): List<Uri> {
        @Suppress("DEPRECATION") val uris =
            intent
                .getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
                ?.mapNotNull { it as? Uri }
                ?: return emptyList()

        return uris
    }

    private fun handleViewIntent(): List<Uri> {
        return listOfNotNull(intent.data)
    }
}

