package kh.edu.istad.platform.business.domain.valueobject;

import kh.edu.istad.platform.business.domain.exception.DomainException;
public record BusinessContact(String email, String phoneNumber) {
    public BusinessContact {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new DomainException("A valid email is required");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new DomainException("Phone number is required");
        email = email.strip(); phoneNumber = phoneNumber.strip();
    }
}
