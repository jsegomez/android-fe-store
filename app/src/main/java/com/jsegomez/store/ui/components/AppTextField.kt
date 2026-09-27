package com.jsegomez.store.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jsegomez.store.ui.theme.AppTextStyles

/**
 * Campo de texto principal de la app. Es stateless: el estado (value) vive en
 * quien lo usa. Los colores se toman del tema para cambiarlos en un solo lugar.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = AppTextStyles.BodyLargeRegular,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            textStyle = AppTextStyles.BodyLargeRegular,
            placeholder = { Text(text = placeholder ?: "", style = AppTextStyles.BodyLargeRegular) },
            supportingText = errorMessage?.let {
                { Text(text = it, style = AppTextStyles.BodySmallRegular) }
            },
            trailingIcon = trailingIcon,
            keyboardOptions = if (isPassword) {
                keyboardOptions.copy(keyboardType = KeyboardType.Password)
            } else {
                keyboardOptions
            },
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                visualTransformation
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
