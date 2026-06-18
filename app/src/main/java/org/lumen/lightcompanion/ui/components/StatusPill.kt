package org.lumen.lightcompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusPill(label: String, value: String, active: Boolean, modifier: Modifier = Modifier) {
    val background = if (active) Color(0xFFD9F0E3) else Color(0xFFE6E8EC)
    Column(
        modifier = modifier
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.labelMedium)
    }
}
