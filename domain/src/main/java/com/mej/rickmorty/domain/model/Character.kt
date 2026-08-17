package com.mej.rickmorty.domain.model

data class Character(
    val id: String,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val imageUrl: String,
)

data class CharacterDetail(
    val character: Character,
    val gender: String,
    val originName: String,
    val episodes: List<Episode>,
)

data class Episode(
    val id: String,
    val name: String,
    val code: String,
    val airDate: String,
)

/**
 * A API devolve o status como string livre. Converter para enum na fronteira
 * impede que a UI trate texto cru e centraliza o valor desconhecido.
 */
enum class CharacterStatus(val apiValue: String) {
    ALIVE("Alive"),
    DEAD("Dead"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromApi(value: String?): CharacterStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/** Página de resultados. [nextPage] nulo indica fim da lista. */
data class CharacterPage(
    val characters: List<Character>,
    val nextPage: Int?,
)

/** Filtro aplicado na query; combina busca por nome e status. */
data class CharacterFilter(
    val name: String = "",
    val status: CharacterStatus? = null,
)
