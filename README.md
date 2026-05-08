# 实践教程5：基于 Spring Data JPA 的数据持久层实践

## 📖 业务背景

欢迎来到“智学云在线学习平台”的第五次实践。本次实践的重点是：把课程与分类这些真实业务数据，从“程序里的对象”变成“数据库里可持久保存、可查询、可更新的数据”，并且通过 Spring Data JPA 建立一套规范的数据访问层。

为什么选择 Spring Data JPA？

- **面向对象建模**：先定义实体，再由框架帮助完成对象与数据表之间的映射，更贴近业务思维。
- **减少重复 SQL**：大量常规的增删改查、分页、排序、条件查询，都可以通过仓库接口快速完成。
- **天然适配 Spring Boot**：配合自动配置、事务管理、参数校验、统一异常处理，能快速形成完整后端项目。
- **逻辑分层**：实体、仓库、服务、控制器职责清晰，适合学生建立规范的后端工程意识。

本次实践本地使用 **H2 文件型数据库**。这样做的目的是让同学把注意力集中在“JPA 如何连接数据库、如何映射实体、如何完成数据访问”上，而不是被数据库安装和环境配置分散注意力。和内存数据库不同，文件型 H2 在应用停止后数据依然保留，更适合反复观察与实验。

---

## 🎯 学习目标

- 掌握 Spring Boot 项目中 Spring Data JPA 的基本使用方式
- 理解实体类、数据表、Repository、Service、Controller 之间的关系
- 掌握多对一、一对多、自关联等典型 JPA 关系映射
- 理解 `CrudRepository`、`PagingAndSortingRepository`、`JpaRepository` 三类接口的特点与适用场景
- 理解为什么在真实项目中 JPA 与直接 SQL 往往会并存
- 学会通过 H2 Console、日志输出和接口调用来验证数据库操作结果
- 建立“先建模、再映射、再访问、再验证”的数据层开发思维

---

## ✅ 你将完成的功能

围绕“课程（Course）”和“分类（Category）”两类核心资源，完成一组典型的数据持久层实践：

- 分类新增：`POST /api/categories`
- 分类列表：`GET /api/categories`
- 分类详情：`GET /api/categories/{categoryId}`
- 分类树查询：`GET /api/categories/tree`
- 分类删除：`DELETE /api/categories/{categoryId}`
- 课程新增：`POST /api/courses`
- 课程目录浏览（分页 + 排序）：`GET /api/courses/catalog`
- 课程后台检索：`GET /api/courses`
- 按教师关键词查询课程：`GET /api/courses/teachers?teacherKeyword=张`
- 单个课程上下架：`PATCH /api/courses/{courseId}/publish`
- 批量课程上下架：`PATCH /api/courses/batch-publish`
- SQL 榜单查询：`GET /api/courses/sql/hot?limit=5`
- SQL 分类调价：`PATCH /api/courses/sql/category-price?categoryId=3&delta=10`
- 分类 QBE 查询：`GET /api/categories/qbe?name=Java`
- 课程 QBE 查询：`GET /api/courses/qbe?title=spring&teacher=张`
- 课程 Specification 查询：`GET /api/courses/spec?minPrice=50&maxPrice=200&published=true`

---

## 🧠 本次实践重点不是“会用”，而是“会选”

很多同学学 JPA 时，容易把注意力全部放在注解和方法名上。但本次实验更重要的目标，是理解：

- 什么场景适合最基础的 CRUD
- 什么场景天然需要分页与排序
- 什么场景需要更强的 JPA 能力
- 什么场景直接写 SQL 反而更合理

所以本项目刻意保留了四种数据访问思路，并将它们放在同一个业务系统中对比演示。

---

## 🧭 推荐完成顺序


在开始动手之前，先对项目结构有一个整体认识：

```text
cloud-learn-exp05/

├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/cs/sbs/web/
    │   │   ├── config/                    # 启动初始化数据
    │   │   │   └── DataInitializer.java
    │   │   ├── controller/                # 控制层：对外提供 HTTP 接口
    │   │   │   ├── CategoryController.java
    │   │   │   ├── CourseController.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── dto/                       # 请求对象、响应对象、统一返回结构
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── CategoryCreateRequest.java
    │   │   │   ├── CategoryResponse.java
    │   │   │   ├── CourseBatchPublishRequest.java
    │   │   │   ├── CourseCreateRequest.java
    │   │   │   ├── CoursePageResponse.java
    │   │   │   ├── CourseQueryRequest.java
    │   │   │   ├── CourseResponse.java
    │   │   │   ├── CourseSqlViewResponse.java
    │   │   │   └── SqlUpdateResponse.java
    │   │   ├── entity/                    # JPA 实体：对象与数据表映射
    │   │   │   ├── Category.java
    │   │   │   └── Course.java
    │   │   ├── repository/                # 数据访问层：四种访问方式并存
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── CourseCatalogRepository.java
    │   │   │   ├── CourseJdbcRepository.java
    │   │   │   └── CourseRepository.java
    │   │   ├── service/                   # 业务编排与规则控制
    │   │   │   ├── CategoryService.java
    │   │   │   ├── CourseService.java
    │   │   │   └── impl/
    │   │   │       ├── CategoryServiceImpl.java
    │   │   │       └── CourseServiceImpl.java
    │   │   └── Application.java           # Spring Boot 启动类
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/cs/sbs/web/
            ├── CloudLearnExp05ApplicationTests.java
            └── RepositoryAccessPatternTests.java
```

### IDEA 与 Apifox 调试指引

- 建议下载安装 [Apifox](https://apifox.com/)，它把接口文档、接口调试、Mock、自动化测试整合在一个平台里，适合课堂实验和本地联调。
- 本项目也引入了 SpringDoc，启动后可以配合 Swagger 页面理解接口结构，再用 Apifox 组织请求、保存测试用例、反复验证返回结果。
- 如果你使用 IDEA，建议把 `Application` 设置为直接运行入口，把 H2 Console 和接口请求一起作为调试窗口使用。

### 第 1 步：理解项目依赖与运行环境

任务项：

- 阅读 `pom.xml`，确认本项目基于 `Spring Boot 3.5.11`。
- 识别本项目的核心依赖：Web、Data JPA、Validation、H2、SpringDoc。
- 明确本项目的 JDK 版本为 `Java 21`。

完成标准：

- 能说清每个核心依赖大致负责什么。
- 知道本项目是一个“Web + JPA + H2”的完整后端工程，而不是单纯的数据库演示。

提示：

- `spring-boot-starter-data-jpa` 是本次实验最核心的依赖，它让 Spring Boot 具备 JPA 持久化能力。

### 第 2 步：启动 Spring Boot 项目

任务项：

- 确认项目存在 `Application` 启动类。
- 启动项目，观察控制台是否成功完成容器启动、JPA 初始化和 H2 连接。

完成标准：

- 能成功启动项目。
- 服务监听在 `8082` 端口。
- 启动时不会出现数据源、实体扫描或仓库代理生成失败的错误。

验证方式：

- 访问 `http://localhost:8082/`，至少不是连接失败。
- 观察控制台中是否出现 Spring Boot 启动完成信息。

### 第 3 步：认识实体类，理解 JPA 映射的起点

任务项：

- 阅读 `Category` 与 `Course` 两个实体类。
- 理解普通字段、主键字段、时间字段、关联字段分别承担什么职责。
- 理解为什么分类支持树形结构，为什么课程归属于一个分类。

完成标准：

- 能说清“对象”和“数据表”之间的映射关系。
- 能解释课程与分类之间为什么是多对一。
- 能解释分类为什么既有父分类，又有子分类集合。

提示：

- 学 JPA 的第一步不是写查询，而是理解实体建模。

### 第 4 步：理解 H2 数据库配置

任务项：

- 阅读 `application.yml` 中的数据源配置。
- 关注数据库 URL、用户名、密码、H2 Console 开关、JPA 配置、日志配置。
- 明确本项目使用的是文件型 H2，而不是内存型 H2。

完成标准：

- 知道数据库连接信息。
- 知道为什么项目重启后数据仍然存在。
- 知道 SQL 日志为什么会在控制台输出。

重点理解：

- 文件型 H2 的价值是方便课堂上多次启动和观察数据变化。
- `ddl-auto: update` 的作用是让实体变化能自动同步表结构，但它主要适合开发学习环境。

### 第 5 步：理解启动初始化数据

任务项：

- 阅读 `DataInitializer` 的职责说明。
- 观察项目启动后，是否已经自动生成分类和课程数据。
- 理解为什么教学项目常常需要初始化样例数据。

完成标准：

- 启动后无需手工录入，就能直接调用查询接口看到数据。
- 能理解“初始化数据”在课堂实验中的价值。

提示：

- 没有初始化数据，很多接口测试都要先做前置录入，学习节奏会被打断。

### 第 6 步：先掌握最基础的 Repository 能力

任务项：

- 理解 `CategoryRepository` 为什么选择 `CrudRepository`。
- 思考分类管理为什么主要是基础增删改查，而不强调复杂分页。
- 观察分类相关业务包括哪些典型数据操作：新增、查询、删除、存在性判断。

完成标准：

- 能说明 `CrudRepository` 更适合基础维护型业务。
- 能理解“能完成基本工作”与“功能最强”并不是同一个概念。

本步重点：

- 先建立“不是所有仓库都必须一上来就用最复杂接口”的意识。

### 第 7 步：理解分页与排序为什么单独抽出来

任务项：

- 理解 `CourseCatalogRepository` 为什么使用 `PagingAndSortingRepository`。
- 把它与课程目录浏览场景对应起来。
- 思考为什么“列表展示”与“后台管理”是两个不同视角。

完成标准：

- 能说清分页和排序为什么是课程目录最核心的需求。
- 能理解为什么目录浏览只保留“已发布课程”这一业务含义。

本步重点：

- 课程目录是面向用户的展示型场景，核心是“看列表”，不是“做复杂管理”。

### 第 8 步：理解后台管理为什么更适合 JpaRepository

任务项：

- 理解 `CourseRepository` 为什么继承 `JpaRepository`。
- 观察它承担了哪些能力：课程后台检索、按教师名查询、批量上下架、复杂条件组合查询。
- 理解 `JpaSpecificationExecutor` 为什么有价值。

完成标准：

- 能说明为什么后台管理业务通常比前台目录更复杂。
- 能理解“高级查询 + 批量操作 + 动态条件”是 JPA 更擅长的典型场景。

本步重点：

- `JpaRepository` 不是为了“看起来高级”，而是为了适应更复杂的后台业务。

### 第 9 步：理解为什么项目还要保留直接 SQL

任务项：

- 理解 `CourseJdbcRepository` 负责的是什么业务。
- 把“热门课程榜单”“分类统一调价”“调价前检查”与 SQL 场景联系起来。
- 思考为什么这些场景直接写 SQL 更自然。

完成标准：

- 能说明 JPA 与 SQL 的边界。
- 能理解“真实项目中常常不是二选一，而是配合使用”。

本步重点：

- 会选工具，比只会单一工具更接近真实开发。

### 第 10 步：理解 Service 层如何组合多种仓库

任务项：

- 阅读 `CategoryService` 与 `CourseService` 暴露的业务能力。
- 理解 Service 层不只是“中转站”，而是负责业务规则。
- 观察删除分类、批量上下架、SQL 调价这些场景中，服务层承担了哪些校验工作。

完成标准：

- 能说清业务规则为什么不应该直接写在 Controller 中。
- 能理解“数据访问”和“业务决策”是两件不同的事。

示例思考：

- 分类为什么不能在仍有子分类或课程时直接删除？
- 调价为什么要先检查是否可能出现负数价格？

### 第 11 步：理解 Controller 如何对外暴露持久化能力

任务项：

- 阅读分类接口与课程接口的路径设计。
- 理解哪些接口更偏“管理后台”，哪些接口更偏“目录浏览”。
- 理解请求参数、请求体、路径参数分别承担什么职责。

完成标准：

- 能根据接口名和路径，大致判断它背后调用的是哪类业务。
- 能把接口调用与 JPA/SQL 行为建立联系。

提示：

- 学习持久层时，不要只停留在数据库层，也要理解它最终如何通过 Web 接口被使用。

### 第 12 步：观察异常处理与统一响应

任务项：

- 理解统一响应结构为什么能让前端更容易处理结果。
- 理解全局异常处理器为什么能让控制层更专注。
- 观察分类重复、资源不存在、参数不合法时，系统如何给出一致反馈。

完成标准：

- 能理解“接口可用”不只是成功返回，还包括错误时是否可读、可定位、可调试。

---

## 🧪 快速验证（建议同学逐步验证）

启动项目：

```bash
mvn spring-boot:run
```

如果启动成功，可以继续做以下验证：

- 访问课程目录接口，确认分页数据可返回
- 访问分类树接口，确认分类层级结构存在
- 打开 H2 Console，确认数据库表和数据都已生成
- 观察控制台，确认 Hibernate 打印了 SQL 与参数绑定日志

建议重点验证以下几个问题：

- 新增分类后，数据库是否真的多了一条记录
- 删除分类时，如果仍有关联数据，系统是否会阻止操作
- 课程目录接口在不同页码、不同排序字段下，结果是否变化
- 批量上下架后，课程的发布状态是否同步变化
- SQL 调价后，价格是否被批量修改

---

## 🌐 本地访问建议

项目启动后，建议学生优先访问以下入口：

- 应用端口：`http://localhost:8082`
- H2 Console：`http://localhost:8082/h2-console`

常用数据库连接信息：

- JDBC URL：`jdbc:h2:file:./data/cloudlearnx;AUTO_SERVER=TRUE`
- 用户名：`sa`
- 密码：`password`

学习建议：

- 调用一次接口，就去 H2 Console 看一次数据变化
- 看一次数据变化，再回控制台看一次 SQL 日志

这样最容易把“接口层、业务层、持久层、数据库层”真正连起来。

---

## 🧩 本次实践中四种数据访问方式的教学含义

为了帮助学生建立“按场景选方案”的思维，本次实践把四类方案拆开演示：

### 1. `CrudRepository`

适合：

- 结构简单
- 操作直接
- 以基础维护为主的业务

本项目中对应：

- 分类新增、查询、删除、存在性判断

### 2. `PagingAndSortingRepository`

适合：

- 前台列表展示
- 必须分页
- 经常排序

本项目中对应：

- 已发布课程目录浏览

### 3. `JpaRepository`

适合：

- 后台管理
- 派生查询
- 批量保存
- 更丰富的 JPA 能力扩展

本项目中对应：

- 课程检索、教师关键词查询、批量上下架

### 4. 直接 SQL

适合：

- 榜单
- 批量更新
- 明确的统计或报表型查询
- 需要更直观表达式的场景

本项目中对应：

- 热门课程榜单
- 分类统一调价

---

## 🔍 同一组查询需求：方法名派生 vs QBE vs Specification

很多同学会问：“我已经会写方法名派生查询了，为什么还要学 QBE 和 Specification？”最好的理解方式，是把它们放到同一组需求里对比。

### 这组查询需求（同一个业务问题）

在课程检索场景中，我们希望能支持这样的筛选组合：

- 按标题关键词筛选（模糊匹配）
- 按教师关键词筛选（模糊匹配，忽略大小写）
- 按发布状态筛选（精确匹配）
- 按价格区间筛选（范围查询）
- 支持分页与排序（例如按 `id/price/lessonCount` 排序）

你会发现：这是典型的“后台检索页”需求——条件多、组合多、并且经常变化。

### 方式一：方法名派生查询（声明式查询语句）

在本项目中，你已经能看到方法名派生的典型用法（例如教师关键词查询，对应接口 `GET /api/courses/teachers`）。

它适合：

- 条件固定且很少变化
- “一个方法 = 一个常用查询”
- 团队希望简单直观、快速落地

它的瓶颈在于：

- 一旦组合条件变多，你要么写很多方法名，要么开始纠结方法名怎么拼
- 当需求出现“可选条件 + 组合变化”时，可维护性会迅速下降

### 方式二：QBE（Query By Example）

QBE 适合“输入什么字段就按什么字段过滤”的低耦合筛选场景：你只要构建一个带部分字段的“示例对象”，再配一个匹配器（例如：字符串包含、忽略大小写），框架就会自动把它翻译成查询条件。

在本项目中，QBE 对应两个入口：

- 分类 QBE：`GET /api/categories/qbe`
- 课程 QBE：`GET /api/courses/qbe`

示例入参（建议同学直接复制到浏览器或 Apifox）：

- 分类 QBE：`GET /api/categories/qbe?name=Java&description=入门`
- 课程 QBE：`GET /api/courses/qbe?title=spring&teacher=张&published=true&page=0&size=5`

它的优势在于：

- 不需要写 SQL/JPQL
- 不需要为每种组合写一个方法名
- 需求小改动时通常只改“示例对象 + 匹配规则”，仓库接口更稳定

它的边界也很清晰：

- 更擅长“等值/字符串匹配”这类条件
- 对“范围查询、复杂 OR、跨表联查、聚合统计”等场景表达力有限

所以你会看到：本项目让 QBE 覆盖“低耦合筛选”，而不是强行用它解决所有查询问题。

### 方式三：Specification（JpaSpecificationExecutor + Criteria API）

Specification 的定位是：当查询条件复杂、组合多、并且你需要更强的表达能力时，用它来动态构造查询。

在本项目中，它对应：

- `GET /api/courses/spec`

示例入参（建议同学先从 3～5 个条件开始）：

- `GET /api/courses/spec?titleKeyword=spring&teacherKeyword=陈&published=true&minPrice=50&maxPrice=200&page=0&size=5&sortBy=price&direction=asc`

它适合：

- 多条件组合（条件可以任意缺省）
- 范围查询（价格区间、课时区间）
- 更细粒度的控制（例如对不同字段使用不同匹配策略）
- 可持续扩展的“后台检索页”

你需要付出的代价是：

- 写法比 QBE 更“工程化”，但换来的是更强的可控性与可扩展性

### 如何在真实项目里做选择

- 条件固定、业务常用：优先方法名派生查询
- 条件可选、只做字符串/等值匹配：优先 QBE
- 条件复杂、包含范围/组合/扩展性要求：优先 Specification


---

## 🧪 数据库事务实践：观察现象、理解原理

本项目提供了一组“事务实验台”接口（统一前缀：`/api/tx-lab`）。建议同学按顺序调用，并且配合三件事一起观察：

- 接口返回结果（success/message/data）
- 控制台 SQL 日志（是否真的执行了 insert/update、是否提交）
- H2 Console（最终数据是否落库）

下面每个实验都给出三件信息：要观察什么现象、为什么会这样、它对应事务概念中的哪一项。

### 1）传播行为（propagation）

#### A. REQUIRED：同一事务里“要么都成功，要么都失败”

接口：`GET /api/tx-lab/propagation/required`

- 时间线（从上到下按时间推进）：

```text
时间 →

Controller(无事务)         Outer Tx (REQUIRED)                          Inner Tx
-----------------------------------------------------------------------------------------
调用 outer.requiredRollbackDemo()
                          BEGIN Tx-A
                          saveAndFlush(outer)  -> INSERT 已执行，但未 COMMIT（仍在 Tx-A）
                          调用 inner.requiredFailRuntime()
                                                                       加入 Tx-A（不新开）
                                                                       抛 RuntimeException
                          Tx-A 标记为 ROLLBACK
                          ROLLBACK Tx-A（outer 插入撤销）
返回 Controller
existsAfterCall = existsByName(outerName) -> false
```

- 要观察什么
  - 接口会返回内部抛出的异常信息
  - `existsAfterCall` 预期为 `false`（外层插入也被回滚）
- 为什么会这样
  - `REQUIRED` 表示“加入当前事务”：内层方法不会开新事务，而是复用外层事务
  - 内层抛出运行时异常会把整个事务标记为回滚
- 对应概念
  - `@Transactional(propagation = Propagation.REQUIRED)`（默认值）

#### B. REQUIRES_NEW：内层独立事务，失败不会拖垮外层

接口：`GET /api/tx-lab/propagation/requires-new`

- 时间线（从上到下按时间推进）：

```text
时间 →

Controller(无事务)         Outer Tx (REQUIRED)                          Inner Tx (REQUIRES_NEW)
-----------------------------------------------------------------------------------------------
调用 outer.requiresNewDemo()
                          BEGIN Tx-A
                          saveAndFlush(outer) -> INSERT 已执行，但未 COMMIT（仍在 Tx-A）
                          调用 inner.requiresNewInsertAndFail()
                          SUSPEND Tx-A
                                                                       BEGIN Tx-B（新事务）
                                                                       saveAndFlush(inner) -> INSERT 属于 Tx-B
                                                                       抛 RuntimeException
                                                                       ROLLBACK Tx-B（inner 插入撤销）
                          RESUME Tx-A
                          catch 异常，方法正常结束
                          COMMIT Tx-A（outer 插入生效）
返回 Controller
outerExistsAfterCall = true
innerExistsAfterCall = false
```

- 要观察什么
  - `outerExistsAfterCall` 预期为 `true`
  - `innerExistsAfterCall` 预期为 `false`
- 为什么会这样
  - `REQUIRES_NEW` 会挂起外层事务并开启新事务
  - 内层事务失败只回滚自己的部分；外层事务继续提交
- 对应概念
  - `@Transactional(propagation = Propagation.REQUIRES_NEW)`

#### C. NOT_SUPPORTED：挂起事务，用“非事务方式”执行

接口：`GET /api/tx-lab/propagation/not-supported`

- 时间线（从上到下按时间推进）：

```text
时间 →

Controller(无事务)         Outer Tx (REQUIRED)                          Inner(非事务 NOT_SUPPORTED)
-----------------------------------------------------------------------------------------------
调用 outer.notSupportedVisibilityDemo()
                          BEGIN Tx-A
                          saveAndFlush(outer) -> INSERT 已执行，但未 COMMIT（仍在 Tx-A）
                          调用 inner.countByNameNotSupported()
                          SUSPEND Tx-A
                                                                       非事务查询 countByName
                                                                       读不到 Tx-A 未提交数据 -> 返回 0
                          RESUME Tx-A
                          COMMIT Tx-A（outer 插入生效）
返回 Controller
countByNameDuringNotSupported = 0
existsAfterCall = true
```

- 要观察什么
  - `countByNameDuringNotSupported` 预期为 `0`（内层查询看不到外层尚未提交的数据）
  - `existsAfterCall` 预期为 `true`（外层事务最后提交）
- 为什么会这样
  - 外层虽然执行了写入并 flush，但事务未提交前对其他“事务/非事务视角”通常不可见
  - `NOT_SUPPORTED` 会挂起当前事务，内层以非事务方式执行查询
- 对应概念
  - `@Transactional(propagation = Propagation.NOT_SUPPORTED)`

### 2）回滚规则（rollbackFor / noRollbackFor）

事务里“抛异常是否回滚”并不是简单的“抛了就回滚”。默认规则是：

- 运行时异常（RuntimeException / Error）默认回滚
- 受检异常（checked exception）默认不回滚

#### A. 受检异常默认不回滚（默认提交）

接口：`GET /api/tx-lab/rollback/checked-default`

- 要观察什么
  - 接口返回中会记录抛出了受检异常
  - `existsAfterCall` 预期为 `true`（数据仍然落库）
- 为什么会这样
  - Spring 默认把“业务可预期的受检异常”视为不需要回滚的情况（可通过配置改变）
- 对应概念
  - `@Transactional` 默认回滚策略

#### B. rollbackFor：显式指定“遇到受检异常也要回滚”

接口：`GET /api/tx-lab/rollback/checked-rollback-for`

- 要观察什么
  - 接口返回中会记录抛出了受检异常
  - `existsAfterCall` 预期为 `false`
- 为什么会这样
  - 通过 `rollbackFor` 把某类受检异常也纳入回滚集合
- 对应概念
  - `@Transactional(rollbackFor = SomeCheckedException.class)`

#### C. noRollbackFor：显式指定“遇到运行时异常也不回滚”

接口：`GET /api/tx-lab/rollback/no-rollback-for`

- 要观察什么
  - 接口会捕获到运行时异常
  - `existsAfterCall` 预期为 `true`
- 为什么会这样
  - 通过 `noRollbackFor` 把某类运行时异常从回滚集合中排除
- 对应概念
  - `@Transactional(noRollbackFor = SomeRuntimeException.class)`

### 3）readOnly / timeout：事务的“使用姿势”

#### A. readOnly：提示优化“我只读，不修改”

接口：`GET /api/tx-lab/read-only`

- 要观察什么
  - `existsAfterCall` 的结果可能因 JPA Provider 与 flush 行为不同而出现差异
  - 建议同学用 H2 Console 验证最终是否落库
- 为什么会这样
  - `readOnly=true` 的核心意义是“提示框架优化”，并不等价于“绝对禁止写入”
  - 是否真正禁止写，取决于底层数据库与 ORM 的实现策略
- 对应概念
  - `@Transactional(readOnly = true)`

#### B. timeout：事务超时后通常会回滚

接口：`GET /api/tx-lab/timeout`

- 要观察什么
  - 接口可能返回事务超时相关异常（不同环境异常类型可能不同）
  - `existsAfterCall` 通常为 `false`（事务被判定超时并回滚）
- 为什么会这样
  - 事务超时相当于“给这段业务加了时间红线”，超过就中断并回滚，避免长事务占用连接与锁
- 对应概念
  - `@Transactional(timeout = N)`

### 4）隔离级别（isolation）：并发下读到的数据“有多稳定”

这一组实验会在两个线程里模拟：

- A 事务：读同一条课程记录两次（firstRead / secondRead）
- B 事务：在中间把课程标题改掉并提交

#### A. READ_COMMITTED：可能出现“不可重复读”

接口：`GET /api/tx-lab/isolation/read-committed`

- 要观察什么
  - `firstRead` 与 `secondRead` 可能不同（第二次读到了 B 已提交的新值）
- 为什么会这样
  - READ_COMMITTED 保证“不读到别人未提交的数据”，但允许读到“别人已提交的新数据”
- 对应概念
  - `@Transactional(isolation = Isolation.READ_COMMITTED)`（或数据库默认等价行为）

#### B. REPEATABLE_READ：倾向保证“同一事务内多次读一致”

接口：`GET /api/tx-lab/isolation/repeatable-read`

- 要观察什么
  - `firstRead` 与 `secondRead` 倾向相同（读到同一份快照）
  - 注意：不同数据库对 REPEATABLE_READ 的实现差异较大，同学要以“现象 + 原因解释”为主
- 为什么会这样
  - REPEATABLE_READ 通常会让同一事务在多次读取时看到一致视图，从而减少不可重复读
- 对应概念
  - `@Transactional(isolation = Isolation.REPEATABLE_READ)`（或数据库支持的等价级别）

---


## 📝 建议的课后练习

为了真正掌握这次实验内容，建议你在项目跑通之后继续完成下面的练习。

### 练习一：扩展字段

给课程增加“难度等级”或“课程状态描述”等新字段，并思考：

- 这些字段应加在实体、DTO、接口的哪些层次
- 哪些查询场景需要跟着调整

### 练习二：扩展查询

设计新的课程筛选条件，例如：

- 按价格区间查询
- 按课时区间查询
- 按是否发布与教师名称联合筛选

### 练习三：扩展 SQL 场景

尝试增加新的直接 SQL 统计能力，例如：

- 各分类下课程数量统计
- 各教师名下课程数量统计
- 已发布课程平均价格统计

### 练习四：迁移数据库

尝试把 H2 切换为 MySQL，并思考：

- 哪些配置要改
- 哪些业务代码基本不需要改
- 为什么 JPA 可以降低数据库切换成本

---

## 📌 本次实践的核心结论

学完这一节后，你最应该记住的不是某几个注解名称，而是下面这套思维方式：

- 先从业务对象出发设计实体
- 再用 JPA 建立对象与数据表之间的映射
- 再根据业务场景选择合适的 Repository 能力
- 再把业务规则放进 Service 层
- 最后通过接口、日志和数据库三方共同验证结果
