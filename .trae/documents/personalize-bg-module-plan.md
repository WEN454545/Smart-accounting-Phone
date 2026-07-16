# 个性化背景设置模块实现计划

## Context

智能记账（Smart Bookkeeping）App 已有 5 个页面支持自定义背景图片（个人主页、日历、登录页、主页、记账弹窗），但存在以下问题：

1. **无图片处理基础设施**：所有背景通过 `ImageView.setImageURI()` 直接加载，无采样、无压缩、无缓存，极易因大图导致 OOM
2. **无缩放模式选择**：所有背景统一使用 `centerCrop`，用户无法选择 `fitCenter`、`fitXY` 等模式
3. **主页顶部背景（`iv_home_header_bg`）闲置**：布局已定义但代码从未使用
4. **大量重复代码**：4 个背景设置对话框布局几乎完全相同，ProfileFragment 中 4 组方法高度重复

本次实现将：
- 创建图像处理工具类，提供采样解码、压缩、磁盘缓存能力
- 为主页顶部 header 区域新增背景设置功能，并作为首个使用新工具类的场景
- 提供缩放模式预览和选择功能

## 目标控件信息

| 属性 | 值 |
|------|-----|
| 控件 ID | `iv_home_header_bg` |
| 布局文件 | `app/src/main/res/layout/fragment_home.xml` 第 27-31 行 |
| 父容器 | `FrameLayout`（`layout_width="match_parent"`, `layout_height="wrap_content"`, `clipToOutline=true`） |
| 父容器背景 | `@drawable/bg_gradient_header_semi`（半透明 teal 渐变，底部圆角 28dp） |
| 控件尺寸 | `match_parent` x `match_parent`（跟随父容器） |
| 估算渲染尺寸 | ~1080px × ~400px（约 360dp × 133dp），取决于屏幕宽度和内容 |
| 当前 scaleType | `centerCrop` |
| 当前状态 | **未使用**（HomeFragment 中仅 bindView，但从未加载） |

## 文件清单

### 新建文件

| 文件 | 说明 |
|------|------|
| `app/src/main/java/com/example/myapplication/util/ImageUtils.java` | 图像处理工具类（采样解码、压缩、磁盘缓存） |
| `app/src/main/res/layout/dialog_home_header_bg.xml` | 主页顶部背景设置对话框（含缩放模式预览） |

### 修改文件

| 文件 | 改动说明 |
|------|----------|
| `app/src/main/res/values/strings.xml` | 新增 8 个字符串资源 |
| `app/src/main/res/layout/dialog_personalize.xml` | 新增第 6 个选项「主页顶部背景」 |
| `app/src/main/java/com/example/myapplication/ui/profile/ProfileFragment.java` | 新增常量、launcher、处理方法（约 80 行） |
| `app/src/main/java/com/example/myapplication/ui/home/HomeFragment.java` | 新增 `onResume()`、`loadHomeHeaderBackground()` 方法（约 20 行） |

## 实现步骤

### 第 1 步：创建 ImageUtils.java

**路径**：`app/src/main/java/com/example/myapplication/util/ImageUtils.java`

**核心方法**：

```java
// 1. 按目标尺寸采样解码（两阶段：先读尺寸，再按比例采样）
public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight)

// 2. 等比缩放 + JPEG 压缩
public static Bitmap compressBitmap(Bitmap source, int maxWidth, int maxHeight, int quality)

// 3. URI → MD5 缓存文件名
public static String getCacheKey(Uri uri)

// 4. 写入磁盘缓存（context.getCacheDir()/bg_cache/）
public static void saveToDiskCache(Context context, Bitmap bitmap, String cacheKey)

// 5. 从磁盘缓存读取
public static Bitmap loadFromDiskCache(Context context, String cacheKey)

// 6. 清空磁盘缓存
public static void clearDiskCache(Context context)

// 7. 一站式加载：缓存命中 → 直接设置；未命中 → 后台线程采样→压缩→缓存→主线程设置
public static void loadBackgroundImage(Context context, ImageView imageView, Uri uri, String cacheKey, ImageView.ScaleType scaleType)
```

**设计要点**：
- 使用 `Executors.newSingleThreadExecutor()` 执行后台 IO（与 `BillRepository` 模式一致）
- 默认最大解码尺寸：2048×2048，压缩质量 85%
- 缓存目录：`context.getCacheDir()/bg_cache/`，应用卸载自动清理
- 所有异常静默处理，不 crash，回退到默认背景

### 第 2 步：新增字符串资源

**文件**：`app/src/main/res/values/strings.xml`

新增：
```xml
<string name="profile_home_header_bg">主页顶部背景</string>
<string name="profile_home_header_bg_subtitle">设置首页顶部区域的背景图片</string>
<string name="profile_home_header_bg_setting">主页顶部背景</string>
<string name="home_header_bg_choose">选择背景图片</string>
<string name="home_header_bg_placeholder">(未设置自定义背景)</string>
<string name="home_header_bg_size_hint">推荐选择横向图片，尺寸建议 1080×400 以上</string>
<string name="scale_mode_crop">裁剪填充</string>
<string name="scale_mode_fit">等比缩放</string>
```

### 第 3 步：创建 dialog_home_header_bg.xml

**路径**：`app/src/main/res/layout/dialog_home_header_bg.xml`

参照 `dialog_home_bg.xml` 结构，增强如下：

1. **预览区域**（120dp 高，模拟 header 宽高比 2.7:1）
2. **缩放模式切换器**：两个并排的 `TextView` 按钮（`tv_scale_crop` 和 `tv_scale_fit`），点击切换预览 ImageView 的 `scaleType`
3. **尺寸提示文字**：`"推荐选择横向图片，尺寸建议 1080×400 以上"`
4. **选择图片按钮**：MaterialButton，带图库图标
5. **恢复默认按钮**：红色文字，浅红背景
6. **取消按钮**：纯文字

### 第 4 步：修改 dialog_personalize.xml

在「日历网格背景」选项之后、「取消」按钮之前，新增第 6 个选项：

```xml
<LinearLayout
    android:id="@+id/opt_home_header_bg"
    ... 样式与现有选项保持一致 ...>
    <!-- 图标：🏠 -->
    <!-- 标题：@string/profile_home_header_bg -->
    <!-- 副标题：@string/profile_home_header_bg_subtitle -->
    <!-- 箭头：> -->
</LinearLayout>
```

### 第 5 步：修改 ProfileFragment.java

**a) 新增常量**（第 65 行后）：
```java
private static final String KEY_HOME_HEADER_BG_URI = "home_header_bg_uri";
```

**b) 新增 launcher**（第 122 行后）：
```java
private final ActivityResultLauncher<String[]> pickHomeHeaderBgLauncher = ...
```

**c) 在 `showPersonalizeDialog()` 中绑定新选项**（第 266 行后）：
```java
dialogView.findViewById(R.id.opt_home_header_bg).setOnClickListener(v -> { ... });
```

**d) 新增 `showHomeHeaderBgSettingDialog()` 方法**（约 60 行）：
- 加载预览，支持 scaleType 切换
- 选择按钮 → `pickHomeHeaderBgLauncher.launch(new String[]{"image/*"})`

**e) 新增 `handleHomeHeaderBgPicked(Uri uri)` 方法**（约 20 行）：
- 校验 MIME 类型（仅 `image/*`）
- 保存 URI 到 SharedPreferences
- 使用 ImageUtils 缓存图片

**f) 新增 `resetHomeHeaderBgToDefault()` 方法**（约 10 行）：
- 清除 SharedPreferences
- 清除对应缓存

**g) 在 `clearCache()` 中增加**：
```java
ImageUtils.clearDiskCache(requireContext());
```

### 第 6 步：修改 HomeFragment.java

**a) 新增 `onResume()` 方法**：
```java
@Override
public void onResume() {
    super.onResume();
    loadHomeBackground();
    loadHomeHeaderBackground();
}
```

**b) 新增 `loadHomeHeaderBackground()` 方法**：
```java
private void loadHomeHeaderBackground() {
    String savedUri = profilePrefs.getString("home_header_bg_uri", null);
    if (savedUri != null) {
        try {
            Uri uri = Uri.parse(savedUri);
            String cacheKey = ImageUtils.getCacheKey(uri);
            ImageUtils.loadBackgroundImage(requireContext(), ivHomeHeaderBg, uri, cacheKey, ImageView.ScaleType.CENTER_CROP);
            ivHomeHeaderBg.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            ivHomeHeaderBg.setVisibility(View.GONE);
        }
    } else {
        ivHomeHeaderBg.setVisibility(View.GONE);
    }
}
```

**c) 在 `onCreateView()` 中 `loadHomeBackground()` 后添加调用**：
```java
loadHomeHeaderBackground();
```

### 第 7 步：验证

1. **编码检查**：`python check_all_encoding.py`
2. **编译验证**：`./gradlew clean assembleDebug`
3. **功能测试**：
   - 个性化设置 → 看到「主页顶部背景」选项
   - 点击 → 弹出设置对话框，可切换缩放模式预览
   - 选择图片 → 预览更新，返回首页 → header 显示背景
   - 选择超大图片（4000×3000）→ 不 OOM
   - 重启应用 → 背景持久化
   - 恢复默认 → 清除背景
   - 清除缓存 → 图片缓存被清理