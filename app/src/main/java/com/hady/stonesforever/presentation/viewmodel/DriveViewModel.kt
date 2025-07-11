package com.hady.stonesforever.presentation.viewmodel

import android.content.Context
import android.os.Environment
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
import com.hady.stonesforever.data.mappers.toEntity
import com.hady.stonesforever.data.mappers.toModel
import com.hady.stonesforever.data.model.BatchMovement
import com.hady.stonesforever.domain.use_cases.BatchMovementUseCases
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
import java.io.FileOutputStream
import javax.inject.Inject

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.google.api.client.http.FileContent
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.outputStream

@HiltViewModel
class DriveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val useCases: BatchMovementUseCases
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
        loadItems()
    }

    private fun loadItems() {
        _batchMovements.value = DriveDataUiState.Loading

        viewModelScope.launch {
            val data = useCases.getLocalBatchMovements()

            if (data.isNotEmpty()) {
                Log.d("CHECK_LOCAL_CACHE", "LOCAL CACHE: ${data.size}")
                _batchMovements.value = DriveDataUiState.Success(
                    model = data.map { it.toModel() }// If mapping is needed
                )
            } else {
                Log.d("CHECK_LOCAL_CACHE", "Calling Networok")
                val folderId = "1-6fW7qApJ-z-PhGxJgB_CNGARVh2ZQf3"
                val fileName = "Batch Movement  .xls"
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
        }
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
                val entityList = result.map { it.toEntity() } // Map to Room Entity
                useCases.saveBatchMovements(entityList)
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

    fun automaticSearchBatchCode(
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

    fun exportToCsv(
        context: Context,
        batchMovements: List<BatchMovement>,
        fileName: String = "BatchMovement.csv"
    ): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/StonesForever")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                Log.e("ExportCSV", "❌ Failed to create file URI")
                return false
            }

            resolver.openOutputStream(uri)?.use { outputStream ->
                writeCsvToStream(outputStream, batchMovements)
                Log.d("ExportCSV", "✅ File saved to Download/StonesForever/$fileName")
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("ExportCSV", "❌ Error exporting CSV: ${e.message}", e)
            false
        }
    }

    private fun writeCsvToStream(outputStream: OutputStream, batchMovements: List<BatchMovement>) {
        val writer = outputStream.bufferedWriter(Charset.forName("UTF-8"))

        // Header
        val headers = listOf("Barcode", "Quantity", "Height", "Width", "Meter Square", "Item")
        writer.appendLine(headers.joinToString(","))

        // Data
        var totalQuantity = 0
        var totalMeterSquare = 0.0

        batchMovements.forEach { item ->
            writer.appendLine(
                listOf(
                    item.barcode,
                    item.quantity.toString(),
                    item.height.toString(),
                    item.width.toString(),
                    item.meterSquare.toString(),
                    item.productName
                ).joinToString(",")
            )
            totalQuantity += item.quantity
            totalMeterSquare += item.meterSquare
        }

        // Footer
        writer.appendLine("Total, $totalQuantity,,, $totalMeterSquare,")

        writer.flush()
    }



    fun saveFileToStonesForever(context: Context, fileName: String, fileBytes: ByteArray): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            println("Permission WRITE_EXTERNAL_STORAGE not granted.")
            return false
        }

        try {
            val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel") // Correct MIME
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/StonesForever")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val stonesForeverDir = java.io.File(downloadDir, "StonesForever").apply { mkdirs() }
                val file = java.io.File(stonesForeverDir, fileName)
                FileOutputStream(file)
            }

            outputStream?.use {
                it.write(fileBytes)
                println("✅ Excel file saved to StonesForever/$fileName")
                return true
            }

            println("❌ OutputStream is null.")
            return false

        } catch (e: IOException) {
            println("❌ Error saving Excel file: ${e.message}")
            return false
        }
    }

    fun generateFormattedFileName(context: Context): String {
        val sdf = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
        val timestamp = sdf.format(Date())

        val dir = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StonesForever")
        val existingFiles = dir.listFiles { _, name -> name.startsWith(timestamp) } ?: emptyArray()
        val count = existingFiles.size + 1

        return "$timestamp $count.csv"
    }

    fun exportAndUpload(context: Context, batchMovements: List<BatchMovement>) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = generateFormattedFileName(context)
            val file = exportToCsvFile(context, fileName, batchMovements)

            if (file != null) {
                uploadFileToDriveStonesFolder(
                    localFile = file,
                    driveService = driveService!!,
                    onSuccess = {
                        Log.d("ExportFlow", "✅ Drive upload complete.")
                    },
                    onError = {
                        Log.e("ExportFlow", "❌ Drive upload failed: ${it.message}", it)
                    }
                )
            } else {
                Log.e("ExportFlow", "❌ Export failed. Aborting Drive upload.")
            }
        }
    }


    fun exportToCsvFile(context: Context, fileName: String, batchMovements: List<BatchMovement>): java.io.File? {
        return try {
            val dir = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StonesForever")
            if (!dir.exists()) dir.mkdirs()

            val file = java.io.File(dir, fileName)
            FileOutputStream(file).use { fos ->
                writeCsvToStream(fos, batchMovements)
            }

            Log.d("ExportCSV", "✅ File saved at: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("ExportCSV", "❌ Failed to export: ${e.message}", e)
            null
        }
    }

    fun uploadFileToDriveStonesFolder(localFile: java.io.File, driveService: Drive, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        try {
            // Step 1: Check/create "StonesForever" folder on Drive
            val query = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='StonesForever' and trashed=false")
                .setSpaces("drive")
                .execute()

            val folderId = if (query.files.isEmpty()) {
                val metadata = File().apply {
                    name = "StonesForever-8"
                    mimeType = "application/vnd.google-apps.folder"
                }
                driveService.files().create(metadata)
                    .setFields("id")
                    .execute().id
            } else {
                query.files[0].id
            }

            // Step 2: Upload file
            val fileMetadata = File().apply {
                name = localFile.name
                parents = listOf(folderId)
            }

            val mediaContent = FileContent("text/csv", localFile)

            val uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, name")
                .execute()

            Log.d("DriveUpload", "✅ Uploaded to Drive: ${uploadedFile.name}, ID: ${uploadedFile.id}")
            onSuccess(uploadedFile.id)

        } catch (e: Exception) {
            Log.e("DriveUpload", "❌ Failed to upload: ${e.message}", e)
            onError(e)
        }
    }



//    fun saveFileToStonesForever(context: Context, fileName: String, fileContent: String): Boolean {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
//            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//            println("Permission WRITE_EXTERNAL_STORAGE not granted.")
//            return false
//        }
//
//        try {
//            val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain") // Change if needed
//                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/StonesForever")
//                }
//
//                val resolver = context.contentResolver
//                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
//
//                if (uri != null) {
//                    resolver.openOutputStream(uri)
//                } else {
//                    println("Failed to create file URI in MediaStore.")
//                    null
//                }
//            } else {
//                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val stonesForeverDir = java.io.File(downloadDir, "StonesForever").apply { mkdirs() }
//                val file = java.io.File(stonesForeverDir, fileName)
//                FileOutputStream(file)
//            }
//
//            outputStream?.use {
//                it.write(fileContent.toByteArray())
//                println("✅ File saved to StonesForever/$fileName")
//                return true
//            }
//
//            println("❌ OutputStream is null.")
//            return false
//
//        } catch (e: IOException) {
//            println("❌ Error saving file: ${e.message}")
//            e.printStackTrace()
//            return false
//        }
//    }

}

sealed interface DriveDataUiState {
    data class Success(val model: List<BatchMovement>) : DriveDataUiState
    data class Error(val error: String) : DriveDataUiState
    data object Loading : DriveDataUiState
    data object Idle : DriveDataUiState
}