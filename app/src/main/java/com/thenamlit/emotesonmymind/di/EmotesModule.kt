package com.thenamlit.emotesonmymind.di

import androidx.work.WorkManager
import coil.ImageLoader
import com.apollographql.apollo3.ApolloClient
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.features.emotes.data.local.dao.MainFeedEmoteSearchHistoryDao
import com.thenamlit.emotesonmymind.features.emotes.data.local.dao.MainFeedEmoteSearchHistoryDaoImpl
import com.thenamlit.emotesonmymind.features.emotes.data.repository.EmoteRepositoryImpl
import com.thenamlit.emotesonmymind.features.emotes.data.repository.EmoteToStickerRepositoryImpl
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteRepository
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteToStickerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object EmotesModule {

    @Provides
    @Singleton
    fun provideEmoteRepository(
        @Named(value = "7TvApolloClient") apolloClient: ApolloClient,
        mainFeedEmoteSearchHistoryDao: MainFeedEmoteSearchHistoryDao,
    ): EmoteRepository {
        return EmoteRepositoryImpl(
            apolloClient = apolloClient,
            mainFeedEmoteSearchHistoryDao = mainFeedEmoteSearchHistoryDao
        )
    }

    @Provides
    @Singleton
    fun provideMainFeedEmoteSearchHistoryDao(realm: Realm): MainFeedEmoteSearchHistoryDao {
        return MainFeedEmoteSearchHistoryDaoImpl(realm = realm)
    }

    @Provides
    @Singleton
    fun provideEmoteToStickerRepository(
        deviceStorageRepository: DeviceStorageRepository,
        imageLoader: ImageLoader,
        workManager: WorkManager,
        json: Json,
    ): EmoteToStickerRepository {
        return EmoteToStickerRepositoryImpl(
            deviceStorageRepository = deviceStorageRepository,
            imageLoader = imageLoader,
            workManager = workManager,
            jsonBuilder = json
        )
    }
}
