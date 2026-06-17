# Be Water 🍅

> 极简侘寂风番茄钟 — Like water, my friend.

一款 Android 番茄钟应用，采用日式侘寂（Wabi-sabi）美学设计，支持深色/浅色模式、横竖屏自适应。

## 特性

- **专注计时** — 25 分钟专注 + 5 分钟短休 / 15 分钟长休，循环切换
- **白噪音** — 内置雨声、海浪、篝火、白噪音四种环境音
- **统计追踪** — 每日/周完成番茄数统计
- **自定义设置** — 专注/休息时长、每日目标、自动开始、白噪音选择
- **深色模式** — 自动跟随系统，也可手动切换
- **横竖屏自适应** — 手机横屏时自动切换沉浸式布局
- **前台通知** — 计时器在后台持续运行，通知栏实时显示剩余时间
- **轻量** — 正式版 APK 仅 2.2MB

## 截图

| 竖屏 | 横屏 |
|------|------|
| ![竖屏](screenshots/portrait.png) | ![横屏](screenshots/landscape.png) |

> 截图目录 `screenshots/` 需自行添加

## 下载

[GitHub Releases](https://github.com/biubiukangkang/be-water/releases) 页面下载最新 APK。

## 构建

```bash
# 克隆
git clone https://github.com/biubiukangkang/be-water.git

# 构建正式版 APK
cd be-water
export JAVA_HOME="path/to/jdk-17"
export ANDROID_HOME="path/to/sdk"
./gradlew assembleRelease

# APK 在 app/build/outputs/apk/release/app-release.apk
```

### 前置要求

- JDK 17+
- Android SDK (platform 34, build-tools 34+)
- Gradle 8.5 (wrapper 已包含)

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material3
- **架构**: MVVM (ViewModel + StateFlow)
- **持久化**: Room (统计) + DataStore (设置)
- **后台**: Foreground Service (计时器通知)
- **构建**: Gradle 8.5 + AGP 8.2.0

## 许可

MIT
