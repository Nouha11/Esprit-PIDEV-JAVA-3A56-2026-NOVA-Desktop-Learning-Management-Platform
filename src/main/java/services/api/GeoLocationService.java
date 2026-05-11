package services.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * GeoLocationService - uses ipwho.is (no API key, HTTPS, reliable).
 */
public class GeoLocationService {

    private static final String IPWHO_URL = "https://ipwho.is/";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // -- Result model --
    public static class GeoInfo {
        public final String ip;
        public final String city;
        public final String region;
        public final String countryCode;
        public final String countryName;
        public final String flagEmoji;
        public final String timezone;
        public final String currency;
        public final double lat;
        public final double lon;

        public GeoInfo(String ip, String city, String region,
                       String countryCode, String countryName,
                       String flagEmoji, String timezone,
                       String currency, double lat, double lon) {
            this.ip          = ip;
            this.city        = city;
            this.region      = region;
            this.countryCode = countryCode;
            this.countryName = countryName;
            this.flagEmoji   = flagEmoji;
            this.timezone    = timezone;
            this.currency    = currency;
            this.lat         = lat;
            this.lon         = lon;
        }

        public String getSummary() {
            String prefix = countryCode != null ? "[" + countryCode + "] " : "";
            String c = city != null && !city.isBlank() ? city : "";
            String n = countryName != null && !countryName.isBlank() ? countryName : countryCode;
            return c.isBlank() ? prefix + n : prefix + c + ", " + n;
        }

        public String getTimezoneDisplay() {
            return timezone != null ? timezone : "...";
        }
    }

    // -- Main method --
    public GeoInfo fetchCurrentLocation() {
        try {
            String json = get(IPWHO_URL);
            if (json == null) return null;

            // Check success field
            String success = extract(json, "success");
            if ("false".equals(success)) return null;

            String ip          = extract(json, "ip");
            String city        = extract(json, "city");
            String region      = extract(json, "region");
            String countryCode = extract(json, "country_code");
            String countryName = extract(json, "country");
            String timezone    = extractNested(json, "timezone", "id");
            String latStr      = extract(json, "latitude");
            String lonStr      = extract(json, "longitude");
            String currency    = extractNested(json, "currency", "code");

            double lat = 0, lon = 0;
            try { if (latStr != null) lat = Double.parseDouble(latStr); } catch (Exception ignored) {}
            try { if (lonStr != null) lon = Double.parseDouble(lonStr); } catch (Exception ignored) {}

            String flagEmoji = countryCodeToFlag(countryCode);

            return new GeoInfo(
                ip          != null ? ip          : "Unknown",
                city        != null ? city        : "Unknown",
                region      != null ? region      : "",
                countryCode != null ? countryCode : "",
                countryName != null ? countryName : "",
                flagEmoji,
                timezone    != null ? timezone    : "Unknown",
                currency    != null ? currency    : "",
                lat, lon
            );
        } catch (Exception e) {
            System.err.println("[GeoLocation] Error: " + e.getMessage());
            return null;
        }
    }

    // -- HTTP --
    private String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(7))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 ? resp.body() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // -- JSON helpers --
    private String extract(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length()) return null;
        char first = json.charAt(idx);
        if (first == '"') {
            int end = json.indexOf('"', idx + 1);
            return end > idx ? json.substring(idx + 1, end) : null;
        } else if (first != '{' && first != '[') {
            int end = idx;
            while (end < json.length() && ",}\n".indexOf(json.charAt(end)) < 0) end++;
            String val = json.substring(idx, end).trim();
            return val.equals("null") ? null : val;
        }
        return null;
    }

    /**
     * Extract a value from a nested object: finds "parentKey":{..."childKey":"value"...}
     */
    private String extractNested(String json, String parentKey, String childKey) {
        String search = "\"" + parentKey + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        // find opening brace
        int braceStart = json.indexOf('{', idx);
        if (braceStart < 0) return null;
        // find closing brace
        int depth = 0, braceEnd = braceStart;
        for (int i = braceStart; i < json.length(); i++) {
            if (json.charAt(i) == '{') depth++;
            else if (json.charAt(i) == '}') { depth--; if (depth == 0) { braceEnd = i; break; } }
        }
        String nested = json.substring(braceStart, braceEnd + 1);
        return extract(nested, childKey);
    }

    private String countryCodeToFlag(String code) {
        if (code == null || code.length() != 2) return "";
        try {
            int base = 0x1F1E6 - 'A';
            return new String(Character.toChars(base + Character.toUpperCase(code.charAt(0))))
                 + new String(Character.toChars(base + Character.toUpperCase(code.charAt(1))));
        } catch (Exception e) { return ""; }
    }
}