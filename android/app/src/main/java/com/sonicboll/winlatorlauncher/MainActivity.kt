package com.sonicboll.winlatorlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import java.io.InputStream
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    companion object {
        private const val TAG = "SonicBollLauncher"

        // Replace this with the actual package used by the Winlator app or fork.
        private const val WINLATOR_PACKAGE = "com.winlator"

        // Replace with the dropbox file URL for the actual Sonic Boll package.
        private const val DROPBOX_ZIP_URL = "https://www.dropbox.com/s/your-file/sonic-boll.zip?dl=1"

        private const val GAME_DIR_NAME = "sonic_boll"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.Main).launch {
            val gameDir = getGameInstallDir()
            if (!gameDir.exists()) {
                downloadAndExtractGame(gameDir)
            }

            launchWinlatorWithGame(gameDir)
        }
    }

    private fun getGameInstallDir(): File {
        val base = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: filesDir
        return File(base, GAME_DIR_NAME)
    }

    private suspend fun downloadAndExtractGame(gameDir: File) = withContext(Dispatchers.IO) {
        try {
            val zipFile = File(filesDir, "sonic_boll.zip")
            val request = Request.Builder().url(DROPBOX_ZIP_URL).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Download failed: ${response.code}")
                }

                response.body?.byteStream()?.use { input ->
                    FileOutputStream(zipFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            unzip(zipFile, gameDir)
            zipFile.delete()

        } catch (t: Throwable) {
            Log.e(TAG, "Failed to download/extract Sonic Boll", t)
            throw t
        }
    }

    private fun unzip(zipFile: File, destinationDir: File) {
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
    }

    private fun launchWinlatorWithGame(gameDir: File) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(WINLATOR_PACKAGE)
                ?: Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$WINLATOR_PACKAGE".toUri())

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("game_path", gameDir.absolutePath)
            intent.putExtra("launch_mode", "sonic_boll")
            startActivity(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to launch Winlator", t)
        }
    }
}
