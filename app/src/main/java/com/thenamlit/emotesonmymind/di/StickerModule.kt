package com.thenamlit.emotesonmymind.di

import android.content.Context
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerCollectionDao
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerCollectionDaoImpl
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerDao
import com.thenamlit.emotesonmymind.features.sticker.data.local.dao.StickerDaoImpl
import com.thenamlit.emotesonmymind.features.sticker.data.repository.StickerCollectionRepositoryImpl
import com.thenamlit.emotesonmymind.features.sticker.data.repository.StickerRepositoryImpl
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection.GetCollectionsUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.CheckForWhatsAppInstallationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object StickerModule {

    @Provides
    @Singleton
    fun provideStickerDao(realm: Realm): StickerDao {
        return StickerDaoImpl(realm = realm)
    }

    @Provides
    @Singleton
    fun provideStickerRepository(stickerDao: StickerDao): StickerRepository {
        return StickerRepositoryImpl(stickerDao = stickerDao)
    }


    @Provides
    @Singleton
    fun provideStickerCollectionDao(realm: Realm): StickerCollectionDao {
        return StickerCollectionDaoImpl(realm = realm)
    }

    @Provides
    @Singleton
    fun provideStickerCollectionRepository(
        stickerCollectionDao: StickerCollectionDao,
    ): StickerCollectionRepository {
        return StickerCollectionRepositoryImpl(stickerCollectionDao = stickerCollectionDao)
    }

    @Provides
    @Singleton
    fun provideGetCollectionsUseCase(
        stickerCollectionRepository: StickerCollectionRepository,
    ): GetCollectionsUseCase {
        return GetCollectionsUseCase(
            stickerCollectionRepository = stickerCollectionRepository
        )
    }

    @Provides
    @Singleton
    fun provideCheckForWhatsAppInstallationUseCase(
        @ApplicationContext context: Context,
    ): CheckForWhatsAppInstallationUseCase {
        return CheckForWhatsAppInstallationUseCase(context = context)
    }
}
