# Encoding Rules

## 硬性规定

- **所有项目源码文件必须使用 UTF-8 编码**，不得使用 GBK、GB2312、ISO-8859-1 等其他编码格式。
- 所有 XML 声明头必须指定 `encoding="utf-8"`，且文件实际编码必须为 UTF-8。
- 所有新的 `.java`、`.xml`、`.gradle`、`.properties`、`.py` 文件创建时必须保存为 UTF-8。
- `.gitattributes` 已配置 `* text=auto working-tree-encoding=UTF-8`，确保 Git 正确处理编码。

## 编码检查

### 提交前检查

每次提交前运行：
```bash
python check_all_encoding.py
```

该脚本会扫描所有源码文件，报告编码异常（GBK 编码、乱码字符等）。

### 修复编码问题

如果发现编码问题，运行修复脚本：
```bash
# 修复整个项目
python fix_encoding.py

# 修复单个文件
python fix_encoding.py "app\src\main\java\com\google\android\accessibility\selecttospeak\SelectToSpeakService.java"
```

`fix_encoding.py` 会自动检测并转换以下情况：
1. 直接 GBK 编码的文件 → 转为 UTF-8
2. 双重编码损坏的文件（GBK → Latin-1 → UTF-8 误存）→ 还原为正确 UTF-8
3. 已经是正确 UTF-8 的文件 → 跳过

## 常见编码乱码原因

| 现象 | 原因 | 修复方式 |
|------|------|----------|
| 中文显示为乱码（如 `鏅鸿兘璁拌处`） | 文件以 GBK 保存，IDE 以 UTF-8 打开 | `fix_encoding.py` |
| 中文显示为问号 `?` | 文件损坏或双重编码错误 | `fix_encoding.py` |
| XML 报 encoding 错误 | XML 声明与文件实际编码不一致 | 确保 XML 声明为 `utf-8`，文件也存为 UTF-8 |

## 注意事项

- 每次创建或修改包含中文的文件后，务必确认文件保存为 UTF-8 编码。
- 不要使用记事本默认保存（记事本默认使用 ANSI/GBK），推荐使用 VS Code 或 Android Studio 等 IDE。
- Java 源文件中的中文注释和字符串常量同样受此规则约束。
- AGENTS.md 和 CLAUDE.md 中关于编码的硬性约束具有最高优先级。

## AI 工具环境编码陷阱（2026-08-02 实战经验）

### 根本原因

在 Windows 中文系统上，AI 编码助手（Codex/Claude）的 Write / Edit 工具执行文件写入时，**系统默认编码为 GBK**。当中文或 emoji 字符通过工具写入文件时，会被自动转换为 GBK 字节存储，但文件声明的是 `encoding="utf-8"`，导致：

- **XML 文件**：AAPT2 解析时报 encoding 错误，Mac 构建失败
- **Java 文件**：javac 编译中文字符串时报 "unmappable character" 错误
- **显示层面**：IDE 打开时中文和 emoji 显示为乱码（mojibake）、问号 `?` 或替换字符 `�`

### 受影响字符类型

| 字符类型 | 示例 | 损坏表现 | GBK 存储特点 |
|----------|------|----------|--------------|
| 中文字符 | 智能记账 | `鏅鸿兘璁拌处`（mojibake） | GBK 双字节被当作 Latin-1 解释为 UTF-8 |
| 人民币符号 ¥ | ¥0.00 | `?0.00`（问号） | U+00A5 在 GBK→UTF-8 转换中丢失 |
| Emoji 表情 | [paint][card][memo] | `�9�6` `�9�8`（替换字符） | Emoji（4 字节 UTF-8）在 GBK 中无对应，被破坏 |
| 特殊符号 | └ ← → | `�6�9` `��`（替换字符） | 制表符/箭头在 GBK 中不完整 |

### 解决方法优先级

#### 方法一：运行 fix_encoding.py（最彻底）

```bash
python fix_encoding.py
```

脚本直接读取文件原始字节，检测 GBK→UTF-8，做真正的字节级编码转换。

**注意**：在 PowerShell 执行策略受限的环境中，可能需要管理员权限：
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
python fix_encoding.py
```

#### 方法二：XML NCR 格式（ASCII 安全，适合 XML 文件）

将中文字符转为 XML 数字字符引用（NCR），纯 ASCII 无编码风险：

```xml
<!-- 原始（会被破坏） -->
<TextView android:text="智能记账" />

<!-- NCR 格式（ASCII 安全） -->
<TextView android:text="&#x667A;&#x80FD;&#x8BB0;&#x8D26;" />
```

常用转换：
- `取消` → `&#x53D6;&#x6D88;`
- `确定` → `&#x786E;&#x5B9A;`
- `¥` → `&#xA5;`
- [paint] → `&#x1F3A8;`

Android 的 XML 解析器自动将 NCR 还原为 Unicode 字符，运行时无差异。

#### 方法三：Java Unicode 转义（ASCII 安全，适合 Java 文件）

```java
// 原始（会被破坏）
String text = "智能记账";
Toast.makeText(this, "已记账", Toast.LENGTH_SHORT).show();

// Unicode 转义（ASCII 安全）
String text = "\u667A\u80FD\u8BB0\u8D26";
Toast.makeText(this, "\u5DF2\u8BB0\u8D26", Toast.LENGTH_SHORT).show();
```

Emoji 需要代理对（surrogate pair）：
- [money] → `\uD83D\uDCB0`（收入图标）
- [card] → `\uD83D\uDCB3`（支出图标）
- `¥` → `\u00A5`（人民币符号）

### 快速排查清单

遇到编码错误时，按以下顺序排查：

1. **运行 `python check_all_encoding.py`** — 扫描全项目编码状态
2. **搜索 `??` 模式** — `\?\?` 出现在 Java/XML 文件中表示字符损坏
3. **搜索 `¥` 符号** — 检查是否被破坏为 `?`，改用 `&#xA5;` 或 `\u00A5`
4. **搜索 emoji 残留** — 搜索 `\uFFFD`（替换字符）或 `�` 字符
5. **检查 XML 声明头** — 确保 `encoding="utf-8"` 与实际编码一致
6. **运行 `python fix_encoding.py`** — 自动修复所有检测到的问题

### 已修复的文件清单（2026-08-02）

以下文件曾出现 GBK 编码问题，已通过 NCR/Unicode 转义修复：

**XML 文件（NCR 格式）：**
- `res/values/strings.xml` — 全部 200+ 条目
- `res/layout/window_confirm_transaction.xml` — 记账确认弹窗（标准样式）
- `res/layout/window_confirm_transaction_island.xml` — 记账确认弹窗（灵动岛样式）
- `res/layout/dialog_bg_setting.xml` — 背景设置弹窗
- `res/layout/dialog_personalize.xml` — 个性化设置弹窗
- `res/layout/dialog_transaction_style.xml` — 交易提醒样式选择
- `res/layout/dialog_permission_request.xml` — 权限请求弹窗
- `res/layout/dialog_notification_settings.xml` — 通知设置弹窗
- `res/layout/item_bill.xml` — 账单列表项
- `res/layout/activity_user_manage.xml` — 用户管理页
- `res/menu/bottom_nav_menu.xml` — 底部导航菜单
- `res/values/colors.xml` — 颜色定义
- 以及其他 8 个布局文件

**Java 文件（Unicode 转义格式）：**
- `SelectToSpeakService.java` — 无障碇服务核心（30+ 处修复）
- `HomeFragment.java` — 首页（22 处修复）
- `AllBillsFragment.java` — 全部账单（11 处修复）

## Edit 工具导致的编码损坏（2026-08-10 实战经验）

### 问题现象

使用 Edit 工具修改 Java 文件时，在字符串中使用了 `¥` 符号，导致该字符被破坏为乱码字符，如 `¥` 变成了 `�0�6`。同时，AGENTS.md、CLAUDE.md、ENCODING.md 等 .md 文件也因 Edit 工具的写入而被转换为 GBK 编码。

### 根本原因

Edit 工具在写入文件时，与 Write 工具一样，在 Windows 中文系统上会受系统默认 GBK 编码影响。任何非 ASCII 字符（包括中文、`¥`、Emoji、特殊符号）都可能在 Edit 工具写入时被破坏。

### 临时修复方法

1. 运行 `python check_all_encoding.py` 扫描全项目
2. 对于 Java 文件：将字符串中的非 ASCII 字符替换为 Unicode 转义（如 `¥` → `\u00A5`）
3. 对于 XML 文件：将非 ASCII 字符替换为 NCR 格式（如 `¥` → `&#xA5;`）
4. 对于 .md /.py 文件：使用 Python 脚本进行 GBK→UTF-8 转换：
   ```python
   data = open('file.md', 'rb').read()
   text = data.decode('gbk')
   open('file.md', 'w', encoding='utf-8').write(text)
   ```

### 预防措施

**使用 Edit 工具修改文件时，始终遵循以下规则：**

1. **禁止在 old_string/new_string 中使用非 ASCII 字符**——包括中文、`¥`、Emoji、特殊符号
2. **Java 文件中的非 ASCII 字符必须用 Unicode 转义**（如 `\u00A5`）
3. **XML 文件中的非 ASCII 字符必须用 NCR 格式**（如 `&#xA5;`）
4. **修改完成后立即运行 `python check_all_encoding.py`** 验证无编码问题

### fix_encoding.py 的局限性

`fix_encoding.py` 的 `looks_like_valid_chinese()` 函数需要文件中包含至少 3 个常见中文字符（如的、了、是）才会尝试修复。对于主要为英文内容、仅含少量中文的 .md 文件（如 AGENTS.md、CLAUDE.md），可能无法自动检测和修复。此时需要手动转换：
```python
data = open('file.md', 'rb').read()
text = data.decode('gbk')
open('file.md', 'w', encoding='utf-8').write(text)
```
