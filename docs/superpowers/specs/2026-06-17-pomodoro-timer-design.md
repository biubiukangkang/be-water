# 番茄钟「静」设计文档

## 概述

极简唯美风格的 Android 番茄钟应用，使用 Kotlin + Jetpack Compose 开发。核心定位：上手舒适、不同屏幕比例自动适配、沉浸式专注体验。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: 单 Activity + ViewModel + StateFlow
- **数据层**: Room（统计记录）+ DataStore（偏好）
- **后台**: Foreground Service（计时+通知）
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)

## 视觉风格

- **日式侘寂风**: 米白/灰褐/暖灰色调，圆润柔和，大量留白
- **深色/浅色双模式**: 跟随系统自动切换
- **字体**: 极简无衬线数字字体（计时器用），正文使用系统默认字体

### 浅色模式色板

| 用途 | 色值 |
|------|------|
| 背景 | #F5F0EB |
| 卡片/面板 | #FFFFFF |
| 主文字 | #3D3530 |
| 次要文字 | #8B7E74 |
| 主色调 | #D4C5B5 |
| 强调色 | #B8A898 |
| 进度环 | #C4A882 |

### 深色模式色板

| 用途 | 色值 |
|------|------|
| 背景 | #2A2520 |
| 卡片/面板 | #35302A |
| 主文字 | #E8E0D8 |
| 次要文字 | #A09888 |
| 主色调 | #A09080 |
| 强调色 | #C4B0A0 |
| 进度环 | #B8A088 |

## 项目结构

```
com.pomodorotimer/
├── MainActivity.kt
├── PomodoroApp.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── timer/
│   │   ├── TimerScreen.kt
│   │   ├── TimerViewModel.kt
│   │   └── components/
│   │       ├── CircularTimer.kt
│   │       ├── TimeDisplay.kt
│   │       ├── Controls.kt
│   │       └── TomatoDots.kt
│   ├── stats/
│   │   ├── StatsPanel.kt
│   │   └── StatsViewModel.kt
│   └── settings/
│       ├── SettingsPanel.kt
│       └── SettingsViewModel.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── PomodoroRecord.kt
│   │   └── PomodoroDao.kt
│   └── preferences/
│       └── SettingsStore.kt
└── service/
    ├── TimerService.kt
    └── AudioManager.kt
```

## 功能规格

### 计时核心

- 专注时长: 可配置，默认 25 分钟
- 短休: 可配置，默认 5 分钟
- 长休: 可配置，默认 15 分钟
- 长休间隔: 可配置，默认 4 个番茄
- 操作: 开始、暂停、重置
- 阶段自动流转: 专注结束自动切休息，休息结束自动切专注
- 前台服务保持后台计时，锁屏不中断

### 完成计数

- 显示今日已完成番茄数 / 每日目标（默认 8 个）
- 当前第几个番茄的进度点指示器
- 每日目标可在设置中调整

### 通知提醒

- 计时中：通知栏显示剩余时间（静音，IMPORTANCE_LOW）
- 阶段结束：高优先级通知 + 震动 + 提示音
- 适配 Android 13+ 通知权限

### 统计

- 存储每次完成的番茄记录（时间、时长、类型）
- 日/周/月三个维度的完成数量
- 通过 BottomSheet 展示

### 白噪音

- 内置 3-4 种音源：雨声、海浪、篝火、白噪音
- 计时开始时可选自动播放
- MediaPlayer 循环播放，音量独立控制

## 可配置项

| 设置项 | 默认值 | 说明 |
|--------|--------|------|
| 专注时长 | 25 min | 专注阶段倒计时 |
| 短休时长 | 5 min | 短休息倒计时 |
| 长休时长 | 15 min | 长休息倒计时 |
| 长休间隔 | 4 个番茄 | 几个专注后切长休 |
| 每日目标 | 8 个番茄 | 今日目标完成数 |
| 自动切换 | 开 | 阶段结束后自动进入下一阶段 |
| 白噪音 | 关 | 专注时自动播放的音频 |

## 屏幕适配策略

- 基于 BoxWithConstraints 按高度分档（<600dp / 600-800dp / >800dp）
- 竖屏：居中布局，随宽度自动调整字号和间距
- 横屏：左侧计时器 + 右侧统计卡片，充分利用横向空间
- 窄屏（<360dp width）：紧凑模式，按钮缩小，边距收窄
- 宽屏（>420dp width）：宽松模式，留白充分

## 后台计时方案

- Foreground Service 运行计时逻辑
- 每 1 秒发送 tick 广播 / 更新 StateFlow
- 系统杀死后自动恢复（START_STICKY）
- 省电优化：专注期间保持轻量唤醒锁

## 数据模型

### PomodoroRecord (Room Entity)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 自增 ID |
| startTime | Long (timestamp) | 开始时间 |
| duration | Int (seconds) | 实际专注时长 |
| type | String | focus / short_break / long_break |
| completed | Boolean | 是否完成 |

### SettingsStore (DataStore)

- 所有可配置项持久化
- 使用 Flow 读取，Compose 自动响应变化

## 后续计划（本期不做）

- 自定义背景图片
- 专注排行榜 / 社交
- 多语言
- Widget
