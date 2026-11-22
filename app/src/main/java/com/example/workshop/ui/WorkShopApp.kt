package com.example.workshop.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workshop.ui.tasks.AddTaskScreen
import com.example.workshop.ui.tasks.TaskListScreen
import com.example.workshop.ui.tasks.TaskViewModel

private enum class Screen { LIST, ADD }

@Composable
fun WorkShopApp(modifier: Modifier = Modifier) {
    val viewModel: TaskViewModel = viewModel()
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }

    BackHandler(enabled = screen == Screen.ADD) {
        screen = Screen.LIST
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (targetState == Screen.ADD) {
                (slideInHorizontally(animationSpec = tween(280)) { it / 4 } + fadeIn(tween(280)))
                    .togetherWith(fadeOut(tween(200)))
            } else {
                fadeIn(tween(280))
                    .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(280)) { it / 4 })
            }
        },
        label = "screen",
        modifier = modifier
    ) { current ->
        when (current) {
            Screen.LIST -> TaskListScreen(
                viewModel = viewModel,
                onAddTaskClick = { screen = Screen.ADD }
            )

            Screen.ADD -> AddTaskScreen(
                viewModel = viewModel,
                onNavigateBack = { screen = Screen.LIST }
            )
        }
    }
}
