package monitoring.config;


import com.typesafe.config.Config;

import java.util.List;

public class Configuration {
    public final int port;
    public final List<String> storages;
    public final List<String> indexes;

    public final Timeouts timeouts;

    public Configuration(int port, List<String> storages, List<String> indexes, Timeouts timeouts) {
        this.port = port;
        this.storages = storages;
        this.indexes = indexes;
        this.timeouts = timeouts;
    }

    public Configuration(Config config) {
        this(config.getInt("port"),
                config.getStringList("storages"),
                config.getStringList("indexes"),
                new Timeouts(config.getConfig("network")));
    }

    @Override
    public String toString() {
        return "Configuration:\n" +
                "\tport=" + port + "\n" +
                "\tstorages=[" + storages + "]\n" +
                "\tindexes=[" + indexes + "]\n" +
                "\ttimeouts=[" + timeouts + "]\n";
    }
}
