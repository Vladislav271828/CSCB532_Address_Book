package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.user.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;
    @BeforeEach
    public void setUp() throws Exception {
        //create user
        var registerDto = RegisterRequest.builder()
                .firstName("Donald")
                .lastName("Trump")
                .email("DonaldTrump@usa.com")
                .password("!IlOvEbAr4kO34Ma")
                .build();
        MvcResult registerResult = createUser(registerDto);

        //extract jwt token
        jwtToken = extractJwtToken(registerResult.getResponse().getContentAsString());
    }
    @Test
    @Transactional
    public void create_contactThenReturns200() throws Exception {

        // prep json payload
        var contactDto = DtoContact.builder()
                .name("Barak")
                .lastName("Obama")
                .phoneNumber("0574965222")
                .nameOfCompany("Obama Care")
                .address("The White House")
                .email("ObamaLovesYou@usa.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("I love this guy.. Obviously.")
                .build();

        String contactJsonPayload = manageContactPayload(contactDto, false);

        // post at /api/v1/contact/create-contact
        MvcResult contactResult = createContact( contactJsonPayload, jwtToken, status().isOk());

        assertEquals("{\"id\":1,\"name\":\"Barak\",\"lastName\":\"Obama\",\"phoneNumber\":\"0574965222\",\"nameOfCompany\":\"Obama Care\",\"address\":\"The White House\",\"email\":\"ObamaLovesYou@usa.com\",\"fax\":\"+1-907-555-1234\",\"mobileNumber\":\"0574965333\",\"comment\":\"I love this guy.. Obviously.\",\"label\":null,\"customRows\":null}", contactResult.getResponse().getContentAsString());

    }


    @Test
    @Transactional
    public void createContactWithoutPhoneNumber_thenReturns400() throws Exception {
        // prep json payload
        var contactDto = DtoContact.builder()
                .name("Barak")
                .lastName("Obama")
                .nameOfCompany("Obama Care")
                .address("The White House")
                .email("ObamaLovesYou@usa.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("I love this guy.. Obviously.")
                .build();


        // post at /api/v1/contact/create-contact
        //case for when not specifying phone number
        String contactJsonPayload = manageContactPayload(contactDto, true);
        MvcResult contactResult = createContact( contactJsonPayload, jwtToken, status().isBadRequest());
        assertTrue(contactResult.getResponse().getContentAsString().contains("Phone Number can't be an empty string.. Phone Number can't be null.") || contactResult.getResponse().getContentAsString().contains("Phone Number can't be null.. Phone Number can't be an empty string."));


        //case for when trying to pass an empty string a phone number
        contactDto.setPhoneNumber("");
        contactJsonPayload = manageContactPayload(contactDto, false);
        contactResult = createContact( contactJsonPayload, jwtToken, status().isBadRequest());
        assertTrue(contactResult.getResponse().getContentAsString().contains("Phone Number can't be an empty string.. Invalid phone number format.") || contactResult.getResponse().getContentAsString().contains("Invalid phone number format. Phone Number can't be an empty string."));

        //case for when trying to pass an incorrect phone number format
        contactDto.setPhoneNumber("asd");
        contactJsonPayload = manageContactPayload(contactDto, false);
        contactResult = createContact( contactJsonPayload, jwtToken, status().isBadRequest());
        assertTrue(contactResult.getResponse().getContentAsString().contains("Invalid phone number format."));

    }


    @Test
    @Transactional
    public void updateContact_thenReturns200() throws Exception {
        // prep json payload
        var contactDtoToCreateContact = DtoContact.builder()
                .name("Barak")
                .lastName("Obama")
                .phoneNumber("0893475000")
                .nameOfCompany("Obama Care")
                .address("The White House")
                .email("ObamaLovesYou@usa.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("I love this guy.. Obviously.")
                .build();


        // post at /api/v1/contact/create-contact
        //case for when not specifying phone number
        String contactJsonPayload = manageContactPayload(contactDtoToCreateContact, false);
        MvcResult contactResult = createContact( contactJsonPayload, jwtToken, status().isOk());
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        //create json payload
        var contactDtoToUpdateContact = DtoContact.builder()
                .name("Donald")
                .lastName("Trump")
                .build();
        String jsonPayloadUpdateContact = manageContactPayloadUpdate(contactDtoToUpdateContact);

        // patch at /api/v1/contact/update-contact/1
        MvcResult updateResult = updateContact(jsonPayloadUpdateContact, idStr, jwtToken, status().isOk());

        // assert equality between expected and actual result
        assertEquals("{\"id\":2,\"name\":\"Donald\",\"lastName\":\"Trump\",\"phoneNumber\":\"0893475000\",\"nameOfCompany\":\"Obama Care\",\"address\":\"The White House\",\"email\":\"ObamaLovesYou@usa.com\",\"fax\":\"+1-907-555-1234\",\"mobileNumber\":\"0574965333\",\"comment\":\"I love this guy.. Obviously.\",\"label\":null,\"customRows\":null}", updateResult.getResponse().getContentAsString());

    }



    @Test
    @Transactional
    public void updateContactWithMissingInput_thenReturns400() throws Exception {
        // prep json payload
        var contactDtoToCreateContact = DtoContact.builder()
                .name("Barak")
                .lastName("Obama")
                .phoneNumber("0893475000")
                .nameOfCompany("Obama Care")
                .address("The White House")
                .email("ObamaLovesYou@usa.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("I love this guy.. Obviously.")
                .build();


        // post at /api/v1/contact/create-contact
        //case for when not specifying phone number
        String contactJsonPayload = manageContactPayload(contactDtoToCreateContact, false);
        MvcResult contactResult = createContact( contactJsonPayload, jwtToken, status().isOk());
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        //create json payload - should be empty
        String jsonPayloadUpdateContact = "{}";

        //make the patch request
        // patch at /api/v1/contact/update-contact/1
        MvcResult updateResult = updateContact(jsonPayloadUpdateContact, idStr, jwtToken, status().isBadRequest());

        assertTrue(updateResult.getResponse().getContentAsString().contains("Missing input."));

    }



    @Test
    @Transactional
    public void updateForeignContact_thenReturns400() throws Exception {
       //create contact of before each user

        //create new user

        //create contact of the new user

        //authenticate as the first user or use the second user to update the other user's contact

        //assert
    }






















    //Creates a user saves them and then returns the result from the request to /api/v1/auth/register
    public MvcResult createUser(RegisterRequest user) throws Exception {
        String jsonRegisterPayload = manageRegisterData(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        String registerUserUri = "/api/v1/auth/register";
        return makePermitAllRequest(jsonRegisterPayload,registerUserUri, status().isOk());
    }

    public MvcResult createContact( String jsonPayload, String jwtToken, ResultMatcher expectedResult) throws Exception {
        return mockMvc.perform(post("/api/v1/contact/create-contact")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(expectedResult)
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult updateContact( String jsonPayload, String contactId, String jwtToken, ResultMatcher expectedResult) throws Exception {
        return mockMvc.perform(patch("/api/v1/contact/update-contact/" + contactId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(expectedResult)
                .andReturn(); // Get the result of the executed request
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

    public String manageContactPayload(DtoContact dtoContact, boolean missingPhoneNumber){

        String phoneNumber = missingPhoneNumber ? "" : "\"phoneNumber\": \""+dtoContact.getPhoneNumber()+"\", " ;
        return "{" +
               "\"name\" : \""+ dtoContact.getName() +"\", " +
               "\"lastName\" : \""+dtoContact.getLastName()+"\", " +
               phoneNumber+
               "\"nameOfCompany\": \""+dtoContact.getNameOfCompany()+"\", " +
               "\"address\": \""+dtoContact.getAddress()+"\", " +
               "\"email\": \""+dtoContact.getEmail()+"\", " +
               "\"fax\": \""+dtoContact.getFax()+"\", " +
               "\"mobileNumber\": \""+dtoContact.getMobileNumber()+"\", " +
               "\"comment\": \""+dtoContact.getComment()+"\" " +
               "}";
    }

    public String manageContactPayloadUpdate(DtoContact dtoContact){

        return "{" +
                "\"name\" : \""+ dtoContact.getName() +"\", " +
                "\"lastName\" : \""+dtoContact.getLastName()+"\" " +
                "}";
    }

    public String manageContactPayloadUpdateMissingInput(DtoContact dtoContact){

        return "{}";
    }
    public String manageLoginData(String email, String password){
        return "{\"email\": \""+email+"\", \"password\": \""+password+"\" }";
    }

    public RequestPostProcessor userAuth(User user) {
        return mockHttpServletRequest -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            return mockHttpServletRequest;
        };
    }

}
