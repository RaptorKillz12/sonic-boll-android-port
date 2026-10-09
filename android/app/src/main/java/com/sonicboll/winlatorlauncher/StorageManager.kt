package com.sonicboll.winlatorlauncher

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.zip.ZipInputStream

class StorageManager(private val context: Context) {

    companion object {
        private const val TAG = "StorageManager"
        private const val GAME_DIR_NAME = "SonicBoll"
        private const val GAMES_FOLDER = "Games"
    }

    /**
     * Get the game installation directory in main phone storage.
     * Path: /storage/emulated/0/Games/SonicBoll/
     * This is accessible to Winlator and other apps.
     */
    fun getGameInstallDir(): File {
        val gamesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).parent, GAMES_FOLDER)
        return File(gamesDir, GAME_DIR_NAME)
    }

    /**
     * Alternative: Get game directory directly from main storage root.
     * Path: /storage/emulated/0/SonicBoll/
     */
    fun getGameInstallDirRoot(): File {
        return File(Environment.getExternalStorageDirectory(), GAME_DIR_NAME)
    }

    /**
     * Get the temp cache directory for the ZIP file.
     */
    fun getTempZipPath(): File {
        return File(context.cacheDir, "sonic_boll.zip")
    }

    /**
     * Check if the game is already installed.
     */
    fun isGameInstalled(): Boolean {
        val gameDir = getGameInstallDir()
        return gameDir.exists() && gameDir.isDirectory && gameDir.listFiles()?.isNotEmpty() == true
    }

    /**
     * Check if external storage is available and writable.
     */
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Unzip the game archive to the installation directory.
     */
    fun unzipGame(zipFile: File, destinationDir: File = getGameInstallDir()) {
        try {
            if (!isExternalStorageAvailable()) {
                throw IllegalStateException("External storage not available")
            }

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

    /**
     * Delete the game installation directory to free up space.
     */
    fun deleteGameInstallation(): Boolean {
        return try {
            val gameDir = getGameInstallDir()
            if (gameDir.exists()) {
                deleteRecursively(gameDir)
                Log.i(TAG, "Game installation deleted")
                true
            } else {
                false
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to delete game installation", t)
            false
        }
    }

    /**
     * Recursively delete a directory and all its contents.
     */
    private fun deleteRecursively(file: File): Boolean {
        return if (file.isDirectory) {
            file.listFiles()?.all { deleteRecursively(it) } == true && file.delete()
        } else {
            file.delete()
        }
    }

    /**
     * Get the absolute path to the game installation.
     */
    fun getGamePath(): String {
        return getGameInstallDir().absolutePath
    }
}
