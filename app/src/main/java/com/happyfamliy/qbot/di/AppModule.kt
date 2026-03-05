package com.happyfamliy.qbot.di

import com.google.ai.client.generativeai.GenerativeModel
import com.happyfamliy.qbot.BuildConfig
import com.happyfamliy.qbot.data.repository.GeminiRepositoryImpl
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(
        generativeModel: GenerativeModel
    ): GeminiRepository {
        return GeminiRepositoryImpl(generativeModel)
    }
}
