# 实践教程7：基于 Spring AI 的 LLM 集成实践

项目名称：`cloud-learn-exp07`

## 📖 业务背景

本次实践以“智学云在线学习平台”为业务载体，聚焦在 Spring Boot 项目中完成 LLM（大语言模型）能力接入，并落地两个最常见的应用场景：

- 线上课程网站的 7×24 智能客服（流式输出 + 多轮对话 + 对话记录落库）
- 后台运营的动态内容生成（课程简介润色 / 章节摘要 / Learning Objectives / FAQ 草稿，生成记录落库）

## 🎯 学习目标

- 理解“LLM 调用”在后端的基本链路：请求 → Prompt → ChatClient → 模型 → 响应
- 掌握 Spring AI 1.1.5 的核心使用方式：`ChatClient`、`Prompt`、`SystemMessage`、`UserMessage`
- 理解流式输出（SSE）与非流式输出的差异与适用场景
- 通过数据库持久化实现可追溯、可回放的 AI 交互记录

## 🧱 技术栈

- Spring Boot 3.5.11 / Java 21
- Spring AI 1.1.5（BOM 管理）
- Spring Web / Spring Data JPA / Validation
- H2 文件型数据库（本地教学友好）
- OpenAI-compatible 接口（通过 `spring.ai.openai.base-url` 接入第三方平台）

## 🔐 环境变量

将 API Key 与 Base URL 配置到环境变量（或 IDE Run Configuration）：

```bash
export OPENAI_API_KEY=xxx
export OPENAI_BASE_URL=https://your-provider.example.com/v1
export OPENAI_CHAT_MODEL=xxx
```

## 🚀 启动项目

```bash
mvn spring-boot:run
```

本地入口：

- 服务端口：`http://localhost:8080`
- H2 Console：`http://localhost:8080/h2-console`

## ✅ 场景一：智能客服（流式 + 多轮）

### 核心接口

- 流式客服对话：`POST /api/ai/support/stream`（SSE）
- 历史对话列表：`GET /api/ai/support/conversations?page=1&size=10`（分页从 1 开始）
- 查看会话消息：`GET /api/ai/support/conversations/{conversationId}/messages`

### 请求示例

```json
{
  "conversationId": "u1",
  "question": "我想找一门适合后端初学者的课程",
  "reset": false,
  "extraBody": {
    "thinking": true
  }
}
```

### 说明

- `conversationId` 用于多轮对话：服务端会拼接历史消息形成上下文
- 使用 SSE 进行流式输出，前端可实现边生成边展示
- 对话记录会落库，支持回放与审计

## ✅ 场景二：后台内容生成与摘要（非流式）

### 核心接口

- 课程简介润色：`POST /api/ai/admin/course-intro/polish`
- 章节摘要生成：`POST /api/ai/admin/chapter-summary`
- Lesson learning objectives：`POST /api/ai/admin/lesson-objectives`
- FAQ 草稿生成：`POST /api/ai/admin/faq-draft`

### extraBody

所有后台生成接口均支持 `extraBody` 透传模型平台扩展字段，例如：

```json
{
  "courseId": 1,
  "draftIntroduction": "讲解 Spring Boot 和 JPA 的基本使用，适合初学者。",
  "extraBody": {
    "thinking": false
  }
}
```

### 说明

- 后台生成场景默认采用同步非流式调用，便于运营人员复制、审阅、二次编辑
- 每次生成的输入 Prompt 与输出内容会落库，便于追溯与复用

## 🗄️ 数据落库说明

本项目会自动建表（`ddl-auto: update`）。主要涉及两张与 LLM 集成相关的数据表：

- `support_chat_message`：客服多轮对话消息（按 `conversation_id` 分组）
- `ai_admin_generated_content`：后台内容生成记录（保存 taskType、prompt、request、output 等）

## 🧪 快速验证建议

- 先调 `POST /api/ai/support/stream`，观察是否能流式返回
- 用同一个 `conversationId` 连续发两次请求，观察多轮对话效果
- 调 `GET /api/ai/support/conversations?page=1&size=10`，确认会话列表可查询
- 调后台 4 个生成接口之一，确认生成内容返回且可在 H2 Console 查看生成记录表
