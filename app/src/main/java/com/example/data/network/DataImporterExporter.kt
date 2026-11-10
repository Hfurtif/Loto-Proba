package com.example.data.network

import com.example.data.model.LotoDraw

object DataImporterExporter {

    /**
     * Génère une représentation CSV de la liste des tirages
     */
    fun exportToCsv(draws: List<LotoDraw>): String {
        val sb = StringBuilder()
        sb.append("Date;N1;N2;N3;N4;N5;NomTirage\n")
        for (draw in draws) {
            sb.append("${draw.dateString};${draw.n1};${draw.n2};${draw.n3};${draw.n4};${draw.n5};${draw.drawName}\n")
        }
        return sb.toString()
    }

    /**
     * Parse un contenu textuel CSV ou texte collé
     */
    fun parseCsv(csvContent: String, sourceId: String): ImportReport {
        val scraper = WebScraper()
        return scraper.parseDraws(csvContent, sourceId, parserPreset = "CSV")
    }
}
