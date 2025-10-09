# 🔍 Debugging Guide for Project Requests Not Showing

## Step 1: Check Logcat Output

Run your app and check **Logcat** for these messages:

### When Loading Requests (SupervisorRequestsActivity):

```
============ USER DATA ============
Supervisor ID: 'YOUR_ID_HERE'
Supervisor Name: 'YOUR_NAME'
Supervisor Email: 'YOUR_EMAIL'
===================================

========== STARTING QUERY ==========
Querying with supervisorId: 'YOUR_ID_HERE'
Collection: project_requests
====================================

📊 DATABASE CHECK:
Total documents in project_requests (first 5): X
✅ Sample documents found:
  [0] Doc ID: xxx
      supervisorId: 'STORED_ID'
      projectTitle: 'Project Name'
      status: 'pending'
      Match? true/false  <-- THIS IS KEY!
```

## Step 2: Compare IDs

Look for this line in the logs:
```
Match? true/false
```

### If Match = FALSE:
The supervisorId you're querying **doesn't match** what's stored!

**Check for:**
- ❌ Extra spaces: `"abc123 "` vs `"abc123"`
- ❌ Different document IDs vs custom IDs
- ❌ Email address stored instead of ID
- ❌ Null values

### If Match = TRUE but still no results:
Check the query result:
```
✅ QUERY SUCCESSFUL!
Documents found: 0  <-- Should be > 0
```

## Step 3: Create a Test Request

1. Open the app and send a **new test request** to the supervisor
2. Check logcat for:
```
CREATE_REQUEST: Storing supervisorId: 'XXX'
```
3. Compare this with the query:
```
QUERY: whereEqualTo('supervisorId', 'YYY')
```

**They MUST match exactly!**

## Step 4: Check Firebase Console

1. Open Firebase Console → Firestore Database
2. Go to `project_requests` collection
3. Click on any document
4. Check the `supervisorId` field value
5. Copy the exact value (with quotes if needed)
6. Compare with what's in your logcat

## Step 5: Common Issues & Fixes

### Issue 1: No Data in Database
**Symptom:** `Total documents in project_requests: 0`

**Fix:** Send a test request first from the student/user side

### Issue 2: supervisorId Mismatch
**Symptom:** `Match? false`

**Fix:** Update how you store the supervisorId to match how you query it

Example - In `RequestSupervisorActivity.kt`, make sure:
```kotlin
// When getting supervisor from intent
supervisorId = intent.getStringExtra("supervisor_id") ?: ""

// When storing in Firestore - MUST use the same ID
"supervisorId" to supervisorId  // <-- Check this matches!
```

### Issue 3: Firestore Rules
**Symptom:** Error like "Missing or insufficient permissions"

**Fix:** Update Firestore Rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /project_requests/{request} {
      allow read: if true;  // Temporary for debugging
      allow write: if true;
    }
  }
}
```

### Issue 4: Wrong Field Name
**Symptom:** Query succeeds but returns 0 documents

**Fix:** Check if field is named differently:
- `supervisorId` vs `supervisor_id` (case-sensitive!)
- `supervisorId` vs `supervisor` 
- Check exact spelling

## Step 6: SQL-Style Debug Query

Add this temporary code to see ALL requests:

```kotlin
db.collection("project_requests")
    .get()
    .addOnSuccessListener { docs ->
        docs.forEach { doc ->
            Log.d("DEBUG_ALL", "ID: ${doc.id}, supervisorId: ${doc.getString("supervisorId")}")
        }
    }
```

## Expected Logcat Output

### ✅ GOOD (Working):
```
============ USER DATA ============
Supervisor ID: 'sup123'
===================================

📊 DATABASE CHECK:
Total documents: 3
  [0] supervisorId: 'sup123'  <-- Match!
      Match? true

✅ QUERY SUCCESSFUL!
Documents found: 3
✅ Added: 'AI Project' - pending
✅ Added: 'Web App' - accepted
Final list size: 3
UI: Showing 3 requests
```

### ❌ BAD (Not Working):
```
============ USER DATA ============
Supervisor ID: 'sup123'
===================================

📊 DATABASE CHECK:
Total documents: 3
  [0] supervisorId: 'supervisor_456'  <-- No Match!
      Match? false

✅ QUERY SUCCESSFUL!
Documents found: 0  <-- No matches!
Final list size: 0
UI: Showing 'No results' view
```

## Next Steps

1. Run the app
2. Check logcat output
3. Look for the "Match?" line
4. Report back what you see!

The logs will tell us exactly what's wrong! 🔍
