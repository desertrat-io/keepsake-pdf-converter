package events;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import entities.PageEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class DocumentSplit implements Event<PageEntity> {

    private final EventStoreDBClient eventStoreDBClient;
    @ConfigProperty(name = "keepsake.default.stream.name")
    String defaultStreamName;
    @ConfigProperty(name = "keepsake.pdf.split.event.name")
    String eventName;

    @Inject
    public DocumentSplit(EventStoreDBClient eventStoreDBClient) {
        this.eventStoreDBClient = eventStoreDBClient;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public void saveToEventStore(PageEntity entityToStore) throws ExecutionException, JsonProcessingException, InterruptedException {
        saveToEventStore(entityToStore, defaultStreamName);
    }

    @Override
    public void saveToEventStore(PageEntity entityToStore, String streamName) throws ExecutionException, JsonProcessingException, InterruptedException {
        try {
            var eventData = EventData
                    .builderAsJson(UUID.randomUUID(), NewDocumentToProcess.class.getCanonicalName(), entityToStore.toEventPayload()).build();
            eventStoreDBClient.appendToStream(streamName, eventData).get();
            Log.info("saved to stream: " + streamName);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            Log.error(e);
            throw e;
        }
    }
}
