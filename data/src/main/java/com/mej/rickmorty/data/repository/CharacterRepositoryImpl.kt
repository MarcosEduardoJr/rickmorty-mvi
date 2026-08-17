package com.mej.rickmorty.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mej.core.common.AppError
import com.mej.core.common.DispatcherProvider
import com.mej.core.common.Outcome
import com.mej.core.network.toAppError
import com.mej.core.network.toOutcome
import com.mej.rickmorty.data.mapper.toDomain
import com.mej.rickmorty.domain.model.CharacterDetail
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterPage
import com.mej.rickmorty.domain.repository.CharacterRepository
import com.mej.rickmorty.graphql.CharacterDetailQuery
import com.mej.rickmorty.graphql.CharactersPageQuery
import com.mej.rickmorty.graphql.type.FilterCharacter
import kotlinx.coroutines.withContext

class CharacterRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val dispatchers: DispatcherProvider,
) : CharacterRepository {

    override suspend fun loadPage(page: Int, filter: CharacterFilter): Outcome<CharacterPage> =
        withContext(dispatchers.io) {
            runCatching {
                apolloClient
                    .query(CharactersPageQuery(page = Optional.present(page), filter = filter.toInput()))
                    .execute()
                    .toOutcome { it.toDomain() }
            }.getOrElse { Outcome.Failure(it.toAppError()) }
        }

    override suspend fun loadDetail(id: String): Outcome<CharacterDetail> =
        withContext(dispatchers.io) {
            runCatching {
                apolloClient
                    .query(CharacterDetailQuery(id = id))
                    .execute()
                    .toOutcome { it.toDomain() }
            }
                .getOrElse { Outcome.Failure(it.toAppError()) }
                .let { outcome ->
                    when (outcome) {
                        is Outcome.Failure -> outcome
                        is Outcome.Success -> {
                            val detail = outcome.data
                            if (detail == null) {
                                Outcome.Failure(AppError.Server("Personagem nao encontrado"))
                            } else {
                                Outcome.Success(detail)
                            }
                        }
                    }
                }
        }

    /**
     * `Optional.absent()` omite o campo da query. Enviar string vazia faria a
     * API filtrar por nome vazio e devolver zero resultados.
     */
    private fun CharacterFilter.toInput() = Optional.present(
        FilterCharacter(
            name = if (name.isBlank()) Optional.absent() else Optional.present(name),
            status = status?.let { Optional.present(it.apiValue) } ?: Optional.absent(),
        ),
    )
}
