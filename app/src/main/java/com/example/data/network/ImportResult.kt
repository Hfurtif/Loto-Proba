package com.example.data.network

import com.example.data.model.LotoDraw

data class InvalidLineReport(
    val lineNumber: Int,
    val rawText: String,
    val reason: String
)

data class ImportReport(
    val totalProcessed: Int,
    val validCount: Int,
    val duplicateCount: Int,
    val invalidCount: Int,
    val invalidLines: List<InvalidLineReport>,
    val parsedDraws: List<LotoDraw>
)
