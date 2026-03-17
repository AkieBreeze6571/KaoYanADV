---
name: android-build-diagnose
description: Diagnose Android/Gradle build, sync, manifest merge, dependency conflict, minSdk/targetSdk mismatch, AAR/ABI/native libs, resource linking, packagingOptions, R8/Proguard, and Compose/AGP/Kotlin compiler mismatch errors. Use when the user pastes Gradle sync/build output, merge/debug/release task failures, AAPT2/resource not found errors, manifest merger failures, dependency resolution conflicts, or Logcat crash traces.
---

# android-build-diagnose

## 目标
从用户粘贴的 **Gradle/Android 构建日志或 Logcat** 中，提取“首要错误 + 第一条因果链”，判断根因类别，并给出**最小、安全**的修复方案（优先只动必要文件），附带可执行的验证步骤。

## 用户会提供的输入
- 错误日志（Gradle sync/build output 或 Logcat）
- 可选：运行设备/SDK 信息（minSdk/targetSdk，模拟器/真机）

## 工作流程（必须遵守）
1) **只抓主错误与第一条因果链**
   - 优先定位日志里最早出现的 `* What went wrong:` / `FAILURE: Build failed with an exception.` 之后的**第一个**关键错误段。
   - 沿着第一条 `Caused by:` / `Manifest merger failed` / `AAPT2` / `Could not resolve` 的链路向上总结。
   - 不要追逐后续噪声（例如：同一个根因引发的几十个 “资源找不到/符号找不到”）。

2) **归类根因（给出 1 个主类别，必要时 1 个次类别）**
   - minSdk/targetSdk 不匹配（含依赖库要求更高的 minSdk）
   - Manifest merge/权限/组件/占位符（`tools:replace`、`android:exported`、`uses-sdk`、`applicationId`、`provider`）
   - 依赖版本冲突/解析失败（BOM、重复类、`Could not resolve`、`Duplicate class`）
   - 资源/主题/属性缺失（AAPT2、`resource ... not found`、`style attribute ... not found`）
   - ABI/Native libs（`UnsatisfiedLinkError`、缺少 `.so`、ABI 过滤）
   - R8/Proguard（release 才失败、missing class、keep 规则）
   - 打包冲突（`META-INF`、`packagingOptions`、重复文件）
   - Compose/Compiler/AGP/Kotlin 不匹配（compose compiler extension、Kotlin 版本、AGP 版本）
   - 其他：KSP/KAPT、JDK/Gradle 版本不匹配、Windows 路径/编码问题等

3) **明确指出要打开哪些文件，以及要改什么**
   - 必须给出：**文件路径（用正斜杠）+ 需要修改的片段**
   - Android 项目优先关注（按常见优先级）：
     - `app/src/main/AndroidManifest.xml`
     - `app/build.gradle` 或 `app/build.gradle.kts`
     - 根目录 `build.gradle` / `build.gradle.kts`、`settings.gradle` / `settings.gradle.kts`
     - `gradle.properties`
     - `gradle/libs.versions.toml`（若使用 Version Catalog）
     - `app/proguard-rules.pro`

4) **先给 1 个最小修复；必要时再给 1-2 个备选方案（含取舍）**
   - 最小修复必须是：对现状侵入最小、最可能直接解除构建/运行阻断的变更。
   - 备选方案最多 2 个，必须写清 trade-off（例如：升级 AGP 风险、降级依赖可能缺新特性、提高 minSdk 会丢设备覆盖等）。

5) **给出验证步骤**
   - 必须给出具体 Gradle 任务/运行步骤，以及“成功的表现”。
   - 不要声称你已经在本地跑过项目（你没有运行权限）。

## 信息不足时（严格限制）
如果缺少关键信息，**最多追问 1 个问题**。除此之外，使用最保守默认假设继续给出方案。

推荐的唯一追问（按场景择一）：
- 若是 minSdk/targetSdk/设备相关：询问“当前 `minSdk/targetSdk` 是多少，以及失败发生在 debug 还是 release？”
- 若是 Compose/编译器相关：询问“AGP 版本 + Kotlin 版本 + Compose Compiler 版本（或 `composeCompilerExtensionVersion`）？”

## 输出格式（严格，不要增删字段）
- Root cause:
- Evidence (quote key lines from my log):
- Fix (minimal):
  - File(s) to change:
  - Patch snippet:
- Alternatives (optional):
- Verify:
  - Commands/steps:

## 输出内容要求（硬性）
- **Evidence** 必须逐行引用日志关键行（原样摘录，少而准）。
- **Patch snippet** 用可复制的片段表达（Gradle/Manifest/Proguard），并与文件路径一一对应。
- **只改必要文件**：不要做无关重构、不要顺手升级一堆依赖。
- 若建议升级/降级版本：说明为什么、影响面、以及回滚方式（例如锁定版本/使用 constraints/BOM）。

## 常见修复模板（按需选用，别全贴）
### Manifest merge
- 在 `app/src/main/AndroidManifest.xml` 添加 `tools:replace` / `tools:node="remove"` / 补 `android:exported`（Android 12+）。

### 依赖冲突 / Duplicate class
- 在 `app/build.gradle(.kts)` 用 `constraints` / `exclude(group=..., module=...)` 或对齐 BOM。
- 用 `./gradlew :app:dependencies --configuration debugRuntimeClasspath` 定位冲突来源。

### minSdk mismatch
- 最小：选择兼容 minSdk 的库版本或移除该依赖；其次才是提高 `minSdk`。

### AAPT2 / 资源缺失
- 确认资源是否在 `app/src/main/res/`、是否拼写/大小写错误、是否缺主题依赖（Material/Compose）。
- 若是库引入的资源：对齐 Material/AndroidX 版本，或修正 `compileSdk`/`targetSdk`。

### R8/Proguard
- 在 `app/proguard-rules.pro` 加 keep（针对日志中被裁剪的类/反射/序列化）。
- 用 `./gradlew :app:assembleRelease` 或 `:app:minifyReleaseWithR8` 复现并验证。

