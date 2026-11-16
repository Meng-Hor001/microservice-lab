package kh.edu.istad.platform.iam.application.port.out;

public interface PasswordResetDelivery {
    void send(String email, String token);
}
