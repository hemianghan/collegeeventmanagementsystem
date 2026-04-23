# Notification Feature Troubleshooting Guide

## Where to Find the Notification Bell 🔔

The notification bell is located in the **top-right corner** of the dashboard, in the navbar next to your username.

```
┌─────────────────────────────────────────────────────────┐
│ 🎓 College Event Management          🔔  Welcome, User  │
│                                       ↑                  │
│                              NOTIFICATION BELL HERE      │
└─────────────────────────────────────────────────────────┘
```

## How to See Notifications

### Step 1: Clear Browser Cache
The browser might be showing an old version of the page. **You MUST do this first:**

**Option A: Hard Refresh (Recommended)**
- Press `Ctrl + Shift + R` (Windows/Linux)
- Or `Ctrl + F5` (Windows/Linux)
- Or `Cmd + Shift + R` (Mac)

**Option B: Clear Cache Manually**
1. Press `F12` to open Developer Tools
2. Right-click the refresh button
3. Select "Empty Cache and Hard Reload"

### Step 2: Login Again
1. Go to: http://localhost:8080/login.html
2. Login with:
   - Email: **hemishaanghan1@gmail.com**
   - Password: **admin123**
   - Role: **Admin**

### Step 3: Check the Notification Bell
After logging in, you should see:
- A **bell icon (🔔)** in the top-right corner
- A **red badge** with a number showing unread notifications
- Click the bell to see the dropdown with notifications

## What Notifications Were Created?

The system automatically created **5 sample notifications** for the admin user:
1. ✅ **Event Approved** - "Your event 'Classical Dance Night' has been approved"
2. 🏆 **Certificate Available** - "Your certificate for 'Classical Dance Night' is ready"
3. 📅 **Event Starting Soon** - 3 notifications for upcoming events

## Debugging Steps

### Check Browser Console
1. Press `F12` to open Developer Tools
2. Go to the **Console** tab
3. Look for these messages:
   ```
   Dashboard loaded - User ID: 1 Role: Admin Name: Admin User
   Loading notifications for userId: 1
   Notifications loaded: 5 notifications
   Unread count: 5
   ```

### Check Network Tab
1. Press `F12` to open Developer Tools
2. Go to the **Network** tab
3. Refresh the page
4. Look for these API calls:
   - `/api/notifications/user/1` - Should return 200 OK with 5 notifications
   - `/api/notifications/user/1/unread-count` - Should return `{"unreadCount": 5}`

### Check localStorage
1. Press `F12` to open Developer Tools
2. Go to the **Application** tab (Chrome) or **Storage** tab (Firefox)
3. Click on **Local Storage** → `http://localhost:8080`
4. Verify these values exist:
   - `userId`: should be `1`
   - `userName`: should be `Admin User`
   - `userEmail`: should be `hemishaanghan1@gmail.com`
   - `userRole`: should be `Admin`

## Still Not Working?

### Option 1: Restart the Server
```bash
# Stop the current server (Ctrl+C in the terminal)
cd server
../gradlew bootRun
```

### Option 2: Use Incognito/Private Window
1. Open a new Incognito/Private window
2. Go to http://localhost:8080/login.html
3. Login with admin credentials
4. The notification bell should appear

### Option 3: Check Server Logs
Look at the server console output for:
```
Sample Notifications Generated!
```

If you don't see this message, the notifications weren't created.

## Visual Guide

### What You Should See:

**Before clicking the bell:**
```
🔔 (5)  ← Red badge with number
```

**After clicking the bell:**
```
┌─────────────────────────────────────────┐
│ Notifications      [Mark all as read]   │
├─────────────────────────────────────────┤
│ ✅ Event Approved          2h ago       │
│ Your event 'Classical Dance Night'...   │
│ [Event Approved]                         │
├─────────────────────────────────────────┤
│ 🏆 Certificate Available   3h ago       │
│ Your certificate for 'Classical...      │
│ [Certificate]                            │
├─────────────────────────────────────────┤
│ 📅 Event Starting Soon     10h ago      │
│ 'Tech Symposium' is starting soon...    │
│ [Upcoming]                               │
└─────────────────────────────────────────┘
```

## Contact

If you still can't see the notification bell after following all these steps, please:
1. Take a screenshot of the dashboard
2. Share the browser console output (F12 → Console tab)
3. Share the network tab showing the API calls
