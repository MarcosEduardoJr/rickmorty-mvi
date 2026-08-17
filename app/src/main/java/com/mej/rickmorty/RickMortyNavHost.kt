package com.mej.rickmorty

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mej.rickmorty.feature.characters.detail.CharacterDetailRoute
import com.mej.rickmorty.feature.characters.list.CharactersRoute

private object Routes {
    const val LIST = "characters"
    const val DETAIL = "characters/{id}"

    fun detail(id: String) = "characters/$id"
}

/**
 * A navegacao mora no `:app`: as features expoem rotas composable e recebem
 * lambdas, sem conhecer o grafo nem umas as outras.
 */
@Composable
fun RickMortyNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            CharactersRoute(
                onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            CharacterDetailRoute(
                characterId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
