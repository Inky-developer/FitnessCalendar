package com.inky.fitnesscalendar.ui.views.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutView(onBack: () -> Unit, onNavigateOpenSourceLicenses: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.about)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            val context = LocalContext.current
            val nameVersionText =
                remember { "${context.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}" }
            Text(nameVersionText, modifier = Modifier.padding(all = 8.dp))
            Button(onClick = { openGithubRepo(context) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.star_on_github))
            }
            Button(onClick = onNavigateOpenSourceLicenses, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.show_open_source_licenses))
            }
        }
    }
}

private fun openGithubRepo(context: Context) {
    val repoUrl = context.getString(R.string.github_url)
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
    context.startActivity(browserIntent)
}