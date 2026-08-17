package com.mej.rickmorty.feature.characters

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mej.core.common.AppError
import com.mej.rickmorty.domain.model.CharacterStatus

/**
 * Traduz [AppError] para texto. O ViewModel guarda o erro tipado; so a UI
 * conhece `R.string`, entao o modulo de dominio segue sem dependencia Android.
 */
@Composable
fun AppError.asMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.error_network)
    AppError.Timeout -> stringResource(R.string.error_timeout)
    is AppError.Server -> stringResource(R.string.error_server, message)
    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

@get:StringRes
val CharacterStatus.labelRes: Int
    get() = when (this) {
        CharacterStatus.ALIVE -> R.string.status_alive
        CharacterStatus.DEAD -> R.string.status_dead
        CharacterStatus.UNKNOWN -> R.string.status_unknown
    }
