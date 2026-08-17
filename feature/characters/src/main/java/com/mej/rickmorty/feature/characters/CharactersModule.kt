package com.mej.rickmorty.feature.characters

import com.mej.rickmorty.feature.characters.detail.DetailViewModel
import com.mej.rickmorty.feature.characters.list.CharactersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Modulo Koin da feature. `viewModel` liga o ciclo de vida do Android sem
 * anotacao nem processador; o id do personagem chega como parametro em runtime.
 */
val charactersModule = module {
    viewModel { CharactersViewModel(get()) }
    viewModel { (characterId: String) -> DetailViewModel(characterId, get()) }
}
