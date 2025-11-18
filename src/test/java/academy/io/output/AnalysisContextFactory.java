package academy.io.output;

import academy.model.AnalysisContext;
import academy.model.Date;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.model.ResponseSize;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AnalysisContextFactory {

    public static AnalysisContext createTestContext() {
        AnalysisContext context = new AnalysisContext();

        context.setFiles(List.of("access.log", "http://example.com/access.log"));

        context.setDateFrom(LocalDate.now().minusDays(7));
        context.setDateTo(LocalDate.now());

        context.setTotalRequestsCount(10000);
        context.setResponseSizeInBytes(new ResponseSize(500.0, 1000, 950));

        context.setResources(
                List.of(new Resource("/downloads/product_1", 1000), new Resource("/downloads/product_2", 100)));

        context.setResponseCodes(
                List.of(new ResponseCode(200, 1000), new ResponseCode(401, 10), new ResponseCode(500, 1)));

        context.setRequestsPerDate(List.of(new Date("2024-03-01", "Monday", 2981, 12.1)));

        context.setUniqueProtocols(Set.of("HTTP/1.1", "HTTP/2.0", "grpc"));

        return context;
    }

    public static AnalysisContext createEmptyContext() {
        AnalysisContext context = new AnalysisContext();
        context.setFiles(List.of());
        context.setTotalRequestsCount(0);
        context.setResponseSizeInBytes(new ResponseSize(0.0, 0, 0));
        context.setResources(List.of());
        context.setResponseCodes(List.of());
        context.setRequestsPerDate(List.of());
        context.setUniqueProtocols(Set.of());
        return context;
    }
}
