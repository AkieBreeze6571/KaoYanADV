package com.example.kaoyanadventure.ai

import android.content.Context
import android.net.Uri
import java.io.File

object ModelFileManager {
    fun modelsDir(context: Context): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Copy user-selected GGUF file into app-private storage and return absolute path. */
    fun importModel(context: Context, uri: Uri, targetName: String = "model.gguf"): String {
        val dir = modelsDir(context)
        val out = File(dir, targetName)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取选择的文件" }
            out.outputStream().use { output -> input.copyTo(output) }
        }
        return out.absolutePath
    }
}
