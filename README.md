# SkillExchange 🤝

A peer-to-peer skill-sharing Android app where users can post service requests, browse others' skills, and exchange services through a swap system — no money involved.

---

## Features

- **Authentication** — Email/password login and registration via Firebase Auth
- **Post Feed** — Browse skill requests with real-time updates and category filters
- **Skill Filters** — Filter posts by 14 trade categories (Plumbing, Electrical, Carpentry, etc.)
- **Swap System** — Request a skill swap, accept/reject, and confirm completion
- **Real-time Chat** — Message between swap participants with system status messages
- **Notifications** — Live badge on dashboard, per-user notification feed
- **User Profile** — Trust score, swaps done counter, initials avatar, edit profile
- **Swap History** — View all completed swaps

---

## Screenshots

<img width="307" height="562" alt="image" src="https://github.com/user-attachments/assets/1f0385c2-fc2a-475c-a32c-97d717f0c0f2" />

<img width="246" height="499" alt="image" src="https://github.com/user-attachments/assets/e0b72676-e833-4137-a66d-1298df0b2ed9" />
<img width="246" height="499" alt="image" src="https://github.com/user-attachments/assets/7b9f030c-e0f1-4c6b-bd25-4e3de27c1e86" />
<img width="240" height="484" alt="image" src="https://github.com/user-attachments/assets/0d89128e-819b-4d17-8d42-d092cf8b4819" />
<img width="303" height="547" alt="image" src="https://github.com/user-attachments/assets/67416405-d418-4932-bafc-abe7742b5208" />
<img width="301" height="547" alt="image" src="https://github.com/user-attachments/assets/60f1ab9c-ecb5-4a39-8cc3-b4254ef6d802" />

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android (minSdk 24, targetSdk 34) |
| Authentication | Firebase Auth |
| Database | Cloud Firestore |
| Image Loading | Glide 4.16.0 |
| UI | Material Design 3, ConstraintLayout, RecyclerView |
| Build | Gradle Kotlin DSL |

---

## Project Structure

```
com.skillexchange/
├── firebase/         # FirebaseClient singleton, FirestoreRefs
├── models/           # User, Post, Swap, Message, Notification
├── repository/       # BaseRepository, FirestoreRepository
├── adapters/         # PostAdapter, ChatAdapter, NotificationAdapter
├── ui/
│   ├── auth/         # SplashActivity, LoginActivity, RegisterActivity
│   ├── home/         # DashboardActivity, CreatePostActivity, PostDetailActivity, NotificationsActivity
│   ├── chat/         # ChatActivity
│   ├── profile/      # ProfileActivity, EditProfileActivity, HistoryActivity
│   ├── theme/        # Compose theme (Color, Type, Theme)
│   └── utils/        # Constants, SessionManager, Extensions
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 21
- A Firebase project with **Authentication** and **Firestore** enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SkillExchange.git
   cd SkillExchange
   ```

2. **Add your `google-services.json`**

   - Go to [Firebase Console](https://console.firebase.google.com)
   - Open your project → Project Settings → Download `google-services.json`
   - Place it in the `app/` directory

   > `google-services.json` is excluded from version control for security. Never commit it.

3. **Enable Firebase services**

   In your Firebase project console:
   - **Authentication** → Sign-in method → Enable **Email/Password**
   - **Firestore Database** → Create database → Start in **test mode**

4. **Build and run**

   Open the project in Android Studio and click **Run**, or:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Firestore Collections

| Collection | Description |
|---|---|
| `users` | User profiles (name, email, skill, trustScore, swapsDone) |
| `posts` | Skill exchange listings |
| `swaps` | Swap lifecycle (Pending → Accepted → Completed) |
| `messages` | Chat messages scoped by swapId |
| `notifications` | Per-user notifications |

---

## Firestore Security Rules

Before going to production, replace the default test rules with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    match /posts/{postId} {
      allow read, write: if request.auth != null;
    }
    match /swaps/{swapId} {
      allow read, write: if request.auth != null;
    }
    match /messages/{msgId} {
      allow read, write: if request.auth != null;
    }
    match /notifications/{notifId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## Skill Categories

Plumbing · Electrical · Carpentry · Masonry · Painting · Welding · Tiling · Roofing · Interior Design · Furniture Repair · Car Mechanic · Construction Work · Water Tank Cleaning

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Author

**Jashwanth R**  
Built with ❤️ using Kotlin + Firebase
