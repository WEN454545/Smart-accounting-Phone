# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**智能记账 (Smart Bookkeeping)** — an Android bill-tracking app written in Java. Tracks income/expenses with monthly budget management, calendar view, and statistical charts.

## Hard Constraints

- **ALL source files MUST use UTF-8 encoding.** Never use any other encoding (GBK, GB2312, ISO-8859-1, etc.) when creating or editing files. If a file contains Chinese characters, ensure it is saved as UTF-8. When in doubt, use the Write tool to rewrite the file with explicit UTF-8 content.
- **Before committing, run** `python check_all_encoding.py` to verify no file has encoding issues.
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

```
app/src/main/java/com/example/myapplication/
├── MyApplication.java              — Application class, holds singleton BillRepository
├── MainActivity.java               — Single Activity, hosts 4 fragments via BottomNavigation
├── data/
│   ├── entity/
│   │   ├── Bill.java               — Room @Entity: id, type, amount, timestamp, location, note, source, category
│   │   └── Budget.java             — Room @Entity: yearMonth (PK), totalBudget, createdAt
│   ├── dao/
│   │   ├── BillDao.java            — CRUD + LiveData queries (getBillsBetween, getIncomeBetween, getExpenseBetween, getExpenseByType, getDailySum)
│   │   ├── BudgetDao.java          — insert (REPLACE), update, getBudget by yearMonth
│   │   ├── TypeSum.java            — Result POJO: type, total (for pie chart)
│   │   └── DaySum.java             — Result POJO: day, income, expense (for calendar + bar chart)
│   ├── database/
│   │   └── AppDatabase.java        — Room DB singleton, version 1
│   └── repository/
│       └── BillRepository.java     — Data access layer wrapping DAOs with ExecutorService
└── ui/
    ├── home/
    │   ├── HomeFragment.java       — Income/expense header (date shown as "yyyy年M月d日"), budget card, recent bill list with swipe-delete + click-edit
    │   ├── HomeViewModel.java      — Exposes LiveData for current month's bills, income, expense, budget
    │   ├── AllBillsFragment.java   — All bills view grouped by year-month, with back navigation
    │   └── AllBillsViewModel.java  — ViewModel for all bills
    ├── calendar/
    │   ├── CalendarFragment.java   — LinearLayout-based calendar grid with prev/next month nav, income/expense per day, color-coded cells
    │   └── CalendarViewModel.java  — Manages month state, uses MediatorLiveData for income/expense/dailySums
    ├── stats/
    │   ├── StatsFragment.java      — PieChart + BarChart with week/month/year period tabs; data from Room
    │   └── StatsViewModel.java     — Period enum (WEEK/MONTH/YEAR), uses MediatorLiveData for expenseByType + dailySums
    ├── profile/
    │   └── ProfileFragment.java    — Avatar, budget summary, CSV export, dark mode toggle, cache clear
    ├── adapter/
    │   ├── BillAdapter.java        — RecyclerView.Adapter with OnBillClickListener, emoji icons, color-coded amounts
    │   └── SectionedBillAdapter.java — Grouped bill list adapter with section headers by year-month
    └── dialog/
        └── AddBillDialog.java      — DialogFragment for add/edit, includes date picker, supports prefill for editing
```

## Key Dependencies

- **Room** — local SQLite ORM (bills + budgets tables)
- **MPAndroidChart** (v3.1.0 via JitPack) — pie and bar charts in stats
- **Material Components** — BottomNavigationView, FAB, themes
- **Lifecycle (ViewModel + LiveData)** — MVVM architecture; all data flows through ViewModel → Fragment

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

Category colors (cat_food, cat_transport, etc.) are pastel backgrounds for emoji icon circles.

## Important Patterns

- **Fragments never access DAOs directly** — go through `MyApplication.getRepository()`
- **Fragments create ViewModels** via `new ViewModelProvider(this).get(XxxViewModel.class)`
- **Room queries return LiveData** — Fragments observe, never poll
- **Write operations** (insert/update/delete) go through Repository's ExecutorService, off the main thread
- **Bill editing** — click a bill item in the list to open the dialog pre-filled; swipe left/right to delete
- **Budget** — click the budget card on Home to open a set-budget dialog; stored per yearMonth
- **MediatorLiveData for dynamic queries** — CalendarViewModel and StatsViewModel use MediatorLiveData to wrap data sources that change when month/period changes; old source is removed before adding new one to avoid stale observers
- **Calendar uses nested LinearLayout** — CalendarFragment builds the calendar grid using nested LinearLayouts (vertical rows + horizontal columns) with layout_weight for equal distribution; more reliable than GridLayout for dynamic content