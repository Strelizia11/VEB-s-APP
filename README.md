# VEB APP

A comprehensive Android productivity application featuring notes, checklists, budget tracking, and calendar management with event notifications.

## 📱 Features

- **Home Dashboard**: Quick overview of your tasks and events
- **Notes**: Create, edit, and manage personal notes
- **Checklist**: Track tasks and to-do items with checkbox functionality
- **Budget**: Monitor expenses and manage your finances
- **Calendar**: View and manage events with holiday integration
- **Event Notifications**: Receive timely reminders for scheduled events
- **Settings**: Customize app preferences

## 🛠️ Technical Stack

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Java
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

### Key Libraries & Dependencies

#### Core Android
- AndroidX AppCompat
- Material Design Components
- ConstraintLayout
- RecyclerView
- GridLayout

#### Architecture Components
- Lifecycle LiveData & ViewModel
- Navigation Component (Fragment & UI)
- View Binding

#### Database
- **Room Database** (v2.6.1)
  - Room Runtime
  - Room RxJava3
  - Room Compiler (annotation processor)

#### Data Handling
- Gson (v2.10.1) for JSON serialization
- Custom Date Converters

#### Permissions
- POST_NOTIFICATIONS - For event reminders
- SCHEDULE_EXACT_ALARM - For precise alarm scheduling
- USE_EXACT_ALARM - For exact alarm functionality

## 📂 Project Structure

```
app/src/main/java/com/example/veb_app/
├── data/                      # Database layer
│   ├── AppDatabase.java       # Room database configuration
│   ├── *Dao.java             # Data Access Objects
│   ├── *Entity.java          # Database entities
│   ├── *Repository.java      # Repository pattern implementation
│   └── DatabaseManager.java  # Database management utilities
├── ui/                        # UI layer (MVVM)
│   ├── home/                 # Home dashboard
│   ├── notes/                # Notes feature
│   ├── checklist/            # Checklist feature
│   ├── budget/               # Budget tracker
│   ├── calendar/             # Calendar & events
│   └── settings/             # App settings
├── notifications/            # Notification system
│   ├── EventNotificationReceiver.java
│   └── EventNotificationScheduler.java
├── MainActivity.java         # Main app activity
└── SplashActivity.java      # Splash screen
```

## 🗄️ Database Schema

The app uses Room Database with the following main entities:

- **NoteEntity**: Store user notes
- **ChecklistEntity & ChecklistTaskEntity**: Manage checklists and their tasks
- **EventEntity**: Calendar events with notifications
- **TransactionEntity**: Budget and financial transactions

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or newer
- Android SDK 26 or higher
- Gradle 8.x

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd VEB_APP
```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Run the app on an emulator or physical device

### Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## 📄 Version History

- **v1.1** (versionCode 2) - Current version
- Initial features and database implementation

## 🔧 Configuration

### Build Configuration
- **Namespace**: `com.example.veb_app`
- **Application ID**: `com.example.veb_app`
- **Compile SDK**: 35
- **Java Version**: 11

### ProGuard
ProGuard rules are configured but minification is currently disabled for debug builds.

## 📚 Additional Documentation

- [Database Migration Guide](DATABASE_MIGRATION_GUIDE.md) - Guidelines for database schema updates

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is for educational/personal use.

## 👤 Author

Jhomar

## 🔮 Future Enhancements

- [ ] Cloud sync functionality
- [ ] Export/Import data
- [ ] Dark mode improvements
- [ ] Widget support
- [ ] Enhanced notification customization
- [ ] Data backup and restore
- [ ] Multi-language support

---

**Note**: This is an active development project. Features and documentation are subject to change.
