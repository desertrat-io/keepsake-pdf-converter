package events;

import com.fasterxml.jackson.core.JsonProcessingException;
import entities.PayloadEntity;

import java.util.concurrent.ExecutionException;

public interface Event<T extends PayloadEntity> {

    String getEventName();

    void saveToEventStore(T entityToStore) throws ExecutionException, JsonProcessingException, InterruptedException;

    void saveToEventStore(T entityToStore, String streamName) throws ExecutionException, JsonProcessingException, InterruptedException;
}
