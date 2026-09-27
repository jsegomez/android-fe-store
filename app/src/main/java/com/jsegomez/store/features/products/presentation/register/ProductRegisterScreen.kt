package com.jsegomez.store.features.products.presentation.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jsegomez.store.ui.components.AppTextField
import com.jsegomez.store.ui.components.PrimaryButton
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun ProductRegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductRegisterViewModel = viewModel(),
    onProductSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp, end = 24.dp, start = 24.dp, bottom = 24.dp),
    ) {
        Text(
            text = "Registrar producto",
            style = AppTextStyles.HeadlineBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Completa los datos del producto",
            style = AppTextStyles.BodyLargeRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = "Nombre",
            placeholder = "Mens Cotton Jacket",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.title.isNotEmpty() && !uiState.isTitleValid,
            errorMessage = if (uiState.title.isNotEmpty() && !uiState.isTitleValid) "Mínimo 3 caracteres" else null,
            modifier = Modifier.padding(top = 24.dp)
        )
        AppTextField(
            value = uiState.price,
            onValueChange = viewModel::onPriceChange,
            label = "Precio",
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = uiState.price.isNotEmpty() && !uiState.isPriceValid,
            errorMessage = if (uiState.price.isNotEmpty() && !uiState.isPriceValid) "Ingresa un precio mayor a 0" else null,
            modifier = Modifier.padding(top = 16.dp)
        )
        AppTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = "Descripción",
            placeholder = "Describe el producto",
            singleLine = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.description.isNotEmpty() && !uiState.isDescriptionValid,
            errorMessage = if (uiState.description.isNotEmpty() && !uiState.isDescriptionValid) "Mínimo 3 caracteres" else null,
            modifier = Modifier.padding(top = 16.dp)
        )
        AppTextField(
            value = uiState.category,
            onValueChange = viewModel::onCategoryChange,
            label = "Categoría",
            placeholder = "men's clothing",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.category.isNotEmpty() && !uiState.isCategoryValid,
            errorMessage = if (uiState.category.isNotEmpty() && !uiState.isCategoryValid) "Mínimo 3 caracteres" else null,
            modifier = Modifier.padding(top = 16.dp)
        )
        AppTextField(
            value = uiState.image,
            onValueChange = viewModel::onImageChange,
            label = "Imagen (URL, opcional)",
            placeholder = "https://...",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            isError = uiState.image.isNotEmpty() && !uiState.isImageValid,
            errorMessage = if (uiState.image.isNotEmpty() && !uiState.isImageValid) "Ingresa una URL válida" else null,
            modifier = Modifier.padding(top = 16.dp)
        )

        PrimaryButton(
            text = "Guardar producto",
            onClick = { viewModel.onSaveClick(onProductSaved) },
            modifier = Modifier.padding(top = 24.dp),
            enabled = uiState.isFormValid && !uiState.isLoading
        )
    }
}
