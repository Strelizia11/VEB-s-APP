package com.example.veb_app.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(
    entities = {
        NoteEntity.class,
        ChecklistEntity.class,
        ChecklistTaskEntity.class,
        TransactionEntity.class,
        EventEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract NoteDao noteDao();
    public abstract ChecklistDao checklistDao();
    public abstract ChecklistTaskDao checklistTaskDao();
    public abstract TransactionDao transactionDao();
    public abstract EventDao eventDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "veb_app_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
