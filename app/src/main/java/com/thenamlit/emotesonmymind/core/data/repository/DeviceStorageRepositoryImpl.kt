package com.thenamlit.emotesonmymind.core.data.repository

import android.content.Context
import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import java.io.File
import javax.inject.Inject


class DeviceStorageRepositoryImpl @Inject constructor(
    private val context: Context,
) : DeviceStorageRepository {
    private val tag = Logging.loggingPrefix + DeviceStorageRepositoryImpl::class.java.simpleName

    override fun createDirectoryIfNotExists(directory: String): SimpleResource {
        Log.d(tag, "createDirectoryIfNotExists | directory: $directory")

        try {
            val absoluteDirectory = "${context.filesDir.path}/$directory"
            val path = File(absoluteDirectory)

            if (!path.exists()) {
                return if (path.mkdirs()) {
                    Log.d(tag, "createDirectoryIfNotExists | Created $absoluteDirectory")

                    Resource.Success(data = null)
                } else {
                    Log.d(tag, "createDirectoryIfNotExists | Couldn't create $absoluteDirectory")

                    Resource.Error(uiText = UiText.DynamicString(value = "Couldn't create Directory"))
                }
            }
            Log.d(tag, "createDirectoryIfNotExists | Directory already existed - Just continue")

            return Resource.Success(data = null)
        } catch (nullPointerException: NullPointerException) {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "createDirectoryIfNotExists | " +
                            "${nullPointerException.message}\n${nullPointerException.stackTrace}"
                )
            )
        } catch (securityException: SecurityException) {
            return Resource.Error(
                uiText = UiText.DynamicString(
                    value = "createDirectoryIfNotExists | " +
                            "${securityException.message}\n${securityException.stackTrace}"
                )
            )
        }
    }

    override fun getFile(path: String): Resource<File> {
        Log.d(tag, "getFile | path: $path")

        return try {
            val localFile = File(path)

            if (localFile.exists()) {
                Resource.Success(data = localFile)
            } else {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "getFile | LocalImageFile doesn't exist"
                    )
                )
            }
        } catch (nullPointerException: NullPointerException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getFile | NullPointerException: " +
                            "${nullPointerException.message}\n${nullPointerException.stackTrace}"
                )
            )
        } catch (securityException: SecurityException) {
            Resource.Error(
                uiText = UiText.DynamicString(
                    value = "getFile | SecurityException: " +
                            "${securityException.message}\n${securityException.stackTrace}"
                )
            )
        }
    }

    override fun deleteFile(path: String): SimpleResource {
        Log.d(tag, "deleteFile | path: $path")

        val absoluteDirectory = "${context.filesDir.path}/$path"

        return try {
            val localFile = File(absoluteDirectory)

            if (localFile.exists()) {
                if (localFile.delete()) {
                    Resource.Success(data = null)
                } else {
                    Resource.Error(
                        uiText = UiText.StringResource(
                            id = R.string.device_storage_repository_could_not_delete_file
                        ),
                        logging = "deleteFile | Couldn't delete file"
                    )
                }
            } else {
                // Just return success because it doesn't exist anyway
                Resource.Success(data = null)
            }
        } catch (nullPointerException: NullPointerException) {
            Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.device_storage_repository_could_not_delete_file
                ),
                logging = "deleteFile | NullPointerException: ${nullPointerException.message}\n" +
                        "${nullPointerException.stackTrace}"
            )
        } catch (securityException: SecurityException) {
            Resource.Error(
                uiText = UiText.StringResource(
                    id = R.string.device_storage_repository_could_not_delete_file
                ),
                logging = "deleteFile | SecurityException: ${securityException.message}\n" +
                        "${securityException.stackTrace}"
            )
        }
    }
}
