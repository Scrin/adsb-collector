package fi.tkgwf.adsb.bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a SBS-1 BaseStation Message.
 * http://woodair.net/sbs/Article/Barebones42_Socket_Data.htm
 */
public class Message {

    public final String raw;
    public final String[] rawArray;
    public static final String[] FIELD_DATA = {
        "MessageType",
        "TransmissionType",
        "SessionID",
        "AircraftID",
        "HexIdent",
        "FlightID",
        "DateMessageGenerated",
        "TimeMessageGenerated",
        "DateMessageLogged",
        "TimeMessageLogged",
        "Callsign",
        "Altitude",
        "GroundSpeed",
        "Track",
        "Latitude",
        "Longitude",
        "VerticalRate",
        "Squawk",
        "Alert",
        "Emergency",
        "SPI",
        "IsOnGround"
    };
    private static final Set<Integer> FIELD_DATA_TAGS = new HashSet(Arrays.asList(0, 1, 4));

    public Message(String raw) {
        this.raw = raw;
        this.rawArray = raw.split(",", -1);
        if (rawArray.length != FIELD_DATA.length) {
            throw new IllegalArgumentException("Message has " + rawArray.length + " fields, expected " + FIELD_DATA.length + ". Raw line: " + raw);
        }
    }

    public String toInfluxData() {
        StringBuilder sb = new StringBuilder("sbs1,messageType=").append(rawArray[0].trim());
        if (rawArray[0].equals("MSG") && StringUtils.isNotBlank(rawArray[1])) {
            sb.append(",transmissionType=").append(rawArray[1].trim());
        }
        if (StringUtils.isNotBlank(rawArray[4])) {
            sb.append(",hexIdent=").append(rawArray[4].trim());
        }
        sb.append(' ');
        for (int i = 2; i < FIELD_DATA.length; i++) {
            if (StringUtils.isNotBlank(rawArray[i]) && !FIELD_DATA_TAGS.contains(i)) {
                sb.append(FIELD_DATA[i]).append('=');
                if (rawArray[i].trim().matches("[-+]?\\d*\\.?\\d+")) { // Check if value is numeric
                    sb.append(rawArray[i].trim());
                } else {
                    sb.append('"').append(rawArray[i].trim()).append('"');
                }
                sb.append(',');
            }
        }
        sb.append("dummy=1"); // InfluxDB always requires at least one field, and it's possible that all other data is set as tags already..
        return sb.toString();
    }

    @Override
    public String toString() {
        return raw;
    }
}
