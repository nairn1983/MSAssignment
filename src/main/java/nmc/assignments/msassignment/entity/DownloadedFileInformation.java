package nmc.assignments.msassignment.entity;

import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
public class DownloadedFileInformation {
    private final String contentType;
    private final String filename;
    private final Resource resource;
    private final long size;

    public DownloadedFileInformation(final String contentType, final String filename, final Resource resource, final long size) {
        this.contentType = contentType;
        this.filename = filename;
        this.resource = resource;
        this.size = size;
    }

    @Override
    public String toString() {
        return "DownloadedFileInformation{" +
            "contentType='" + contentType + '\'' +
            ", filename='" + filename + '\'' +
            ", resource=" + resource +
            ", size=" + size +
            '}';
    }
}
