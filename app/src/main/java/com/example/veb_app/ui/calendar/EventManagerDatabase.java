package com.example.veb_app.ui.calendar;

import android.content.Context;
import com.example.veb_app.data.EventEntity;
import com.example.veb_app.data.EventRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EventManagerDatabase {
    private static EventManagerDatabase instance;
    private EventRepository eventRepository;
    private List<Event> events;
    private Context context;
    
    private EventManagerDatabase(Context context) {
        this.context = context.getApplicationContext();
        this.eventRepository = new EventRepository(this.context);
        this.events = new ArrayList<>();
        loadEvents();
    }
    
    public static synchronized EventManagerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new EventManagerDatabase(context);
        }
        return instance;
    }
    
    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.DataCallback<List<EventEntity>>() {
            @Override
            public void onSuccess(List<EventEntity> eventEntities) {
                events.clear();
                for (EventEntity entity : eventEntities) {
                    events.add(convertEntityToEvent(entity));
                }
                Collections.sort(events, (e1, e2) -> 
                    Long.compare(e1.getDate().getTime(), e2.getDate().getTime()));
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("EventManagerDatabase", "Error loading events", error);
            }
        });
    }
    
    public void addEvent(Event event, Callback callback) {
        EventEntity entity = convertEventToEntity(event);
        eventRepository.insertEvent(entity, new EventRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                event.setId(id);
                events.add(event);
                sortEvents();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("EventManagerDatabase", "Error adding event", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void updateEvent(Event event, Callback callback) {
        EventEntity entity = convertEventToEntity(event);
        eventRepository.updateEvent(entity, new EventRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Update local list
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getId() == event.getId()) {
                        events.set(i, event);
                        break;
                    }
                }
                sortEvents();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("EventManagerDatabase", "Error updating event", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void deleteEvent(Event event, Callback callback) {
        EventEntity entity = convertEventToEntity(event);
        eventRepository.deleteEvent(entity, new EventRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                events.removeIf(e -> e.getId() == event.getId());
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("EventManagerDatabase", "Error deleting event", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    public List<Event> getEventsByDate(Date date) {
        List<Event> result = new ArrayList<>();
        for (Event event : events) {
            if (isSameDate(event.getDate(), date)) {
                result.add(event);
            }
        }
        return result;
    }
    
    public List<Event> getEventsByCategory(String category) {
        List<Event> result = new ArrayList<>();
        for (Event event : events) {
            if (event.getCategory().equals(category)) {
                result.add(event);
            }
        }
        return result;
    }
    
    public List<String> getEventCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Work");
        categories.add("Health");
        categories.add("Education");
        categories.add("Social");
        categories.add("Other");
        return categories;
    }
    
    public List<String> getEventColors() {
        List<String> colors = new ArrayList<>();
        colors.add("#2196F3"); // Blue
        colors.add("#4CAF50"); // Green
        colors.add("#FF9800"); // Orange
        colors.add("#F44336"); // Red
        colors.add("#9C27B0"); // Purple
        colors.add("#00BCD4"); // Cyan
        return colors;
    }
    
    public void refreshEvents(Callback callback) {
        loadEvents();
        if (callback != null) callback.onSuccess();
    }
    
    private void sortEvents() {
        Collections.sort(events, (e1, e2) -> 
            Long.compare(e1.getDate().getTime(), e2.getDate().getTime()));
    }
    
    private boolean isSameDate(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(date2);
        
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
               cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }
    
    private EventEntity convertEventToEntity(Event event) {
        EventEntity entity = new EventEntity(
            event.getTitle(),
            event.getDescription(),
            event.getTime(),
            event.getCategory(),
            event.getColor(),
            event.isAllDay(),
            event.getDate().getTime()
        );
        entity.setId((int) event.getId());
        entity.setCreatedDate(System.currentTimeMillis());
        entity.setModifiedDate(System.currentTimeMillis());
        return entity;
    }
    
    private Event convertEntityToEvent(EventEntity entity) {
        Event event = new Event(
            entity.getTitle(),
            entity.getDescription(),
            entity.getEventDateAsDate(),
            entity.getTime(),
            entity.getCategory(),
            entity.getColor(),
            entity.isAllDay()
        );
        event.setId(entity.getId());
        return event;
    }
    
    public interface Callback {
        void onSuccess();
        void onError(Exception error);
    }
}
