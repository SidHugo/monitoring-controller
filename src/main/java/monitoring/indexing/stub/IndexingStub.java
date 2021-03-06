package monitoring.indexing.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import monitoring.indexing.IndexingResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.nio.charset.Charset;

import static spark.Spark.*;

public class IndexingStub {
    private static final Logger logger = LogManager.getLogger(IndexingStub.class);

    public static void main(String[] args) {
        final String fromField = "starttime";
        final String toField = "endtime";
        final String countField = "count";
        final int port = 8081;

        ObjectMapper objectMapper = new ObjectMapper();

        port(port);
        get("/getdata", (req, res) -> {
            String fromParam = req.queryParams(fromField);
            String toParam = req.queryParams(toField);
            String countParam = req.queryParams(countField);

            if (toParam != null) {
                logger.debug("Received request with non-null " + toField + " request field");
                IndexingResponse response = new IndexingResponse();
                response.setStatus("ok");
                response.setData(new String[]{toParam});

                res.status(200);
                String responseString = objectMapper.writeValueAsString(response);

                logger.info("Returning to response " + responseString);
                return responseString;
            } else if (countParam != null) {
                logger.debug("Received request with non-null " + countField + " request field");

                IndexingResponse response1 = new IndexingResponse();
                response1.setStatus("ok");
                response1.setData(new String[]{fromParam, countParam});

                IndexingResponse response2 = new IndexingResponse();
                response2.setStatus("error");
                response2.setData(new String[]{fromParam, countParam});

                IndexingResponse response3 = new IndexingResponse();
                response3.setStatus("error");
                response3.setData(new String[]{fromParam, countParam});

                res.status(200);
                String responseString1 = objectMapper.writeValueAsString(response1);
                String responseString2 = objectMapper.writeValueAsString(response2);
                String responseString3 = objectMapper.writeValueAsString(response3);

                OutputStream out = res.raw().getOutputStream();
                out.write(responseString1.getBytes(Charset.forName("UTF-8")));
                out.write("@".getBytes(Charset.forName("UTF-8")));
                out.flush();

                out.write(responseString2.getBytes(Charset.forName("UTF-8")));
                out.write("@".getBytes(Charset.forName("UTF-8")));
                out.flush();

                out.write(responseString3.getBytes(Charset.forName("UTF-8")));
                out.flush();
                halt();

                logger.info("Returning count response " + responseString1);
                return responseString1;
            } else {
                res.status(404);
                return "Not found!";
            }
        });

        logger.info("Started to listen on localhost:" + port);
    }
}
