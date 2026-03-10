# 📱 QBot for Android 开发计划与任务拆解

## 🏗️ 架构与技术栈选型预设定
在开始让 Agent 写代码前，我们先定下 Android 端的标准技术栈：
* **UI 层**: Jetpack Compose (完全声明式，适合聊天界面的快速构建)
* **架构模式**: MVVM (Model-View-ViewModel) + 单向数据流 (UDF)
* **依赖注入**: Dagger Hilt (让 Agent 帮你管理复杂的系统级实例)
* **本地存储**: Room Database (完美替代原生的 SQLite 裸写)
* **AI SDK**: `com.google.ai.client.generativeai` (Google 官方 Android Gemini SDK, Embedding 暂用 REST 补齐)
* **异步与并发**: Kotlin Coroutines + Flow
* **后台任务**: WorkManager / ViewModel Coroutines

---

## 📅 阶段一：基础设施与“骨架”搭建 (Phase 1: Foundation)
**目标**：搭建起 Android 工程骨架，配置好底层数据库和依赖。

### [x] Task 1.1: 初始化工程与依赖
* **状态**：已完成。配置了 Compose, Hilt, Room, Gemini SDK, MockK 等。
* **Agent 指令**：
  > “创建一个全新的 Android 项目，使用 Empty Compose Activity。在 `build.gradle.kts` 中配置以下依赖：Jetpack Compose 最新版, Room, Hilt, Google Generative AI Android SDK, ViewModel Compose, Coroutines。配置完整的包结构：`ui`, `data`, `domain`, `di`。”


### [x] Task 1.2: 设计并实现 Room 数据库 (记忆持久化)
* **状态**：已完成。实现了 `SessionEntity`, `MessageEntity`, `FactEntity` 及其对应的 DAO。
* **Agent 指令**：
  > “使用 Room 搭建本地数据库。
  > 1. 创建实体类 `SessionEntity` (id, title, createdAt, updatedAt)。
  > 2. 创建实体类 `MessageEntity` (id, sessionId, role[user/model], content, timestamp)。
  > 3. 创建实体类 `FactEntity` (id, content, topic, embedding[FloatArray序列化为String或ByteArray], accessCount, lastUpdatedAt)。
  > 4. 编写对应的 DAO (Data Access Object) 接口，包含增删改查及按 sessionId 查询消息记录（Flow返回）。
  > 5. 使用 Hilt 提供 Database 和 Dao 的单例注入。”

---

## 💬 阶段二：核心聊天流与 Gemini 集成 (Phase 2: Core AI Chat)
**目标**：跑通与 Gemini 的基础对话，支持流式打字机效果。

### [x] Task 2.1: 封装 Gemini API (Domain 层)
* **状态**：已完成。封装了 `GeminiRepository`，支持流式响应及历史上下文。
* **Agent 指令**：
  > “在 `domain` 层创建一个 `GeminiRepository`。
  > 1. 初始化 `GenerativeModel`，配置 API Key（暂存放在 `local.properties` 中并通过 BuildConfig 读取）。
  > 2. 实现一个方法 `sendMessageStream(history: List<MessageEntity>, prompt: String): Flow<String>`。
  > 3. 需要将本地的 `MessageEntity` 历史记录转换为 Gemini SDK 支持的 `Content` 格式，以实现上下文感知。”

### [x] Task 2.2: 构建聊天 UI 与 ViewModel (UI 层)
* **状态**：已完成。实现了 `ChatScreen` 和 `ChatViewModel`，支持实时打字机效果。
* **Agent 指令**：
  > “1. 创建 `ChatViewModel`，持有当前的会话状态（List<Message>）和输入框状态。
  > 2. 实现发送消息逻辑：保存用户消息到 Room -> 调用 Repository 获取流式响应 -> 实时更新 UI 状态 -> 接收完毕后保存 AI 消息到 Room。
  > 3. 使用 Jetpack Compose 编写 `ChatScreen`。包含一个 `LazyColumn` (用于显示气泡列表) 和一个底部的输入框区域 (`OutlinedTextField` + 发送按钮)。”

### [x] Task 2.3: 聊天记录持久化与会话恢复
* **状态**：已完成。修复了每次进退应用/切换界面导致聊天记录丢失的问题。
* **问题根因**：`ChatViewModel` 重建后 `currentSessionId` 归 null，每次 `sendMessage` 都会创建新 session，历史消息从不自动加载。
* **修复内容**：
  1. `SessionDao` 新增 `getLatestSession()` 查询。
  2. `ChatViewModel.init {}` 中自动加载最近一次 session 及其消息，无历史则等用户首次发送时再创建。
  3. 修复 `sendMessage` 中 `\${e.message}` 转义 bug，改为 `${e.message}`。

### [x] Task 2.4: 长按气泡删除消息
* **状态**：已完成。长按聊天气泡弹出确认对话框，确认后删除该条消息并同步从数据库移除。
* **改动内容**：
  1. `MessageDao` 新增 `deleteMessageById(id: Long)`。
  2. `ChatViewModel` 新增 `deleteMessage(messageId: Long)`，删除后刷新消息列表。
  3. `ChatScreen` 的 `MessageBubble` 添加长按手势，弹出 `AlertDialog` 确认；流式生成中的气泡（`id == -1`）不可删除。

---

## 🧠 阶段三：QBot 灵魂移植 —— 原子化事实提取与去重 (Phase 3: Smart Memory)
**目标**：将 CLI 版最核心的 RAG 机制（事实提取与语义去重）在手机端跑通。

### [x] Task 3.1: 事实提取 logic (后台分析)
* **状态**：已完成。引入 `GenerativeModelWrapper` 解决 mock 问题，并实现 `FactExtractionUseCase`。
* **Agent 指令**：
  > “实现一个 `FactExtractionUseCase`。
  > 1. 定义一个特殊的 Prompt，要求模型对传入的对话上下文进行分析，提取出 JSON 格式的原子事实（包含 content 和 topic）。
  > 2. 约束输出格式为：`[{"fact": "...", "topic": "..."}]`。
  > 3. 在 `ChatViewModel` 中，当一轮对话结束（AI 回复完毕）后，启动一个后台协程调用此 UseCase，默默处理刚刚的对话内容。”

### [x] Task 3.2: 向量嵌入 (Embedding) 与语义去重计算
* **状态**：已完成。针对 SDK 缺失 `embedContent` 的问题采用了直接 REST API 调用的方案。实现了 `SaveFactsUseCase` 及其去重逻辑 (相似度 > 0.92)。
* **Agent 指令**：
  > “实现嵌入和去重逻辑。
  > 1. 使用 Gemini 的 `embedding-001` 模型，将提取到的事实文本转换成 `FloatArray` (向量)。
  > 2. 编写一个计算**余弦相似度 (Cosine Similarity)** 的工具函数。
  > 3. 当新事实产生时，从 Room 的 `FactEntity` 中加载所有已有事实的向量，计算相似度。
  > 4. **去重逻辑**：如果最高相似度 > 0.92，则认为是重复事实，仅更新原记录的 `accessCount` + 1 和 `lastUpdatedAt`；如果 <= 0.92，则作为新记录插入 Room。”

---

## 🛠️ 阶段四：系统助理能力打通 —— Function Calling (Phase 4: System Integration)
**目标**：让大模型长出“手脚”，直接操作手机本地的日历、闹钟和电话。

### ⬜ Task 4.1: 定义 Tool / Function Declarations
* **Agent 指令**：
  > “在 `GeminiRepository` 的模型初始化中，配置 `Tool`。
  > 声明三个函数签名 (Function Declaration)：
  > 1. `set_alarm(time: String, label: String)`
  > 2. `add_calendar_event(title: String, startTime: String, endTime: String)`
  > 3. `make_phone_call(name_or_number: String)`”

### ⬜ Task 4.2: 系统操作执行器 (System Executors)
* **Agent 指令**：
  > “创建三个具体的系统操作工具类 (注入 ApplicationContext)：
  > 1. **AlarmExecutor**: 使用 `Intent(AlarmClock.ACTION_SET_ALARM)` 实现设置闹钟。
  > 2. **CalendarExecutor**: 使用 `CalendarContract` API，申请日历读写权限后，静默插入日程。
  > 3. **PhoneExecutor**: 使用 `Intent(Intent.ACTION_DIAL)` 传递 `tel:号码` 给系统拨号盘。
  > (注：这部分需要 Agent 帮你处理 AndroidManifest 中的权限声明及 Compose 的动态权限申请逻辑)”

### ⬜ Task 4.3: 闭环 Function Calling 的分发逻辑
* **Agent 指令**：
  > “修改 `GeminiRepository` 的聊天逻辑。当收到 Gemini 的响应类型为 `FunctionCall` 时：
  > 1. 解析要调用的函数名和参数。
  > 2. 调用 Task 4.2 中对应的 Executor。
  > 3. 将执行结果 (如 '闹钟设置成功' 或 '无权限') 封装成 `FunctionResponse`，再次发回给 Gemini，让它生成最终的自然语言回复给用户。”

---

## 🚀 阶段五：深度整合与体验打磨 (Phase 5: Native Assistant Polish)
**目标**：它不再是个普通 App，而是真正的“系统级助手”。

### [x] Task 5.1: Markdown 解析与富文本展示
* **Agent 指令**：
  > “引入 `compose-markdown` 库（或使用 Android `Html.fromHtml` 的兼容方案）。改造 `ChatScreen` 中的消息气泡，使其完美渲染 Markdown 语法（包括加粗、列表、代码块高亮）。这对于 QBot 记笔记的体验至关重要。”

### [x] Task 5.2: 调试面板 (Debug Memory Dashboard)
* **Agent 指令**：
  > “在 App 中添加一个侧边栏 (Drawer) 或设置入口，实现 QBot CLI 的 `debug-memory` 功能。
  > 创建一个 `MemoryScreen`，使用 Compose 读取 Room 数据库中的 `FactEntity`。用卡片或表格形式列出所有原子事实，显示其 Topic、内容、向量值（缩略）、访问次数。提供搜索和手动删除功能。”

### ⬜ Task 5.3 (进阶): 接入 Android 系统级助手入口
* **Agent 指令**：
  > “继承 `VoiceInteractionService`。
  > 1. 在 AndroidManifest 中将其声明为 `android.service.voice.VoiceInteractionService`。
  > 2. 配置对应的 XML meta-data。
  > 3. 实现长按电源键或手势呼出时，直接弹出 QBot 的一个透明底部的 Compose 浮窗，允许用户快速输入语音或文本。”
