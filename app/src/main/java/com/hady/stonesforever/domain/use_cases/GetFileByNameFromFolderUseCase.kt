package com.hady.stonesforever.domain.use_cases

import com.google.api.services.drive.model.File
import com.hady.stonesforever.domain.Repository.DriveRepository

class GetFileByNameFromFolderUseCase(
    private val repository: DriveRepository
) {
    suspend operator fun invoke(folderId: String, fileName: String): File? {
        return repository.getFileByNameFromFolder(folderId, fileName)
    }
}