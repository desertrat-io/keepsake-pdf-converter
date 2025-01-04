package entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public abstract class PayloadEntity {
    protected final JsonMapper jsonMapper = new JsonMapper();

    // this is why LSB and traits are something that Java really needs
    public abstract byte[] toEventPayload() throws JsonProcessingException;
}
