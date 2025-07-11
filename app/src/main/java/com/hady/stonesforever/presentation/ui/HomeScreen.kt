package com.hady.stonesforever.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hady.stonesforever.common.InputScanOption
import com.hady.stonesforever.data.model.BatchMovement
import com.hady.stonesforever.presentation.component.FloatingTableRow
import com.hady.stonesforever.presentation.component.InputOptionsScreen
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadius
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadiusFullWidth
import com.hady.stonesforever.presentation.component.RoundedInputDialog
import com.hady.stonesforever.presentation.viewmodel.DriveViewModel
import com.hady.stonesforever.presentation.viewmodel.InputActionViewModel

@Composable
fun HomeScreenRoute(
    inputScanViewModel: InputActionViewModel = hiltViewModel(),
    viewModel: DriveViewModel = hiltViewModel()
) {

    val selectedScanOption by inputScanViewModel.selectedScanOption.collectAsStateWithLifecycle()
    val batchMovement by viewModel.batchMovements.collectAsStateWithLifecycle()
    val filteredItem by viewModel.filteredByBatchCode.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()


    val shouldShowInputDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

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

    LaunchedEffect(batchMovement) {
        Log.d("DriveHome", "📦 Parsed Batch Movement count: ${batchMovement.size}")
        batchMovement.forEach {
            Log.d("BATCH_MOVEMENT", "🧾 ${it.productName} - ${it.barcode}")
        }
    }

    HomeScreen(
        selectedScanOption = selectedScanOption,
        shouldShowInputDialog = shouldShowInputDialog,
        inputScanViewModel = inputScanViewModel,
        filteredItem = filteredItem,
        viewModel = viewModel, selectedItem = selectedItem
    )
}

@Composable
internal fun HomeScreen(
    selectedScanOption: InputScanOption,
    shouldShowInputDialog: MutableState<Boolean>,
    inputScanViewModel: InputActionViewModel,
    filteredItem: BatchMovement?,
    viewModel: DriveViewModel,
    selectedItem: List<BatchMovement>
) {

    var enteredBarcode by remember { mutableStateOf<String>("") }
    var actualBarcode by remember { mutableStateOf("") }

    var customBarcode by remember { mutableStateOf("") }
    var customBarcodeQuantity by remember { mutableStateOf("") }


    when (selectedScanOption) {
        InputScanOption.DEFAULT -> {
            shouldShowInputDialog.value = true
        }

        InputScanOption.CAMERA -> {
            shouldShowInputDialog.value = false
        }

        InputScanOption.CUSTOM_INPUT -> {
            shouldShowInputDialog.value = true
        }

        InputScanOption.NONE -> {
            shouldShowInputDialog.value = false
        }
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        ProductInfoSection(
            inputScanViewModel = inputScanViewModel,
            enteredBarcode = actualBarcode,
            selectedScanOption = selectedScanOption,
            filteredItem = filteredItem
        )
        ThemedDivider()
        //Spacer(Modifier.height(8.dp))
        SummaryHeader(totalPieces = 238, totalArea = 664.04)
        //Spacer(Modifier.height(8.dp))
        if (selectedItem.isNotEmpty())
            SlabListTable(
                selectedItem = selectedItem,
                scanOption = selectedScanOption,
                customBarcodeQuantity = customBarcodeQuantity
            )
        else
            Text("No Item selected yet")
        Spacer(Modifier.weight(1f))
        if (selectedItem.isNotEmpty())
            BottomBar(
                selectedItem = selectedItem
            )
    }

    if (shouldShowInputDialog.value) {
        RoundedInputDialog(
            value = enteredBarcode,
            onValueChange = { newVal -> enteredBarcode = newVal },
            onConfirm = {
                shouldShowInputDialog.value = false
                actualBarcode = enteredBarcode
                enteredBarcode = ""
                viewModel.searchByBatchCode(
                    batchCode = actualBarcode.trim(),
                    selectedScanOption = selectedScanOption,
                    customBarcodeQuantity = customBarcodeQuantity
                )

                customBarcodeQuantity = ""
                inputScanViewModel.updateScanOption(scanOption = InputScanOption.NONE)
                //inputScanViewModel.addItem(filteredItem!!)
                //viewModel.clearFilteredItem()
            },
            onDismiss = {
                shouldShowInputDialog.value = false
                inputScanViewModel.updateScanOption(scanOption = InputScanOption.NONE)
            },
            scanOption = selectedScanOption.name,
            quantityValue = customBarcodeQuantity,
            onQuantityValueChange = {newVal -> customBarcodeQuantity = newVal}
        )
    }
}
//SF04820

@Composable
fun ProductInfoSection(
    inputScanViewModel: InputActionViewModel,
    enteredBarcode: String,
    selectedScanOption: InputScanOption,
    filteredItem: BatchMovement?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (filteredItem != null)
                ReadOnlyTextFieldWithCornerRadiusFullWidth(
                    text = filteredItem.productName,
                    modifier = Modifier.fillMaxWidth()
                )

            InputOptionsScreen(
                enteredBarcode = enteredBarcode,
                selectedScanOption = selectedScanOption,
                onDefaultClick = {
                    Log.d("DIALOG_EVENT", "HomeScreen: Called")
                    inputScanViewModel.updateScanOption(scanOption = InputScanOption.DEFAULT)
                },
                onCameraClick = {
                    inputScanViewModel.updateScanOption(scanOption = InputScanOption.CAMERA)
                },
                onCustomInputClick = {
                    inputScanViewModel.updateScanOption(scanOption = InputScanOption.CUSTOM_INPUT)
                }
            )

            if (filteredItem != null)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp) // clean spacing between items
                ) {
                    ReadOnlyTextFieldWithCornerRadius(
                        text = filteredItem.height.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ReadOnlyTextFieldWithCornerRadius(
                        text = filteredItem.width.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ReadOnlyTextFieldWithCornerRadius(
                        text = filteredItem.quantity.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ReadOnlyTextFieldWithCornerRadius(
                        text = filteredItem.meterSquare.toString(),
                        modifier = Modifier.weight(1f)
                    )
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
        Spacer(modifier = Modifier.padding(start = 8.dp))
        ReadOnlyTextFieldWithCornerRadius(text = "$totalArea", modifier = Modifier.weight(0.5f))
    }
}

@Composable
fun SlabListTable(
    selectedItem: List<BatchMovement>,
    scanOption: InputScanOption,
    customBarcodeQuantity: String
) {
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
        selectedItem.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell("X", 30.dp)
                TableCell("${index + 1}", 50.dp)
                TableCell(item.barcode, 80.dp)
                TableCell("${item.height}", 40.dp)
                TableCell("${item.width}", 40.dp)
                if (scanOption == InputScanOption.CUSTOM_INPUT) {
                    TableCell(customBarcodeQuantity, 40.dp)
                } else {
                    TableCell("${item.quantity}", 40.dp)
                }

                TableCell(String.format("%.2f", item.meterSquare), 50.dp)
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
fun BottomBar(selectedItem: List<BatchMovement>) {
    val totalQuantity = selectedItem.sumOf { it.quantity }
    val totalMeterSquare = selectedItem.sumOf { it.meterSquare }
    FloatingTableRow(
        textFieldOneValue = selectedItem.size.toString(),
        textFieldTwoValue = totalQuantity.toString(),
        textFieldThreeValue = String.format("%.2f", totalMeterSquare),
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

@Composable
fun ThemedDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}
