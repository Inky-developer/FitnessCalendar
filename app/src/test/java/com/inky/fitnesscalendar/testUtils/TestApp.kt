package com.inky.fitnesscalendar.testUtils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.ui.ProvideSharedContent

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TestApp(content: @Composable context(AppRepository) () -> Unit) {
    MockApplication {
        SharedTransitionLayout {
            AnimatedContent(Unit, label = "rootAnimatedContentScope") {
                // state parameter has to be used, otherwise ide is angry :(
                println(it)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    content()
                }
            }
        }
    }
}