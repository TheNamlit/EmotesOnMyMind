package com.thenamlit.emotesonmymind.di

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import com.apollographql.apollo3.ApolloClient
import com.thenamlit.emotesonmymind.core.data.repository.DeviceStorageRepositoryImpl
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.util.SevenTv
import com.thenamlit.emotesonmymind.features.emotes.data.local.schema.MainFeedEmoteSearchHistoryItemSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerCollectionSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerImageDataSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataHostFileSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataOwnerSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerRemoteEmoteDataSchema
import com.thenamlit.emotesonmymind.features.sticker.data.local.schema.StickerSchema
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMongoDbRealmDatabase(): Realm {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                StickerSchema::class,
                StickerImageDataSchema::class,
                StickerRemoteEmoteDataSchema::class,
                StickerRemoteEmoteDataHostFileSchema::class,
                StickerRemoteEmoteDataOwnerSchema::class,
                StickerCollectionSchema::class,
                MainFeedEmoteSearchHistoryItemSchema::class
            )
        )
            // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/schemas/change-an-object-model/#overview
            .deleteRealmIfMigrationNeeded() // TODO: Remove this for production!!!
//            .compactOnLaunch()
            .build()

        return Realm.open(configuration = config)
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    // https://blog.logrocket.com/building-android-app-graphql/
    @Provides
    @Singleton
    fun provideOkhttpClient(): OkHttpClient {
//        val logging = HttpLoggingInterceptor()
//            .apply { level = HttpLoggingInterceptor.Level.BODY  }
        return OkHttpClient.Builder()
//            .addInterceptor(AuthInterceptor())
//            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named(value = "7TvApolloClient")
    fun provide7TvApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        //  According to documentation, it's not required to have the OkHttpClient
        //  Was probably just for the Logging stuff
        //  https://www.apollographql.com/docs/kotlin/tutorial/04-execute-the-query
        return ApolloClient.Builder()
            .serverUrl(SevenTv.GRAPHQL_URL)
//            .okHttpClient(okHttpClient = okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideImageLoader(app: Application): ImageLoader {
        // Caching: https://coil-kt.github.io/coil/image_loaders/#caching
        return ImageLoader.Builder(app)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(app.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }.build()
    }

    @Provides
    @Singleton
    fun provideJsonBuilder(): Json {
        return Json { ignoreUnknownKeys = true }
    }

    @Provides
    @Singleton
    fun provideDeviceStorageRepository(
        @ApplicationContext context: Context,
    ): DeviceStorageRepository {
        return DeviceStorageRepositoryImpl(context = context)
    }
}
