package com.sonicboll.winlatorlauncher

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.zip.ZipInputStream

class StorageManager(private val context: Context) {

    companion object {
        private const val TAG = "StorageManager"
        private const val GAME_DIR_NAME = "sonic_boll"
    }

    fun getGameInstallDir(): File {
        return File(context.filesDir, GAME_DIR_NAME)
    }

    fun getTempZipPath(): File {
        return File(context.cacheDir, "sonic_boll.zip")
    }

    fun getGameInstallDirExternal(): File {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return if (externalDir != null && isExternalStorageAvailable()) {
            File(externalDir, GAME_DIR_NAME)
        } else {
            getGameInstallDir()
        }
    }

    fun isGameInstalled(): Boolean {
        val gameDir = getGameInstallDir()
        return gameDir.exists() && gameDir.isDirectory && gameDir.listFiles()?.isNotEmpty() == true
    }

    fun unzipGame(zipFile: File, destinationDir: File = getGameInstallDir()) {
        try {
            destinationDir.mkdirs()
            ZipInputStream(zipFile.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(destinationDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { output ->
                            zis.copyTo(output)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            Log.i(TAG, "Game extracted successfully to ${destinationDir.absolutePath}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to unzip game files", t)
            throw t
        }
    }

    fun deleteGameInstallation(): Boolean {
        return try {
            val gameDir = getGameInstallDir()
            if (gameDir.exists()) {
                deleteRecursively(gameDir)
                true
            } else {
                false
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to delete game installation", t)
            false
        }
    }

    private fun deleteRecursively(file: File): Boolean {
        return if (file.isDirectory) {
            file.listFiles()?.all { deleteRecursively(it) } == true && file.delete()
        } else {
            file.delete()
        }
    }

    private fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
