package nmc.assignments.msassignment.entity;

import lombok.Getter;

@Getter
public class FileInformation {
    private final String contentType;
    private final String filename;
    private final String path;
    private final long size;

    public FileInformation(final String contentType, final String filename, final String path, final long size) {
        this.contentType = contentType;
        this.filename = filename;
        this.path = path;
        this.size = size;
    }

    @Override
    public String toString() {
        return "FileInformation{" +
            "contentType='" + contentType + '\'' +
            ", filename='" + filename + '\'' +
            ", path='" + path + '\'' +
            ", size=" + size +
            '}';
    }
}
