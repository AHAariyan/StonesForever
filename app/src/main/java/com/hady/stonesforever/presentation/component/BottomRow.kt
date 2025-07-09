package com.hady.stonesforever.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hady.stonesforever.ui.theme.StonesForeverTheme

@Composable
fun FloatingTableRow(
    buttonText: String,
    textFieldOneValue: String,
    textFieldTwoValue: String,
    onButtonClick: () -> Unit,
    textFieldThreeValue: String,
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val textStyle = TextStyle(fontSize = 14.sp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
            .background(backgroundColor, shape = RoundedCornerShape(24.dp))
            .padding(5.dp)
    ) {
        // 1. Rounded Label
        CircularText(text = textFieldOneValue, backgroundColor = MaterialTheme.colorScheme.tertiary.copy(0.2f))
        Spacer(modifier = Modifier.width(8.dp))

        // 2. Rectangular Button with 25.dp radius
        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .weight(3f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ) {
            Text(
                buttonText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3. First TextField - Fully Circular
        CircularText(text = textFieldTwoValue, backgroundColor = MaterialTheme.colorScheme.tertiary.copy(0.3f))

        Spacer(modifier = Modifier.width(8.dp))

        // 4. Second TextField - Fully Circular
        CircularText(text = textFieldThreeValue, backgroundColor = MaterialTheme.colorScheme.tertiary.copy(0.4f))
    }
}

@Composable
fun CircularText(
    modifier: Modifier = Modifier,
    text: String, // The text to display inside the circle
    circleSize: Dp = 48.dp, // The diameter of the circular display
    backgroundColor: Color = Color.Blue.copy(alpha = 0.7f), // Background color of the circle
    textColor: Color = Color.White // Color of the text
) {
    Box(
        modifier = modifier
            .size(circleSize) // Set the size (width and height) of the Box
            .background(color = backgroundColor, shape = CircleShape), // Apply circular background
        contentAlignment = Alignment.Center // Center the content (Text) inside the Box
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = (circleSize.value / 3).sp, // Dynamically adjust font size based on circle size
            textAlign = TextAlign.Center, // Ensure text is centered within its own bounds
            // Consider adding maxLines and overflow if text might be too long
            // maxLines = 1,
            // overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
internal fun PreviewCircularText() {
    StonesForeverTheme {
        CircularText(
            text = "A",
            circleSize = 60.dp,
            backgroundColor = Color.Red,
            textColor = Color.White
        )
    }
}


@Preview
@Composable
fun SampleScreen() {
    var tf1 by remember { mutableStateOf("") }
    var tf2 by remember { mutableStateOf("") }

    FloatingTableRow(
        buttonText = "Check",
        textFieldOneValue = tf1,
        textFieldTwoValue = tf2,
        textFieldThreeValue = "A",
        onButtonClick = { /* Do something */ },
    )
}
