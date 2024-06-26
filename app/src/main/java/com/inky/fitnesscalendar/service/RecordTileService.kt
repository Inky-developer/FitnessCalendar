package com.inky.fitnesscalendar.service

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.ui.theme.FitnessCalendarTheme
import com.inky.fitnesscalendar.ui.views.QsTileRecordActivityDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecordTileService : TileService() {
    @Inject
    lateinit var repository: AppRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onClick() {
        super.onClick()
        showDialog(
            TileServiceDialog(this, repository, onStartRecording = { startRecording(it) })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun startRecording(typeRecording: TypeRecording) = scope.launch {
        repository.startRecording(
            typeRecording,
            this@RecordTileService
        )
    }

    class TileServiceDialog(
        context: Context,
        val repository: AppRepository,
        val onStartRecording: (TypeRecording) -> Unit
    ) : Dialog(context, R.style.FullHeightDialog) {
        private val lifecycleOwner = MyHackyLifecycleOwner()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            lifecycleOwner.onStart()
            val composeView = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    FitnessCalendarTheme {
                        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                            val typeRows by repository.getActivityTypeRows()
                                .collectAsState(initial = emptyList())
                            QsTileRecordActivityDialog(
                                typeRows,
                                onSave = {
                                    onStartRecording(it)
                                    dismiss()
                                },
                                onDismiss = { dismiss() }
                            )
                        }
                    }
                }
            }

//            setTitle(R.string.record_activity)
            setContentView(composeView)
        }

        override fun onStop() {
            lifecycleOwner.onStop()
            super.onStop()
        }

        /**
         * https://stackoverflow.com/questions/65755763/inputmethodservice-with-jetpack-compose-composeview-causes-composed-into-the
         */
        class MyHackyLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val savedStateRegistryController = SavedStateRegistryController.create(this)
            private val store = ViewModelStore()

            override val lifecycle: Lifecycle
                get() = lifecycleRegistry

            override val savedStateRegistry: SavedStateRegistry
                get() = savedStateRegistryController.savedStateRegistry

            override val viewModelStore: ViewModelStore
                get() = store

            fun onStart() {
                savedStateRegistryController.performRestore(null)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            fun onStop() {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                store.clear()
            }
        }
    }
}