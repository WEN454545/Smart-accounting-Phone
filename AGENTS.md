# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**智能记账 (Smart Bookkeeping, v2.1.1)** — an Android bill-tracking app written in Java. Tracks income/expenses with monthly budget management, calendar view, statistical charts, multi-user support, and automatic bill detection via accessibility service.

## Hard Constraints

- **ALL source files MUST use UTF-8 encoding.** Never use any other encoding (GBK, GB2312, ISO-8859-1, etc.) when creating or editing files. If a file contains Chinese characters, ensure it is saved as UTF-8. When in doubt, use the Write tool to rewrite the file with explicit UTF-8 content.
- **Before committing, run** `python check_all_encoding.py` to verify no file has encoding issues.
- **Read [ENCODING.md](ENCODING.md) before every task** — contains the full encoding policy, common garbled text causes, and fix procedures.
- **Before every task**: If you suspect encoding issues, run `python fix_encoding.py` to auto-detect and fix all non-UTF-8 files.
- All XML declaration headers must specify `encoding="utf-8"` AND the file must actually be saved as UTF-8 (not just declared).
- All Gradle files must specify `-Dfile.encoding=UTF-8` if JVM args are configured.
- **.gitattributes** enforces UTF-8 for all text files via `* text=auto working-tree-encoding=UTF-8`.

## Build & Run

```bash
./gradlew assembleDebug       # Build debug APK
./gradlew installDebug        # Install to device/emulator
./gradlew test                # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests
./gradlew clean assembleDebug # Clean build
```

Min SDK: 24 | Target/Compile SDK: 34 | Java 17 | ViewBinding enabled

## Architecture (MVVM + Repository)

### Main app (`com.example.myapplication`)

```
app/src/main/java/com/example/myapplication/
├── MyApplication.java              — Application class, holds singleton BillRepository, creates admin user, initializes color scheme theme via ColorSchemeManager
├── MainActivity.java               — Single Activity, hosts 4 fragments (Home/Calendar/Stats/Profile) via BottomNavigation, checks login/session, applies global home background
├── data/
│   ├── entity/
│   │   ├── Bill.java               — Room @Entity: id, type, amount, timestamp, location, note, source, category, userId
│   │   ├── Budget.java             — Room @Entity: yearMonth (PK), totalBudget, createdAt, userId
│   │   └── User.java               — Room @Entity: id, username, password, isAdmin, createdAt
│   ├── dao/
│   │   ├── BillDao.java            — CRUD + LiveData queries (userId-filtered: getBillsBetween, getIncomeBetween, getExpenseBetween, getExpenseByType, getIncomeByType, getDailySum, deleteBillsBefore, deleteAllBills, renameCategory)
│   │   ├── BudgetDao.java          — insert (REPLACE), update, getBudget by yearMonth + userId
│   │   ├── UserDao.java            — CRUD + login, getAdminUser, getAllUsers, getAllUsersSync
│   │   ├── TypeSum.java            — Result POJO: type, total (for pie chart)
│   │   └── DaySum.java             — Result POJO: day, income, expense (for calendar + bar chart)
│   ├── database/
│   │   └── AppDatabase.java        — Room DB singleton, version 2, entities: Bill, Budget, User
│   ├── notification/
│   │   └── BudgetNotificationHelper.java — Budget overrun notifications
│   ├── repository/
│   │   └── BillRepository.java     — Data access layer wrapping DAOs with ExecutorService, includes user management and category rename sync
│   ├── NotificationSettings.java   — Notification rule configuration
│   └── SessionManager.java         — Login session persistence (SharedPreferences)
├── ui/
│   ├── auth/
│   │   ├── SplashActivity.java     — Launch screen with video/dynamic background
│   │   ├── LoginActivity.java      — Username/password login, admin account creation
│   │   ├── UserManageActivity.java — Admin user management (add/edit/delete users)
│   │   ├── DynamicBackgroundView.java — Animated gradient background
│   │   └── ScalableVideoView.java  — Scaled video background player
│   ├── home/
│   │   ├── HomeFragment.java       — Income/expense header with theme-aware gradient + year-month picker, budget card, day-grouped recent bill list (DaySectionedBillAdapter) with swipe-delete + click-edit, FAB add bill, settings icon (iv_settings) launches AutoSettingsActivity for auto-bookkeeping
│   │   ├── HomeViewModel.java      — Exposes LiveData for current month's bills, income, expense, budget
│   │   ├── AllBillsFragment.java   — All bills view grouped by year-month, with back navigation
│   │   └── AllBillsViewModel.java  — ViewModel for all bills
│   ├── calendar/
│   │   ├── CalendarFragment.java   — LinearLayout-based calendar grid with prev/next month nav, income/expense per day, color-coded cells; calendar grid + selected-day bill list scroll together in a NestedScrollView (page-level scrolling, top month bar fixed)
│   │   └── CalendarViewModel.java  — Manages month state, uses MediatorLiveData for income/expense/dailySums
│   ├── stats/
│   │   ├── StatsFragment.java      — PieChart + BarChart + TrendBarView with week/month/year period tabs; data from Room
│   │   ├── StatsViewModel.java     — Period enum (WEEK/MONTH/YEAR), uses MediatorLiveData for expenseByType + dailySums
│   │   └── CsvImportPreviewActivity.java — CSV import preview and confirmation (columns: date,type,category,amount,note; category optional - empty category auto-inferred from built-in note keyword table, then history vote, fallback Other)
│   ├── profile/
│   │   └── ProfileFragment.java    — Avatar (with crop via CropImageActivity), username display, CSV import/export + template download, user management entry (admin), budget notification settings (with warning percent), background personalization (home/calendar/login/dialog-bill/home-header/profile targets), transaction style picker (standard/island), color scheme picker (Teal Sakura/Lavender Dream/Ocean Mint), cache clear, logout
│   ├── crop/
│   │   ├── CropImageActivity.java  — Image cropping activity for avatars/backgrounds
│   │   └── CropImageView.java      — Custom crop view with pinch-to-zoom and rotation
│   ├── adapter/
│   │   ├── BillAdapter.java        — RecyclerView.Adapter with OnBillClickListener, emoji/PNG icons, color-coded amounts
│   │   ├── SectionedBillAdapter.java — Grouped bill list adapter with section headers by year-month
│   │   ├── DaySectionedBillAdapter.java — Day-grouped bill list adapter
│   │   └── CsvImportPreviewAdapter.java — CSV import preview with row selection
│   ├── dialog/
│   │   └── AddBillDialog.java      — DialogFragment for add/edit, includes date picker, supports prefill for editing
│   └── view/
│       └── TrendBarView.java       — Custom trend bar chart view
└── util/
    ├── CategoryIconHelper.java     — Category icon registry: custom icon map -> 10 legacy PNG defaults -> emoji fallback (30-icon neutral pool incl. 20 vector icons, new icons unbound by default)
    ├── ColorSchemeManager.java     — Color scheme switching (Teal Sakura / Lavender Dream / Ocean Mint) with persistence; provides theme resId, primary/primary-dark colors per scheme
    └── ImageUtils.java             — Image scaling, cropping, and format conversion utilities
```

### Auto-bookkeeping subsystem (`com.example.autobookkeep`)

```
app/src/main/java/com/example/autobookkeep/
├── BackupManager.java              — Data backup and restore
├── PermissionUtils.java            — Runtime permission helpers
├── database/
│   ├── AppDatabase.java            — Separate Room DB "autobookkeep_db" (version 1)
│   ├── Transaction.java            — Auto-tracked transaction entity
│   ├── AssetAccount.java           — Payment asset account entity (WeChat, Alipay, etc.)
│   ├── TransactionDao.java         — Transaction CRUD
│   └── AssetAccountDao.java        — Asset account CRUD
├── ui/
│   ├── AutoSettingsActivity.java   — Auto-bookkeeping main settings
│   ├── BudgetSettingsActivity.java — Monthly budget management
│   ├── AssistantManagerActivity.java — Assistant/voice assistant settings
│   ├── CategorySettingsActivity.java — Category preference settings
│   ├── AutoTrackLogActivity.java   — Accessibility service scan log viewer
│   ├── CategoryAdapter.java        — Category list adapter
│   └── CustomHighlightEditText.java — Custom EditText with text highlighting
├── util/
│   ├── AssistantConfig.java        — Assistant feature configuration
│   ├── AutoAssetManager.java       — Asset account auto-management
│   ├── AutoTrackLogManager.java    — Scan log management
│   ├── AssetSpinnerAdapter.java    — Asset dropdown spinner adapter
│   ├── CategoryManager.java        — Category CRUD, preferences, custom icon mapping and rename migration
│   ├── CurrencyUtils.java          — Currency formatting and conversion
│   └── KeywordManager.java         — Screen keyword matching for auto-categorization
├── viewmodel/
│   └── FinanceViewModel.java       — Financial data ViewModel for auto-bookkeeping
└── widget/
    └── WidgetUtils.java            — Desktop widget utilities
```

### Accessibility service

```
app/src/main/java/com/google/android/accessibility/selecttospeak/
└── SelectToSpeakService.java       — AccessibilityService for screen auto-tracking; detects payment pages (WeChat, Alipay, Pinduoduo, JD, UnionPay) and extracts transaction info
```

## Key Dependencies

- **Room** — local SQLite ORM (bills + budgets + users tables in main DB; transactions + asset accounts in autobookkeep DB)
- **MPAndroidChart** (v3.1.0 via JitPack) — pie and bar charts in stats
- **Material Components** — BottomNavigationView, FAB, themes
- **Lifecycle (ViewModel + LiveData)** — MVVM architecture; all data flows through ViewModel → Fragment
- **CardView** — floating window cards for transaction confirmation
- **FlexboxLayout** — category grid layout
- **Glide** — image/GIF/video loading for backgrounds and avatars
- **Navigation** — fragment navigation framework

## Color System (colors.xml)

| Token | Hex | Usage |
|-------|-----|-------|
| primary | #0d9488 | Teal — buttons, active states |
| primary_dark | #0f766e | Status bar, gradient end |
| income | #22c55e | Income amounts |
| expense | #ef4444 | Expense amounts |
| bg | #f0f4f8 | Main background |
| surface | #ffffff | Cards |
| ink | #1e293b | Primary text |
| muted | #64748b | Secondary text |
| rule | #e2e8f0 | Dividers |

Category colors (cat_food, cat_transport, cat_shopping, cat_entertainment, cat_housing, cat_medical, cat_income, cat_other, cat_travel, cat_communication, cat_social, cat_education, cat_beauty, cat_transfer, cat_redpacket, cat_refund) are pastel backgrounds for icon circles.

## Important Patterns

- **Fragments never access DAOs directly** — go through `MyApplication.getRepository()`
- **Fragments create ViewModels** via `new ViewModelProvider(this).get(XxxViewModel.class)`
- **Room queries return LiveData** — Fragments observe, never poll
- **Write operations** (insert/update/delete) go through Repository's ExecutorService, off the main thread
- **Bill editing** — click a bill item in the list to open the dialog pre-filled; swipe left/right to delete
- **Budget** — click the budget card on Home to open a set-budget dialog; stored per yearMonth
- **MediatorLiveData for dynamic queries** — CalendarViewModel and StatsViewModel use MediatorLiveData to wrap data sources that change when month/period changes; old source is removed before adding new one to avoid stale observers
- **Calendar uses nested LinearLayout** — CalendarFragment builds the calendar grid using nested LinearLayouts (vertical rows + horizontal columns) with layout_weight for equal distribution; more reliable than GridLayout for dynamic content
- **Calendar page-level scrolling** - fragment_calendar.xml wraps the calendar card, selected-day bar and bill list in a NestedScrollView (fillViewport); the top month bar stays fixed; rv_day_bills uses wrap_content + nestedScrollingEnabled=false so bills flow with the page scroll
- **Category rename sync** - renaming a category in CategorySettingsActivity updates historical bills for ALL users via BillDao.renameCategory (through BillRepository.renameCategory) and migrates the custom icon mapping to the new name; deleting a category clears its orphan icon mapping
- **Icon change refresh** - icon changes only touch SharedPreferences (no DB write), so bill LiveData does not re-emit; Home/AllBills/Calendar fragments rebind icons via adapter.notifyDataSetChanged() in onResume
- **CSV import encoding auto-detection** - CsvImportPreviewActivity reads the whole file first, tries a strict UTF-8 decode and falls back to GB18030 (superset of GBK) because CSVs saved by Excel on Chinese Windows are GBK; note: keep doc files UTF-8, the Edit tool may re-encode whole files as GBK
- **Multi-user isolation** — All bill/budget queries are filtered by userId; SessionManager persists current login session
- **Auto-bookkeeping** — AccessibilityService monitors screen content, matches keywords to categories, extracts amounts, and shows floating confirmation window
- **Two separate Room databases** — main `bill_database` for bills/budgets/users; `autobookkeep_db` for auto-tracked transactions and asset accounts
- **Transaction confirmation window** — uses `showConfirmWindow` as a style dispatcher (standard/island styles); `showStandardConfirmWindow` handles the full standard window logic directly without going through the dispatcher to avoid circular calls
- **Category icons** — [CategoryIconHelper.java](app/src/main/java/com/example/myapplication/util/CategoryIconHelper.java) binding priority: user custom icon map (CategoryManager, key_category_icon_map) -> 10 legacy PNG defaults -> emoji fallback; the 20 newer vector icons (ic_cat_housing, ic_cat_pet, ...) form a neutral pool with NO default binding - a category shows a custom icon only after explicit selection in CategorySettingsActivity
- **Color scheme switching** — ColorSchemeManager persists selected scheme (Teal Sakura / Lavender Dream / Ocean Mint) in SharedPreferences; MyApplication loads theme resId at startup; MainActivity calls setTheme(MyApplication.getThemeResId()) before super.onCreate
- **Background personalization** — ProfileFragment lets users pick image/video backgrounds for multiple screens (home, calendar, login, dialog-bill, home-header, profile); URIs stored in SharedPreferences (profile_settings) keys like home_bg_uri, cal_bg_uri, login_bg_uri, dialog_bill_bg_uri, home_header_bg_uri
- **Transaction confirmation style** — SharedPreferences key transaction_style ("standard" / "island") controls confirmation window style; SelectToSpeakService reads via ProfileFragment.STYLE_STANDARD / STYLE_ISLAND constants
- **Year-month picker** — HomeFragment date header is clickable; launches dialog_year_month_picker.xml dialog to switch the viewed month (replaces only month navigation previously on calendar)