package com.mej.rickmorty.feature.characters.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * Ponto de entrada da tela. `koinViewModel()` resolve o ViewModel pelo
 * container do Koin, declarado em `featureModule`.
 */
@Composable
fun CharactersRoute(
    onOpenDetail: (String) -> Unit,
    viewModel: CharactersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CharactersScreen(
        state = state,
        effects = viewModel.effects,
        onIntent = viewModel::dispatch,
        onOpenDetail = onOpenDetail,
    )
}
