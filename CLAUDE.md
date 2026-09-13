# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

天机学堂（tjxt）在线教育平台，Spring Cloud Alibaba 微服务单体仓库（Maven 多模块聚合）。

- 技术栈：Java 11、Spring Boot 2.7.2、Spring Cloud 2021.0.3、Spring Cloud Alibaba 2021.0.1.0、MyBatis-Plus 3.4.3、Hutool、knife4j(Swagger)、Redisson、xxl-job、Elasticsearch、腾讯云/阿里云 SDK。
- 代码注释与 Git 提交均为简体中文，字段/类命名遵循 Java 驼峰规范，全面使用 Lombok（`@Data`/`@Slf4j`/`@RequiredArgsConstructor`，以构造器注入为主）。
- 统一依赖方向：业务服务 → `tj-api`（传递依赖 `tj-common`）/ `tj-auth-resource-sdk` → `tj-common`。`tj-common` 提供全局自动配置与工具类。

## 构建与运行

构建整个仓库（在根目录）：

```bash
mvn clean install -DskipTests
```

构建并启动单个模块（`-am` 会连带构建它依赖的上游模块，如 tj-common/tj-api）：

```bash
mvn -pl tj-promotion -am package -DskipTests
mvn -pl tj-promotion spring-boot:run
```

运行单元测试（各模块测试极少，散落于 `src/test`，多为集成/支付测试，依赖外部环境，一般不常跑）：

```bash
mvn -pl tj-promotion test
```

注意事项：

- 所有服务的**外部配置依赖 Nacos**（服务注册 + 配置中心），本地需要能连通 Nacos、MySQL、Redis、RabbitMQ 等基础设施。
- 每个服务根目录同时存在 `bootstrap.yml`（默认）+ `bootstrap-dev.yml` + `bootstrap-local.yml`，默认 `spring.profiles.active: dev`。`dev` 指向公司内网 `192.168.150.101`；本地联调建议用 `local` profile（`bootstrap-local.yml`），在启动类 VM 参数加 `-Dspring.profiles.active=local`，或在 IDEA 中指定。
- 各服务通过 Nacos 拉取共享配置（`shared-configs`）：`shared-spring / shared-redis / shared-mybatis / shared-logs`，业务模块再叠加 `shared-feign / shared-mq / shared-xxljob`。数据库名通过 `tj.jdbc.database` 指定。
- 部署方式是 Jenkins 构建 jar → Docker（见根目录 `Dockerfile`/`startup.sh`，容器网络 `heima-net`），日常开发直接在 IDEA 中启动对应 Application 类即可。

## 模块与服务清单

根 `pom.xml` 聚合 16 个模块。其中 `tj-auth`、`tj-message`、`tj-pay` 是父 POM，内部再分子模块（真正的服务在 `*-service` 子模块）。`tj-common`、`tj-api` 为纯 jar 库，无启动类。

| 模块 | 启动类（包根 com.tianji.xxx） | Nacos 服务名 | 端口 | 网关前缀 | 职责 |
|---|---|---|---|---|---|
| tj-gateway | GatewayApplication | gateway-service | 10010 | - | Spring Cloud Gateway 统一入口，路由/跨域/登录鉴权/请求ID透传 |
| tj-auth | tj-auth-service → AuthApplication | auth-service | 8081 | /as | 自研 JWT 认证中心（账号/角色/权限/公钥），含 gateway-sdk、resource-sdk、auth-common |
| tj-user | UserApplication | user-service | 8082 | /us | 用户（学生/老师） |
| tj-course | CourseApplication | course-service | 8086 | /cs | 课程/章节课/分类 |
| tj-search | SearchApplication | search-service | 8083 | /ss | ES 课程搜索 |
| tj-learning | LearningApplication | learning-service | 8090 | /ls | 学习进度、互动问答、签到/积分 |
| tj-remark | RemarkApplication | remark-service | 8091 | /rs | 点赞/收藏 |
| tj-media | MediaApplication | media-service | 8084 | /ms | 媒资/视频点播 |
| tj-message | tj-message-service → MessageApplication | message-service | 8085 | /sms | 站内信/短信 |
| tj-trade | TradeApplication | trade-service | 8088 | /ts | 订单/购物车 |
| tj-pay | tj-pay-service → PayApplication | pay-service | 8087 | /ps | 支付（支付宝/微信） |
| tj-exam | ExamApplication | exam-service | 8089 | /es | 考试 |
| tj-promotion | PromotionApplication | promotion-service | 8093 | /prs | 营销/优惠券/兑换码 |
| tj-data | DataCenterApplication | data-service | 8093* | /ds | 数据统计看板 |

\* `tj-promotion` 与 `tj-data` 的 bootstrap 端口同为 8093，不要同时本地启动。网关中的 `os → order-service` 是历史遗留死路由（无对应服务）。

## 关键架构机制

**认证鉴权（自研 JWT + RSA，非 OAuth2）**：`auth-service` 用 Hutool JWT + RSA 私钥签发 access/refresh token（私钥存 `tjxt.jks`）。网关 `AccountAuthFilter`（`GlobalFilter`）通过 `tj-auth-gateway-sdk` 的 `AuthUtil.parseToken` 从 auth-service 拉取公钥本地验签，再把 userId 写入 `USER_HEADER` 下传给业务服务；业务服务通过 `tj-auth-resource-sdk` 校验登录态。业务代码里用 `tj-common` 的 `UserContext` 取当前登录用户。无需登录的路径在白名单 `tj.auth.resource.excludeLoginPaths` 配置。

**跨服务调用**：`tj-api` 是 Feign Client（`@FeignClient("服务名")`，服务名即 Nacos 注册名）与跨服务共享 DTO 的仓库，client 与 dto 按域分包（注意历史包名拼写 `dto/leanring` 是笔误）。部分 Client 配了 Sentinel fallback。改跨服务字段时注意该模块的 `dto`/`client` 是否被其他服务依赖。

**tj-common 自动配置**（通过 `spring.factories` 注入，无需业务方显式开启）：统一返回 `R<T>` 包装（`WrapperResponseBodyAdvice` + knife4j 开启 `enableResponseWrap` 时生效）、全局异常处理（`CommonExceptionAdvice`）、MyBatis-Plus 字段自动填充（`BaseMetaObjectHandler`）、Redisson 分布式锁 `@Lock` 注解、RabbitMQ 封装（`RabbitMqHelper` + 延迟消息 `DelayedMessageProcessor`，交换机/路由键见 `MqConstants`）、xxl-job 配置。新增此类通用能力放在 tj-common 并注册到 `META-INF/spring.factories`。

**定时任务**：xxl-job（共享配置 `shared-xxljob.yaml` 开启）。每个 `@XxlJob("xxx")` 处理器在 xxl-job 后台配置调度，本地开发注意任务触发依赖 xxl-job 服务。

## 业务模块统一代码布局

业务服务包根均为 `com.tianji.<模块>`，子包风格统一：

```
controller      # REST 接口，/xxx 前缀，Swagger 注解
service/        # 接口
service/impl/   # 实现
mapper/         # MyBatis-Plus Mapper（启动类上 @MapperScan）
domain/po       # 数据库实体（@TableName）
domain/dto      # 前端表单入参（XxxFormDTO）
domain/vo       # 查询出参
domain/query    # 分页/查询条件，继承 tj-common 的 PageQuery
domain/enums    # 业务枚举（实现 tj-common 的 BaseEnum）
config/ constants/ handler/ utils/
```

新建代码请遵循该布局，PO 与数据库表一一对应。

## 当前活跃开发（tj-promotion 优惠券）

最近的 Git 提交（分支 `test-promotion`）集中在 tj-promotion 的营销模块，当前正处于一次**包结构迁移中**：业务类从旧的顶层包 `enums/`、`query/` 迁移到 `domain/enums`、`domain/query`。迁移尚未完成：

- `handler/CouponIssueTask.java` 仍 import 已删除的顶层 `com.tianji.promotion.enums.CouponStatus`，会编译报错；
- `controller/ExchangeCodeController.java`、`service/IExchangeCodeService.java`、`service/impl/ExchangeCodeServiceImpl.java` 引用遗留的顶层 `query` 包（其 `CodeQuery` 与 `domain/query/CodeQuery.java` 内容重复，但顶层副本尚未删除）；
- 另有一个误命名的测试包 `src/test/java/com/baiyang/`，为代码生成器残留，非 `com.tianji` 包。

改动时请优先统一到 `domain/` 子包并清理顶层残留，避免新旧包混用。该模块的营销/发券/兑换码能力结构：`CouponController`/`ExchangeCodeController`（后台管理）、`ICouponService`/`IExchangeCodeService`（发券 `beginIssue`、兑换码异步生成 `asyncGenerateCode`）、`handler/CouponIssueTask`（xxl-job `beginCouponIssueJob`/`endCouponIssueJob` 定时开售/停售）、`config/PromotionConfig`（兑换码线程池）、`utils/CodeUtil`+`Base32`（兑换码生成算法）。
