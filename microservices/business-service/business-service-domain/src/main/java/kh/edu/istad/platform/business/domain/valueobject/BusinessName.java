package kh.edu.istad.platform.business.domain.valueobject;

import kh.edu.istad.platform.business.domain.exception.DomainException;
public record BusinessName(String legalName, String displayName) {
    public BusinessName {
        if (legalName == null || legalName.isBlank() || displayName == null || displayName.isBlank())
            throw new DomainException("Legal and display names are required");
        legalName = legalName.strip(); displayName = displayName.strip();
    }
}
