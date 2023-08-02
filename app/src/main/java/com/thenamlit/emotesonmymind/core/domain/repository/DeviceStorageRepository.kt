package com.thenamlit.emotesonmymind.core.domain.repository

import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import java.io.File


interface DeviceStorageRepository {
    fun createDirectoryIfNotExists(directory: String): SimpleResource

    fun getFile(path: String): Resource<File>

    fun deleteFile(path: String): SimpleResource
}
