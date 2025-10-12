package com.example.veb_app.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface EventDao {
    
    @Query("SELECT * FROM events ORDER BY eventDate ASC, time ASC")
    List<EventEntity> getAllEvents();
    
    @Query("SELECT * FROM events WHERE id = :id")
    EventEntity getEventById(int id);
    
    @Query("SELECT * FROM events WHERE eventDate = :eventDate ORDER BY time ASC")
    List<EventEntity> getEventsByDate(long eventDate);
    
    @Query("SELECT * FROM events WHERE eventDate >= :startDate AND eventDate <= :endDate ORDER BY eventDate ASC, time ASC")
    List<EventEntity> getEventsByDateRange(long startDate, long endDate);
    
    @Query("SELECT * FROM events WHERE category = :category ORDER BY eventDate ASC")
    List<EventEntity> getEventsByCategory(String category);
    
    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY eventDate ASC")
    List<EventEntity> searchEvents(String query);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEvent(EventEntity event);
    
    @Update
    void updateEvent(EventEntity event);
    
    @Delete
    void deleteEvent(EventEntity event);
    
    @Query("DELETE FROM events WHERE id = :id")
    void deleteEventById(int id);
    
    @Query("SELECT COUNT(*) FROM events")
    int getEventsCount();
    
    @Query("SELECT DISTINCT category FROM events ORDER BY category")
    List<String> getAllEventCategories();
}
