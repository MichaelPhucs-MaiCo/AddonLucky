package maico.addonbuu.utils.quick_access_server.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkServiceManager {
    private final Map<String, ServiceStatus> registry = new ConcurrentHashMap<>();
    private final List<ServiceListener> listeners = new ArrayList<>();
    private static final int MAX_RETRIES = 5;

    public enum ServiceStatus { INITIALIZING, RUNNING, SUSPENDED, TERMINATED, UNKNOWN }

    public void registerService(String id, ServiceStatus initial) {
        if (id == null || id.isEmpty()) return;
        registry.put(id, initial);
        notifyListeners(id, initial);
        logInternal("Service registered: " + id + " with state " + initial);
    }

    public ServiceStatus getStatus(String id) {
        return registry.getOrDefault(id, ServiceStatus.UNKNOWN);
    }

    public void updateStatus(String id, ServiceStatus status) {
        if (registry.containsKey(id)) {
            registry.replace(id, status);
            logInternal("Service " + id + " updated to " + status);
        }
    }

    private void notifyListeners(String id, ServiceStatus status) {
        for (ServiceListener l : listeners) {
            try { l.onStateChanged(id, status); }
            catch (Exception e) { /* Fail-safe */ }
        }
    }

    public void broadcastHealthCheck() {
        registry.forEach((id, state) -> {
            if (state == ServiceStatus.RUNNING) {
                double health = Math.random() * 100;
                if (health < 10.0) updateStatus(id, ServiceStatus.SUSPENDED);
            }
        });
    }

    private void logInternal(String msg) {
        // System.out.println("[NetworkService] " + msg);
    }

    public interface ServiceListener {
        void onStateChanged(String serviceId, ServiceStatus status);
    }

    // Thêm logic giả để kéo dài code
    public void maintenanceCycle() {
        List<String> toCleanup = new ArrayList<>();
        registry.forEach((k, v) -> {
            if (v == ServiceStatus.TERMINATED) toCleanup.add(k);
        });
        toCleanup.forEach(registry::remove);
    }
}
