package com.hady.stonesforever.domain

import com.google.firebase.auth.FirebaseUser
import java.io.File

interface GoogleDriveRepository {
    fun buildDriveService(account: FirebaseUser): Drive
    suspend fun uploadFile(drive: Drive, file: File): DriveFileInfo
    suspend fun listFiles(drive: Drive): List<DriveFileInfo>
    suspend fun downloadFile(drive: Drive, fileId: String, dest: File)
}
