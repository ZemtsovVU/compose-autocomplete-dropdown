package com.study.composedropdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.study.composedropdown.ui.theme.MainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                MainScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
//                        .padding(16.dp, 500.dp, 16.dp, 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    val filteredItems = smallItems.filter { it.contains(fieldValue.text, ignoreCase = true) }

    var allowExpanded by remember { mutableStateOf(false) }
    val expanded = allowExpanded and filteredItems.isNotEmpty()

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                allowExpanded = true
            }
        }
    }

    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize(0, 0)) }

    // helper for click outside detection
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    // click outside
                    allowExpanded = false
                }
            },
        color = Color.Transparent
    ) {}

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                allowExpanded = true
                fieldValue = it
            },
            singleLine = true,
            label = { Text("Test label") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            interactionSource = interactionSource,
            modifier = Modifier.onSizeChanged { fieldSize = it },
        )
        /* Do not use ExposedDropdownMenu, because of https://issuetracker.google.com/issues/244620242?pli=1 */
        /* There is a scrollbar bug inside DropdownMenu: https://issuetracker.google.com/issues/243812426 */
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {}, // потому что DropdownMenu.onDismissRequest вызывается и при вводе с клавиатуры
            modifier = Modifier.requiredSize(
                width = with(density) { fieldSize.width.toDp() },
                height = with(density) { fieldSize.height.toDp() * filteredItems.size.coerceAtMost(3) },
            ),
            properties = PopupProperties(focusable = false),
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        fieldValue = fieldValue.copy(
                            text = item,
                            selection = TextRange(index = item.length),
                        )
                        allowExpanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainTheme {
        MainScreen()
    }
}
