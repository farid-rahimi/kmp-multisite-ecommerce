package com.solutionium.sharedui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.shared.data.model.DisplayableTerm


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AllItemsScreen(
    title: String,
    items: List<DisplayableTerm>, // A generic list of items with ID and Name
    imageFinder: (Int) -> String?,
    onItemClick: (id: Int, title: String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlatformTopBar(
            title = { Text(title) },
            onBack = onBack,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        // LazyVerticalGrid is perfect for showing a large, scrollable number of items.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // We'll use 2 columns for a consistent look
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                // We can reuse your existing SmallAttributeCard because it's visually appealing
                // and just needs a name and an image.
                MediumItemCard(
                    // Create a temporary AttributeTerm to satisfy the card's data requirement.
                    term = item,
                    image = item.imageUrl ?: imageFinder(item.id),
                    onClick = { onItemClick(item.id, item.name) }
                )
            }
        }
    }
}
