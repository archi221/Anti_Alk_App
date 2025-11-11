# SoberUp App - UML Klassendiagramm (Mermaid)

## Vollständiges Klassendiagramm

```mermaid
classDiagram
    %% Activity & Navigation
    class MainActivity {
        -SessionManager sessionManager
        +onCreate(Bundle)
    }
    
    class NavGraph {
        +String? currentUserId
        +NavGraph(...)
    }
    
    class Screen {
        <<sealed>>
        +String route
        Login
        PatientDashboard
        PatientProfile
        PatientSettings
        SupportLocations
        TriggerManagement
    }
    
    %% Data Models
    class User {
        +String id
        +String username
        +String password
        +String role
        +String name
        +String email
        +Int soberDays
        +Timestamp? soberSince
        +SOSContact? sosContact
        +List~String~ triggers
        +String assignedDoctorId
        +Timestamp? createdAt
        +Timestamp? lastMoodCheck
        +calculateSoberDays() Int
        +isPatient() Boolean
        +isDoctor() Boolean
        +isAdmin() Boolean
    }
    
    class MoodEntry {
        +String id
        +Timestamp? date
        +Int moodValue
        +String note
        +Timestamp? createdAt
        +getMoodColor() MoodColor
    }
    
    class MoodColor {
        <<enumeration>>
        RED
        YELLOW
        GREEN
    }
    
    class SOSContact {
        +String name
        +String phone
    }
    
    class SupportLocation {
        +String id
        +String name
        +String address
        +String openingHours
        +String emergencyNumber
        +String createdBy
        +Timestamp? createdAt
    }
    
    %% Repositories
    class UserRepository {
        -FirebaseFirestore firestore
        +updateUser(User) Boolean
        +updateSOSContact(String, SOSContact) Boolean
        +updateTriggers(String, List~String~) Boolean
        +updateSoberDays(String, Int) Boolean
        +updateSoberSince(String, Timestamp) Boolean
        +getUser(String) User?
        -calculateSoberDays(Timestamp) Int
    }
    
    class AuthRepository {
        -FirebaseFirestore firestore
        +loginByName(String, String) User?
        +usernameExists(String) Boolean
    }
    
    class MoodRepository {
        -FirebaseFirestore firestore
        +getMoodEntries(String, Date?, Date?) List~MoodEntry~
        +getMoodEntriesForMonth(String, Int, Int) List~MoodEntry~
        +getMoodEntryForDate(String, Date) MoodEntry?
        +saveMoodEntry(String, MoodEntry) Boolean
    }
    
    class SupportLocationRepository {
        -FirebaseFirestore firestore
        +getAllSupportLocations() List~SupportLocation~
    }
    
    class SessionManager {
        -SharedPreferences sharedPreferences
        +saveSession(String, String, String)
        +clearSession()
        +getUserId() Flow~String?~
        +getUsername() Flow~String?~
        +getRole() Flow~String?~
    }
    
    %% ViewModels
    class PatientDashboardViewModel {
        -String userId
        -MoodRepository moodRepository
        -UserRepository userRepository
        -MutableStateFlow~User?~ _user
        -MutableStateFlow~List~MoodEntry~~ _moodEntries
        -MutableStateFlow~MoodEntry?~ _todayMood
        -MutableStateFlow~Boolean~ _isLoading
        -MutableStateFlow~String?~ _errorMessage
        +StateFlow~User?~ user
        +StateFlow~List~MoodEntry~~ moodEntries
        +StateFlow~MoodEntry?~ todayMood
        +StateFlow~Boolean~ isLoading
        +StateFlow~String?~ errorMessage
        +loadUserData()
        +loadMoodEntries()
        +saveMood(Int, String)
        +getMoodForDate(Date) MoodEntry?
        +updateSoberSince(Timestamp)
        +markAsDrunk()
        +clearError()
    }
    
    class PatientSettingsViewModel {
        -String userId
        -UserRepository userRepository
        -MutableStateFlow~User?~ _user
        -MutableStateFlow~Boolean~ _isLoading
        -MutableStateFlow~String?~ _errorMessage
        -MutableStateFlow~String?~ _successMessage
        +StateFlow~User?~ user
        +StateFlow~Boolean~ isLoading
        +StateFlow~String?~ errorMessage
        +StateFlow~String?~ successMessage
        +updateSOSContact(SOSContact)
    }
    
    %% UI Screens
    class LoginScreen {
        +onLoginSuccess: (User) -> Unit
    }
    
    class PatientDashboardScreen {
        +String userId
        +onNavigateToProfile: () -> Unit
        +onNavigateToSettings: () -> Unit
        +onNavigateToSupportLocations: () -> Unit
        +onNavigateToTriggers: () -> Unit
    }
    
    class PatientSettingsScreen {
        +String userId
        +onNavigateBack: () -> Unit
    }
    
    class TriggerManagementScreen {
        +String userId
        +onNavigateBack: () -> Unit
    }
    
    class SupportLocationsScreen {
        +onNavigateBack: () -> Unit
    }
    
    %% Relationships
    MainActivity --> NavGraph : uses
    NavGraph --> Screen : uses
    NavGraph --> LoginScreen : navigates to
    NavGraph --> PatientDashboardScreen : navigates to
    NavGraph --> PatientSettingsScreen : navigates to
    NavGraph --> TriggerManagementScreen : navigates to
    NavGraph --> SupportLocationsScreen : navigates to
    NavGraph --> SessionManager : uses
    
    PatientDashboardScreen --> PatientDashboardViewModel : uses
    PatientSettingsScreen --> PatientSettingsViewModel : uses
    
    PatientDashboardViewModel --> UserRepository : uses
    PatientDashboardViewModel --> MoodRepository : uses
    PatientSettingsViewModel --> UserRepository : uses
    
    UserRepository --> User : creates/updates
    AuthRepository --> User : creates
    MoodRepository --> MoodEntry : creates/updates
    SupportLocationRepository --> SupportLocation : creates
    
    User --> SOSContact : contains
    MoodEntry --> MoodColor : uses
    
    LoginScreen --> AuthRepository : uses
    LoginScreen --> SessionManager : uses
    
    SupportLocationsScreen --> SupportLocationRepository : uses
```

## Vereinfachtes Architektur-Diagramm

```mermaid
graph TB
    subgraph "UI Layer"
        A[MainActivity] --> B[NavGraph]
        B --> C[LoginScreen]
        B --> D[PatientDashboardScreen]
        B --> E[PatientSettingsScreen]
        B --> F[TriggerManagementScreen]
        B --> G[SupportLocationsScreen]
    end
    
    subgraph "ViewModel Layer"
        D --> H[PatientDashboardViewModel]
        E --> I[PatientSettingsViewModel]
    end
    
    subgraph "Repository Layer"
        H --> J[UserRepository]
        H --> K[MoodRepository]
        I --> J
        C --> L[AuthRepository]
        G --> M[SupportLocationRepository]
    end
    
    subgraph "Data Layer"
        J --> N[(Firestore)]
        K --> N
        L --> N
        M --> N
    end
    
    subgraph "Data Models"
        J --> O[User]
        K --> P[MoodEntry]
        M --> Q[SupportLocation]
        O --> R[SOSContact]
        P --> S[MoodColor]
    end
    
    style A fill:#E8F4F8
    style H fill:#FFF4E6
    style I fill:#FFF4E6
    style J fill:#E6F7E6
    style K fill:#E6F7E6
    style L fill:#E6F7E6
    style M fill:#E6F7E6
    style N fill:#FFE6E6
```

## Komponenten-Übersicht

### UI Layer (Blau)
- **MainActivity**: Haupteinstiegspunkt
- **NavGraph**: Navigation zwischen Screens
- **Screens**: Alle UI-Komponenten

### ViewModel Layer (Gelb)
- **PatientDashboardViewModel**: Logik für Dashboard
- **PatientSettingsViewModel**: Logik für Einstellungen

### Repository Layer (Grün)
- **UserRepository**: Benutzerdaten
- **MoodRepository**: Stimmungseinträge
- **AuthRepository**: Authentifizierung
- **SupportLocationRepository**: Anlaufstellen

### Data Layer (Rot)
- **Firestore**: Cloud-Datenbank

### Data Models
- **User**: Hauptbenutzermodell
- **MoodEntry**: Stimmungseintrag
- **SupportLocation**: Anlaufstelle
- **SOSContact**: Notfallkontakt
- **MoodColor**: Stimmungsfarben-Enum

