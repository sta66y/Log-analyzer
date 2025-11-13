package academy.util;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class AnalysisContext {
    private List<String> files;
    private int totalRequestsCount;
    private ResponseSize responseSizeInBytes;
    private List<Resource> resources;
    private List<ResponseCode> responseCodes;
    private List<Date> requestsPerDate;
    private Set<String> uniqueProtocols;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public int getTotalRequestsCount() {
        return totalRequestsCount;
    }

    public void setTotalRequestsCount(int totalRequestsCount) {
        this.totalRequestsCount = totalRequestsCount;
    }

    public ResponseSize getResponseSizeInBytes() {
        return responseSizeInBytes;
    }

    public void setResponseSizeInBytes(ResponseSize responseSizeInBytes) {
        this.responseSizeInBytes = responseSizeInBytes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<ResponseCode> getResponseCodes() {
        return responseCodes;
    }

    public void setResponseCodes(List<ResponseCode> responseCodes) {
        this.responseCodes = responseCodes;
    }

    public List<Date> getRequestsPerDate() {
        return requestsPerDate;
    }

    public void setRequestsPerDate(List<Date> requestsPerDate) {
        this.requestsPerDate = requestsPerDate;
    }

    public Set<String> getUniqueProtocols() {
        return uniqueProtocols;
    }

    public void setUniqueProtocols(Set<String> uniqueProtocols) {
        this.uniqueProtocols = uniqueProtocols;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }
}
