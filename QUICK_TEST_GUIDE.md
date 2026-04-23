# 🚀 QUICK TEST GUIDE - Student Registration Fix

## ⚡ IMMEDIATE TESTING STEPS

### **1. Restart Server**
```bash
# Stop current server (Ctrl+C in terminal)
cd server
..\gradlew bootRun
```

### **2. Test Registration Count**
Open browser: `http://localhost:8080/test-registration-count.html`

Click **"Clear & Create Sample Data"** button

Expected result: "Created 12 sample registrations"

### **3. Test Student Login**
1. Go to: `http://localhost:8080/login.html`
2. **Email**: `student@college.edu`
3. **Password**: `test123` (any password works)
4. **Role**: Student
5. Click **Login**

### **4. Check My Events**
1. In student dashboard, click **"📋 My Events"** in left sidebar
2. **Expected**: Should show **12 registrations** (not 401)
3. Each should show:
   - Event name
   - "✅ Paid" status
   - Download receipt button

### **5. Test Admin Panel (Optional)**
1. Logout and login as admin:
   - **Email**: `hemishaanghan1@gmail.com`
   - **Password**: `admin123`
   - **Role**: Admin
2. Click **"📋 Student Registrations"** in sidebar
3. Select any event from dropdown
4. Should show the sample registrations

## 🔧 If Still Not Working

### **Option A: Manual API Test**
Open browser console (F12) and run:
```javascript
// Clear all registrations
fetch('/api/registrations/clear-all', {method: 'DELETE'})
  .then(r => r.json())
  .then(console.log);

// Create sample data
fetch('/api/registrations/create-sample', {method: 'POST'})
  .then(r => r.json())
  .then(console.log);

// Check student count
fetch('/api/registrations/user/1001/detailed')
  .then(r => r.json())
  .then(data => console.log('Student registrations:', data.length));
```

### **Option B: Check Server Logs**
Look for these messages in server console:
```
Cleared all existing registrations - My Events count reset to 0
Created 12 sample student registrations for test student (ID: 1001)
```

### **Option C: Database Check**
1. Go to: `http://localhost:8080/h2-console`
2. **JDBC URL**: `jdbc:h2:file:./data/eventdb`
3. **Username**: `sa`
4. **Password**: (empty)
5. Run SQL: `SELECT COUNT(*) FROM REGISTRATIONS WHERE USER_ID = 1001;`
6. Should return: **12**

## ✅ SUCCESS CRITERIA

- ✅ Student sees **12 events** in "My Events"
- ✅ Admin panel shows **all registrations** (unchanged)
- ✅ Test page shows **12 registrations** for user 1001
- ✅ No errors in server console

## 🆘 TROUBLESHOOTING

### **Problem**: Still seeing 401 events
**Solution**: 
1. Make sure you're logged in as `student@college.edu`
2. Clear browser cache (Ctrl+Shift+Delete)
3. Restart server completely

### **Problem**: "Reset Student Data" button not visible
**Solution**: 
1. Make sure you're logged in as **Admin**
2. Check left sidebar under "FEATURES" section

### **Problem**: API endpoints not working
**Solution**: 
1. Verify server is running on port 8080
2. Check server console for errors
3. Try restarting server

---

## 📞 QUICK VERIFICATION

**Run this in browser console on dashboard:**
```javascript
fetch('/api/registrations/user/1001/detailed')
  .then(r => r.json())
  .then(data => alert(`Student has ${data.length} registrations`));
```

**Expected**: Alert showing "Student has 12 registrations"