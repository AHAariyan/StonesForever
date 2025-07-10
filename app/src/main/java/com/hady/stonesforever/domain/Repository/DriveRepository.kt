package com.hady.stonesforever.domain.Repository

import com.google.api.services.drive.model.File

interface DriveRepository {
    suspend fun listFiles(): List<File>
    suspend fun getFileByNameFromFolder(folderId: String, fileName: String): File?
}
