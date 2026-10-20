package kh.edu.istad.platform.customer.domain.dto;

import java.util.List;
import java.util.Map;

public record CustomerStatistics(long totalCustomers, Map<String, Long> byStatus,
                                 Map<String, Long> bySegment, List<Group> byStatusAndSegment) {
    public record Group(String status, String segmentId, long total) {}
}
