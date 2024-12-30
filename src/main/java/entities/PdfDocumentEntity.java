package entities;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PdfDocumentEntity extends PayloadEntity {

    private final String fileKey;
    private final String fileName;

    public PdfDocumentEntity(String fileKey, String fileName) {
        this.fileKey = fileKey;
        this.fileName = fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] toEventPayload() throws JsonProcessingException {
        return jsonMapper.writeValueAsBytes(this);
    }
}
