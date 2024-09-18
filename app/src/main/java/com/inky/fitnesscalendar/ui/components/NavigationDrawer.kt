package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.views.Views


val selectableViews = listOf(
    Views.Home,
    Views.DayView(),
    Views.ActivityLog(),
    Views.Statistics(),
    Views.Settings
)

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    currentView: Views?,
    onNavigate: (Views) -> Unit,
    content: @Composable () -> Unit
) {

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primaryContainer) {
            val scrollState = rememberScrollState()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_launcher_foreground),
                    stringResource(R.string.app_icon)
                )
                Text(
                    stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(all = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = TextStyle(
                        fontSize = 26.sp,
                        shadow = Shadow(color = MaterialTheme.colorScheme.primary, blurRadius = 4f),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                for (view in selectableViews) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(view.nameId)) },
                        selected = currentView != null && currentView::class == view::class,
                        onClick = { if (currentView != view) onNavigate(view) },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }) {
        content()
    }
}