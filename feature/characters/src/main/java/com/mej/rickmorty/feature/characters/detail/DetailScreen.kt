package com.mej.rickmorty.feature.characters.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mej.core.designsystem.AppButton
import com.mej.core.designsystem.AppCard
import com.mej.core.designsystem.AppIconButton
import com.mej.core.designsystem.AppTheme
import com.mej.core.designsystem.CaptionText
import com.mej.core.designsystem.DecorativeSpinner
import com.mej.core.designsystem.SectionHeading
import com.mej.core.designsystem.StatusText
import com.mej.rickmorty.domain.model.CharacterDetail
import com.mej.rickmorty.feature.characters.R
import com.mej.rickmorty.feature.characters.asMessage
import com.mej.rickmorty.feature.characters.labelRes
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HERO_HEIGHT_DP = 260

@Composable
fun CharacterDetailRoute(
    characterId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(characterId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.dispatch(DetailIntent.Started) }

    DetailScreen(
        state = state,
        onBack = onBack,
        onRetry = { viewModel.dispatch(DetailIntent.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.detail?.character?.name ?: stringResource(R.string.detail_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.detail_back),
                        onClick = onBack,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        AppTheme.spacing.md,
                        Alignment.CenterVertically,
                    ),
                ) {
                    DecorativeSpinner()
                    StatusText(text = stringResource(R.string.state_loading))
                }

                state.error != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
                ) {
                    SectionHeading(text = stringResource(R.string.state_error_title))
                    StatusText(
                        text = state.error.asMessage(),
                        assertive = true,
                        color = MaterialTheme.colorScheme.error,
                    )
                    AppButton(text = stringResource(R.string.action_retry), onClick = onRetry)
                }

                state.detail != null -> DetailContent(state.detail)
            }
        }
    }
}

@Composable
private fun DetailContent(detail: CharacterDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        item(key = "hero") {
            AsyncImage(
                model = detail.character.imageUrl,
                contentDescription = stringResource(
                    R.string.detail_image_of,
                    detail.character.name,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_HEIGHT_DP.dp)
                    .clip(MaterialTheme.shapes.large),
            )
        }

        item(key = "facts") {
            AppCard {
                Fact(stringResource(R.string.detail_status), stringResource(detail.character.status.labelRes))
                Fact(stringResource(R.string.detail_species), detail.character.species)
                Fact(stringResource(R.string.detail_gender), detail.gender)
                Fact(stringResource(R.string.detail_origin), detail.originName)
            }
        }

        item(key = "episodes-header") {
            SectionHeading(
                text = stringResource(R.string.detail_episodes, detail.episodes.size),
                modifier = Modifier.padding(top = AppTheme.spacing.sm),
            )
        }

        items(items = detail.episodes, key = { it.id }) { episode ->
            AppCard {
                Text(
                    text = stringResource(R.string.detail_episode_item, episode.code, episode.name),
                    style = MaterialTheme.typography.bodyLarge,
                )
                CaptionText(text = episode.airDate)
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CaptionText(text = label)
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
