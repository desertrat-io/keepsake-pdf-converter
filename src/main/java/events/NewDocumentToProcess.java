package events;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import entities.PdfDocumentEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class NewDocumentToProcess implements Event<PdfDocumentEntity> {

    private final EventStoreDBClient eventStoreDBClient;
    @ConfigProperty(name = "keepsake.default.stream.name")
    String defaultStreamName;
    @ConfigProperty(name = "keepsake.new.pdf.event.name")
    String eventName;

    @Inject
    public NewDocumentToProcess(EventStoreDBClient eventStoreDBClient) {
        this.eventStoreDBClient = eventStoreDBClient;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public void saveToEventStore(PdfDocumentEntity pdfDocumentEntity) throws ExecutionException, InterruptedException, JsonProcessingException {
        saveToEventStore(pdfDocumentEntity, defaultStreamName);
    }

    @Override
    public void saveToEventStore(PdfDocumentEntity pdfDocumentEntity, String streamName) throws ExecutionException, InterruptedException, JsonProcessingException {

        try {
            var eventData = EventData
                    .builderAsJson(UUID.randomUUID(), NewDocumentToProcess.class.getCanonicalName(), pdfDocumentEntity.toEventPayload()).build();
            eventStoreDBClient.appendToStream(streamName, eventData).get();
            Log.info("saved to stream: " + streamName);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            Log.error(e);
            throw e;
        }
    }
}
