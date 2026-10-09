package com.sonicboll.winlatorlauncher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val storageManager by lazy { StorageManager(this) }

    private lateinit var gameImageView: ImageView
    private lateinit var gameTitleText: TextView
    private lateinit var statusText: TextView
    private lateinit var filesListText: TextView
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "SonicBollLauncher"
        private const val WINLATOR_PACKAGE = "com.winlator"
        private const val DROPBOX_ZIP_URL = "https://www.dropbox.com/scl/fi/b1niwoyxm6kb64utk4w3i/game.zip?rlkey=pymghznnk4phoow743wzpziki&st=2un49jr8&dl=1"
        private const val GAME_EXECUTABLE = "sonicboll-200.exe"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameImageView = findViewById(R.id.game_image)
        gameTitleText = findViewById(R.id.game_title)
        statusText = findViewById(R.id.status_text)
        filesListText = findViewById(R.id.files_list)
        progressBar = findViewById(R.id.progress_bar)

        // Set the logo image
        gameImageView.setImageResource(R.drawable.sonic_kiosk_logo)

        // Set title
        gameTitleText.text = "Sonic Boll"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val zipFile = storageManager.getZipDownloadPath()
                val gameCacheDir = storageManager.getGameCacheDir()

                if (!gameCacheDir.exists()) {
                    updateStatus("Downloading game...")
                    downloadZipToCache(zipFile)

                    updateStatus("Extracting files...")
                    storageManager.ensureGameFolders()
                    storageManager.unzipGameToCache(zipFile, gameCacheDir)

                    updateStatus("Setting up game folders...")
                    storageManager.copyGameFoldersToMainStorage(gameCacheDir)

                    updateStatus("Installation complete!")
                    displayExtractedFiles(gameCacheDir)
                } else {
                    updateStatus("Game already installed")
                    displayExtractedFiles(gameCacheDir)
                }

                Thread.sleep(2000)

                // Find and launch the game executable
                val gameExecutable = findGameExecutable(gameCacheDir)
                if (gameExecutable != null) {
                    launchWinlatorWithGame(gameExecutable, gameCacheDir)
                } else {
                    updateStatus("Error: Game executable not found")
                    Log.e(TAG, "Could not locate $GAME_EXECUTABLE")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Startup failed", t)
                updateStatus("Error: ${t.message}")
            }
        }
    }

    private suspend fun downloadZipToCache(zipFile: File) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(DROPBOX_ZIP_URL).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Download failed with code ${response.code}")
            }

            response.body?.byteStream()?.use { input ->
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun findGameExecutable(gameCacheDir: File): File? {
        if (!gameCacheDir.exists()) return null

        // Search recursively for the executable
        return searchForFile(gameCacheDir, GAME_EXECUTABLE)
    }

    private fun searchForFile(directory: File, fileName: String): File? {
        val files = directory.listFiles() ?: return null

        for (file in files) {
            if (file.isFile && file.name == fileName) {
                return file
            }
            if (file.isDirectory) {
                val found = searchForFile(file, fileName)
                if (found != null) return found
            }
        }
        return null
    }

    private fun displayExtractedFiles(gameCacheDir: File) {
        if (!gameCacheDir.exists()) {
            filesListText.text = "No files extracted"
            return
        }

        val fileList = mutableListOf<String>()
        val files = gameCacheDir.listFiles()

        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (file.isDirectory) {
                    fileList.add("📁 ${file.name}/")
                } else {
                    fileList.add("📄 ${file.name}")
                }
            }
        }

        filesListText.text = fileList.joinToString("\n")
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
            Log.d(TAG, message)
        }
    }

    private fun launchWinlatorWithGame(gameExecutable: File, gameCacheDir: File) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(WINLATOR_PACKAGE)
                ?: Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$WINLATOR_PACKAGE".toUri())

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.putExtra("game_path", gameCacheDir.absolutePath)
            launchIntent.putExtra("executable_path", gameExecutable.absolutePath)
            launchIntent.putExtra("launch_mode", "sonic_boll")
            launchIntent.putExtra("game_name", "Sonic Boll")

            updateStatus("Launching Sonic Boll...")
            startActivity(launchIntent)
        } catch (t: Throwable) {
            Log.e(TAG, "Could not launch Winlator", t)
            updateStatus("Could not launch Winlator: ${t.message}")
        }
    }
}
