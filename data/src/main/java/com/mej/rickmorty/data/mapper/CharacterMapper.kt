package com.mej.rickmorty.data.mapper

import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.domain.model.CharacterDetail
import com.mej.rickmorty.domain.model.CharacterPage
import com.mej.rickmorty.domain.model.CharacterStatus
import com.mej.rickmorty.domain.model.Episode
import com.mej.rickmorty.graphql.CharacterDetailQuery
import com.mej.rickmorty.graphql.CharactersPageQuery

/**
 * Traduz o modelo gerado pelo Apollo para o dominio.
 *
 * Todo campo do schema e opcional, entao o mapper e o unico lugar que lida com
 * nulo: o dominio recebe tipos completos e a UI nunca escreve `?:`.
 */
fun CharactersPageQuery.Data.toDomain(): CharacterPage = CharacterPage(
    characters = characters?.results.orEmpty().filterNotNull().mapNotNull { it.toDomain() },
    nextPage = characters?.info?.next,
)

private fun CharactersPageQuery.Result.toDomain(): Character? {
    val id = id ?: return null
    return Character(
        id = id,
        name = name.orEmpty(),
        status = CharacterStatus.fromApi(status),
        species = species.orEmpty(),
        imageUrl = image.orEmpty(),
    )
}

fun CharacterDetailQuery.Data.toDomain(): CharacterDetail? {
    val remote = character ?: return null
    val id = remote.id ?: return null

    return CharacterDetail(
        character = Character(
            id = id,
            name = remote.name.orEmpty(),
            status = CharacterStatus.fromApi(remote.status),
            species = remote.species.orEmpty(),
            imageUrl = remote.image.orEmpty(),
        ),
        gender = remote.gender.orEmpty(),
        originName = remote.origin?.name.orEmpty(),
        episodes = remote.episode.orEmpty().filterNotNull().mapNotNull { episode ->
            episode.id?.let {
                Episode(
                    id = it,
                    name = episode.name.orEmpty(),
                    code = episode.episode.orEmpty(),
                    airDate = episode.air_date.orEmpty(),
                )
            }
        },
    )
}
