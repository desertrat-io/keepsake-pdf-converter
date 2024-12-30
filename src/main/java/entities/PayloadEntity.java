package entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public abstract class PayloadEntity {
    protected final JsonMapper jsonMapper = new JsonMapper();

    abstract byte[] toEventPayload() throws JsonProcessingException;
}
