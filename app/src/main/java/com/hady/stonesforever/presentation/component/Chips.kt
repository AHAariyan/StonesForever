package com.hady.stonesforever.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hady.stonesforever.common.ChipSelectorItems

@Composable
fun ChipSelector(
    selectedOption: ChipSelectorItems,
    onOptionSelected: (ChipSelectorItems) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ChipSelectorItems.entries.forEach { option ->
            val isSelected = option == selectedOption
            ChipItem(
                text = option.name,
                isSelected = isSelected,
                onClick = { onOptionSelected(option) },
                modifier = Modifier.weight(1f) // 🟢 Equally distribute chips
            )
        }
    }
}

@Composable
fun ChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray),
        shadowElevation = 2.dp,
        onClick = onClick,
        modifier = modifier
            .height(42.dp) // keep height consistent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = if (isSelected) Color.White else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(
    name = "Chip Selector Preview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewChipSelector() {
    var selected by remember { mutableStateOf(ChipSelectorItems.SLABS) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Type",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ChipSelector(
                selectedOption = selected,
                onOptionSelected = { selected = it }
            )
        }
    }
}


