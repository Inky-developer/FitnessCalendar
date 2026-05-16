package com.inky.fitnesscalendar.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    crossinline factory: AppContext.() -> VM
): VM {
    val app = LocalContext.current.appContext
    return viewModel(
        key = VM::class.qualifiedName,
        factory = viewModelFactory {
            initializer { app.factory() }
        }
    )
}
