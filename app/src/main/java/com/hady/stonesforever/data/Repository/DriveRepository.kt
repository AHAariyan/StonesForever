package com.hady.stonesforever.data.Repository

import android.util.Log
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.hady.stonesforever.domain.Repository.DriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveRepositoryImpl(
    private val driveService: Drive
) : DriveRepository {

    override suspend fun listFiles(): List<File> = withContext(Dispatchers.IO) {
        try {
            val result = driveService.files().list()
                .setFields("files(id, name, mimeType)")
                .execute()
            result.files ?: emptyList()
        } catch (e: Exception) {
            Log.e("DriveRepositoryImpl", "Failed to list files", e)
            emptyList()
        }
    }

    override suspend fun getFileByNameFromFolder(
        folderId: String,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val query = "'$folderId' in parents and trashed = false"
            Log.d("DriveRepositoryImpl", "Query: ${query}")
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, mimeType, parents)")
                .execute()

            result.files?.forEach {
                Log.d("DriveRepositoryImpl", "Found file: '${it.name}' (id: ${it.id})")
            }

            result.files?.firstOrNull { file ->
                file.parents?.contains(folderId) == true &&
                        file.name.replace("\\s+".toRegex(), " ").trim()
                            .equals(fileName.replace("\\s+".toRegex(), " ").trim(), ignoreCase = true)
            }


        } catch (e: Exception) {
            Log.e("DriveRepositoryImpl", "Error listing files", e)
            null
        }
    }
}