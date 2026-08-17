package com.mej.rickmorty

import android.app.Application
import com.mej.rickmorty.data.di.dataModule
import com.mej.rickmorty.feature.characters.charactersModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Inicializacao do Koin.
 *
 * O grafo e montado em runtime a partir da lista de modulos, e nao gerado em
 * tempo de compilacao. Em troca da ausencia de verificacao pelo compilador,
 * existe `KoinModulesTest`, que falha o build se alguma dependencia nao puder
 * ser resolvida.
 */
class RickMortyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@RickMortyApp)
            modules(dataModule, charactersModule)
        }
    }
}
