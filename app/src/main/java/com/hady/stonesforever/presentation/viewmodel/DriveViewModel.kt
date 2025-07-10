package com.hady.stonesforever.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
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

    private val _batchMovements = MutableStateFlow<List<BatchMovement>>(emptyList())
    val batchMovements: StateFlow<List<BatchMovement>> = _batchMovements

    private var driveService: Drive? = null


    private val _filesState = MutableStateFlow<List<File>>(emptyList())
    val filesState: StateFlow<List<File>> = _filesState

    fun initializeDriveAccess() {
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

    fun getFileByNameFromFolder(
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
                Log.d("ExcelParser", "⬇️ Starting file download from Drive (fileId: $fileId)")

                val outputStream = ByteArrayOutputStream()
                driveService?.files()
                    ?.get(fileId)
                    ?.executeMediaAndDownloadTo(outputStream)

                Log.d("ExcelParser", "✅ File downloaded successfully, converting to InputStream...")

                val inputStream = ByteArrayInputStream(outputStream.toByteArray())
                val workbook = HSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                Log.d("ExcelParser", "📄 Excel Sheet '${sheet.sheetName}' opened. Rows: ${sheet.lastRowNum + 1}")

                val result = mutableListOf<BatchMovement>()
                var currentProductName = ""

                for (i in 0..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val firstCell = row.getCell(0)?.stringCellValue?.trim()

                    if (firstCell?.startsWith("Product Name:") == true) {
                        currentProductName = firstCell.removePrefix("Product Name:").trim()
                        Log.d("ExcelParser", "➡️ Found new product section: $currentProductName")
                        continue
                    }

                    if (firstCell == "Check#" || firstCell.isNullOrBlank()) continue

                    try {
                        val inward = row.getCell(3)?.numericCellValue ?: 0.0
                        val closing = row.getCell(5)?.numericCellValue ?: 0.0
                        val pcs = row.getCell(7)?.numericCellValue?.toInt() ?: 0
                        val height = row.getCell(9)?.numericCellValue?.toInt() ?: 0
                        val width = row.getCell(10)?.numericCellValue?.toInt() ?: 0
                        val barcode = row.getCell(2)?.stringCellValue?.trim() ?: ""

                        result.add(
                            BatchMovement(
                                productName = currentProductName,
                                meterSquare = closing,
                                width = width,
                                height = height,
                                quantity = pcs,
                                barcode = barcode
                            )
                        )

                        Log.d(
                            "ExcelParser",
                            "✅ Parsed Row [$i]: product=$currentProductName, meter=$closing, width=$width, height=$height, qty=$pcs, barcode=$barcode"
                        )
                    } catch (e: Exception) {
                        Log.e("ExcelParser", "⚠️ Error parsing row $i, skipping. Reason: ${e.message}")
                    }
                }

                workbook.close()
                _batchMovements.value = result

                Log.d("ExcelParser", "✅ Parsing complete. Total parsed entries: ${result.size}")

            } catch (e: Exception) {
                Log.e("ExcelParser", "❌ Failed to parse Excel file: ${e.message}", e)
            }
        }
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
                    Log.d("DriveQuickCheck", "📄 File: '${it.name}' (id: ${it.id}), parents: ${it.parents}")
                }

            } catch (e: Exception) {
                Log.e("DriveQuickCheck", "💥 Exception while listing files", e)
            }
        }

    }


}