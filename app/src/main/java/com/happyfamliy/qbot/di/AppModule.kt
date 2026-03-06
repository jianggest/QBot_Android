package com.happyfamliy.qbot.di

import com.google.ai.client.generativeai.GenerativeModel
import com.happyfamliy.qbot.BuildConfig
import com.happyfamliy.qbot.data.repository.GeminiRepositoryImpl
import com.happyfamliy.qbot.data.repository.GenerativeModelWrapperImpl
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
    fun provideChatModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    @Named("jsonModel")
    fun provideJsonModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    @Named("chatWrapper")
    fun provideChatWrapper(@Named("chatModel") model: GenerativeModel): GenerativeModelWrapper {
        return GenerativeModelWrapperImpl(model)
    }

    @Provides
    @Singleton
    @Named("jsonWrapper")
    fun provideJsonWrapper(@Named("jsonModel") model: GenerativeModel): GenerativeModelWrapper {
        return GenerativeModelWrapperImpl(model)
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(
        @Named("chatWrapper") chatWrapper: GenerativeModelWrapper
    ): GeminiRepository {
        return GeminiRepositoryImpl(chatWrapper)
    }
}
