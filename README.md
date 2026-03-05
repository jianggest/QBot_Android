# QBot Android

基于 Google Gemini API 构建的智能 Android 个人助理，旨在通过本地知识库、原子记忆提取和端侧工具调用（Function Calling）成为系统中无处不在的智能助手。

## 🎯 项目概览

QBot Android 不仅仅是一个普通的对话机器人，它是一个 **系统级助理**。通过在后台默默运行，QBot 能通过基于检索增强生成（RAG）对日常聊天进行“原子化”特征提取与语义去重，使对话机器人真正具备“记忆”。

本项目当前处于开发的 **Phase 1**，已完成了基础工程的搭建及本地持久层（Room）的初始化。

## ✨ 核心特性 (规划中)

- **智能对话**：完美集成 Google Generative AI Android SDK (`gemini-pro`)。
- **本地记忆库**：使用 Room 持久化用户的多轮对话和原子化的“事实”，实现长期记忆去重与回溯（RAG）。
- **系统级集成**：基于 Function Calling 直接操作 Android 本地日历、闹钟功能及拨打电话等系统 API。
- **透明侧滑窗**：支持通过 `VoiceInteractionService` 作为系统助手快捷呼出。
- **纯净 UI**：采用最新的完全声明式 UI 框架 **Jetpack Compose** 打造流畅的交互体验，支持 Markdown 富文本渲染。

## 🚀 快速开始

### 运行要求
- Android Studio Iguana | 2023.2.1 或更新版本（推荐）
- JDK 17
- 最低支持 SDK 版本的设备或模拟器（Android 8.0, API 26+）

### 构建步骤
1. 克隆此仓库到本地：
   ```bash
   git clone https://github.com/jianggest/QBot_Android.git
   ```
2. 在 Android Studio 中打开项目。
3. （后续需求）在根目录的 `local.properties` 文件中添加 Gemini 的 API Key (例如：`GEMINI_API_KEY=AIzaSy...`)。
4. 同步 Gradle 项目。
5. 点击运行按钮，将应用安装到设备/模拟器中。

## 🛠 当前阶段 (Phase 1)
已完成工程初始化、依赖配置及本地数据库架构（`Session`, `Message`, `Fact`），并加入了本地 Robolectric 测试以遵循 TDD 开发模式。详细的技术架构说明请参阅 [Architecture.md](Architecture.md) 或参考原始拆解文档 [Task.md](Task.md)。

## 📝 许可证
[MIT License](LICENSE)
