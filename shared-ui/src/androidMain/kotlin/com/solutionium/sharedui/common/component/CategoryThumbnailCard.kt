package com.solutionium.sharedui.common.component


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.Category

@Composable
fun CategoryThumbnailCard(
    modifier: Modifier = Modifier,
    category: Category,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
    ) {
        CategoryThumbnailCardContent(category)
    }
}

@Composable
private fun CategoryThumbnailCardContent(
    category: Category,
) {
    Column {
        AsyncImage(
            model = category.imageUrl,
            contentDescription = category.name,
            contentScale = ContentScale.FillWidth,
        )
        Text(text = category.name)
    }

}
