package kh.edu.istad.platform.order.client.exception;

public class DownStreamServiceException extends RuntimeException {

    private String service;

    public DownStreamServiceException(String message) {
        super(message);
    }

    public DownStreamServiceException(String message, String service) {
        super(message);
        this.service = service;
    }

    public DownStreamServiceException(String message, Throwable cause, String service) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
