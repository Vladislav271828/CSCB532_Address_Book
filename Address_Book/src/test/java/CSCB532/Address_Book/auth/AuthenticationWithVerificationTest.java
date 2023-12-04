package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.auth.emailVerification.Verification;
import CSCB532.Address_Book.auth.emailVerification.VerificationRepository;
import CSCB532.Address_Book.token.Token;
import CSCB532.Address_Book.token.TokenRepository;
import CSCB532.Address_Book.user.User;
import CSCB532.Address_Book.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationWithVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationRepository verificationRepository;

    private String jwtToken;
    private String email;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    @Transactional
    public void setUp() throws Exception {
        String firstName = "Barak";
        String lastName = "Obama";
        String email = "SomeLongEmailYo123@gmail.com";
        String password = "!ITooLoveDonald";

        String jsonPayloadRegister = manageRegisterData(firstName, lastName, email, password);

        // Simulate user registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayloadRegister))
                .andExpect(status().isCreated());

        // Retrieve the verification code from the repository
        Optional<Verification> verificationOpt = verificationRepository.findLatestByUserEmail(email);

        String verificationCode;
        if (verificationOpt.isPresent()) {
            verificationCode = verificationOpt.get().getVerificationCode();
        } else {
            fail("Verification code not found");
            return;
        }

        // Use the retrieved verification code to verify the account
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/verify")
                        .param("code", verificationCode))
                .andExpect(status().isOk()).andReturn();

        //extract jwt token
        Optional<User> user = userRepository.findByEmail(email);
        Integer userId = null;
        if (user.isPresent()) {
            userId = user.get().getId();
        }
        this.email = email;
        Optional<Token> optionalToken = tokenRepository.findRecentActiveToken(userId);

        if (optionalToken.isPresent()) {
            Token token = optionalToken.get();
            jwtToken = token.getToken();
        }

    }


    @Test
    @Transactional
    public void registerWithEmailVerification_thenExpect200() throws Exception {
        String firstName = "Barak";
        String lastName = "Obama";
        String email = "123456@gmail.com";
        String password = "!ITooLoveDonald";

        String jsonPayloadRegister = manageRegisterData(firstName, lastName, email, password);

        // Simulate user registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayloadRegister))
                .andExpect(status().isCreated());

        // Retrieve the verification code from the repository
        Optional<Verification> verificationOpt = verificationRepository.findLatestByUserEmail(email);

        String verificationCode;
        if (verificationOpt.isPresent()) {
            verificationCode = verificationOpt.get().getVerificationCode();
        } else {
            fail("Verification code not found");
            return;
        }

        // Use the retrieved verification code to verify the account
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/verify")
                        .param("code", verificationCode))
                .andExpect(status().isOk());
    }


    @Test
    @Transactional
    public void updateEmailWithVerification_thenExpect200() throws Exception {


        String jsonPayloadEmailUpdate = "{\"email\":\"abijklmnop@gmail.com\"}";

        // Simulate user email update
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-profile/update-email-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(jsonPayloadEmailUpdate))
                .andExpect(status().isCreated());


        // Retrieve the verification code from the repository
        Optional<Verification> verificationOpt2 = verificationRepository.findLatestByUserEmail(email);


        String verificationCode2;
        if (verificationOpt2.isPresent()) {
            verificationCode2 = verificationOpt2.get().getVerificationCode();
        } else {
            fail("Verification code not found");
            return;
        }

        // Use the retrieved verification code to verify the account
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-profile/verifyEmailChange")
                        .param("code", verificationCode2)
                        .param("email", "abcdefghijklmnop@gmail.com")
                )
                .andExpect(status().isOk());


    }


    public String manageRegisterData(String firstName, String lastName, String email, String password) {
        return "{" +
                "\"firstName\" : \"" + firstName + "\", " +
                "\"lastName\" : \"" + lastName + "\", " +
                "\"email\": \"" + email + "\", " +
                "\"password\": \"" + password + "\" " +
                "}";
    }
}
