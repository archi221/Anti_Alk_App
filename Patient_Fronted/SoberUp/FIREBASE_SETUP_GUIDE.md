# Firebase Setup Guide for SoberUp Login

## Overview
This guide will help you configure Firebase Firestore for user authentication in your SoberUp Android application. Since users cannot register themselves, you'll be adding accounts directly to the Firestore database.

## Step 1: Firebase Console Setup

### 1.1 Access Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **soberup-a9be3**

### 1.2 Enable Firestore Database
1. In the left sidebar, click on **"Firestore Database"**
2. Click **"Create database"** (if not already created)
3. Choose **"Start in test mode"** for development (you can secure it later)
4. Select a location closest to your users
5. Click **"Enable"**

### 1.3 Configure Firestore Security Rules
1. Go to **Firestore Database** → **Rules** tab
2. For development, you can use these rules (⚠️ **NOT for production**):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow write: if false; // Only admins can write via console
    }
  }
}
```

3. For production, use more secure rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null; // Only authenticated users
      allow write: if false; // No client-side writes
    }
  }
}
```

## Step 2: Create Users Collection Structure

### 2.1 Create Collection
1. In Firestore Database, click **"Start collection"**
2. Collection ID: **`users`**
3. Click **"Next"**

### 2.2 Add First User Document
1. Document ID: You can use the username or auto-generate (we'll query by username)
2. Add the following fields:
   - **Field name:** `username` | **Type:** string | **Value:** (e.g., "admin")
   - **Field name:** `password` | **Type:** string | **Value:** (e.g., "password123")
   - **Field name:** `role` | **Type:** string | **Value:** (e.g., "admin" or "user")
3. Click **"Save"**

### 2.3 Example User Structure
```
Collection: users
├── Document ID: (auto-generated or username)
    ├── username: "admin"
    ├── password: "admin123"
    └── role: "admin"
```

## Step 3: Add Index for Username Queries (Optional but Recommended)

1. Go to **Firestore Database** → **Indexes** tab
2. Click **"Create Index"**
3. Collection ID: **`users`**
4. Fields to index:
   - Field: `username` | Order: Ascending
5. Click **"Create"**

## Step 4: Security Considerations

### ⚠️ Important Security Notes:

1. **Password Storage**: Currently, passwords are stored in plain text. For production:
   - Use Firebase Authentication (recommended)
   - OR hash passwords using bcrypt or similar before storing
   - Never store plain text passwords in production

2. **Network Security**: 
   - Ensure your app uses HTTPS
   - Consider implementing certificate pinning

3. **Firestore Rules**: 
   - Restrict read access to authenticated users only
   - Never allow client-side password updates

## Step 5: Testing Your Setup

1. Add a test user in Firestore:
   - Username: `testuser`
   - Password: `testpass`
   - Role: `user`

2. Run your Android app
3. Try logging in with the test credentials

## Step 6: Adding More Users

To add users manually:
1. Go to Firestore Database
2. Click on the **`users`** collection
3. Click **"Add document"**
4. Add fields: `username`, `password`, `role`
5. Save

## Troubleshooting

### Issue: "Permission denied" error
- **Solution**: Check Firestore security rules allow read access

### Issue: "No documents found"
- **Solution**: Verify the collection name is exactly `users` (lowercase)
- Check that documents have the correct field names: `username`, `password`, `role`

### Issue: Query not working
- **Solution**: Create an index for the `username` field in Firestore

## Next Steps

After setting up the login:
1. Implement password hashing for security
2. Add session management (SharedPreferences or DataStore)
3. Create role-based navigation
4. Add logout functionality
5. Implement password reset (if needed)

