package fi.tkgwf.adsb;

import fi.tkgwf.adsb.bean.Message;
import fi.tkgwf.adsb.db.InfluxDBConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        // Defaults..
        String influxUrlBase = "http://localhost:8086";
        String influxDatabase = "adsb";
        String influxUser = "adsb";
        String influxPassword = "adsb";
        String sbsHost = "localhost";
        int sbsPort = 30003;

        // Read config..
        try {
            File jarLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            File[] configFiles = jarLocation.listFiles(f -> f.isFile() && f.getName().equals("adsb-collector.properties"));
            if (configFiles == null || configFiles.length == 0) {
                // look for config files in the parent directory if none found in the current directory, this is useful during development when
                // the application can be run from maven target directory directly while the config file sits in the project root
                configFiles = jarLocation.getParentFile().listFiles(f -> f.isFile() && f.getName().equals("adsb-collector.properties"));
            }
            if (configFiles != null && configFiles.length > 0) {
                LOG.debug("Config: " + configFiles[0]);
                Properties props = new Properties();
                props.load(new FileInputStream(configFiles[0]));
                Enumeration<?> e = props.propertyNames();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String value = props.getProperty(key);
                    switch (key) {
                        case "influxUrlBase":
                            influxUrlBase = value;
                            break;
                        case "influxDatabase":
                            influxDatabase = value;
                            break;
                        case "influxUser":
                            influxUser = value;
                            break;
                        case "influxPassword":
                            influxPassword = value;
                            break;
                        case "sbs.host":
                            sbsHost = value;
                            break;
                        case "sbs.port":
                            sbsPort = Integer.parseInt(value);
                            break;
                    }
                }
            }
        } catch (URISyntaxException | IOException ex) {
            LOG.warn("Failed to read configuration, using default values...", ex);
        }
        String influxUrl = String.format("%s/write?db=%s&u=%s&p=%s", influxUrlBase, influxDatabase, influxUser, influxPassword);

        try {
            InfluxDBConnection db = new InfluxDBConnection(influxUrl);
            while (true) {
                try (Socket s = new Socket(sbsHost, sbsPort)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            try {
                                Message m = new Message(line);
                                db.post(m);
                            } catch (IllegalArgumentException ex) {
                                LOG.warn("Invalid message! Skipping...", ex);
                            }
                        }
                    } catch (IOException ex) {
                        LOG.warn("Error reading from from socket. Connection lost? Retrying in 5 seconds...", ex);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex1) {
                            LOG.error("Interrupted! Exiting...", ex1);
                            break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to connect", ex);
        }
        System.out.println("Exiting...");
    }
}
