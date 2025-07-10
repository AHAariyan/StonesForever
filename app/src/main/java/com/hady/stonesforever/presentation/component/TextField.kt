package com.hady.stonesforever.presentation.component

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hady.stonesforever.ui.theme.StonesForeverTheme

@Composable
fun EditableTextFieldWithIcon(
    modifier: Modifier = Modifier,
    label: String = "Enter text",
    initialText: String = "",
    onValueChange: (String) -> Unit = {},
    icon: @Composable (() -> Unit)? = null
) {
    var text by remember { mutableStateOf(initialText) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it)
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = icon // The icon is placed here, on the right side.
    )
}

// A preview Composable to demonstrate the EditableTextFieldWithIcon.
@Preview(showBackground = true)
@Composable
fun PreviewEditableTextFieldWithIcon() {
    Row(modifier = Modifier.padding(16.dp)) {
        EditableTextFieldWithIcon(
            label = "Search",
            onValueChange = { /* Handle text change */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            }
        )
    }
}

@Composable
fun EditableTextFieldWithCornerRadius(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    cornerRadius: Int = 8
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(42.dp),
        label = label?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(cornerRadius.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Preview
@Composable
internal fun PreviewEditText() {
    StonesForeverTheme {
        var value1 by remember { mutableStateOf("85") }
        var value2 by remember { mutableStateOf("319") }
        var value3 by remember { mutableStateOf("1") }
        var value4 by remember { mutableStateOf("2.71") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EditableTextFieldWithCornerRadius(
                value = value1,
                onValueChange = { value1 = it },
                modifier = Modifier.weight(1f)
            )
            EditableTextFieldWithCornerRadius(
                value = value2,
                onValueChange = { value2 = it },
                modifier = Modifier.weight(1f)
            )
            EditableTextFieldWithCornerRadius(
                value = value3,
                onValueChange = { value3 = it },
                modifier = Modifier.weight(1f)
            )
            EditableTextFieldWithCornerRadius(
                value = value4,
                onValueChange = { value4 = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun ReadOnlyTextFieldWithCornerRadius(
    modifier: Modifier = Modifier,
    text: String,
    label: String? = null,
    cornerRadius: Int = 8
) {
    Box(
        modifier = modifier
            .background(
                color = Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun ReadOnlyTextFieldWithCornerRadiusFullWidth(
    modifier: Modifier = Modifier,
    text: String, // The text to display in the TextField
    label: String? = null, // Optional label for the TextField
    cornerRadius: Int = 8
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = Color.LightGray.copy(alpha = 0.2f), // Background color for the "field"
                shape = RoundedCornerShape(cornerRadius.dp) // Apply the specified corner radius
            )
            .padding(6.dp), // Add padding inside the box for the text
        // contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,// Ensure text fills the width for alignment
            textAlign = TextAlign.Center // Apply text alignment directly to the Text Composable
        )
    }
}

// A preview Composable to demonstrate the ReadOnlyTextFieldWithCornerRadius.
@Preview(showBackground = true)
@Composable
fun PreviewReadOnlyTextFields() {
    Column(modifier = Modifier.padding(16.dp)) {
        ReadOnlyTextFieldWithCornerRadius(
            text = "This is a read-only text field.",
            label = "Display Information",
            cornerRadius = 8
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReadOnlyTextFieldWithCornerRadius(
            text = "Another example with a different corner radius.",
            label = "Important Note",
            cornerRadius = 16 // Example of a different corner radius
        )
    }
}