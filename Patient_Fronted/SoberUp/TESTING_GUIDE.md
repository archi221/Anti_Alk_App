    # Testing Guide - SoberUp Login

## Quick Test Checklist

Before testing, make sure you've completed these steps:

- [ ] Firestore Database is enabled in Firebase Console
- [ ] Security rules allow read access (for testing: `allow read: if true`)
- [ ] Created a `users` collection in Firestore
- [ ] Added at least one test user document with `username`, `password`, and `role` fields
- [ ] Built and synced your Android project

## Step-by-Step Testing Instructions

### Step 1: Verify Firebase Setup

1. **Open Firebase Console**
   - Go to: https://console.firebase.google.com/
   - Select project: **soberup-a9be3**

2. **Check Firestore Database**
   - Click on **"Firestore Database"** in the left sidebar
   - You should see your database (if not, create it following the main guide)

3. **Verify Users Collection**
   - Look for a collection named **`users`** (exactly lowercase)
   - If it doesn't exist, create it:
     - Click **"Start collection"**
     - Collection ID: `users`
     - Click **"Next"**

### Step 2: Add a Test User

1. **In Firestore Database**, click on the **`users`** collection
2. Click **"Add document"**
3. **Document ID**: You can either:
   - Click **"Auto-ID"** (recommended for first test)
   - OR enter a custom ID (e.g., "testuser1")
4. **Add these fields** (click "Add field" for each):
   
   | Field Name | Type | Value |
   |------------|------|-------|
   | `username` | string | `testuser` |
   | `password` | string | `testpass123` |
   | `role` | string | `user` |

5. Click **"Save"**

**Example Test Users:**
```
User 1:
- username: "admin"
- password: "admin123"
- role: "admin"

User 2:
- username: "testuser"
- password: "testpass123"
- role: "user"
```

### Step 3: Verify Security Rules

1. Go to **Firestore Database** → **Rules** tab
2. Make sure you have these rules for testing:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```
3. Click **"Publish"** if you made changes

### Step 4: Build and Run the App

1. **In Android Studio:**
   - Click **"Sync Project with Gradle Files"** (if needed)
   - Wait for sync to complete
   - Connect your Android device or start an emulator
   - Click **"Run"** (green play button) or press `Shift + F10`

2. **Wait for the app to install and launch**
   - You should see the login screen with:
     - "SoberUp" title
     - "Login" subtitle
     - Username field
     - Password field
     - Login button

### Step 5: Test Login

1. **Enter Test Credentials:**
   - Username: `testuser` (or whatever username you created)
   - Password: `testpass123` (or whatever password you created)

2. **Click the "Login" button**
   - You should see a loading indicator
   - After a moment, you should see a Toast message: "Welcome testuser! Role: user"

3. **Test Invalid Credentials:**
   - Try wrong username: `wronguser` / `testpass123`
   - Try wrong password: `testuser` / `wrongpass`
   - You should see error message: "Invalid username or password"

## Expected Results

### ✅ Successful Login
- Loading indicator appears briefly
- Toast message shows: "Welcome [username]! Role: [role]"
- No error messages

### ❌ Failed Login (Invalid Credentials)
- Error message appears: "Invalid username or password"
- Login button becomes enabled again

### ❌ Failed Login (Network/Connection Error)
- Error message appears: "Login failed: [error message]"
- Check your internet connection
- Verify Firebase is properly configured

## Troubleshooting

### Issue: "Invalid username or password" even with correct credentials

**Check:**
1. **Field names are exact** (case-sensitive):
   - `username` (lowercase)
   - `password` (lowercase)
   - `role` (lowercase)

2. **Collection name is exact**: `users` (lowercase, plural)

3. **No extra spaces**: Check username/password in Firestore for leading/trailing spaces

4. **Check Logcat** (Android Studio bottom panel):
   - Look for error messages
   - Filter by "Firebase" or "AuthRepository"

### Issue: "Permission denied" error

**Solution:**
1. Go to Firestore → Rules
2. Make sure rules allow read: `allow read: if true;`
3. Click **"Publish"**
4. Wait a few seconds for rules to propagate
5. Try again

### Issue: App crashes on launch

**Check:**
1. **Internet permission** - Should be automatically added, but verify in AndroidManifest.xml
2. **Google Services** - Make sure `google-services.json` is in `app/` folder
3. **Gradle sync** - Try: File → Sync Project with Gradle Files
4. **Clean build** - Build → Clean Project, then Build → Rebuild Project

### Issue: "No documents found" or query returns empty

**Check:**
1. Collection name is exactly `users` (not `Users` or `USERS`)
2. Field name is exactly `username` (not `userName` or `Username`)
3. User document exists in Firestore
4. Try creating an index (Firestore → Indexes → Create Index)

### Issue: Loading forever / No response

**Check:**
1. **Internet connection** - App needs internet to connect to Firestore
2. **Firebase project** - Verify you're using the correct Firebase project
3. **Check Logcat** for Firebase connection errors
4. **Try restarting the app**

## Debug Tips

### View Logcat Output
1. In Android Studio, open **Logcat** (bottom panel)
2. Filter by: `AuthRepository` or `Firebase`
3. Look for error messages or connection issues

### Verify User in Firestore
1. Go to Firebase Console → Firestore Database
2. Click on `users` collection
3. Verify your test user document exists
4. Click on the document to see all fields
5. Make sure field types are correct (all should be "string")

### Test with Multiple Users
Try creating multiple test users with different roles:
- Admin user: `admin` / `admin123` / `admin`
- Regular user: `user1` / `pass123` / `user`
- Test different roles to verify role-based functionality later

## Next Steps After Successful Login

Once login works:
1. ✅ You'll see the welcome Toast message
2. Next: Implement navigation to different screens based on user role
3. Add session management (remember logged-in user)
4. Create home screen for authenticated users

## Quick Reference: Test Credentials

Create these users in Firestore for testing:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | admin |
| testuser | testpass123 | user |
| demo | demo123 | user |

---

**Still having issues?** Check the Logcat output in Android Studio for detailed error messages!

