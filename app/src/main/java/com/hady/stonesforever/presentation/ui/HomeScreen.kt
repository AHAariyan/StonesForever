package com.hady.stonesforever.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hady.stonesforever.common.InputScanOption
import com.hady.stonesforever.data.model.BatchMovement
import com.hady.stonesforever.presentation.component.ConfirmationDialog
import com.hady.stonesforever.presentation.component.FloatingTableRow
import com.hady.stonesforever.presentation.component.InputOptionsScreen
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadius
import com.hady.stonesforever.presentation.component.ReadOnlyTextFieldWithCornerRadiusFullWidth
import com.hady.stonesforever.presentation.component.RoundedInputDialog
import com.hady.stonesforever.presentation.viewmodel.DriveDataUiState
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

    HomeScreen(
        selectedScanOption = selectedScanOption,
        shouldShowInputDialog = shouldShowInputDialog,
        inputScanViewModel = inputScanViewModel,
        filteredItem = filteredItem,
        viewModel = viewModel, selectedItem = selectedItem,
        batchMovement = batchMovement
    )
}

@Composable
internal fun HomeScreen(
    selectedScanOption: InputScanOption,
    shouldShowInputDialog: MutableState<Boolean>,
    inputScanViewModel: InputActionViewModel,
    filteredItem: BatchMovement?,
    viewModel: DriveViewModel,
    selectedItem: List<BatchMovement>,
    batchMovement: DriveDataUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var enteredBarcode by rememberSaveable { mutableStateOf("") }
    var actualBarcode by rememberSaveable { mutableStateOf("") }
    var customBarcodeQuantity by rememberSaveable { mutableStateOf("") }

    val currentBatchState by rememberUpdatedState(batchMovement)

    // Effect handler (Snackbar / Toast)
    LaunchedEffect(currentBatchState) {
        when (val state = currentBatchState) {
            is DriveDataUiState.Error -> {
                snackbarHostState.showSnackbar("❌ ${state.error}")
            }
            is DriveDataUiState.Success -> {
                snackbarHostState.showSnackbar("✅ Batch Movement synced")
            }
            is DriveDataUiState.Loading -> {
                // Optionally show inline loading message
            }
            DriveDataUiState.Idle -> {}
        }
    }

    // Dialog control
    when (selectedScanOption) {
        InputScanOption.DEFAULT, InputScanOption.CUSTOM_INPUT -> shouldShowInputDialog.value = true
        else -> shouldShowInputDialog.value = false
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {

            ProductInfoSection(
                inputScanViewModel = inputScanViewModel,
                enteredBarcode = actualBarcode,
                selectedScanOption = selectedScanOption,
                filteredItem = filteredItem
            )

            ThemedDivider()

//            SummaryHeader(
//                totalPieces = selectedItem.sumOf { it.pcs },
//                totalArea = selectedItem.sumOf { it.meterSquare }
//            )

            if (selectedItem.isNotEmpty()) {
                SlabListTable(
                    selectedItem = selectedItem,
                    scanOption = selectedScanOption,
                    customBarcodeQuantity = customBarcodeQuantity,
                    onConfirmDelete = { index ->
                        viewModel.removeItemAt(index)
                    }
                )
            } else {
                Text(
                    text = "No Item selected yet",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (selectedItem.isNotEmpty()) {
                BottomBar(selectedItem = selectedItem)
            }
        }

        // Loading overlay
        if (batchMovement is DriveDataUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Dialog for input
    if (shouldShowInputDialog.value) {
        RoundedInputDialog(
            value = enteredBarcode,
            onValueChange = { enteredBarcode = it },
            onConfirm = {
                shouldShowInputDialog.value = false
                actualBarcode = enteredBarcode
                viewModel.searchByBatchCode(
                    batchCode = actualBarcode.trim(),
                    selectedScanOption = selectedScanOption,
                    customBarcodeQuantity = customBarcodeQuantity
                )
                enteredBarcode = ""
                customBarcodeQuantity = ""
                inputScanViewModel.updateScanOption(InputScanOption.NONE)
            },
            onDismiss = {
                shouldShowInputDialog.value = false
                inputScanViewModel.updateScanOption(InputScanOption.NONE)
            },
            scanOption = selectedScanOption.name,
            quantityValue = customBarcodeQuantity,
            onQuantityValueChange = { customBarcodeQuantity = it }
        )
    }
}


//@Composable
//internal fun HomeScreen(
//    selectedScanOption: InputScanOption,
//    shouldShowInputDialog: MutableState<Boolean>,
//    inputScanViewModel: InputActionViewModel,
//    filteredItem: BatchMovement?,
//    viewModel: DriveViewModel,
//    selectedItem: List<BatchMovement>,
//    batchMovement: DriveDataUiState
//) {
//
//    var enteredBarcode by remember { mutableStateOf<String>("") }
//    var actualBarcode by remember { mutableStateOf("") }
//    var customBarcodeQuantity by remember { mutableStateOf("") }
//
//    val context = LocalContext.current
//
//
//    when (selectedScanOption) {
//        InputScanOption.DEFAULT -> {
//            shouldShowInputDialog.value = true
//        }
//
//        InputScanOption.CAMERA -> {
//            shouldShowInputDialog.value = false
//        }
//
//        InputScanOption.CUSTOM_INPUT -> {
//            shouldShowInputDialog.value = true
//        }
//
//        InputScanOption.NONE -> {
//            shouldShowInputDialog.value = false
//        }
//    }
//
//    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
//
//        when(batchMovement) {
//            is DriveDataUiState.Error -> {
//                Toast.makeText(context, batchMovement.error, Toast.LENGTH_LONG).show()
//            }
//            DriveDataUiState.Idle -> {}
//            DriveDataUiState.Loading -> {
//                Row (
//                  modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//
//                Toast.makeText(context, "Loading data from drive", Toast.LENGTH_LONG).show()
//            }
//            is DriveDataUiState.Success -> {
//                Toast.makeText(context, "Batch Movement is synced to use..", Toast.LENGTH_LONG).show()
//            }
//        }
//
//        ProductInfoSection(
//            inputScanViewModel = inputScanViewModel,
//            enteredBarcode = actualBarcode,
//            selectedScanOption = selectedScanOption,
//            filteredItem = filteredItem
//        )
//        ThemedDivider()
//        //Spacer(Modifier.height(8.dp))
//        SummaryHeader(totalPieces = 238, totalArea = 664.04)
//        //Spacer(Modifier.height(8.dp))
//        if (selectedItem.isNotEmpty())
//            SlabListTable(
//                selectedItem = selectedItem,
//                scanOption = selectedScanOption,
//                customBarcodeQuantity = customBarcodeQuantity,
//                onConfirmDelete = { index ->
//                    viewModel.removeItemAt(index = index)
//                }
//            )
//        else
//            Text("No Item selected yet")
//        Spacer(Modifier.weight(1f))
//        if (selectedItem.isNotEmpty())
//            BottomBar(
//                selectedItem = selectedItem
//            )
//    }
//
//    if (shouldShowInputDialog.value) {
//        RoundedInputDialog(
//            value = enteredBarcode,
//            onValueChange = { newVal -> enteredBarcode = newVal },
//            onConfirm = {
//                shouldShowInputDialog.value = false
//                actualBarcode = enteredBarcode
//                enteredBarcode = ""
//                viewModel.searchByBatchCode(
//                    batchCode = actualBarcode.trim(),
//                    selectedScanOption = selectedScanOption,
//                    customBarcodeQuantity = customBarcodeQuantity
//                )
//
//                customBarcodeQuantity = ""
//                inputScanViewModel.updateScanOption(scanOption = InputScanOption.NONE)
//                //inputScanViewModel.addItem(filteredItem!!)
//                //viewModel.clearFilteredItem()
//            },
//            onDismiss = {
//                shouldShowInputDialog.value = false
//                inputScanViewModel.updateScanOption(scanOption = InputScanOption.NONE)
//            },
//            scanOption = selectedScanOption.name,
//            quantityValue = customBarcodeQuantity,
//            onQuantityValueChange = { newVal -> customBarcodeQuantity = newVal }
//        )
//    }
//}
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
    customBarcodeQuantity: String,
    onConfirmDelete : (Int) -> Unit
) {
    val rowColor =
        if (selectedItem.size % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(
            0.3f
        )

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var targetIndex by remember { mutableStateOf(-1) }

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
                    .background(rowColor),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell("X", 30.dp, onRemoveItemClick = { index ->
                    targetIndex = index
                    showDeleteConfirmationDialog = true
                }, index = index)
                TableCell("${index + 1}", 50.dp,)
                TableCell(item.barcode, 80.dp,)
                TableCell("${item.height}", 40.dp,)
                TableCell("${item.width}", 40.dp,)
                if (scanOption == InputScanOption.CUSTOM_INPUT) {
                    TableCell(customBarcodeQuantity, 40.dp,)
                } else {
                    TableCell("${item.quantity}", 40.dp,)
                }

                TableCell(String.format("%.2f", item.meterSquare), 50.dp,)
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = {
                onConfirmDelete(targetIndex)
                showDeleteConfirmationDialog = false
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
            }
        )
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
fun TableCell(
    text: String,
    width: Dp,
    onRemoveItemClick: ((index: Int) -> Unit)? = null,
    index: Int? = null
) {
    val modifier = if (onRemoveItemClick != null && index != null) {
        Modifier
            .clickable { onRemoveItemClick(index) }
            .width(width)
            .padding(4.dp)
    } else {
        Modifier
            .width(width)
            .padding(4.dp)
    }

    Text(
        text = text,
        modifier = modifier,
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
