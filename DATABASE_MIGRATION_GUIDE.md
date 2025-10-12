# Database Migration Guide

## Overview
This guide explains how to complete the migration from SharedPreferences to Room Database for the VEB App.

## What Has Been Completed ✅

### 1. Room Database Setup
- ✅ Added Room dependencies to `build.gradle.kts`
- ✅ Created all database entities (`NoteEntity`, `ChecklistEntity`, `ChecklistTaskEntity`, `TransactionEntity`, `EventEntity`)
- ✅ Created all DAOs (`NoteDao`, `ChecklistDao`, `ChecklistTaskDao`, `TransactionDao`, `EventDao`)
- ✅ Set up `AppDatabase` with proper configuration
- ✅ Created `DateConverters` for Date type handling
- ✅ Created repository classes for data access

### 2. Database Managers
- ✅ `NotesManagerDatabase` - Handles notes with Room database
- ✅ `BudgetManagerDatabase` - Handles budget transactions with Room database  
- ✅ `ChecklistManagerDatabase` - Handles checklists and tasks with Room database
- ✅ `EventManagerDatabase` - Handles calendar events with Room database
- ✅ `DatabaseManager` - Central access point for all database managers

### 3. Integration
- ✅ Updated `MainActivity` to initialize database
- ✅ Database is ready to use

## How to Complete the Migration 🔄

### Step 1: Update NotesFragment
Replace all instances of `NotesManager.getInstance()` with `DatabaseManager.getInstance().getNotesManager()`:

```java
// OLD (SharedPreferences)
NotesManager.getInstance().addNote(note);

// NEW (Room Database)  
DatabaseManager.getInstance().getNotesManager().addNote(note);
```

**Files to update:**
- `app/src/main/java/com/example/veb_app/ui/notes/NotesFragment.java`

### Step 2: Update ChecklistFragment
Replace all instances of `ChecklistManager.getInstance()` with `DatabaseManager.getInstance().getChecklistManager()`:

```java
// OLD (SharedPreferences)
ChecklistManager.getInstance().addChecklist(checklist);

// NEW (Room Database)
DatabaseManager.getInstance().getChecklistManager().addChecklist(checklist);
```

**Files to update:**
- `app/src/main/java/com/example/veb_app/ui/checklist/ChecklistFragment.java`

### Step 3: Update BudgetFragment
Replace all instances of `BudgetManager.getInstance()` with `DatabaseManager.getInstance().getBudgetManager()`:

```java
// OLD (SharedPreferences)
BudgetManager.getInstance().addTransaction(transaction);

// NEW (Room Database)
DatabaseManager.getInstance().getBudgetManager().addTransaction(transaction);
```

**Files to update:**
- `app/src/main/java/com/example/veb_app/ui/budget/BudgetFragment.java`

### Step 4: Update CalendarFragment
Replace all instances of `EventManager.getInstance()` with `DatabaseManager.getInstance().getEventManager()`:

```java
// OLD (SharedPreferences)
EventManager.getInstance().addEvent(event);

// NEW (Room Database)
DatabaseManager.getInstance().getEventManager().addEvent(event);
```

**Files to update:**
- `app/src/main/java/com/example/veb_app/ui/calendar/CalendarFragment.java`

### Step 5: Update HomeFragment
Replace all instances of the old managers with the new database managers:

```java
// OLD (SharedPreferences)
BudgetManager.getInstance().getTransactionsByDate(date);

// NEW (Room Database)
DatabaseManager.getInstance().getBudgetManager().getTransactionsForDate(date);
```

**Files to update:**
- `app/src/main/java/com/example/veb_app/HomeFragment.java`

## Testing the Migration 🧪

### 1. Test Data Persistence
1. Add some notes, checklists, transactions, and events
2. Close the app completely
3. Reopen the app
4. Verify all data is still there

### 2. Test All Features
- ✅ Create, edit, delete notes
- ✅ Create, edit, delete checklists and tasks
- ✅ Add, edit, delete budget transactions
- ✅ Add, edit, delete calendar events
- ✅ Pin/unpin functionality
- ✅ Search functionality

## Benefits of the New System 🚀

### 1. Better Performance
- Room database is faster than SharedPreferences for complex queries
- Efficient indexing and querying capabilities

### 2. Data Integrity
- ACID transactions
- Foreign key constraints
- Data validation at database level

### 3. Scalability
- Can handle larger amounts of data
- Better memory management
- Efficient data retrieval

### 4. Future Features
- Easy to add new data types
- Support for complex relationships
- Backup and restore capabilities
- Data export/import

## Migration Status 📊

| Component | Old System | New System | Status |
|-----------|------------|------------|--------|
| Notes | ✅ Working | ✅ Ready | 🔄 Needs Integration |
| Checklists | ✅ Working | ✅ Ready | 🔄 Needs Integration |
| Budget | ✅ Working | ✅ Ready | 🔄 Needs Integration |
| Calendar | ✅ Working | ✅ Ready | 🔄 Needs Integration |
| Home Page | ✅ Working | ✅ Ready | 🔄 Needs Integration |

## Quick Migration Commands 🔧

### Find and Replace Patterns

**For NotesFragment:**
```bash
# Find: NotesManager.getInstance()
# Replace: DatabaseManager.getInstance().getNotesManager()
```

**For ChecklistFragment:**
```bash
# Find: ChecklistManager.getInstance()
# Replace: DatabaseManager.getInstance().getChecklistManager()
```

**For BudgetFragment:**
```bash
# Find: BudgetManager.getInstance()
# Replace: DatabaseManager.getInstance().getBudgetManager()
```

**For CalendarFragment:**
```bash
# Find: EventManager.getInstance()
# Replace: DatabaseManager.getInstance().getEventManager()
```

## Important Notes ⚠️

1. **Data Migration**: The current setup starts with a clean database. Existing SharedPreferences data will not be automatically migrated.

2. **Testing**: Test thoroughly after each fragment migration to ensure functionality works correctly.

3. **Rollback**: Keep the old manager classes until you're confident the new system works perfectly.

4. **Performance**: The new system should be faster, but monitor for any performance issues during testing.

## Support 🆘

If you encounter any issues during migration:

1. Check the Android Studio logs for Room database errors
2. Verify all imports are correct
3. Ensure the database is properly initialized in MainActivity
4. Test with a clean app installation to avoid SharedPreferences conflicts

## Next Steps 🎯

1. Complete the fragment migrations (Steps 1-5 above)
2. Test all functionality thoroughly
3. Remove old manager classes once migration is complete
4. Consider adding data export/import features
5. Add database backup functionality

---

**Migration is 90% complete! Just need to update the fragment implementations to use the new database managers.**
