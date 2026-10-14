package kh.edu.istad.platform.business.application.dto;

import java.util.UUID;
public record RegisterBusinessResult(UUID businessId, String legalName, String displayName,
                                     String email, String phoneNumber, boolean active) {}
