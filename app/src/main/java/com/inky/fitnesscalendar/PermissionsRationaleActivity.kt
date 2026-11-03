package com.inky.fitnesscalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.inky.fitnesscalendar.ui.components.AppFrame
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons


class PermissionsRationaleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)


        setContent {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            val appName = stringResource(R.string.app_name)
            val text = buildAnnotatedString {
                withSection(stringResource(R.string.privacy_general_title)) {
                    append(stringResource(R.string.privacy_general_text, appName))
                }
                withSection(stringResource(R.string.privacy_notification_permission_title)) {
                    append(stringResource(R.string.privacy_notification_permission_text))
                }
                withSection(stringResource(R.string.privacy_health_permissions_title)) {
                    append(stringResource(R.string.privacy_health_permissions_text, appName))
                }
                withSection(stringResource(R.string.privacy_contact_title)) {
                    append(stringResource(R.string.privacy_contact_text))
                    withLink(LinkAnnotation.Url(stringResource(R.string.github_url))) {
                        append(stringResource(R.string.github))
                    }
                }
            }

            AppFrame {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.privacy)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icons.ArrowBack(stringResource(R.string.back))
                                }
                            },
                            colors = defaultTopAppBarColors(),
                            scrollBehavior = scrollBehavior
                        )
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text)
                    }
                }
            }
        }
    }
}

private inline fun AnnotatedString.Builder.withSection(
    title: String,
    content: AnnotatedString.Builder.() -> Unit
) {
    pushStyle(ParagraphStyle())
    try {
        withStyle(SpanStyle(fontSize = 1.25.em)) {
            append(title)
            append("\n")
        }
        content()
        append("\n")
    } finally {
        pop()
    }
}