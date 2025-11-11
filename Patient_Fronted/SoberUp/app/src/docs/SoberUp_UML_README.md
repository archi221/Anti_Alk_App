# SoberUp App - UML Klassendiagramm

## Übersicht

Dieses Dokument enthält das UML-Klassendiagramm für die SoberUp Android-App. Das Diagramm zeigt die Struktur der App, ihre Klassen, Beziehungen und Abhängigkeiten.

## Datei

- **SoberUp_UML_Diagram.puml** - PlantUML-Datei mit dem vollständigen Klassendiagramm

## Verwendung

### Option 1: Online Viewer
1. Öffne [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Kopiere den Inhalt der `.puml` Datei
3. Füge ihn in den Editor ein
4. Das Diagramm wird automatisch generiert

### Option 2: VS Code Extension
1. Installiere die "PlantUML" Extension in VS Code
2. Öffne die `.puml` Datei
3. Drücke `Alt+D` oder klicke auf "Preview"

### Option 3: IntelliJ IDEA / Android Studio
1. Installiere das PlantUML Plugin
2. Öffne die `.puml` Datei
3. Rechtsklick → "Preview Diagram"

### Option 4: Command Line
```bash
# Installiere PlantUML (Java erforderlich)
# Windows: choco install plantuml
# Mac: brew install plantuml
# Linux: sudo apt-get install plantuml

# Generiere PNG
plantuml SoberUp_UML_Diagram.puml

# Generiere SVG
plantuml -tsvg SoberUp_UML_Diagram.puml
```

## Diagramm-Struktur

Das Diagramm ist in folgende Bereiche unterteilt:

### 1. **Activity & Navigation**
- `MainActivity` - Haupteinstiegspunkt der App
- `NavGraph` - Navigation zwischen Screens
- `Screen` - Sealed Class für alle Routen

### 2. **Data Models**
- `User` - Benutzerdatenmodell
- `MoodEntry` - Stimmungseintrag
- `MoodColor` - Enum für Stimmungsfarben
- `SOSContact` - Notfallkontakt
- `SupportLocation` - Anlaufstelle

### 3. **Repositories**
- `UserRepository` - CRUD für Benutzer
- `AuthRepository` - Authentifizierung
- `MoodRepository` - Stimmungseinträge
- `SupportLocationRepository` - Anlaufstellen
- `SessionManager` - Session-Verwaltung

### 4. **ViewModels**
- `PatientDashboardViewModel` - Dashboard-Logik
- `PatientSettingsViewModel` - Einstellungen-Logik

### 5. **UI Screens**
- `LoginScreen` - Login-Ansicht
- `PatientDashboardScreen` - Hauptdashboard
- `PatientSettingsScreen` - Einstellungen
- `TriggerManagementScreen` - Trigger-Verwaltung
- `SupportLocationsScreen` - Anlaufstellen-Karte

## Beziehungen

- **Komposition**: MainActivity → NavGraph
- **Aggregation**: ViewModels → Repositories
- **Dependency**: Screens → ViewModels, Repositories → Data Models

## Farben

- **Hellblau**: Alle Klassen
- **Grün**: Data Models (optional, kann angepasst werden)
- **Gelb**: ViewModels (optional, kann angepasst werden)

## Aktualisierung

Wenn neue Klassen hinzugefügt werden, bitte das Diagramm entsprechend aktualisieren:
1. Öffne `SoberUp_UML_Diagram.puml`
2. Füge neue Klassen hinzu
3. Definiere Beziehungen
4. Generiere das Diagramm neu

