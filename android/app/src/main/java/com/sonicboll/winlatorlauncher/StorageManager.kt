package com.sonicboll.winlatorlauncher

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class StorageManager(private val context: Context) {

    companion object {
        private const val TAG = "StorageManager"
        private const val GAME_DIR_NAME = "SonicBoll"
        private const val MODS_DIR = "mods"
        private const val SAVES_DIR = "saves"
        private const val SKINS_DIR = "skins"
        private const val BUNDLES_DIR = "bundles"
    }

    /**
     * App internal cache location for the downloaded ZIP file.
     * Example: /data/data/com.sonicboll.winlatorlauncher/cache/sonic_boll.zip
     */
    fun getZipDownloadPath(): File {
        return File(context.cacheDir, "sonic_boll.zip")
    }

    /**
     * Main phone storage location for game and user-editable content.
     * Example: /storage/emulated/0/Games/SonicBoll/
     */
    fun getGameInstallDir(): File {
        val gamesRoot = File(Environment.getExternalStorageDirectory(), "Games")
        return File(gamesRoot, GAME_DIR_NAME)
    }

    fun getModFolder(): File = File(getGameInstallDir(), MODS_DIR)
    fun getSavesFolder(): File = File(getGameInstallDir(), SAVES_DIR)
    fun getSkinsFolder(): File = File(getGameInstallDir(), SKINS_DIR)
    fun getBundlesFolder(): File = File(getGameInstallDir(), BUNDLES_DIR)

    /**
     * Ensures the main storage game folders exist.
     */
    fun ensureGameFolders() {
        val root = getGameInstallDir()
        root.mkdirs()
        getModFolder().mkdirs()
        getSavesFolder().mkdirs()
        getSkinsFolder().mkdirs()
        getBundlesFolder().mkdirs()
    }

    /**
     * Extract the downloaded ZIP into the main phone storage folder.
     */
    fun unzipGameToMainStorage(zipFile: File, destinationDir: File = getGameInstallDir()) {
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
            Log.i(TAG, "Game extracted to main storage: ${destinationDir.absolutePath}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to unzip the game archive", t)
            throw t
        }
    }

    /**
     * Copies a folder from the extracted directory into the persistent main storage folder.
     * Use for saves, mods, skins, and bundles.
     */
    fun copyFolderToMainStorage(sourceDir: File, targetDir: File) {
        if (!sourceDir.exists()) return
        targetDir.mkdirs()

        sourceDir.listFiles()?.forEach { child ->
            val targetChild = File(targetDir, child.name)
            if (child.isDirectory) {
                copyFolderToMainStorage(child, targetChild)
            } else {
                copyFile(child, targetChild)
            }
        }
    }

    private fun copyFile(sourceFile: File, targetFile: File) {
        targetFile.parentFile?.mkdirs()
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Returns the game folder path in main storage for Winlator.
     */
    fun getGamePath(): String {
        return getGameInstallDir().absolutePath
    }

    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun deleteGameInstallation(): Boolean {
        val gameDir = getGameInstallDir()
        return if (gameDir.exists()) {
            deleteRecursively(gameDir)
        } else {
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
}
