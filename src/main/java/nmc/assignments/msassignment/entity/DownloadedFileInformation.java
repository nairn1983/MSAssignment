package nmc.assignments.msassignment.entity;

import lombok.Getter;
import org.springframework.core.io.Resource;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final DownloadedFileInformation that = (DownloadedFileInformation) o;
        return size == that.size && Objects.equals(contentType, that.contentType) && Objects.equals(filename, that.filename) && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, filename, resource, size);
    }
}
