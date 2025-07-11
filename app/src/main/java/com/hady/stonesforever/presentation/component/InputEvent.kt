package com.hady.stonesforever.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hady.stonesforever.common.InputScanOption

// Define your Composable for the main screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputOptionsScreen(
    onDefaultClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCustomInputClick: () -> Unit,
    enteredBarcode: String,
    selectedScanOption: InputScanOption,
    onInputChange: (String)-> Unit,
    inputValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
            //ReadOnlyTextFieldWithCornerRadius(text = enteredBarcode)
        }

        Spacer(modifier = Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            InputOptionButton(
                text = "Default",
                isSelected = selectedScanOption.name == InputScanOption.DEFAULT.name,
                onClick = onDefaultClick
            )
        }

        Spacer(modifier = Modifier.padding(start = 8.dp))

        Box(
            modifier = Modifier.weight(1f)
        ) {
            InputOptionButton(
                text = "Camera",
                isSelected = selectedScanOption.name == InputScanOption.CAMERA.name,
                onClick = onCameraClick
            )
        }
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            InputOptionButton(
                text = "Custom Input",
                isSelected = selectedScanOption.name == InputScanOption.CUSTOM_INPUT.name,
                onClick = onCustomInputClick
            )
        }
    }

}

/**
 * Reusable Composable for an input option button.
 * Changes appearance based on whether it's selected.
 */
@Composable
fun InputOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                //.fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun InputOptionsScreenPreview() {
    MaterialTheme { // Use your app's theme or a default MaterialTheme
        //InputOptionsScreen()
    }
}