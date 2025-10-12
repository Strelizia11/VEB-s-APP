package com.example.veb_app.data;

import android.content.Context;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {
    private EventDao eventDao;
    private ExecutorService executor;
    
    public EventRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        eventDao = database.eventDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public void getAllEvents(DataCallback<List<EventEntity>> callback) {
        executor.execute(() -> {
            try {
                List<EventEntity> events = eventDao.getAllEvents();
                callback.onSuccess(events);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getEventById(int id, DataCallback<EventEntity> callback) {
        executor.execute(() -> {
            try {
                EventEntity event = eventDao.getEventById(id);
                callback.onSuccess(event);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getEventsByDate(Date date, DataCallback<List<EventEntity>> callback) {
        executor.execute(() -> {
            try {
                long dateMillis = date.getTime();
                List<EventEntity> events = eventDao.getEventsByDate(dateMillis);
                callback.onSuccess(events);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getEventsByDateRange(Date startDate, Date endDate, DataCallback<List<EventEntity>> callback) {
        executor.execute(() -> {
            try {
                long start = startDate.getTime();
                long end = endDate.getTime();
                List<EventEntity> events = eventDao.getEventsByDateRange(start, end);
                callback.onSuccess(events);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getEventsByCategory(String category, DataCallback<List<EventEntity>> callback) {
        executor.execute(() -> {
            try {
                List<EventEntity> events = eventDao.getEventsByCategory(category);
                callback.onSuccess(events);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void searchEvents(String query, DataCallback<List<EventEntity>> callback) {
        executor.execute(() -> {
            try {
                List<EventEntity> events = eventDao.searchEvents(query);
                callback.onSuccess(events);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertEvent(EventEntity event, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = eventDao.insertEvent(event);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateEvent(EventEntity event, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                eventDao.updateEvent(event);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteEvent(EventEntity event, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                eventDao.deleteEvent(event);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteEventById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                eventDao.deleteEventById(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getEventsCount(DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = eventDao.getEventsCount();
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getAllEventCategories(DataCallback<List<String>> callback) {
        executor.execute(() -> {
            try {
                List<String> categories = eventDao.getAllEventCategories();
                callback.onSuccess(categories);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception error);
    }
}
