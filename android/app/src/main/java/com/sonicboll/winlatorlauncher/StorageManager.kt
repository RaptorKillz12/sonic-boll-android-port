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
     * App cache location for downloaded ZIP and extracted game files.
     * Example: /data/data/com.sonicboll.winlatorlauncher/cache/
     */
    fun getZipDownloadPath(): File {
        return File(context.cacheDir, "sonic_boll.zip")
    }

    fun getGameCacheDir(): File {
        return File(context.cacheDir, GAME_DIR_NAME)
    }

    /**
     * Main phone storage location for user-editable game folders only.
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
     * Ensures user-editable folders exist in main phone storage.
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
     * Extract the downloaded ZIP into app cache.
     */
    fun unzipGameToCache(zipFile: File, destinationDir: File = getGameCacheDir()) {
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
            Log.i(TAG, "Game extracted to app cache: ${destinationDir.absolutePath}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to unzip the game archive", t)
            throw t
        }
    }

    /**
     * Copies specific game folders from app cache to main storage.
     * Use for saves, mods, skins, and bundles.
     */
    fun copyGameFoldersToMainStorage(sourceCacheDir: File = getGameCacheDir()) {
        val cacheModsDir = File(sourceCacheDir, MODS_DIR)
        val cacheSavesDir = File(sourceCacheDir, SAVES_DIR)
        val cacheSkinsDir = File(sourceCacheDir, SKINS_DIR)
        val cacheBundlesDir = File(sourceCacheDir, BUNDLES_DIR)

        if (cacheModsDir.exists()) copyFolderToMainStorage(cacheModsDir, getModFolder())
        if (cacheSavesDir.exists()) copyFolderToMainStorage(cacheSavesDir, getSavesFolder())
        if (cacheSkinsDir.exists()) copyFolderToMainStorage(cacheSkinsDir, getSkinsFolder())
        if (cacheBundlesDir.exists()) copyFolderToMainStorage(cacheBundlesDir, getBundlesFolder())
    }

    /**
     * Copies a folder recursively from source to target.
     */
    private fun copyFolderToMainStorage(sourceDir: File, targetDir: File) {\n        if (!sourceDir.exists()) return\n        targetDir.mkdirs()\n\n        sourceDir.listFiles()?.forEach { child ->\n            val targetChild = File(targetDir, child.name)\n            if (child.isDirectory) {\n                copyFolderToMainStorage(child, targetChild)\n            } else {\n                copyFile(child, targetChild)\n            }\n        }\n    }\n\n    private fun copyFile(sourceFile: File, targetFile: File) {\n        targetFile.parentFile?.mkdirs()\n        FileInputStream(sourceFile).use { input ->\n            FileOutputStream(targetFile).use { output ->\n                input.copyTo(output)\n            }\n        }\n    }\n\n    /**\n     * Returns the game executable path in app cache for Winlator.\n     */\n    fun getGameCachePath(): String {\n        return getGameCacheDir().absolutePath\n    }\n\n    /**\n     * Returns the shared game folder path in main storage for user content.\n     */\n    fun getGameStoragePath(): String {\n        return getGameInstallDir().absolutePath\n    }\n\n    fun isExternalStorageAvailable(): Boolean {\n        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED\n    }\n\n    fun deleteGameCache(): Boolean {\n        val cacheDir = getGameCacheDir()\n        return if (cacheDir.exists()) {\n            deleteRecursively(cacheDir)\n        } else {\n            false\n        }\n    }\n\n    fun deleteGameStorage(): Boolean {\n        val storageDir = getGameInstallDir()\n        return if (storageDir.exists()) {\n            deleteRecursively(storageDir)\n        } else {\n            false\n        }\n    }\n\n    private fun deleteRecursively(file: File): Boolean {\n        return if (file.isDirectory) {\n            file.listFiles()?.all { deleteRecursively(it) } == true && file.delete()\n        } else {\n            file.delete()\n        }\n    }\n}\n