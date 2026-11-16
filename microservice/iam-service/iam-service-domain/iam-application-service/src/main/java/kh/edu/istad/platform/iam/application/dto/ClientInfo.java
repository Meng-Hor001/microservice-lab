package kh.edu.istad.platform.iam.application.dto;

public record ClientInfo(String type, String device, String browser, String os, String ip) {
    public static ClientInfo unknown() {
        return new ClientInfo("desktop", "Unknown", "Unknown", "Unknown", "Unknown");
    }
}
