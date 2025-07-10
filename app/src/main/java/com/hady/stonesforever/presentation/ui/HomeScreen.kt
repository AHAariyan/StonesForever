package com.hady.stonesforever.presentation.ui

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hady.stonesforever.presentation.component.EditableTextFieldWithIcon
import com.hady.stonesforever.presentation.component.FloatingTableRow
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadius
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadiusFullWidth
import com.hady.stonesforever.presentation.viewmodel.DriveViewModel
import com.hady.stonesforever.ui.theme.StonesForeverTheme

@Composable
fun HomeScreenRoute() {

    val context = LocalContext.current
    val viewModel: DriveViewModel = hiltViewModel()
    val files by viewModel.filesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeDriveAccess()
        viewModel.listFiles()
    }

    files.forEach {
        Text(text = it.name ?: "Unnamed File")
    }


    LaunchedEffect(Unit) {
        viewModel.initializeDriveAccess()

        val folderId = "1-6fW7qApJ-z-PhGxJgB_CNGARVh2ZQf3"
        val fileName = "Batch Movement  .xls" // 🔁 Replace with the actual file name

        viewModel.getFileByNameFromFolder(
            folderId = folderId,
            fileName = fileName,
            onSuccess = { file ->
                Toast.makeText(context, "${file.name} Found", Toast.LENGTH_LONG).show()
                Log.d("DriveHome", "✅ File found: ${file.name}, ID: ${file.id}")
                viewModel.parseExcelFile(fileId = file.id)
            },
            onError = { error ->
                Log.e("DriveHome", "❌ Failed to fetch file: ${error.message}", error)
            }
        )
    }

//    LaunchedEffect(Unit) {
//        viewModel.initializeDriveAccess()
//        viewModel.debugListAllFiles()
//    }


    HomeScreen()
}

@Composable
internal fun HomeScreen() {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        ProductInfoSection()
        ThemedDivider()
        //Spacer(Modifier.height(8.dp))
        SummaryHeader(totalPieces = 238, totalArea = 664.04)
        //Spacer(Modifier.height(8.dp))
        SlabListTable(sampleData)
        Spacer(Modifier.weight(1f))
        BottomBar(count = 7, totalArea = 18.98)
    }
}

@Composable
fun ProductInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            ReadOnlyTextFieldWithCornerRadiusFullWidth(
                text = "Product Name",
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReadOnlyTextFieldWithCornerRadius(
                    text = "SF47436",
                    modifier = Modifier.weight(0.2f)
                )
                Spacer(modifier = Modifier.padding(start = 4.dp))
                ReadOnlyTextFieldWithCornerRadius(text = "85", modifier = Modifier.weight(0.1f))
                Spacer(modifier = Modifier.padding(start = 4.dp))
                ReadOnlyTextFieldWithCornerRadius(text = "319", modifier = Modifier.weight(0.1f))
                Spacer(modifier = Modifier.padding(start = 4.dp))
                ReadOnlyTextFieldWithCornerRadius(text = "1", modifier = Modifier.weight(0.1f))
                Spacer(modifier = Modifier.padding(start = 4.dp))
                ReadOnlyTextFieldWithCornerRadius(text = "2.71", modifier = Modifier.weight(0.1f))
            }
        }

    }
}

@Composable
fun SummaryHeader(totalPieces: Int, totalArea: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ReadOnlyTextFieldWithCornerRadius(text = "$totalPieces", modifier = Modifier.weight(0.5f))
        ReadOnlyTextFieldWithCornerRadius(text = "$totalArea", modifier = Modifier.weight(0.5f))
    }
}

@Composable
fun SlabListTable(data: List<SlabItem>) {
    Column(Modifier.fillMaxWidth()) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeader("D", 30.dp)
            TableHeader("S. No.", 50.dp)
            TableHeader("Code", 80.dp)
            TableHeader("H", 40.dp)
            TableHeader("L", 40.dp)
            TableHeader("Qty", 40.dp)
            TableHeader("M²", 50.dp)
        }

        // Table rows
        data.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell("X", 30.dp)
                TableCell("${index + 1}", 50.dp)
                TableCell(item.code, 80.dp)
                TableCell("${item.height}", 40.dp)
                TableCell("${item.length}", 40.dp)
                TableCell("${item.qty}", 40.dp)
                TableCell(String.format("%.2f", item.area), 50.dp)
            }
        }
    }
}

@Composable
fun TableHeader(text: String, width: Dp) {
    Text(
        text,
        modifier = Modifier
            .width(width)
            .padding(4.dp),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center
    )
}

@Composable
fun TableCell(text: String, width: Dp) {
    Text(
        text,
        modifier = Modifier
            .width(width)
            .padding(4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun BottomBar(count: Int, totalArea: Double) {
    FloatingTableRow(
        textFieldOneValue = "A",
        textFieldTwoValue = "B",
        textFieldThreeValue = "C",
        buttonText = "Save",
        onButtonClick = {}
    )
}

data class SlabItem(
    val code: String,
    val height: Int,
    val length: Int,
    val qty: Int,
    val area: Double
)

val sampleData = listOf(
    SlabItem("SF47418", 89, 279, 1, 2.48),
    SlabItem("SF47420", 89, 287, 1, 2.55),
    SlabItem("SF47520", 99, 299, 1, 2.96),
    SlabItem("SF47513", 99, 295, 1, 2.92),
    SlabItem("SF47511", 99, 292, 1, 2.89),
    SlabItem("SF47416", 89, 276, 1, 2.46),
    SlabItem("SF47436", 85, 319, 1, 2.71)
)

@Composable
fun ThemedDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

@Preview
@Composable
internal fun PreviewHomeScreen() {
    StonesForeverTheme {
        HomeScreen()
    }
}
