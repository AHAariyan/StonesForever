package com.hady.stonesforever.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.hady.stonesforever.common.ChipSelectorItems
import com.hady.stonesforever.common.InputScanOption
import com.hady.stonesforever.data.Repository.DriveRepositoryImpl
import com.hady.stonesforever.data.drive.DriveServiceBuilder
import com.hady.stonesforever.data.model.BatchMovement
import com.hady.stonesforever.domain.use_cases.GetFileByNameFromFolderUseCase
import com.hady.stonesforever.domain.use_cases.ListDriveFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var listFilesUseCase: ListDriveFilesUseCase? = null
    private var getFileByNameFromFolderUseCase: GetFileByNameFromFolderUseCase? = null

    private val _batchMovements = MutableStateFlow<DriveDataUiState>(DriveDataUiState.Idle)
    val batchMovements: StateFlow<DriveDataUiState> = _batchMovements.asStateFlow()

    private val _filteredByBatchCode = MutableStateFlow<BatchMovement?>(null)
    val filteredByBatchCode: StateFlow<BatchMovement?> = _filteredByBatchCode.asStateFlow()


    private var driveService: Drive? = null


    private val _filesState = MutableStateFlow<List<File>>(emptyList())
    val filesState: StateFlow<List<File>> = _filesState

    init {
        initializeDriveAccess()

        val folderId = "1-6fW7qApJ-z-PhGxJgB_CNGARVh2ZQf3"
        val fileName = "Batch Movement  .xls"

        _batchMovements.value = DriveDataUiState.Loading
        getFileByNameFromFolder(
            folderId = folderId,
            fileName = fileName,
            onSuccess = { file ->
                Log.d("DriveViewModel", "✅ File found on init: ${file.name}, ID: ${file.id}")
                parseExcelFile(fileId = file.id)
            },
            onError = { error ->
                _batchMovements.value = DriveDataUiState.Error(error = error.message.toString())
                Log.e("DriveViewModel", "❌ Failed to fetch file: ${error.message}", error)
            }
        )
    }

    private fun initializeDriveAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            driveService = DriveServiceBuilder.buildService(context, account)
            val repository = DriveRepositoryImpl(driveService!!)
            listFilesUseCase = ListDriveFilesUseCase(repository)
            getFileByNameFromFolderUseCase = GetFileByNameFromFolderUseCase(repository)
        }
    }


    fun listFiles() {
        viewModelScope.launch {
            val files = listFilesUseCase?.invoke() ?: emptyList()
            Log.d("DriveFiles", "listFiles: ${files.size}")
            _filesState.value = files
        }
    }

    private fun getFileByNameFromFolder(
        folderId: String,
        fileName: String,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val file = getFileByNameFromFolderUseCase?.invoke(folderId, fileName)
                Log.d("DriveHome", "getFileByNameFromFolder: ${folderId}\n $fileName")
                if (file != null) {
                    onSuccess(file)
                } else {
                    onError(Exception("File not found"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun parseExcelFile(fileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ExcelParser", "⬇️ Downloading Excel file (ID: $fileId)...")

                val outputStream = ByteArrayOutputStream()
                driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(outputStream)
                val inputStream = ByteArrayInputStream(outputStream.toByteArray())
                val workbook = HSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                Log.d(
                    "ExcelParser",
                    "✅ Sheet '${sheet.sheetName}' loaded. Total rows: ${sheet.lastRowNum + 1}"
                )

                val result = mutableListOf<BatchMovement>()
                var currentProductName = ""

                for (i in 2..sheet.lastRowNum) { // Start from row index 2 (skip headers)
                    val row = sheet.getRow(i) ?: continue

                    val col0 = row.getCell(0)?.toString()?.trim() ?: ""
                    val col1 = row.getCell(1)?.toString()?.trim() ?: ""
                    val col3 = row.getCell(3)?.toString()?.trim() ?: ""

                    // 🟦 Product Name row
                    if (col0.lowercase().startsWith("product name:")) {
                        currentProductName = col0.removePrefix("Product Name:").trim()
                        Log.d("ExcelParser", "➡️ Found product: $currentProductName (at row $i)")
                        continue
                    }

                    // 🟥 Skip if not valid data row
                    if (col1.lowercase() != "checked" || col3.isBlank()) {
                        continue
                    }

                    try {
                        if (row.lastCellNum < 14) {
                            Log.w(
                                "ExcelParser",
                                "⚠️ Row $i has only ${row.lastCellNum} columns. Skipping."
                            )
                            continue
                        }

                        val movement = BatchMovement(
                            productName = currentProductName,
                            barcode = row.getCell(3)?.toString()?.trim() ?: "",
                            meterSquare = row.getCell(6)?.numericCellValue ?: 0.0,
                            quantity = row.getCell(9)?.numericCellValue?.toInt() ?: 0,
                            width = row.getCell(12)?.numericCellValue?.toInt() ?: 0,
                            height = row.getCell(11)?.numericCellValue?.toInt() ?: 0,
                        )

                        result.add(movement)
                        Log.d("ExcelParser", "✅ Row $i parsed: $movement")

                    } catch (e: Exception) {
                        Log.e("ExcelParser", "⚠️ Error parsing row $i: ${e.message}")
                    }
                }

                workbook.close()
                _batchMovements.value = DriveDataUiState.Success(model = result)

                Log.d("ExcelParser", "✅ Parsing complete. Total parsed entries: ${result.size}")

            } catch (e: Exception) {
                _batchMovements.value = DriveDataUiState.Error(error = e.message.toString())
                Log.e("ExcelParser", "❌ Failed to parse Excel file: ${e.message}", e)
            }
        }
    }

    fun searchByBatchCode(
        batchCode: String,
        selectedScanOption: InputScanOption,
        customBarcodeQuantity: String,
        itemTypes: ChipSelectorItems
    ) {
        val query = batchCode.trim().lowercase()

        val batchList = when (val state = _batchMovements.value) {
            is DriveDataUiState.Success -> state.model
            else -> emptyList()
        }

        val result = batchList.firstOrNull {
            it.barcode.lowercase() == query
        }

        _filteredByBatchCode.value = result

        if (result != null) {
            if (itemTypes.name == ChipSelectorItems.TILES.name) {
                addItemWithQuantity(
                    singleItem = result,
                    customBarcodeQuantity = customBarcodeQuantity
                )
            } else {
                addItem(result)
            }

            Log.d("ExcelParser", "🔍 Found batch: ${result.barcode}, Product: ${result.productName}")
        } else {
            if (selectedScanOption == InputScanOption.CUSTOM_INPUT) {
                addCustomItem(batchCode = batchCode, customBarcodeQuantity = customBarcodeQuantity)
            }
            Log.w("ExcelParser", "❌ No match found for batch code: $query")
        }
    }


//    fun searchByBatchCode(
//        batchCode: String,
//        selectedScanOption: InputScanOption,
//        customBarcodeQuantity: String
//        ) {
//        val query = batchCode.trim().lowercase()
//
//        val result = _batchMovements.value.firstOrNull {
//            it.barcode.lowercase() == query
//        }
//
//        _filteredByBatchCode.value = result
//
//        if (result != null) {
//            addItem(singleItem = filteredByBatchCode.value!!)
//
//            Log.d("ExcelParser", "🔍 Found batch: ${result.barcode}, Product: ${result.productName}")
//        } else {
//            if (selectedScanOption == InputScanOption.CUSTOM_INPUT) {
//                addCustomItem(batchCode = batchCode, customBarcodeQuantity = customBarcodeQuantity)
//            }
//            Log.w("ExcelParser", "❌ No match found for batch code: $query")
//        }
//    }

    private val _selectedItem = MutableStateFlow<List<BatchMovement>>(emptyList())
    val selectedItem: StateFlow<List<BatchMovement>> = _selectedItem.asStateFlow()

    private fun addItemWithQuantity(singleItem: BatchMovement, customBarcodeQuantity: String) {
        val updatedItem = singleItem.copy(quantity = customBarcodeQuantity.toInt())

        _selectedItem.value += updatedItem

        Log.d(
            "ITEM_SIZE",
            "✅ Added item with quantity $customBarcodeQuantity. Total items: ${_selectedItem.value.size}"
        )
    }


    private fun addItem(singleItem: BatchMovement) {
        _selectedItem.value += singleItem

        Log.d("ITEM_SIZE", "addItem: ${selectedItem.value.size}")
    }

    private fun addCustomItem(
        batchCode: String, customBarcodeQuantity: String
    ) {
        val populateResult = BatchMovement(
            productName = "Custom Input",
            meterSquare = 0.0,
            width = 0,
            height = 0,
            quantity = customBarcodeQuantity.toInt(),
            barcode = batchCode
        )

        _selectedItem.value += populateResult

        Log.d("ITEM_SIZE", "addItem: ${selectedItem.value.size}")
    }

    fun removeItemAt(index: Int) {
        val currentList = _selectedItem.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedItem.value = currentList
            Log.d("ITEM_SIZE", "removeItemAt: ${_selectedItem.value.size}")
        } else {
            Log.w("ITEM_SIZE", "removeItemAt: Invalid index $index")
        }
    }


    fun clearFilteredItem() {
        _filteredByBatchCode.value = null
    }


    fun debugListAllFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (driveService == null) {
                    Log.e("DriveQuickCheck", "❌ Drive service is null")
                    return@launch
                }

                Log.d("DriveQuickCheck", "✅ Drive service initialized")

                val result = driveService?.files()?.list()
                    ?.setPageSize(20)
                    ?.setFields("files(id, name, parents, mimeType)")
                    ?.execute()

                Log.d("DriveQuickCheck", "📦 Total files fetched: ${result?.files?.size ?: 0}")

                result?.files?.forEach {
                    Log.d(
                        "DriveQuickCheck",
                        "📄 File: '${it.name}' (id: ${it.id}), parents: ${it.parents}"
                    )
                }

            } catch (e: Exception) {
                Log.e("DriveQuickCheck", "💥 Exception while listing files", e)
            }
        }

    }


}

sealed interface DriveDataUiState {
    data class Success(val model: List<BatchMovement>) : DriveDataUiState
    data class Error(val error: String) : DriveDataUiState
    data object Loading : DriveDataUiState
    data object Idle : DriveDataUiState
}