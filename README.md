# 万事屋

「万事屋」包含 Android 客户端与 Go 后端。后端提供 APK 分发、Markdown 内容服务与局域网服务状态监控；Android 端基于 Jetpack Compose 实现今天吃什么、往期题目、设置等页面。

## 功能特性

### 后端（Go）

- **APK 管理与分发**：列出可用 APK、返回版本信息与更新日志，并提供文件下载。
- **Markdown 内容服务**：列出并读取 `outdate-test-markdown/` 目录下的 Markdown 文件。
- **静态文件服务**：以 gzip 压缩透传项目根目录的静态资源。
- **内存缓存**：对读取类数据做 60 秒 TTL 缓存。

### Android 应用

- **Jetpack Compose + Miuix 设计系统** 实现的原生界面。
- **三个主页面**：
  - 今天吃什么 —— 首页转盘加权随机，权重 = 预算 ÷ 价格，支持分享结果。
  - 往期题目 —— 浏览服务端 Markdown 文档（答题解析与历史内容）。
  - 设置 —— 主题、语言、检查更新、食物管理、服务状态、开源许可证等。
- **自动更新**：启动时检查新版本并引导下载安装。
- **主题切换**：浅色 / 深色 / 跟随系统。
- **Markdown 渲染**：题目与内容支持内联 Markdown。

## 项目结构

```
gift/
├── android/              # Android 应用（Kotlin + Jetpack Compose）
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/chronie/gift/
│   │       │   ├── data/            # 数据管理（更新检查、主题、语言、食物、服务监控等）
│   │       │   ├── ui/
│   │       │   │   ├── components/  # 通用 UI 组件
│   │       │   │   ├── navigation/  # 导航（Nav3）与页签 Key
│   │       │   │   └── screens/     # 各页面（FoodScreen / AnswerKeysScreen / SettingsScreen 等）
│   │       │   ├── MainActivity.kt
│   │       │   └── GiftApplication.kt
│   │       └── res/                 # 资源与多语言字符串
│   └── gradle/
├── server-go/            # Go 后端源码与可执行文件
│   ├── main.go           # 服务入口、路由、gzip 中间件
│   ├── handlers.go       # HTTP 处理器（APK、Markdown）
│   ├── cache.go          # 内存缓存与 JSON 读取
│   ├── go.mod
│   └── gift-server.exe   # 预编译可执行文件
├── server/               # 运行数据目录（由 server-go 在启动时读取，../server 相对路径）
│   ├── data/             # 业务数据（见「数据文件」）
│   └── apk/              # 待分发的 APK 文件
└── README.md
```

## 技术栈

### 后端

- **运行时**：Go 1.27.0
- **标准库**：`net/http`、`encoding/json`、`os`、`path/filepath`、`sync`
- **特性**：内置 HTTP 服务、gzip 压缩中间件、带 TTL 的内存缓存（60 秒）

### Android 应用

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Miuix KMP 设计系统
- **关键库**：
  - Ktor Client —— HTTP 请求
  - Coil —— 图片加载
  - Kotlinx Serialization —— JSON 序列化
  - Navigation3 —— 页面导航

## API 接口

所有响应均使用统一信封格式：

```json
{
  "success": true,
  "data": [ ... ]
}
```

### APK 管理

- `GET /api/download_apk` — 获取可用 APK 列表与版本信息。
- `GET /api/download_apk/{filename}` — 下载指定 APK 文件。

`/api/download_apk` 响应额外包含版本信息：

```json
{
  "success": true,
  "data": ["app-release.apk"],
  "latest": "app-release.apk",
  "latestSize": "15.5",
  "versionCode": 100,
  "versionName": "1.0.0",
  "changelog": {
    "en": "Version notes",
    "zh-cn": "版本说明"
  }
}
```

### Markdown 内容

- `GET /api/outdate-test/markdown` — 列出 Markdown 文件。
- `GET /api/outdate-test/markdown/{filename}` — 获取指定 Markdown 文件内容。

## 数据文件（`server/data/`）

| 文件 | 说明 |
|------|------|
| `changelog.json` | 版本更新日志（多语言）。 |
| `outdate-test-markdown/` | Markdown 内容目录。 |

`changelog.json` 示例：

```json
{
  "changelog": {
    "en": "Version notes in English",
    "zh-cn": "中文（简体）版本说明",
    "zh-tw": "中文（繁體）版本說明",
    "ja": "バージョンノート"
  }
}
```

> 服务端仅在启动时读取一次 `outdate-test-markdown/` 与版本元数据，修改数据后需**重启服务**才能生效。

## 配置

### 服务端口

通过环境变量 `PORT` 配置，默认 `3002`：

```bash
PORT=3002 ./gift-server.exe
```

### 安卓端 API 地址

后端地址在 Android 代码中硬编码，默认 `http://192.168.10.9:3002`：

- 更新检查：`UpdateChecker.kt` 的 `apiBaseUrl`
- 服务状态监控：`ServerMonitorApi.kt` 的主机与端口

部署到新环境时，请同步修改这两处常量。

## 构建与运行

### 后端

```bash
cd server-go

# 构建（Windows）
go build -o gift-server.exe

# 启动（默认监听 http://0.0.0.0:3002，数据目录为 ../server）
./gift-server.exe
```

跨平台构建：

```bash
# Linux
GOOS=linux GOARCH=amd64 go build -o gift-server

# macOS
GOOS=darwin GOARCH=arm64 go build -o gift-server
```

> 服务从 `server-go/` 目录读取 `../server/data` 与 `../server/apk`，请在该目录下启动。

### Android 应用

1. 用 Android Studio 打开 `android/` 目录，等待 Gradle 同步完成。
2. 构建并运行到模拟器或真机。
3. 发布 APK 时，将生成的 `app-release.apk` 放入 `server/apk/`，并确认 `server/apk/output-metadata.json` 中的 `versionCode` / `versionName` 已更新，以便更新检查生效。

## 版本管理

### Android 版本号

采用基于时间戳的版本方案：`1.YYYYMMDD.HHMM`（例如 `1.20260203.0031`），构建时按系统时间自动生成。

### 版本信息接口

后端从 `server/apk/output-metadata.json` 读取版本信息：

```json
{
  "elements": [
    {
      "outputFile": "app-release.apk",
      "versionCode": 100,
      "versionName": "1.0.0"
    }
  ]
}
```

Android 端通过 `GET /api/download_apk` 获取该信息并对比自身版本，提示用户更新。
