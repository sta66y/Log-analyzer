package academy.io.output;

import academy.util.AnalysisContext;
import academy.util.Date;
import academy.util.Resource;
import academy.util.ResponseCode;
import academy.util.ResponseSize;
import java.util.List;
import java.util.Set;

public class AnalysisContextFactory {

    public static AnalysisContext createFromExampleData() {
        AnalysisContext context = new AnalysisContext();

        context.setFiles(List.of(
            "access.log",
            "http://example.com/access.log"
        ));

        context.setTotalRequestsCount(10000);

        context.setResponseSizeInBytes(new ResponseSize(
            500.0,
            1000,
            950
        ));

        context.setResources(List.of(
            new Resource("/downloads/product_1", 1000),
            new Resource("/downloads/product_2", 100)
        ));

        context.setResponseCodes(List.of(
            new ResponseCode(500, 1),
            new ResponseCode(401, 10),
            new ResponseCode(200, 1000)
        ));

        context.setRequestsPerDate(List.of(
            new Date("2024-03-01", "Monday", 2981, 12.10)
        ));

        context.setUniqueProtocols(Set.of(
            "HTTP/1.1",
            "HTTP/2.0",
            "grpc"
        ));

        return context;
    }
}
