package CSCB532.Address_Book.auth.emailVerification;

import CSCB532.Address_Book.user.User;
import CSCB532.Address_Book.user.UserRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailService {


    private VerificationRepository verificationRepository;
    private UserRepository userRepository;
    private String sendGridApiKey;

    public EmailService(VerificationRepository verificationRepository, UserRepository userRepository, @Value("${spring.mail.sendgrid.api-key}") String sendGridApiKey) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.sendGridApiKey = sendGridApiKey;
    }

    public void sendVerificationEmail(String to) throws IOException {
        Optional<User> user = userRepository.findByEmail(to);
        if (user.isEmpty()) {
            // Handle the case where the user is not found
            throw new IllegalArgumentException("No user found with email: " + to);
        }

        String verificationCode = createVerificationCode();
        Verification verification = Verification.builder()
                .user(user.get())
                .verificationCode(verificationCode)
                .expired(false)
                .build();
        verificationRepository.save(verification);

        Email from = new Email("AddressBookCSB532@gmail.com");
        Email toEmail = new Email(to);
        String subject = "Email Verification";
        Content content = new Content("text/plain", "Please click the link to verify your email: " +
                "http://localhost:8080/api/v1/auth/verify?code=" + verificationCode);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);

        // Optionally log the response status and body for debugging purposes
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        System.out.println(response.getHeaders());
    }

    private String createVerificationCode() {
        // Generate a unique verification code
        return UUID.randomUUID().toString();
    }
}