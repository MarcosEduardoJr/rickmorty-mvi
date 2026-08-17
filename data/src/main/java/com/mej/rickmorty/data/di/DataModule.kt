package com.mej.rickmorty.data.di

import com.mej.core.common.DefaultDispatcherProvider
import com.mej.core.common.DispatcherProvider
import com.mej.core.network.ApolloClientFactory
import com.mej.rickmorty.data.repository.CharacterRepositoryImpl
import com.mej.rickmorty.domain.repository.CharacterRepository
import com.mej.rickmorty.domain.usecase.LoadCharacterDetailUseCase
import com.mej.rickmorty.domain.usecase.LoadCharactersUseCase
import org.koin.dsl.module

private const val SERVER_URL = "https://rickandmortyapi.com/graphql"

/**
 * Modulo Koin da camada de dados.
 *
 * A ligacao e declarada em codigo, resolvida em runtime — diferente de um
 * container gerado em tempo de compilacao. O custo e nao ter verificacao do
 * grafo pelo compilador, e por isso existe um teste que valida o modulo.
 */
val dataModule = module {
    single { ApolloClientFactory.create(SERVER_URL) }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }

    factory { LoadCharactersUseCase(get()) }
    factory { LoadCharacterDetailUseCase(get()) }
}
