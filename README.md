# Hum - Shared Family Expense Tracker

An Android app for families to collaboratively track shared expenses in real-time.

## Features

- **Google Sign-In** — One-tap authentication, no passwords.
- **Family Groups** — Create or join a family with a 6-character invite code.
- **Real-time Sync** — All expenses sync instantly across family members via Firestore.
- **Recurring Expenses** — Track monthly bills (electricity, gas, wifi, etc.).
- **Insights** — Monthly spending charts, category breakdowns, top spender.
- **Offline Support** — Works without internet; syncs when back online.

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Firebase Auth (Google Sign-In via Credential Manager)
- Cloud Firestore (real-time listeners)
- Hilt (dependency injection)
- MVVM architecture

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- A Firebase project

### Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd Hum
   ```

2. **Create a Firebase project**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project

3. **Enable Authentication**
   - In Firebase Console → Authentication → Sign-in method
   - Enable **Google** provider
   - Note your **Web Client ID** (found in the Google sign-in provider config)

4. **Enable Cloud Firestore**
   - In Firebase Console → Firestore Database → Create database
   - Start in **test mode** (deploy proper rules before production)

5. **Add Android app to Firebase**
   - Package name: `com.hum.app`
   - Download `google-services.json`
   - Place it in the `app/` directory

6. **Configure Web Client ID**
   - Open `app/src/main/res/values/strings.xml`
   - Replace `YOUR_WEB_CLIENT_ID` with your actual Web Client ID from Firebase

7. **Build & Run**
   - Open in Android Studio
   - Sync Gradle
   - Run on device/emulator (API 26+)

### Firestore Security Rules

Deploy these rules in Firebase Console → Firestore → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /families/{familyId} {
      allow read: if request.auth != null && request.auth.uid in resource.data.memberIds;
      allow create: if request.auth != null;
      allow update: if request.auth != null && request.auth.uid in resource.data.memberIds;
      allow list: if request.auth != null;
    }
    match /expenses/{expenseId} {
      allow read, write: if request.auth != null;
      allow create: if request.auth != null;
    }
  }
}
```

## Project Structure

```
app/src/main/java/com/hum/app/
├── di/AppModule.kt                  # Hilt dependency injection
├── data/
│   ├── model/                       # User, Family, Expense data classes
│   └── repository/                  # Auth, Family, Expense repositories
├── ui/
│   ├── theme/                       # Colors, Typography, Material Theme
│   ├── navigation/NavGraph.kt       # Compose Navigation + Bottom Nav
│   ├── login/                       # Google Sign-In screen
│   ├── familysetup/                 # Create/Join family screen
│   ├── home/                        # Dashboard with expense list
│   ├── addexpense/                  # Bottom sheet for adding expenses
│   ├── recurring/                   # Recurring expenses list
│   ├── insights/                    # Charts and spending analytics
│   ├── family/                      # Family members + invite code
│   └── components/                  # Shared UI components
├── util/                            # Constants, extension functions
├── HumApp.kt                       # Application class
└── MainActivity.kt                  # Entry point
```
