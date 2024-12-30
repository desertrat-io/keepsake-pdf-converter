package config;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class EventStoreDBConfig {

    @ConfigProperty(name = "eventstore.db.uri")
    String eventStoreUri;

    @Produces
    public EventStoreDBClient createEventStoreDBClient() {
        var settings = EventStoreDBConnectionString.parseOrThrow(eventStoreUri);
        return EventStoreDBClient.create(settings);
    }

}
