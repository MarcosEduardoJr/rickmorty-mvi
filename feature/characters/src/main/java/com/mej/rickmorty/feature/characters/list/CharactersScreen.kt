package com.mej.rickmorty.feature.characters.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mej.core.designsystem.AppButton
import com.mej.core.designsystem.AppCard
import com.mej.core.designsystem.AppSearchField
import com.mej.core.designsystem.AppSnackbarHost
import com.mej.core.designsystem.AppTheme
import com.mej.core.designsystem.CaptionText
import com.mej.core.designsystem.ChoiceChipGroup
import com.mej.core.designsystem.DecorativeSpinner
import com.mej.core.designsystem.SectionHeading
import com.mej.core.designsystem.StatusText
import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.feature.characters.R
import com.mej.rickmorty.feature.characters.asMessage
import com.mej.rickmorty.feature.characters.labelRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private const val AVATAR_SIZE_DP = 56
private const val LOAD_MORE_THRESHOLD = 4

/**
 * A UI so despacha intents e le o estado. Nenhuma decisao de negocio acontece
 * aqui, nem mesmo quando pedir a proxima pagina — a rolagem vira um intent e o
 * ViewModel decide se ha pagina seguinte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    state: CharactersState,
    effects: Flow<CharactersEffect>,
    onIntent: (CharactersIntent) -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val dismissLabel = stringResource(R.string.snackbar_dismiss)

    LaunchedEffect(Unit) { onIntent(CharactersIntent.Started) }

    ObserveEffects(
        effects = effects,
        snackbarHostState = snackbarHostState,
        onOpenDetail = onOpenDetail,
    )

    ObserveScrollForPaging(listState = listState, state = state, onIntent = onIntent)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.characters_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState, dismissLabel) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppTheme.spacing.lg)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            Filters(state = state, onIntent = onIntent)

            when {
                state.isLoading -> CenteredStatus(stringResource(R.string.state_loading))

                state.error != null && state.characters.isEmpty() ->
                    ErrorState(message = state.error.asMessage(), onRetry = { onIntent(CharactersIntent.Retry) })

                state.isEmpty -> EmptyState()

                else -> CharacterList(
                    state = state,
                    listState = listState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun ObserveEffects(
    effects: Flow<CharactersEffect>,
    snackbarHostState: SnackbarHostState,
    onOpenDetail: (String) -> Unit,
) {
    val networkMessage = stringResource(R.string.error_network)
    val dismiss = stringResource(R.string.snackbar_dismiss)

    LaunchedEffect(Unit) {
        effects.collectLatest { effect ->
            when (effect) {
                is CharactersEffect.OpenDetail -> onOpenDetail(effect.id)
                is CharactersEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = networkMessage,
                    actionLabel = dismiss,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }
}

/** Rolar ate perto do fim vira um intent; a decisao de paginar e do ViewModel. */
@Composable
private fun ObserveScrollForPaging(
    listState: LazyListState,
    state: CharactersState,
    onIntent: (CharactersIntent) -> Unit,
) {
    val shouldLoadMore by remember(state) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.canLoadMore && lastVisible >= state.characters.lastIndex - LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onIntent(CharactersIntent.LoadMore)
    }
}

@Composable
private fun Filters(state: CharactersState, onIntent: (CharactersIntent) -> Unit) {
    val allLabel = stringResource(R.string.characters_filter_all)

    AppSearchField(
        value = state.filter.name,
        onValueChange = { onIntent(CharactersIntent.QueryChanged(it)) },
        label = stringResource(R.string.characters_search_label),
        supportingText = stringResource(R.string.characters_search_helper),
    )

    ChoiceChipGroup(
        options = CharactersReducer.statusOptions(),
        selected = state.filter.status,
        onSelect = { onIntent(CharactersIntent.StatusSelected(it)) },
        optionLabel = { status -> status?.let { stringResource(it.labelRes) } ?: allLabel },
    )
}

@Composable
private fun CharacterList(
    state: CharactersState,
    listState: LazyListState,
    onIntent: (CharactersIntent) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = AppTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        items(items = state.characters, key = { it.id }) { character ->
            CharacterRow(character = character, onIntent = onIntent)
        }

        if (state.isLoadingMore) {
            item(key = "loading-more") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.lg),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StatusText(text = stringResource(R.string.state_loading_more))
                }
            }
        }
    }
}

@Composable
private fun CharacterRow(character: Character, onIntent: (CharactersIntent) -> Unit) {
    val openLabel = stringResource(R.string.character_open, character.name)
    val statusLabel = stringResource(character.status.labelRes)

    AppCard(
        modifier = Modifier
            .heightIn(min = AppTheme.sizing.minTouchTarget)
            .semantics(mergeDescendants = true) { role = Role.Button },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = character.imageUrl,
                // A foto e decorativa: o nome ja esta no texto ao lado.
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .clip(CircleShape),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                CaptionText(
                    text = stringResource(R.string.character_meta, statusLabel, character.species),
                )
            }

            AppButton(
                text = stringResource(R.string.detail_title),
                onClick = { onIntent(CharactersIntent.CharacterClicked(character.id)) },
                modifier = Modifier.semantics { contentDescription = openLabel },
            )
        }
    }
}

@Composable
private fun CenteredStatus(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        ) {
            DecorativeSpinner()
            StatusText(text = text)
        }
    }
}

@Composable
private fun EmptyState() {
    AppCard {
        SectionHeading(text = stringResource(R.string.state_empty_title))
        CaptionText(text = stringResource(R.string.state_empty_body))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    AppCard {
        SectionHeading(text = stringResource(R.string.state_error_title))
        StatusText(
            text = message,
            assertive = true,
            color = MaterialTheme.colorScheme.error,
        )
        AppButton(text = stringResource(R.string.action_retry), onClick = onRetry)
    }
}
