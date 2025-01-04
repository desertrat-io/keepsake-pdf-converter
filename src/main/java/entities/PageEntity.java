package entities;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PageEntity extends PayloadEntity {

    private String fileName;
    private String fileFinalLocation;
    private Integer pageNum;
    private Integer pageFileSize;
    private String containingPath;

    public PageEntity() {
    }

    public PageEntity(String fileName, String fileFinalLocation, Integer pageNum, Integer pageFileSize, String containingPath) {
        this.fileName = fileName;
        this.fileFinalLocation = fileFinalLocation;
        this.pageNum = pageNum;
        this.pageFileSize = pageFileSize;
        this.containingPath = containingPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileFinalLocation() {
        return fileFinalLocation;
    }

    public void setFileFinalLocation(String fileFinalLocation) {
        this.fileFinalLocation = fileFinalLocation;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public String getContainingPath() {
        return containingPath;
    }

    public void setContainingPath(String containingPath) {
        this.containingPath = containingPath;
    }

    public Integer getPageFileSize() {
        return pageFileSize;
    }

    public void setPageFileSize(Integer pageFileSize) {
        this.pageFileSize = pageFileSize;
    }

    @Override
    public byte[] toEventPayload() throws JsonProcessingException {
        return jsonMapper.writeValueAsBytes(this);
    }
}
