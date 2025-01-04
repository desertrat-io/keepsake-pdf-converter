package entities;

import com.fasterxml.jackson.core.JsonProcessingException;


public class PdfDocumentEntity extends PayloadEntity {

    private String fileKey;
    private String fileName;
    private int numPages;

    public PdfDocumentEntity() {
    }

    public PdfDocumentEntity(String fileKey, String fileName, int numPages) {
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.numPages = numPages;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    @Override
    public byte[] toEventPayload() throws JsonProcessingException {
        return jsonMapper.writeValueAsBytes(this);
    }
}
