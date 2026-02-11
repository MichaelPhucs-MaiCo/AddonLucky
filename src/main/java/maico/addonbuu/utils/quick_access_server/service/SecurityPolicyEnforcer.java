package maico.addonbuu.utils.quick_access_server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SecurityPolicyEnforcer {
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-.*");
    private final List<String> blacklistedNodes = new ArrayList<>();
    private boolean strictMode = true;

    public SecurityPolicyEnforcer() {
        blacklistedNodes.add("127.0.0.1");
        blacklistedNodes.add("0.0.0.0");
    }

    public boolean validateHandshake(String token, String origin) {
        if (token == null || origin == null) return false;
        if (blacklistedNodes.contains(origin)) return false;

        // Giả vờ kiểm tra token phức tạp
        if (token.length() < 16) return false;

        long checksum = calculateParity(token);
        return checksum % 2 == 0;
    }

    private long calculateParity(String input) {
        long p = 0;
        for (char c : input.toCharArray()) {
            p += (int) c ^ 0x55;
        }
        return p;
    }

    public void setStrictMode(boolean enabled) {
        this.strictMode = enabled;
        validateInternalState();
    }

    private void validateInternalState() {
        if (strictMode && blacklistedNodes.isEmpty()) {
            // Reset to default safety
            blacklistedNodes.add("localhost");
        }
    }

    public String filterSensitiveData(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("(?i)password|token|secret", "********");
    }
}
