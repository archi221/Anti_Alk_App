# context.md  
## Projektname: SoberUp
### Ziel
SoberUp ist eine mobile Android-App zur Unterstützung von Menschen mit Alkoholabhängigkeit.  
Die App hilft Patient*innen, ihre Stimmung und Nüchternheit zu dokumentieren, Rückfälle früh zu erkennen und Ärzt*innen über kritische Veränderungen zu informieren.

---

## 🧠 Übersicht

### Hauptnutzerrollen
| Rolle | Beschreibung | Zugriff |
|-------|---------------|----------|
| **Patient** | Nutzt die App auf dem Smartphone, trägt täglich Daten ein, kann SOS-Kontakte bestimmen und Trigger verwalten. | Mobile App |
| **Arzt / Therapeut** | Verwaltet Patientenkonten, überwacht deren Stimmung und Aktivitäten über eine eigene Web- oder Tablet-View. | Web-View |
| **Admin** | Systemverwaltung (Benutzer, Zugriffsrechte, technische Pflege). | Intern |

---

## 🏠 Dashboard (Patient)
Beim Öffnen der App sieht der Patient ein übersichtliches **Dashboard**, das folgende Elemente anzeigt:

| Feature | Beschreibung |
|----------|---------------|
| **Kalenderansicht** | Monatskalender, farbliche Markierung pro Tag basierend auf Stimmung: <br>🔴 Rot = Stimmung 1–3, 🟡 Gelb = 4–7, 🟢 Grün = 8–10 |
| **Heutige Stimmung** | Feld zur manuellen Eingabe (1–10). |
| **Nüchterne Tage** | Zähler, wie viele Tage seit der letzten Trinkepisode vergangen sind. |
| **SOS-Indikator** | Zeigt an, ob ein SOS-Kontakt aktiv ist und wer kontaktiert wird. |
| **Triggerübersicht** | Schnellansicht der aktuellen Triggerpunkte (aus der Erstanmeldung). |

Die Stimmung wird **manuell eingetragen**, lokal angezeigt und in **Firestore gespeichert**.

---

## 🕦 Stimmungstracking und Kalenderlogik

- Der Patient gibt täglich seine Stimmung als Wert zwischen **1–10** an.  
- Jeder Tag im Kalender ist markiert:
  - **Rot (1–3):** kritisch  
  - **Gelb (4–7):** neutral  
  - **Grün (8–10):** stabil  
- Änderungen können rückwirkend bearbeitet werden.  
- Daten werden automatisch in Firestore unter `users/{userId}/moodEntries` gespeichert.  

---

## ⚠️ Früherkennung & SOS-System

### Funktionsweise
1. Firestore überwacht täglich die Stimmungseinträge pro Nutzer (Cloud Function).
2. Wenn **zwei aufeinanderfolgende Tage** mit einer Stimmung **1–3** erkannt werden:
   - Eine automatische SMS wird an den festgelegten SOS-Kontakt gesendet.
   - Die Nachricht enthält den Namen des Patienten und einen Warnhinweis.  

### SMS-Logik (Cloud Function)
```js
onWrite(moodEntry) {
  if (today and yesterday both <= 3) {
     sendSMS(user.sosContact.phone, `Warnung: ${user.name} hat seit 2 Tagen schlechte Stimmung.`);
  }
}
```

### Einstellungsmöglichkeiten
Im Tab **Profil → Einstellungen → SOS** kann der Patient auswählen, wer benachrichtigt wird:

- Arzt (Standardkontakt)
- Manuell hinzugefügter Notfallkontakt (Name, Telefonnummer)

---

## 🧩 Trigger-System

### Erstanmeldung
Bei der ersten Anmeldung muss der Patient mindestens einen Triggerpunkt angeben.  
Beispiel: *„Geselligkeit mit Alkohol“*, *„Stress auf Arbeit“*.

### Verwaltung
Arzt und Patient können Trigger später unter **Einstellungen → Trigger** bearbeiten:

- Hinzufügen
- Löschen
- Umbenennen

Trigger dienen später der Rückfallanalyse und Frühwarnung.

---

## 🗺️ Karte mit Anlaufstellen

### Beschreibung
Die Karte zeigt Anlaufstellen, die vom Arzt selbst hinzugefügt werden können.  
Daten werden in Firestore unter `supportLocations` gespeichert.

**Angezeigte Informationen:**
- Name  
- Adresse  
- Öffnungszeiten  
- Notfallnummer  

Der Patient sieht die Karte in der App unter **„Hilfe in meiner Nähe“**.

---

## 🔑 Benutzeroberflächen

### 👤 Patient View (Mobile App)
- Dashboard mit Stimmung & Nüchternheit  
- Kalender  
- SOS-Einstellungen  
- Trigger-Verwaltung  
- Karte mit Hilfsstellen  

### 🧟‍⚕️ Arzt View (Web/App)
- Übersicht aller Patient*innen  
- Tabelle mit Stimmungstrends, Triggern und Warnungen  
- Möglichkeit, neue Patientenaccounts anzulegen  
- Anlegen von Hilfsstellen  

---

# 📘 Firestore Datenbankstruktur – SoberUp App

## 🧟‍♂️ Collection: `users`
> Enthält alle Benutzer (Patient*innen, Ärzt*innen, Admins).  
> Der Zugriff und die Oberfläche hängen von der Rolle ab.

**Pfad:** `users/{userId}`

| Feldname | Typ | Beschreibung |
|-----------|-----|--------------|
| `role` | string | `"patient"`, `"doctor"` oder `"admin"` |
| `name` | string | Vollständiger Name |
| `email` | string | E-Mail-Adresse für Login |
| `soberDays` | number | Anzahl der nüchternen Tage |
| `sosContact` | map | Notfallkontaktinformationen |
| ├─ `name` | string | Name des SOS-Kontakts |
| └─ `phone` | string | Telefonnummer im internationalen Format |
| `triggers` | array(string) | Liste persönlicher Rückfall-Trigger |
| `assignedDoctorId` | string | ID des behandelnden Arztes |
| `createdAt` | timestamp | Zeitpunkt der Registrierung |
| `lastMoodCheck` | timestamp | Zeitpunkt der letzten Stimmungseintragung |

---

### 🔹 Subcollection: `moodEntries`
> Stimmungseinträge pro Tag, vom User manuell gepflegt.  
> Dient zur Kalenderanzeige und Rückfallerkennung.

**Pfad:** `users/{userId}/moodEntries/{entryId}`

| Feldname | Typ | Beschreibung |
|-----------|-----|--------------|
| `date` | timestamp | Datum des Eintrags |
| `moodValue` | number (1–10) | Stimmung (1–3 rot, 4–7 gelb, 8–10 grün) |
| `note` | string (optional) | Freitext-Notiz |
| `createdAt` | timestamp | Zeitpunkt der Erstellung |

---

### 🔹 Subcollection: `notifications`
> Enthält automatisch oder manuell ausgelöste Benachrichtigungen.

**Pfad:** `users/{userId}/notifications/{notificationId}`

| Feldname | Typ | Beschreibung |
|-----------|-----|--------------|
| `type` | string | `"sos"` oder `"info"` |
| `message` | string | Text der Benachrichtigung |
| `timestamp` | timestamp | Zeitpunkt des Ereignisses |
| `status` | string | `"sent"`, `"pending"` oder `"failed"` |

---

## 👨‍⚕️ Collection: `doctors`
> Ärzt*innen mit Zugriff auf ihre zugeordneten Patient*innen.

**Pfad:** `doctors/{doctorId}`

| Feldname | Typ | Beschreibung |
|-----------|-----|--------------|
| `name` | string | Name des Arztes |
| `email` | string | E-Mail-Adresse |
| `assignedPatients` | array(string) | IDs der betreuten Patient*innen |
| `createdAt` | timestamp | Zeitpunkt der Kontoerstellung |

---

## 🏥 Collection: `supportLocations`
> Anlaufstellen und Notfallhilfe – wird von Ärzt*innen gepflegt.

**Pfad:** `supportLocations/{locationId}`

| Feldname | Typ | Beschreibung |
|-----------|-----|--------------|
| `name` | string | Name der Einrichtung |
| `address` | string | Adresse |
| `openingHours` | string | Öffnungszeiten |
| `emergencyNumber` | string | Telefonnummer |
| `createdBy` | string (doctorId) | Arzt, der den Eintrag erstellt hat |
| `createdAt` | timestamp | Zeitpunkt der Erstellung |

---

## ⚙️ Automatische Logik (Firebase Cloud Functions)

| Auslöser | Beschreibung | Aktion |
|-----------|---------------|--------|
| Zwei aufeinanderfolgende `moodValue <= 3` | Möglicher Rückfall erkannt | SMS an `sosContact.phone` senden |
| Neuer Patient erstellt | Zuweisung an behandelnden Arzt | Arzt erhält Benachrichtigung |
| Neue Stimmung gespeichert | Aktualisierung `lastMoodCheck` und `soberDays` | Dashboard aktualisieren |

---

## 🔢 Typenübersicht (Firestore)

| Firestore-Typ | Bedeutung | Beispiel |
|----------------|------------|-----------|
| `string` | Text | `"Max Mustermann"` |
| `number` | Zahl | `5` |
| `boolean` | Wahr/Falsch | `true` |
| `timestamp` | Zeitstempel | `2025-11-10T12:00:00Z` |
| `map` | Objekt mit Schlüsseln | `{ "name": "Dr. X", "phone": "+49..." }` |
| `array` | Liste | `["Stress", "Feiern"]` |

---

## 🔒 Rollen & Berechtigungen

| Rolle | Berechtigungen |
|--------|----------------|
| **Patient** | Eigene Stimmung eintragen, Trigger bearbeiten, SOS-Kontakt festlegen |
| **Arzt** | Patient*innen anlegen, Stimmung überwachen, Anlaufstellen hinzufügen |
| **Admin** | Verwaltung der gesamten Plattform |

---

## 🚨 Beispiel: Automatische SOS-Benachrichtigung

1. Patient trägt Stimmung 2 ein (rot).  
2. Am nächsten Tag erneut 3 oder niedriger → Cloud Function erkennt „zwei rote Tage“.  
3. Nachricht wird per SMS an hinterlegte `sosContact.phone` gesendet.  
4. Gleichzeitig wird ein Dokument in `notifications` erstellt.

---

## 🗍️ Beispiel: Standort (Support Location)

```json
{
  "name": "Klinik Musterstadt",
  "address": "Hauptstraße 12, 12345 Musterstadt",
  "openingHours": "Mo-Fr 8–18 Uhr",
  "emergencyNumber": "+49 123 456789",
  "createdBy": "doctor_abc123",
  "createdAt": "2025-11-10T12:00:00Z"
}
```

