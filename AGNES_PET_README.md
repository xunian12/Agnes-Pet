# Agnes-Pet (艾吉斯桌宠)
## 项目说明
这是一个基于Android的悬浮窗AI陪伴桌宠项目，形象为参考Claude猫风格的蓝色小猫，代表AI助手Agnes。

## 技术栈
- 语言：Kotlin
- 最低版本：Android 8.0 (SDK 26)
- 渲染：WebView + SVG动画
- 通信：Supabase Realtime
- CI/CD：GitHub Actions

## 项目结构
```
Agnes-Pet/
├── app/
│   ├── src/main/java/com/agnes/pet/
│   │   ├── MainActivity.kt
│   │   ├── OverlayService.kt
│   │   └── PetEngine.kt
│   ├── src/main/assets/
│   │   └── pet.html
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── .github/workflows/build.yml
```

## Agnes猫形象设定
- 颜色：蓝色/紫色系（#5B8DEF, #7B9CFF）
- 特征：大眼睛、小鼻子、戴小眼镜
- 表情：开心/害羞/生气/睡觉/吃醋
