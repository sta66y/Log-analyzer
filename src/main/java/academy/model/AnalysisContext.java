package academy.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Хранит всю статистику проведенного анализа */
public class AnalysisContext {
    private List<String> files;
    private int totalRequestsCount;
    private ResponseSize responseSizeInBytes;
    private List<Resource> resources;
    private List<ResponseCode> responseCodes;
    private List<Date> requestsPerDate;
    private Set<String> uniqueProtocols;
    private LocalDate dateFrom;
    private LocalDate dateTo;

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

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }
}
