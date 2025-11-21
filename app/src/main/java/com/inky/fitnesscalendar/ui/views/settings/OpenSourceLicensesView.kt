package com.inky.fitnesscalendar.ui.views.settings

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesView(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.open_source_licenses)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icons.ArrowBack(stringResource(R.string.back))
                    }
                },
            )
        },
    ) { paddingValues ->
        AndroidView(
            factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            },
            update = { it.loadUrl("file:///android_asset/open_source_licenses.html") },
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        )
    }
}