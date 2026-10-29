package com.upchiapas.mathenglishquest.domain.portfolio

object PortfolioFilter {
    /** [difficultyName] null = todas. Compara con el nombre que se guardó ("Fácil", "Medio", "Difícil", "Muy Difícil"). */
    fun byDifficulty(entries: List<EvidenceEntry>, difficultyName: String?): List<EvidenceEntry> =
        if (difficultyName == null) entries else entries.filter { it.difficulty == difficultyName }
}
