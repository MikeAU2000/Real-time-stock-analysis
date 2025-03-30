# 实时股票分析系统

这是一个基于前后端分离架构的实时股票分析系统，提供股票数据的实时展示和分析功能。

## 技术栈

### 前端技术栈
- Vue 3
- TypeScript
- Vite
- Chart.js (用于图表展示)
- Pinia (状态管理)
- Vue Router
- Vitest (单元测试)
- Playwright (E2E测试)

### 后端技术栈
- Spring Boot 3.4.3
- Java 17
- PostgreSQL
- Redis
- Spring Data JPA
- Spring Data Redis
- Lombok
- Jackson

## 项目结构

```
.
├── stock_frontend_project_latest/    # 前端项目
│   ├── src/                         # 源代码
│   ├── public/                      # 静态资源
│   ├── dist/                        # 构建输出
│   └── e2e/                         # E2E测试
└── demo-bc-xfin-service-latest/     # 后端项目
    └── src/                         # 源代码
```

## 开发环境要求

- Node.js (推荐最新LTS版本)
- Java 17
- Maven
- PostgreSQL
- Redis

## 运行说明

### 前端项目

1. 进入前端项目目录：
```bash
cd stock_frontend_project_latest
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 构建生产版本：
```bash
npm run build
```

### 后端项目

1. 进入后端项目目录：
```bash
cd demo-bc-xfin-service-latest
```

2. 使用Maven构建项目：
```bash
mvn clean install
```

3. 运行应用：
```bash
mvn spring-boot:run
```

## 测试

### 前端测试
- 运行单元测试：
```bash
npm run test:unit
```
- 运行E2E测试：
```bash
npm run test:e2e
```

### 后端测试
```bash
mvn test
```

## 功能特性

- 实时股票数据展示
- 股票图表分析
- 数据缓存
- 响应式设计
- 完整的测试覆盖

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License