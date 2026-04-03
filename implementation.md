# Hum - Shared Family Expense Tracker

## Overview

**Hum** is an Android app that lets family members collaboratively track shared expenses in real-time. Members create or join a family group, add one-time or recurring expenses, and get insights into spending patterns — all synced instantly via Firebase Firestore.

---

## Core Principles

- **Minimal clicks** — Adding an expense should take ≤ 3 taps from the home screen.
- **Real-time sync** — All family members see changes instantly via Firestore snapshots.
- **Offline-first** — Firestore offline persistence; data syncs when connectivity returns.
- **Google-only auth** — No email/password flows. One-tap Google Sign-In.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Auth | Firebase Authentication (Google Sign-In) |
| Database | Cloud Firestore (real-time listeners) |
| DI | Hilt |
| Navigation | Compose Navigation |
| Build | Gradle Kotlin DSL, Version Catalogs |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## Firestore Data Model

### Collection: `users`
```
users/{userId}
  ├── displayName: String
  ├── email: String
  ├── photoUrl: String
  ├── familyId: String?          // null if not in a family yet
  ├── createdAt: Timestamp
  └── updatedAt: Timestamp
```

### Collection: `families`
```
families/{familyId}
  ├── name: String                // e.g. "Patel Family"
  ├── createdBy: String           // userId of creator
  ├── inviteCode: String          // 6-char unique code for joining
  ├── memberIds: List<String>     // list of userIds
  ├── createdAt: Timestamp
  └── updatedAt: Timestamp
```

### Collection: `expenses`
```
expenses/{expenseId}
  ├── familyId: String            // which family this belongs to
  ├── title: String               // e.g. "Electricity Bill"
  ├── amount: Double
  ├── currency: String            // default "INR"
  ├── category: String            // enum: FOOD, TRANSPORT, UTILITIES, RENT, MEDICAL, SHOPPING, ENTERTAINMENT, OTHER
  ├── paidBy: String              // userId who paid
  ├── paidByName: String          // denormalized display name
  ├── isRecurring: Boolean
  ├── recurringType: String?      // MONTHLY, QUARTERLY, YEARLY (null if not recurring)
  ├── recurringDay: Int?          // day of month (1-31) for auto-reminder
  ├── notes: String?
  ├── date: Timestamp             // when the expense occurred
  ├── createdAt: Timestamp
  └── updatedAt: Timestamp
```

### Firestore Rules (Summary)
- Users can read/write their own `users` doc.
- Family members can read/write their `families` doc and all `expenses` where `familyId` matches.
- Invite code lookup is allowed for authenticated users.

---

## Screens & Navigation

### 1. Login Screen
- Google One-Tap Sign-In button.
- On success → check if user has `familyId` → route to Home or Family Setup.

### 2. Family Setup Screen
- Two options: **Create Family** or **Join Family**.
- Create: Enter family name → generates invite code → navigates to Home.
- Join: Enter 6-char invite code → joins family → navigates to Home.

### 3. Home Screen (Main Dashboard)
- **Top bar**: Family name, settings icon.
- **Summary card**: Total spent this month, your share.
- **Quick-add FAB**: Floating action button → opens Add Expense bottom sheet.
- **Recent expenses list**: Scrollable list grouped by date, each item shows title, amount, who paid, category icon.
- **Bottom navigation**: Home | Recurring | Insights | Family.

### 4. Add Expense (Bottom Sheet)
- **Amount field** (large, auto-focused with numeric keyboard).
- **Title** (text field with common suggestions).
- **Category** (horizontal chip row — tap to select).
- **Date** (defaults to today, tap to change).
- **Recurring toggle** — if ON, show frequency picker (Monthly/Quarterly/Yearly) and day-of-month.
- **Notes** (optional, collapsible).
- **Save button**.
- Total taps to add a basic expense: tap FAB → type amount → type title → tap category → tap Save = **5 interactions**.

### 5. Recurring Expenses Screen
- List of all recurring expenses with next due date.
- Mark as paid this period.
- Edit / delete options.

### 6. Insights Screen
- **Monthly total** bar chart (last 6 months).
- **Category breakdown** donut chart for current month.
- **Top spender** in the family this month.
- **Recurring vs one-time** split.

### 7. Family Screen
- List of members with avatars.
- Invite code display with share button.
- Leave family option.

### 8. Settings
- Profile info (from Google).
- Currency preference.
- Sign out.

---

## Navigation Graph

```
LoginScreen
    ├── (no family) → FamilySetupScreen → HomeScreen
    └── (has family) → HomeScreen

HomeScreen (with BottomNavBar)
    ├── Tab: Home (dashboard + expense list)
    ├── Tab: Recurring
    ├── Tab: Insights
    └── Tab: Family

HomeScreen → AddExpenseSheet (bottom sheet overlay)
```

---

## Module / Package Structure

```
com.hum.app
  ├── di/                     # Hilt modules
  │   └── AppModule.kt
  ├── data/
  │   ├── model/              # Data classes matching Firestore docs
  │   │   ├── User.kt
  │   │   ├── Family.kt
  │   │   └── Expense.kt
  │   └── repository/
  │       ├── AuthRepository.kt
  │       ├── FamilyRepository.kt
  │       └── ExpenseRepository.kt
  ├── ui/
  │   ├── theme/
  │   │   ├── Color.kt
  │   │   ├── Type.kt
  │   │   └── Theme.kt
  │   ├── navigation/
  │   │   └── NavGraph.kt
  │   ├── login/
  │   │   ├── LoginScreen.kt
  │   │   └── LoginViewModel.kt
  │   ├── familysetup/
  │   │   ├── FamilySetupScreen.kt
  │   │   └── FamilySetupViewModel.kt
  │   ├── home/
  │   │   ├── HomeScreen.kt
  │   │   └── HomeViewModel.kt
  │   ├── addexpense/
  │   │   ├── AddExpenseSheet.kt
  │   │   └── AddExpenseViewModel.kt
  │   ├── recurring/
  │   │   ├── RecurringScreen.kt
  │   │   └── RecurringViewModel.kt
  │   ├── insights/
  │   │   ├── InsightsScreen.kt
  │   │   └── InsightsViewModel.kt
  │   ├── family/
  │   │   ├── FamilyScreen.kt
  │   │   └── FamilyViewModel.kt
  │   └── components/          # Shared composables
  │       ├── ExpenseCard.kt
  │       ├── CategoryChip.kt
  │       └── SummaryCard.kt
  ├── util/
  │   ├── Constants.kt
  │   └── Extensions.kt
  └── HumApp.kt               # Application class
  └── MainActivity.kt
```

---

## Key Implementation Details

### Google Sign-In Flow
1. Use `CredentialManager` API (modern replacement for legacy GoogleSignInClient).
2. On credential received → get `idToken` → `Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))`.
3. On first login → create `users/{uid}` doc in Firestore.
4. Check `familyId` field → route accordingly.

### Real-Time Sync
- Use `snapshotFlow` on Firestore collection queries wrapped in a Kotlin `Flow`.
- ViewModel collects flows and exposes `StateFlow<UiState>` to Compose.
- Firestore offline persistence enabled by default — works without internet.

### Recurring Expense Logic
- Recurring expenses are stored once with `isRecurring = true`.
- The "Recurring" tab shows them with calculated next-due dates.
- Users tap "Mark Paid" to create a new normal expense entry for that period, keeping history.

### Invite Code
- Generated as a random 6-character alphanumeric string on family creation.
- Stored in `families` doc.
- Joining: query `families` where `inviteCode == input` → add user to `memberIds` → update user's `familyId`.

### Currency
- Default to INR (₹).
- Stored per-expense for future multi-currency support.

---

## Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /families/{familyId} {
      allow read: if request.auth != null &&
        request.auth.uid in resource.data.memberIds;
      allow create: if request.auth != null;
      allow update: if request.auth != null &&
        request.auth.uid in resource.data.memberIds;
      // Allow reading by invite code for joining
      allow list: if request.auth != null;
    }

    match /expenses/{expenseId} {
      allow read, write: if request.auth != null &&
        exists(/databases/$(database)/documents/families/$(resource.data.familyId)) &&
        request.auth.uid in get(/databases/$(database)/documents/families/$(resource.data.familyId)).data.memberIds;
      allow create: if request.auth != null;
    }
  }
}
```

---

## Build & Setup Requirements

1. Create Firebase project at console.firebase.google.com.
2. Enable **Google Sign-In** in Firebase Auth.
3. Add Android app with package `com.hum.app` and download `google-services.json`.
4. Place `google-services.json` in `app/` directory.
5. Enable **Cloud Firestore** in Firebase console.
6. Deploy security rules above.

---

## Future Enhancements (Out of Scope for v1)

- Push notifications for new expenses.
- Expense splitting (who owes whom).
- Receipt photo attachments (Firebase Storage).
- Monthly PDF reports.
- Budget limits per category with alerts.
- Multi-currency with exchange rates.
- Dark mode toggle (system default for v1).
