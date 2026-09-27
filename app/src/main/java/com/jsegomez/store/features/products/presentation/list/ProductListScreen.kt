package com.jsegomez.store.features.products.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.jsegomez.store.features.products.data.FakeProducts
import com.jsegomez.store.features.products.domain.model.Product
import com.jsegomez.store.ui.components.BackButton
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductListContent(products = uiState.products, onBack = onBack, modifier = modifier)
}

@Composable
private fun ProductListContent(
    products: List<Product>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = products, key = { product -> product.id }) { product ->
                ProductCard(product = product)
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = product.image,
                contentDescription = product.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.category,
                    style = AppTextStyles.BodySmallMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.title,
                    style = AppTextStyles.BodyLargeSemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${product.price}",
                    style = AppTextStyles.BodyXLargeMedium
                )
                product.rating?.let { rating ->
                    Text(
                        text = "★ ${rating.rate} (${rating.count})",
                        style = AppTextStyles.BodySmallRegular
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductListScreenPreview() {
    ProductListContent(products = FakeProducts.products, onBack = {})
}
