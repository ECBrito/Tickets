package com.example.eventify.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A styled and reusable OutlinedTextField for the Eventify app.
 *
 * @param value The input text to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param label A label to display inside or above the text field.
 * @param modifier The Modifier to be applied to this text field.
 * @param keyboardType The type of keyboard to use, e.g., Text, Number, Email.
 * @param isError Whether the text field is in an error state.
 * @param visualTransformation Transforms the visual representation of the input text, e.g., for password fields.
 * @param supportingText A composable that is displayed below the text field, often for error messages.
 */
@Composable
fun EventifyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        supportingText = supportingText
    )
}