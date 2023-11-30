package CSCB532.Address_Book.auth;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@ActiveProfiles("test")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @Transactional
    public void whenRegisterUser_thenReturns200() throws Exception {
        // Arrange
        String registerUserUri = "/api/v1/auth/register";
        String firstName = "Donald";
        String lastName = "Trump";
        String email = "make_america_great_again@gmail.com";
        String password = "ILBarakObama!!!";


        String jsonPayload = manageRegisterData(firstName, lastName, email, password);

        MvcResult result = makePermitAllRequest(jsonPayload,registerUserUri, status().isOk());
        assertTrue(result.getResponse().getContentAsString().contains("token"));
    }

    @Test
    @Transactional
    public void whenRegisteringWithEmptyFirstName_thenReturns400() throws Exception {
        String registerUserUri = "/api/v1/auth/register";
        String jsonPayloadRegister = manageRegisterData("", "", "moni@gmail.comm", "PasswordPasswor@1");

        // Create a user and get the result
        MvcResult result = makePermitAllRequest(jsonPayloadRegister, registerUserUri, status().isBadRequest());

        assertTrue(result.getResponse().getContentAsString().contains("First name is required."));
    }

    //First name must be at least 3 characters long.
    @Test
    @Transactional
    public void whenRegisteringWithShortName_thenReturns400() throws Exception { // checks for when first name is too short or too long
        String registerUserUri = "/api/v1/auth/register";
        String email = "example@gmail.com";
        String password = "Password!";
        String lastName = "";

        String firstNameShort = "Me"; //2 characters long
        String jsonPayloadRegister = manageRegisterData(firstNameShort, lastName, email , password);

        // Create a user
        MvcResult result = makePermitAllRequest(jsonPayloadRegister, registerUserUri, status().isBadRequest());
        assertTrue(result.getResponse().getContentAsString().contains("First name must be longer than 3 characters."));

        String firstNameLong = "Some Very Long First Name Shenanigans";//this is 38 characters
        jsonPayloadRegister = manageRegisterData(firstNameLong, lastName, email , password);

        // Create a user
        result = makePermitAllRequest(jsonPayloadRegister, registerUserUri, status().isBadRequest());
        assertTrue(result.getResponse().getContentAsString().contains("First name is too long, must be less than 18 characters."));



    }


    @Test
    @Transactional
    public void whenRegisterUserWithDuplicateEmail_thenReturns400() throws Exception {
        // Arrange
        String jsonPayload = "{\"firstName\": \"Moni\", \"lastName\": \"Doe\", \"email\": \"moni@gmail.com\", \"password\": \"Moni!Moni\" }";

        // Perform the first registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Act & Assert
        // Try to register again with the same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request

    }


    @Test
    @Transactional
    public void whenRegisterUserWithShortPassword_thenReturnsBadRequest() throws Exception {
        // Arrange
        String jsonPayload = "{\"firstName\": \"Moni\", \"lastName\": \"Doe\", \"email\": \"moni@gmail.com\", \"password\": \"short\" }";

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert
        String actualResponseBody = result.getResponse().getContentAsString();
        assertTrue(actualResponseBody.contains("Password must be at least 8 characters long"));
        assertTrue(actualResponseBody.contains("Password must contain an uppercase letter"));
        assertTrue(actualResponseBody.contains("Password must contain a special character"));
    }


    @Test
    @Transactional
    public void whenRegisterUserWithoutSpecialCharacter_thenReturnsBadRequest() throws Exception {
        // Arrange
        String firstName = "Donald";
        String lastName = "Trump";
        String email = "make_america_great_again@gmail.com";
        String password = "ILBarakObama"; // no special character
        String registerUserUri = "/api/v1/auth/register";

        String jsonPayload = manageRegisterData(firstName, lastName, email, password);

        // Act & Assert
        MvcResult result = makePermitAllRequest(jsonPayload,registerUserUri, status().isBadRequest());

        // Check if the exception is of type MethodArgumentNotValidException
        Exception resolvedException = result.getResolvedException();
        assertNotNull(resolvedException);
        assertTrue(resolvedException instanceof MethodArgumentNotValidException);

        // Further assert the details of the validation error
        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;
        String actualErrorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String expectedErrorMessage = "Password must contain a special character."; // Adjust the message as per your validation message
        assertEquals(expectedErrorMessage, actualErrorMessage);
    }


    @Test
    @Transactional
    public void whenRegisterUserWithoutCapitalLetter_thenReturnsBadRequest() throws Exception {
        // Arrange
        String firstName = "Donald";
        String lastName = "Trump";
        String email = "make_america_great_again@gmail.com";
        String password = "ilbarakobama!"; // no uppercase character
        String registerUserUri = "/api/v1/auth/register";

        String jsonPayload = manageRegisterData(firstName, lastName, email, password);

        // Act & Assert
        MvcResult result = makePermitAllRequest(jsonPayload,registerUserUri, status().isBadRequest());

        // Check if the exception is of type MethodArgumentNotValidException
        Exception resolvedException = result.getResolvedException();
        assertNotNull(resolvedException);
        assertTrue(resolvedException instanceof MethodArgumentNotValidException);

        // Further assert the details of the validation error
        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;
        String actualErrorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String expectedErrorMessage = "Password must contain an uppercase letter."; // Adjust the message as per your validation message
        assertEquals(expectedErrorMessage, actualErrorMessage);
    }


    @Test
    @Transactional
    public void whenLoggingInWithCorrectCredentials_thenExpect200() throws Exception {
        // Arrange
        String firstName = "Donald";
        String lastName = "Trump";
        String email = "make_america_great_again@gmail.com";
        String password = "ILBarakObama!";
        String registerUserUri = "/api/v1/auth/register";

        String jsonPayload = manageRegisterData(firstName, lastName, email, password);

        // Register user
       makePermitAllRequest(jsonPayload,registerUserUri, status().isOk());

        jsonPayload = "{\"email\": \""+email+"\", \"password\": \""+password+"\" }";

        // Authenticate the user
        String authenticateUri = "/api/v1/auth/authenticate";
        authenticateRequest(jsonPayload, authenticateUri, status().isOk());
    }


    @Test
    @Transactional
    public void whenLoggingInWithIncorrectEmail() throws Exception {
        String jsonPayload = "{\"email\": \"moni@gmail.comm\", \"password\": \"PasswordPasswor@1\" }";
        String authenticateUri = "/api/v1/auth/authenticate";

        // Make a request to authenticate should return a not found since there is no user created in the database with such email
        MvcResult result = makePermitAllRequest(jsonPayload, authenticateUri, status().isNotFound());

        assertTrue(result.getResponse().getContentAsString().contains("User with that email not found."));
    }

    @Test
    @Transactional
    public void whenLoggingInWithIncorrectPassword() throws Exception {
        String jsonPayloadRegister = "{\"firstName\" : \"Moni\",\"lastName\" : \"\", \"email\": \"moni@gmail.comm\", \"password\": \"PasswordPasswor@1\" }";

        String jsonPayloadLogin = "{\"email\": \"moni@gmail.comm\", \"password\": \"PasswordPasswor@\" }";

        String registerUserUri = "/api/v1/auth/register";
        String authenticateUri = "/api/v1/auth/authenticate";

        // Create a user
        makePermitAllRequest(jsonPayloadRegister, registerUserUri, status().isOk());

        // Make a request to authenticate should return a not found since there is no user created in the database with such email
        MvcResult result = makePermitAllRequest(jsonPayloadLogin, authenticateUri, status().isBadRequest());

        assertTrue(result.getResponse().getContentAsString().contains("Bad credentials."));
    }




















    public MvcResult makePermitAllRequest(String jsonPayload, String uri, ResultMatcher expectedResult) throws Exception {

        return mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(expectedResult)
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult authenticateRequest(String jsonPayload, String uri, ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(resultMatcher)
                .andReturn(); // Get the result of the executed request
    }

    public String extractJwtToken(String responseContent){
        // Find the start of the token
        int tokenStart = responseContent.indexOf("\"token\":\"") + 9; // 9 is the length of "\"token\":\""

        // Find the end of the token
        int tokenEnd = responseContent.indexOf("\"", tokenStart);

        // Extract the token
        return responseContent.substring(tokenStart, tokenEnd);
    }

    public String manageRegisterData(String firstName, String lastName, String email, String password){
        return  "{" +
                "\"firstName\" : \""+ firstName +"\", " +
                "\"lastName\" : \""+lastName+"\", " +
                "\"email\": \""+email+"\", " +
                "\"password\": \""+password+"\" " +
                "}";
    }

}
