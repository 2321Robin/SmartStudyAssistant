# SmartStudyAssistant

基于微服务架构的智能学习协作平台，面向学生学习场景，提供用户认证、学习计划生成、学习资源搜索、学习笔记与笔记广场等功能。

## 功能概览

- 用户注册与登录
- 网关统一入口与基础鉴权
- 学习计划生成、任务状态更新和计划删除
- DeepSeek 智能生成学习计划，失败时自动回退本地模板
- 学习资源关键词搜索，展示资源类型、来源、阶段和简介
- 学习笔记创建、私有/公开区分、笔记广场展示
- 无 Docker 本地演示和 Docker Compose 部署配置
- Jenkins 流水线配置

## 技术栈

- 后端：Spring Boot、Spring Cloud Gateway、Maven
- 前端：Vue 3、Vue Router、Vite
- 数据：MySQL（用户服务生产配置）、H2（本地演示）、本地 JSON 文件（计划与笔记演示持久化）
- 工程化：Docker Compose、Jenkins、Git
- 基础设施：Nacos、Redis（Docker Compose 中作为扩展组件预留）
- 智能能力：DeepSeek API（可选）

## 项目结构

```text
backend/
  common/
  gateway-service/
  user-service/
  plan-service/
  resource-service/
  note-service/
ops/
  docker-compose.yml
  mysql/init.sql
  jenkins/Jenkinsfile
web-client/
start-local-demo.ps1
```

## 无 Docker 本地演示

如果没有 Docker Desktop，可以直接在本机运行各个服务。推荐使用一键启动脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\start-local-demo.ps1
```

启动后访问：

- 前端：`http://localhost:5173`
- 网关：`http://localhost:8088`
- `user-service`：`http://localhost:8081`
- `plan-service`：`http://localhost:8082`
- `resource-service`：`http://localhost:8083`
- `note-service`：`http://localhost:8084`

如果需要启用 DeepSeek 生成学习计划，请先设置环境变量：

```powershell
$env:DEEPSEEK_API_KEY='your-key'
```

如果没有设置该变量，系统会自动使用本地模板生成学习计划。

## Docker Compose 轻量部署（低配服务器推荐）

轻量部署不启动 Nacos、Redis、MySQL，`user-service` 使用 H2，`plan-service` 和 `note-service` 使用 Docker volume 保存 JSON 数据，适合 2GB 左右内存的课程演示服务器。

验证 Compose 配置：

```bash
docker-compose -f ops/docker-compose-light.yml config
```

启动服务：

```bash
export SMARTSTUDY_JWT_SECRET='change-this-to-a-long-random-secret'
docker-compose -f ops/docker-compose-light.yml up -d
```

轻量 Compose 模式下访问：

- 前端：`http://服务器IP:4173`
- 网关：`http://服务器IP:8080`

前端容器会通过 `VITE_PROXY_TARGET=http://gateway-service:8080` 将 `/api` 请求转发到网关。主机直跑前端时默认转发到 `http://localhost:8088`。

停止服务：

```bash
docker-compose -f ops/docker-compose-light.yml down
```

## Docker Compose 完整部署

完整部署会额外启动 Nacos、Redis、MySQL，建议服务器内存不少于 4GB。

验证 Compose 配置：

```bash
docker compose -f ops/docker-compose.yml config
```

启动服务：

```bash
docker compose -f ops/docker-compose.yml up -d
```

完整 Compose 模式下访问：

- 前端：`http://localhost:4173`
- 网关：`http://localhost:8080`

停止服务：

```bash
docker compose -f ops/docker-compose.yml down
```

## Jenkins 流水线

流水线文件位于：

```text
ops/jenkins/Jenkinsfile
```

主要阶段包括：

- Backend Test
- Frontend Test
- Package

## 本地数据说明

无 Docker 本地演示会保留部分历史数据：

- `user-service`：`${user.home}/.smartstudyassistant/user-service-local`
- `plan-service`：`.smartstudyassistant/plans.json`
- `note-service`：`.smartstudyassistant/notes.json`

如果需要重置本地演示数据，停止服务后删除上述文件即可。

## 验证命令

后端测试：

```bash
mvn -q -f backend/pom.xml test
```

后端打包：

```bash
mvn -q -f backend/pom.xml package -DskipTests
```

前端测试与构建：

```bash
cd web-client
npm test
npm run build
```
