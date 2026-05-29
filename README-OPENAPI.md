# OpenAPI 契约导出（openapi.json）

本项目已集成 `springdoc-openapi-starter-webmvc-ui`，支持在 **Spring Boot 运行期** 通过反射扫描自动生成 OpenAPI Specification（用于前端代码生成）。

目标产物：

- `openapi.json`：提供给前端（OpenAPI Generator / Swagger Codegen 等）作为接口契约输入

---

## 1. 前置条件

- 已启动本项目（默认端口 `8080`，可通过 `PORT` 环境变量覆盖）
- 导出 OpenAPI 文档本身不依赖大模型 Key：只要应用能启动并暴露 `/v3/api-docs` 即可
- 只有当你需要实际调用 AI 接口时，才需要配置模型平台相关环境变量

```bash
export OPENAI_API_KEY=xxx
export OPENAI_BASE_URL=https://your-provider.example.com/v1
export OPENAI_CHAT_MODEL=xxx
```

启动：

```bash
mvn spring-boot:run
```

---

## 2. 确认 OpenAPI 已生成

SpringDoc 默认提供两个入口：

- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

如果能在 Swagger UI 中看到接口列表，说明扫描生效。

---

## 3. 导出 openapi.json（推荐做法）

在项目根目录（`cloud-learn-exp07/`）执行：

```bash
curl -s http://localhost:8080/v3/api-docs > openapi.json
```

导出后你会得到：

- `cloud-learn-exp07/openapi.json`

如果你希望输出更可读（格式化）：

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool > openapi.json
```

---

## 4. operationId 精确定制（推荐用于“稳定契约”）

### 4.1 默认行为

SpringDoc 通常会基于 Controller 方法名生成 `operationId`。缺点是：

- 代码重构（方法改名）会导致 `operationId` 改变
- 前端代码生成会产生 breaking change

### 4.2 推荐做法：在每个接口上显式声明 operationId

在 Controller 方法上加 `@Operation(operationId = "...")`，例如：

```java
@PostMapping("/course-intro/polish")
@Operation(operationId = "aiAdminPolishCourseIntroduction")
public ApiResponse<AiTextGenerationResponse> polishCourseIntroduction(...) { ... }
```

建议命名规则：

- `aiSupportStreamReply`
- `aiSupportListConversations`
- `aiSupportListMessages`
- `aiAdminPolishCourseIntroduction`
- `aiAdminGenerateChapterSummary`
- `aiAdminGenerateLessonObjectives`
- `aiAdminGenerateFaqDraft`
- `categoryCreate / categoryList / categoryDetail ...`
- `courseCreate / courseCatalog / courseSearch ...`

这样可以保证：

- 前端生成代码的函数名稳定
- 契约更接近“接口设计”，而不是“后端实现细节”

---

## 5. 校验规则（minimum/maxLength 等）如何保留

SpringDoc 会读取 Bean Validation 注解并转换到 OpenAPI Schema 中，例如：

- `@NotNull`
- `@NotBlank`
- `@Size(min=, max=)`
- `@Min` / `@Max`

因此建议：

- 请求体 DTO 上用校验注解表达字段约束
- Controller 入参使用 `@Valid` 触发校验

这些约束会自动出现在 `openapi.json` 的 schema 中，供前端生成更准确的类型和校验提示。

---

## 6. 给前端代码生成使用（示例）

以下以 OpenAPI Generator 为例（仅示意，生成器语言/框架按前端栈选择）：

```bash
openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-axios \
  -o ./frontend-api
```

常见 generator：

- `typescript-axios`
- `typescript-fetch`
- `typescript-angular`

---

## 7. 注意事项

- 流式接口（SSE）`POST /api/ai/support/stream` 的响应是 `text/event-stream`。
  - 部分前端生成器对 SSE 的类型支持有限，前端可选择手写一个 `fetch + ReadableStream` 的调用封装。
- 如果你的项目端口不是 `8080`，请替换导出命令中的端口：

```bash
curl -s http://localhost:${PORT}/v3/api-docs > openapi.json
```
