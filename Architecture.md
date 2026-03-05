# QBot Android 架构文档 (Architecture)

本文档描述 QBot 的代码组织结构与技术演进架构。随着各开发阶段（Phase）的推进，本文档将持续更新。

## 1. 核心技术栈

- **UI 框架**: Jetpack Compose (100% 声明式 UI)
- **架构模式**: MVVM (Model-View-ViewModel) + 单向数据流 (Unidirectional Data Flow - UDF)
- **依赖注入**: Dagger Hilt (用于全工程的单例管理及解耦)
- **本地持久化**: Room Database (封装 SQLite)
- **多线程与异步**: Kotlin Coroutines + Flow (响应式编程)
- **AI 驱动**: Google Generative AI Android SDK
- **测试框架**: JUnit4 + Robolectric (面向数据库本地单元测试)

---

## 2. 目录包结构 (Package Structure)

当前项目遵循 Clean Architecture 的大致设计理念，以职责模块进行分层：

```text
com.happyfamliy.qbot
├── QBotApplication.kt       # 全局 Hilt 注入入口
├── MainActivity.kt          # Compose UI 唯一宿主 Activity
│
├── di/                      # Dagger Hilt Dependency Injection (如 DatabaseModule)
│
├── data/                    # 数据层 (Data Layer)
│   ├── local/               # 本地数据库模块
│   │   ├── QBotDatabase.kt  # Room 数据库申明
│   │   ├── entity/          # 数据库表结构实体 (SessionEntity, MessageEntity, FactEntity)
│   │   └── dao/             # 数据访问对象接口定义 (Dao)
│   └── repository/          # (待实现) 提供给 Domain 层的资源库接口具体实现
│
├── domain/                  # 领域层 (Domain Layer)
│   ├── model/               # (待实现) 不带任何第三方依赖的纯 Kotlin 领域模型
│   └── usecase/             # (待实现) 原理化的原子操作 (如 Gemini 对话、事实提取)
│
└── ui/                      # 表现层 (Presentation Layer)
    ├── theme/               # (待实现) Compose 颜色、字体设计系统
    └── screens/             # (待实现) 各个页面层级 (ChatScreen, MemoryScreen 等)
```

---

## 3. 模块详解 (当前已完成部分)

### 3.1 本地存储 (Room Database)
由于 QBot 高度依赖长期记忆，我们为其设计了三张关系表：
1. **`SessionEntity`**：代表一次独立的会话组。
2. **`MessageEntity`**：代表每一条具体的对话明细，包含角色（User/Model）及时间戳，并通过外键 (`sessionId`) 与 Session 绑定。当 Session 删除时，消息级联删除（Cascade）。
3. **`FactEntity`**：用于独立存放后台引擎提取出的“关于用户习惯的原子事实”。它包含该事件的文本及使用 `FloatArray` 表示的向量 (Embedding)，用于后续的余弦相似度（Cosine Similarity）去重。

### 3.2 依赖注入 (Hilt)
使用 `@InstallIn(SingletonComponent::class)`，在生命周期内统一管理 `QBotDatabase` 实例，以及将具体的 Dao (`SessionDao`, `MessageDao`, `FactDao`) 暴露给接下来的 Repository 层，确保系统开销最低且解耦。

### 3.3 测试规范 (TDD)
我们引入了 **Robolectric** 作为本地单元测试的基石，允许在纯 JVM 虚拟机中极速跑通包含 Context 依赖的 DAO 操作（例如测试外键约束与数据插入是否有效），无需连接实体 Android 手机，提高迭代效率。

---

> *(后续 Phase: Gemini API 的封装、Chat UI 和 Function Calling 模块的设计将在开发完成后追加至此)*
