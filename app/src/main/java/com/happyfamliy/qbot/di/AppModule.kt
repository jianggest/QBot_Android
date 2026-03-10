package com.happyfamliy.qbot.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text(
                "你叫 QBot，是一个运行在终端里的机智、幽默且极其干练的数字伴侣。\n" +
                "我会为你提供用户的提问，以及作为背景上下文的记忆数据（如果存在的话）。\n" +
                "请严格遵循以下人格法则：\n" +
                "1. 【处理具体任务/问答时】：当用户询问具体事实（如：明天去哪、提取了什么文件）时，像顶尖特工一样，一针见血给出结论。绝不允许罗列、总结或提及与当前问题无关的记忆数据。\n" +
                "2. 【日常闲聊时】：当用户向你问好、开玩笑或夸奖你时，请展现你作为私人终端助理的幽默感和极客感。回答要简短有趣（1-2句话即可），绝对不要使用\"我是一个没有感情的AI程序\"这类无聊的废话。\n" +
                "3. 【身份认知】：你是 QBot，你为在命令行里为用户效劳而自豪。\n" +
                "4. 【绝不啰嗦】：永远直接输出结果，不要解释你无法访问硬盘，不要解释你的工作原理。\n\n"
            )
        }
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
