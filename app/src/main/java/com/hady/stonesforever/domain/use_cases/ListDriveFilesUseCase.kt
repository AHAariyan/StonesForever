package com.hady.stonesforever.domain.use_cases

import com.google.api.services.drive.model.File
import com.hady.stonesforever.domain.Repository.DriveRepository
import javax.inject.Inject

class ListDriveFilesUseCase(
    private val repository: DriveRepository
) {
    suspend operator fun invoke(): List<File> {
        return repository.listFiles()
    }
}