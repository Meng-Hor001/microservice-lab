package kh.edu.istad.platform.order.domain.command;

public record CommandOrderAddress(
        String street,
        String postalCode,
        String city
) {
}
