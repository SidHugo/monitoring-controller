package monitoring;

import monitoring.indexing.IndexingManager;
import monitoring.storage.StorageManager;
import monitoring.storage.StorageResponse;
import monitoring.utils.JsonUtils;
import monitoring.web.ClientMessageHandler;
import monitoring.web.request.ClientRequest;
import monitoring.web.request.TimeAndCountRequest;
import monitoring.web.request.TimeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static spark.Spark.get;
import static spark.Spark.port;

public class AppInitializer {
    private static final Logger logger = LogManager.getLogger(AppInitializer.class);

    public void start(int port) {
        port(port);
        createRoutes();

        logger.info("Server successfully started at port: " + port);
    }

    private void createRoutes() {
        final String fromField = "starttime";
        final String toField = "endtime";
        final String countField = "count";
        final String hostField = "host";
        final String portField = "port";

        get("/getdata", (req, res) -> {
            Map<String, String> params = req.params();
            Set<String> queryParams = req.queryParams();
            logger.info("All parameters: " + params + "," + "query params: " + queryParams);
            String fromParam = req.queryParams(fromField);
            String toParam = req.queryParams(toField);
            String countParam = req.queryParams(countField);

            if (fromParam == null) {
                logger.error("Request from " + req.ip() + " does not contain " + fromField + " parameter");
                res.status(404);
                return "Request does not contain " + fromField + " parameter";
            }
            long from = Long.parseLong(fromParam);

            ClientRequest request = null;

            if (toParam != null) {
                long to = Long.parseLong(toParam);
                request = new TimeRequest(from, to);
            } else if (countParam != null) {
                int count = Integer.parseInt(countParam);
                request = new TimeAndCountRequest(from, count);
            } else {
                logger.error("Unknown request type");
                res.status(404);
                return "Unknown request type";
            }

            ClientMessageHandler handler = new ClientMessageHandler();

            try {
                List<StorageResponse> responses = handler.handle(request);
                String json = JsonUtils.serialize(responses);
                res.status(200);
                return json;
            } catch (RuntimeException e) {
                logger.error("Error with request", e);
                res.status(500);
                return "Error:" + e;
            } catch (TimeoutException e) {
                logger.error("Timeout exception", e);
                res.status(500);
                return "Timeout exception:" + e;
            }
        });

        get("/addIndexing", (req, res) -> {
            String host = req.queryParams(hostField);
            String port = req.queryParams(portField);
            if (host == null) {
                String error = "No " + hostField + " parameter";
                logger.error(error);
                res.status(500);
                return error;
            } else if (port == null) {
                String error = "No " + portField + " parameter";
                logger.error(error);
                res.status(500);
                return error;
            } else {
                logger.debug("Received request to add indexing service with host " + host + ":" + port);
                try {
                    Socket sock = new Socket(host, Integer.parseInt(port));
                    sock.close();

                    URL url = new URL("http://" + host + ":" + port + "/");
                    IndexingManager.instance().add(url);
                    String response = "Indexing service at URL " + url + " successfully added";
                    logger.info(response);
                    res.status(200);
                    return response;
                } catch (UnknownHostException e) {
                    String error = "Unknown host at " + host + ":" + port;
                    logger.error(error, e);
                    res.status(500);
                    return error;
                } catch (IOException e) {
                    logger.error("Error at adding host", e);
                    String error = "Error adding host " + host + ":" + port + ", exception: " + e;
                    res.status(500);
                    return error;
                }
            }
        });

        get("/addStorage", (req, res) -> {
            String host = req.queryParams(hostField);
            String port = req.queryParams(portField);
            if (host == null) {
                String error = "No " + hostField + " parameter";
                logger.error(error);
                res.status(500);
                return error;
            } else if (port == null) {
                String error = "No " + portField + " parameter";
                logger.error(error);
                res.status(500);
                return error;
            } else {
                logger.debug("Received request to add storage with host " + host + ":" + port);
                try {
                    Socket sock = new Socket(host, Integer.parseInt(port));
                    sock.close();

                    URL url = new URL("http://" + host + ":" + port + "/");
                    StorageManager.instance().add(url);
                    String response = "Storage service at URL " + url + " successfully added";
                    logger.info(response);
                    res.status(200);
                    return response;
                } catch (UnknownHostException e) {
                    String error = "Unknown host at " + host + ":" + port;
                    logger.error(error, e);
                    res.status(500);
                    return error;
                } catch (IOException e) {
                    logger.error("Error at adding host", e);
                    String error = "Error adding host " + host + ":" + port + ", exception: " + e;
                    res.status(500);
                    return error;
                }
            }
        });
    }

    public void stop() {
        Spark.stop();
    }
}
