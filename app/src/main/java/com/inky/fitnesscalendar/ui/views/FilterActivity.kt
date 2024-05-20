package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FilterActivity(
    filter: ActivityFilter,
    onFilterChange: (ActivityFilter) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit
) {
    val appBar = @Composable {
        with(sharedTransitionScope) {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ), title = {
                TextField(
                    filter.text ?: "",
                    onValueChange = {
                        onFilterChange(filter.copy(text = it))
                    },
                    placeholder = { Text(stringResource(R.string.search_for_activity)) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    singleLine = true
                )
            }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        stringResource(R.string.back),
                    )
                }
            }, actions = {
                IconButton(
                    onClick = { onFilterChange(ActivityFilter()) }, enabled = !filter.isEmpty()
                ) {
                    Icon(Icons.Outlined.Clear, stringResource(R.string.reset_filters))
                }
            }, modifier = Modifier.sharedBounds(
                rememberSharedContentState(key = "appBar"),
                animatedVisibilityScope = animatedContentScope
            )
            )
        }
    }
    Scaffold(
        topBar = appBar, containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val context = LocalContext.current
            val selectionLabel =
                if (filter.types.isEmpty()) null else filter.types.joinToString(", ") {
                    context.getString(
                        it.nameId
                    )
                }
            OptionGroup(
                label = stringResource(R.string.select_activity),
                selectionLabel = selectionLabel,
                modifier = Modifier.padding(all = 8.dp)
            ) {
                ActivityTypeSelector(
                    isSelected = { filter.types.contains(it) },
                    onSelect = { activityType ->
                        val oldSelection = filter.types
                        val newSelection =
                            oldSelection.filter { it != activityType }.toMutableList()
                        if (newSelection.size == oldSelection.size) {
                            newSelection.add(activityType)
                        }
                        onFilterChange(filter.copy(types = newSelection))
                    })
            }

        }
    }
}