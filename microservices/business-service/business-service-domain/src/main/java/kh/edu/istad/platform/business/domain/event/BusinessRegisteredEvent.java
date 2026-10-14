package kh.edu.istad.platform.business.domain.event;

import java.time.ZonedDateTime;
import kh.edu.istad.platform.business.domain.entity.Business;
public record BusinessRegisteredEvent(Business business, ZonedDateTime createdAt) {}
