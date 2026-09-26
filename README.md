# 绝希 (Smart Bookkeeping)

一款 Android 记账应用，使用 Java 编写。支持收支记录、月度预算管理、日历视图、统计图表、多用户隔离，并可通过无障碍服务实现支付页面自动记账。

## 功能特性

- **收支记账** — 手动记账（支持日期选择、备注、分类），账单点击可编辑、滑动可删除
- **自动记账** — 基于无障碍服务监测微信、支付宝、拼多多、京东、云闪付等支付页面，自动提取金额并弹出确认窗口；支持按历史备注关键词学习推荐分类
- **账单导入** — 支持 CSV 与微信/支付宝导出的 XLSX 账单文件导入（自动识别编码、自动解析时间，支持导入预览与勾选）
- **预算管理** — 按月设置总预算，超支通知提醒
- **日历视图** — 按日展示收支与账单明细
- **统计图表** — 饼图 / 柱状图 / 趋势图，支持周 / 月 / 年切换
- **多用户** — 多账户隔离，管理员可管理其他用户
- **个性化** — 亮色主题、三套配色方案、多页面自定义背景、桌面小组件

## 技术栈

- Java 17，Min SDK 24 / Target SDK 34
- MVVM + Repository 架构（ViewModel + LiveData）
- Room 本地数据库（主库 bills/budgets/users + 自动记账库 transactions/asset_accounts）
- MPAndroidChart 图表、Material Components、Glide
- 无障碍服务（AccessibilityService）实现自动记账

## 构建方法

```bash
./gradlew assembleDebug    # 构建 debug APK
./gradlew installDebug     # 安装到设备/模拟器
```

release 签名配置从 `local.properties`（不入库）读取：

```properties
smartbook.storeFile=../smartbook.jks
smartbook.storePassword=你的密码
smartbook.keyAlias=你的别名
smartbook.keyPassword=你的密码
```

未配置签名时仍可正常构建 debug 版本，release 版本将不签名。

## 目录结构

```
app/src/main/java/
├── com/example/myapplication/     # 主应用（UI / 数据层 / 仓库）
├── com/example/autobookkeep/      # 自动记账子系统（设置 / 分类学习 / 资产账户）
└── com/google/android/accessibility/selecttospeak/  # 无障碍服务
```

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
