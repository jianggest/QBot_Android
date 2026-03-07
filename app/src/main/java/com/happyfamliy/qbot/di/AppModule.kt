package com.happyfamliy.qbot.di

import com.google.ai.client.generativeai.GenerativeModel
import com.happyfamliy.qbot.BuildConfig
import com.happyfamliy.qbot.data.repository.EmbeddingRepositoryImpl
import com.happyfamliy.qbot.data.repository.GeminiRepositoryImpl
import com.happyfamliy.qbot.data.repository.GenerativeModelWrapperImpl
import com.happyfamliy.qbot.domain.repository.EmbeddingRepository
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("chatModel")
    fun provideChatModel(): GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    @Provides
    @Singleton
    @Named("jsonModel")
    fun provideJsonModel(): GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    @Provides
    @Singleton
    @Named("chatWrapper")
    fun provideChatWrapper(@Named("chatModel") model: GenerativeModel): GenerativeModelWrapper =
        GenerativeModelWrapperImpl(model)

    @Provides
    @Singleton
    @Named("jsonWrapper")
    fun provideJsonWrapper(@Named("jsonModel") model: GenerativeModel): GenerativeModelWrapper =
        GenerativeModelWrapperImpl(model)

    @Provides
    @Singleton
    fun provideGeminiRepository(
        @Named("chatWrapper") chatWrapper: GenerativeModelWrapper
    ): GeminiRepository = GeminiRepositoryImpl(chatWrapper)

    @Provides
    @Singleton
    fun provideEmbeddingRepository(): EmbeddingRepository = EmbeddingRepositoryImpl()
}
