package com.example.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun AuraSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search songs, artists, albums...",
    onFilterClick: (() -> Unit)? = null,
    testTag: String = "aura_search_bar"
) {
    AuraTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingIcon = if (onFilterClick != null) Icons.Default.FilterList else null,
        onTrailingIconClick = onFilterClick
    )
}
