# Render 部署说明

本文档用于说明如何将当前仓库部署到 Render 免费版，并通过 GitHub Actions 在 `push main` 后自动触发部署。

## 1. 当前方案

- Web Service：Render Java Web Service
- 数据库：Render Postgres（免费版）
- 部署触发：GitHub Actions 调用 Render Deploy Hook
- 仓库结构：实际 Spring Boot 项目位于 `cloud-learn-exp05`

## 2. 本仓库已补充的文件

- `render.yaml`
- `.github/workflows/render-deploy.yml`
- `src/main/resources/application-prod.yml`

## 3. 第一次部署前要做的事

### 3.1 推送代码到 GitHub

先把整个仓库推送到你的 GitHub 仓库，并确保默认部署分支为 `main`。

### 3.2 在 Render 中创建 Blueprint

1. 登录 [Render](https://render.com/)
2. 点击 `New` -> `Blueprint`
3. 连接你的 GitHub 仓库
4. 选择 `main` 分支
5. 使用仓库根目录下的 `render.yaml`
6. 确认创建以下资源：
    - 一个名为 `cloud-learn-exp05` 的 Web Service
    - 一个名为 `cloud-learn-exp05-db` 的 Postgres 数据库

说明：

- `render.yaml` 已经把项目根目录设置为 `cloud-learn-exp05`
- Render 会为数据库自动注入主机、端口、库名、用户名、密码等环境变量

## 4. 环境变量说明

以下环境变量已经通过 `render.yaml` 自动配置：

- `SPRING_PROFILES_ACTIVE=prod`
- `JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

Render 会自动提供：

- `PORT`

应用在生产环境下会自动读取这些变量，因此不需要你手工再到 Spring Boot 配置里写死。

## 5. GitHub Actions 自动部署配置

当前工作流文件为：

- `.github/workflows/render-deploy.yml`

它的执行流程是：

1. 当代码 `push` 到 `main` 分支时触发
2. 在 GitHub Actions 中使用 Java 21
3. 执行：

   `mvn -B -ntp test -f cloud-learn-exp05/pom.xml`

4. 测试通过后，调用 Render Deploy Hook 触发部署

## 6. 你还需要在 GitHub 中手工补的 Secret

你还需要在 GitHub 仓库中添加一个 Secret：

- `RENDER_DEPLOY_HOOK_URL`

添加路径：

- GitHub 仓库 -> `Settings` -> `Secrets and variables` -> `Actions` -> `New repository secret`

## 7. 如何获取 Render Deploy Hook

1. 打开 Render 中已经创建好的 `cloud-learn-exp05` Web Service
2. 进入 `Settings`
3. 找到 `Deploy Hook`
4. 创建一个新的 Deploy Hook
5. 复制该 URL
6. 把它保存到 GitHub Secret `RENDER_DEPLOY_HOOK_URL`

## 8. 为什么这里把 Render 自动部署关掉

在 `render.yaml` 中，Web Service 设置了：

- `autoDeploy: false`

这是为了避免两套自动触发机制同时生效：

- GitHub push 后 Render 自己部署一次
- GitHub Actions 测试通过后再调用 Deploy Hook 部署一次

现在的设计是：

- Render 负责托管服务和数据库
- GitHub Actions 负责“先测试、后部署”

## 9. 生产环境配置说明

当前生产环境使用：

- `application-prod.yml`

它和本地开发的区别是：

- 使用 PostgreSQL 驱动，不再使用 H2
- 关闭 H2 Console
- 降低 SQL 日志级别
- 读取 Render 提供的数据库环境变量
- 端口改为读取 `PORT`

## 10. 推荐的首次验证步骤

首次部署成功后，建议按下面顺序验证：

1. 打开 Render 服务页面，确认构建成功
2. 打开服务访问地址，确认应用已启动
3. 访问：

   `/api/categories`

   确认接口有返回

4. 打开日志，确认数据库连接成功
5. 再观察 Render Postgres 中是否已写入初始化数据

## 11. 本地与生产的区别

本地开发：

- 使用 H2 文件数据库
- 主要配置在 `application.yml`

Render 生产：

- 使用 PostgreSQL
- 主要配置在 `application-prod.yml`

因此本地开发和线上部署互不冲突。

## 12. 后续建议

如果后续你希望更像真实项目，建议继续补充：

- `application-dev.yml`
- `application-test.yml`
- Flyway 或 Liquibase 数据库版本管理
- Actuator 健康检查端点
- 更严格的生产日志策略
