# ADS-B Collector

Collects ADS-B messages from a SBS-1 BaseStation source (for example dump1090) and stores them in InfluxDB.

NOTICE: This is really early in development, and is likely to have a lot of bugs, there fore this is not ready for production use.

### Requirements

* A source for SBS-1 BaseStation format messages (for example, from dump1090)
* InfluxDB instance for storing the data (obviously)
* Maven (For building from sources)
* JDK8 (For building from sources, JRE8 is enough for just running the built JAR)

### Building

Execute 

```sh
mvn clean package
```

### Installation

TODO: Service scripts and other necessary stuff for "properly installing" this will be added later.
- Run the built JAR-file with `java -jar adsb-collector-0.1.jar`. Note: as there is no service scripts yet, it's recommended to run this for example inside *screen* to avoid the application being killed when terminal session ends
- To configure the settings, copy the `adsb-collector.properties.example` to `adsb-collector.properties` and place it in the same directory as the JAR file and edit the file according to your needs.

### Running

For built version (while in the same directory as the JAR file):

```sh
java -jar adsb-collector-0.1.jar
```

For built version (while in the "root" of the project):

```sh
java -jar target/adsb-collector-0.1.jar
```

Easily compile and run while developing:

```
mvn compile exec:java
```
