# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**智能记账 (Smart Bookkeeping)** — an Android bill-tracking app written in Java. Tracks income/expenses with monthly budget management, calendar view, statistical charts, multi-user support, and automatic bill detection via accessibility service.

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
├── MyApplication.java              — Application class, holds singleton BillRepository, creates admin user
├── MainActivity.java               — Single Activity, hosts 4 fragments via BottomNavigation, checks login/session
├── data/
│   ├── entity/
│   │   ├── Bill.java               — Room @Entity: id, type, amount, timestamp, location, note, source, category, userId
│   │   ├── Budget.java             — Room @Entity: yearMonth (PK), totalBudget, createdAt, userId
│   │   └── User.java               — Room @Entity: id, username, password, isAdmin, createdAt
│   ├── dao/
│   │   ├── BillDao.java            — CRUD + LiveData queries (userId-filtered: getBillsBetween, getIncomeBetween, getExpenseBetween, getExpenseByType, getIncomeByType, getDailySum, deleteBillsBefore, deleteAllBills)
│   │   ├── BudgetDao.java          — insert (REPLACE), update, getBudget by yearMonth + userId
│   │   ├── UserDao.java            — CRUD + login, getAdminUser, getAllUsers, getAllUsersSync
│   │   ├── TypeSum.java            — Result POJO: type, total (for pie chart)
│   │   └── DaySum.java             — Result POJO: day, income, expense (for calendar + bar chart)
│   ├── database/
│   │   └── AppDatabase.java        — Room DB singleton, version 2, entities: Bill, Budget, User
│   ├── notification/
│   │   └── BudgetNotificationHelper.java — Budget overrun notifications
│   ├── repository/
│   │   └── BillRepository.java     — Data access layer wrapping DAOs with ExecutorService, includes user management
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
│   │   ├── HomeFragment.java       — Income/expense header (date shown as "yyyy年M月d日"), budget card, recent bill list with swipe-delete + click-edit
│   │   ├── HomeViewModel.java      — Exposes LiveData for current month's bills, income, expense, budget
│   │   ├── AllBillsFragment.java   — All bills view grouped by year-month, with back navigation
│   │   └── AllBillsViewModel.java  — ViewModel for all bills
│   ├── calendar/
│   │   ├── CalendarFragment.java   — LinearLayout-based calendar grid with prev/next month nav, income/expense per day, color-coded cells
│   │   └── CalendarViewModel.java  — Manages month state, uses MediatorLiveData for income/expense/dailySums
│   ├── stats/
│   │   ├── StatsFragment.java      — PieChart + BarChart + TrendBarView with week/month/year period tabs; data from Room
│   │   ├── StatsViewModel.java     — Period enum (WEEK/MONTH/YEAR), uses MediatorLiveData for expenseByType + dailySums
│   │   └── CsvImportPreviewActivity.java — CSV import preview and confirmation
│   ├── profile/
│   │   └── ProfileFragment.java    — Avatar, budget summary, CSV export, dark mode toggle, cache clear, auto-bookkeeping settings entry
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
    ├── CategoryIconHelper.java     — Maps bill category names to PNG drawable icon resources
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
│   ├── CategoryManager.java        — Category CRUD and preferences
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
- **Multi-user isolation** — All bill/budget queries are filtered by userId; SessionManager persists current login session
- **Auto-bookkeeping** — AccessibilityService monitors screen content, matches keywords to categories, extracts amounts, and shows floating confirmation window
- **Two separate Room databases** — main `bill_database` for bills/budgets/users; `autobookkeep_db` for auto-tracked transactions and asset accounts
- **Transaction confirmation window** — uses `showConfirmWindow` as a style dispatcher (standard/island styles); `showStandardConfirmWindow` handles the full standard window logic directly without going through the dispatcher to avoid circular calls
- **Category icons** — [CategoryIconHelper.java](app/src/main/java/com/example/myapplication/util/CategoryIconHelper.java) maps category names to PNG drawable resources; falls back to emoji display when no icon exists