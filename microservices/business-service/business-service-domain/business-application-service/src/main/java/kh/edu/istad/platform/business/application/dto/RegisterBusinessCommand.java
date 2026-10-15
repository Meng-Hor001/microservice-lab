package kh.edu.istad.platform.business.application.dto;

public record RegisterBusinessCommand(
        String legalName,
        String displayName,
        String email,
        String phoneNumber) {}
