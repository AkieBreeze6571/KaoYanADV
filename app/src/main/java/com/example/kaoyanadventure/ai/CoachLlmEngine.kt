package com.example.kaoyanadventure.ai

import io.aatricks.llmedge.SmolLM

class CoachLlmEngine {
    private var smol: SmolLM? = null
    private var loadedPath: String = ""

    fun isReady(): Boolean = smol != null

    suspend fun load(modelPath: String) {
        if (modelPath.isBlank()) return
        if (smol != null && loadedPath == modelPath) return

        smol?.close()
        smol = SmolLM()

        val threads = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
        val params = SmolLM.InferenceParams(
            numThreads = threads,
            contextSize = 2048L
        )

        smol!!.load(modelPath, params)
        loadedPath = modelPath
    }

    fun generate(prompt: String): String {
        val s = smol ?: return "教练还没上岗：请先在设置里选择本地模型（.gguf）。"
        return s.getResponse(prompt)
    }

    fun close() {
        smol?.close()
        smol = null
        loadedPath = ""
    }
}
