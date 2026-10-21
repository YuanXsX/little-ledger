# 小微企业数字化台账管理系统

聚焦小微企业「轻量化、易操作、高安全」需求的数字化台账管理体系，实现台账全流程规范化管理、数据校验自动化、风险预警实时化、数据安全可保障、决策支持智能化、财税咨询智能化。

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + Element Plus + Vite + Vue Router + Axios |
| 后端 | Spring Boot 3.3 + Spring Data JPA + Spring Security + Spring Validation |
| 数据库 | MySQL 8（开发环境内置 H2 内存库，无需安装即可运行） |
| 接口文档 | SpringDoc OpenAPI（`/swagger-ui.html`） |
| AI（后续阶段） | LLM API + 轻量向量库（RAG 财税知识库） |

## 目录结构

```
小微企业/
├── backend/                 # 后端 Spring Boot 工程
│   ├── pom.xml
│   └── src/main/java/com/sme/ledger/
│       ├── LedgerApplication.java   # 启动类
│       ├── common/                  # 统一返回、异常处理
│       ├── config/                  # CORS、安全配置
│       ├── controller/              # 接口层
│       ├── service/                 # 业务逻辑层
│       ├── repository/              # 数据访问层
│       ├── entity/                  # 实体
│       └── dto/                     # 入参/出参 DTO
├── frontend/                # 前端 Vue3 工程
│   └── src/{api,router,views,components}
├── sql/init.sql             # 数据库初始化脚本（核心表 + 默认角色）
└── docs/                    # 项目文档
```

## 快速开始

### 环境要求
- JDK 17+（本机为 JDK 22）
- Maven 3.9+
- Node.js 18+
- MySQL 8（生产环境，开发环境可跳过）

### 1. 启动后端（默认 H2 内存库，无需 MySQL）

```bash
cd backend
mvn spring-boot:run
```

启动后：
- 健康检查：`http://localhost:8080/api/ping`
- 接口文档：`http://localhost:8080/swagger-ui.html`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL 见 `application-dev.yml`）

> 生产/联调 MySQL：先执行 `sql/init.sql`，再以
> `mvn spring-boot:run -Dspring-boot.run.profiles=prod`
> 启动，并通过环境变量 `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD` 配置连接。

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`，前端已配置 `/api` 代理到后端 8080。

## 功能模块（对应需求文档）

1. 企业基础信息模块（资质备案 / 联系人脱敏 / 更新日志）
2. 台账分类管理模块（预设三大台账 / 自定义分类与字段 / 跨台账关联）
3. 数据录入与校验模块（手动 / 批量导入 / 重复校验 / 格式校验）
4. 智能报表模块（三大报表 / 折线·饼图·柱状图 / 筛选与导出）
5. 权限控制模块（四类角色 / 模块+操作细粒度权限 / 二次验证）
6. 数据备份与恢复模块（定时备份 / 失败重试 / 快速·定点恢复）
7. 异常预警模块（库存阈值 / 消息·短信·邮件 / 状态跟踪）
8. 财税 RAG 智能助手（知识库维护 / AI 问答 / 台账摘要异常提示）

## 开发规范

- **分支管理**：禁止直接提交主分支，功能分支开发 → 代码评审 → 合并
- **提交说明**：每次提交填写清晰说明
- **注释率**：源码注释率不低于 50%
- **文档命名**：`文档类型+版本号+日期`

## 待办（脚手架阶段已预留 TODO）

- [ ] 权限控制：接入 JWT 登录、分级角色、细粒度权限校验、连续 3 次失败锁定
- [ ] 敏感数据脱敏存储与展示、字段级加密
- [ ] 定时备份与异常恢复逻辑
- [ ] 库存预警规则与多渠道通知
- [ ] 财税 RAG 知识库与 AI 问答
