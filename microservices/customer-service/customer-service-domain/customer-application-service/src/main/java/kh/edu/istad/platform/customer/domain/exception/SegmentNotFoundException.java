package kh.edu.istad.platform.customer.domain.exception;

public class SegmentNotFoundException extends RuntimeException {
    public SegmentNotFoundException(String id) {
        super("Segment not found :" + id);
    }
}
