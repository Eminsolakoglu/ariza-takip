# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Arıza Kayıt Takip Sistemi** — An Android fault/issue tracking system with role-based access control. Users can create, assign, and track maintenance issues. Built entirely in Java.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew build                  # Full build (all variants)
./gradlew lint                   # Run lint checks
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew testDebugUnitTest      # Run a single test variant
```

## Tech Stack

- **Language:** Java 11
- **Min SDK:** 26, **Compile/Target SDK:** 34, **AGP:** 9.1.1
- **Database:** Room 2.5.2 (`ariza_db`, version 4, `fallbackToDestructiveMigration` enabled)
- **UI:** ViewBinding + DataBinding, Material Design 1.9.0, ConstraintLayout
- **Architecture libs:** AndroidX Lifecycle ViewModel 2.6.1 + LiveData 2.6.1 (available but not yet used in Activities)
- **Networking:** Retrofit 2.9.0 + OkHttp 4.10.0 + Gson (configured, not yet wired to real API)
- **Firebase:** Auth + Realtime Database (BOM 32.0.0, declared but unused)
- **Dependency versions:** managed via `gradle/libs.versions.toml` (version catalog)

## Architecture

Activities talk directly to Repositories; no ViewModels are used yet despite the dependency being present.

```
LoginActivity / MainActivity / ArizaEkleActivity / ArizaDetayActivity
        ↓
ArizaRepository / UserRepository
        ↓
ArizaDao / UserDao  →  AppDatabase (Room singleton)
        +
SessionManager (SharedPreferences — stores current user/role)
```

**Role-based access:** three roles defined in `User.java` — `admin`, `tech`, `client`. Role is read from `SessionManager` in each Activity to show/hide actions.

**Status filtering** (OPEN / IN_PROGRESS / CLOSED) is done in-memory inside Activities, not via Room queries.

**Demo data** is seeded at startup in `UserRepository.prepopulateUsers()` (called from `App.java`). Default credentials: admin/1234, tech/1234, tech2/1234, client/1234, client2/1234.

## Key Files

| File | Purpose |
|------|---------|
| `app/build.gradle` | Module build config, `buildConfigField` for API URLs per variant |
| `data/AppDatabase.java` | Room singleton; `allowMainThreadQueries()` is on (dev only) |
| `data/ArizaDao.java` | SQL queries for issues |
| `data/UserDao.java` | SQL queries for users |
| `utils/SessionManager.java` | SharedPreferences wrapper for auth state |
| `adapter/ArizaAdapter.java` | RecyclerView adapter; status → color mapping |

## Important Caveats

- `allowMainThreadQueries()` is enabled — any DB work added should move to a background thread (executor or coroutine) before production.
- `fallbackToDestructiveMigration()` means schema changes drop and recreate tables; write proper migrations if data must survive upgrades.
- Passwords are stored plaintext in the Room database and SharedPreferences. No encryption is in place.
- `ArizaListeActivity` is declared in the manifest but not navigated to from any other screen (appears unused).
- Build config fields `API_BASE_URL` and `DEBUG_LOGGING` are defined but Retrofit is not yet integrated with actual endpoints.
