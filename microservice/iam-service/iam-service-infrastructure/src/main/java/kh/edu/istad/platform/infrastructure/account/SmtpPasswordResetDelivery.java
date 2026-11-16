package kh.edu.istad.platform.infrastructure.account;

import kh.edu.istad.platform.iam.application.port.out.PasswordResetDelivery;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetDelivery implements PasswordResetDelivery {
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;
    private final String resetPage;

    public SmtpPasswordResetDelivery(ObjectProvider<JavaMailSender> mail,
                                    @Value("${iam.password-reset.from:}") String from,
                                    @Value("${iam.password-reset.page:http://localhost:5173/me/reset-password}") String resetPage) {
        this.mail = mail;
        this.from = from;
        this.resetPage = resetPage;
    }

    @Override
    public void send(String email, String token) {
        var sender = mail.getIfAvailable();
        if (sender == null || from.isBlank()) {
            throw new IllegalStateException("Configure spring.mail.host and iam.password-reset.from to send reset emails");
        }
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reset your IAM password");
        message.setText("Use this link within 15 minutes to reset your password:\n\n"
                + resetPage + "?token=" + token + "\n\nIf you did not request this, ignore this email.");
        sender.send(message);
    }
}
