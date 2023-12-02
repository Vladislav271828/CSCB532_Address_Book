package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.customRow.DtoCustomRow;
import CSCB532.Address_Book.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;
    @BeforeEach
    @Transactional
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
        String contactId = convertFromStringToJson(contactResult.getResponse().getContentAsString()).get("id").asText();

        assertEquals("{\"id\":"+contactId+",\"name\":\"Barak\",\"lastName\":\"Obama\",\"phoneNumber\":\"0574965222\",\"nameOfCompany\":\"Obama Care\",\"address\":\"The White House\",\"email\":\"ObamaLovesYou@usa.com\",\"fax\":\"+1-907-555-1234\",\"mobileNumber\":\"0574965333\",\"comment\":\"I love this guy.. Obviously.\",\"label\":null,\"customRows\":null}", contactResult.getResponse().getContentAsString());

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
        String contactId = convertFromStringToJson(contactResult.getResponse().getContentAsString()).get("id").asText();

        // assert equality between expected and actual result
        assertEquals("{\"id\":"+contactId+",\"name\":\"Donald\",\"lastName\":\"Trump\",\"phoneNumber\":\"0893475000\",\"nameOfCompany\":\"Obama Care\",\"address\":\"The White House\",\"email\":\"ObamaLovesYou@usa.com\",\"fax\":\"+1-907-555-1234\",\"mobileNumber\":\"0574965333\",\"comment\":\"I love this guy.. Obviously.\",\"label\":null,\"customRows\":null}", updateResult.getResponse().getContentAsString());

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
        // prep json payload
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));


        //create new user
        //create user
        var barakObama = RegisterRequest.builder()
                .firstName("Barak")
                .lastName("Obama")
                .email("BarakObama@usa.com")
                .password("!IlOvED0n41d7RumP")
                .build();
        MvcResult registerResult = createUser(barakObama);

        //extract jwt token
        String jwtTokenObama = extractJwtToken(registerResult.getResponse().getContentAsString());


        //create contact of the new user
        // prep json payload
        var contactDtoToCreateContactDonald = DtoContact.builder()
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
        String jsonPayloadObama = manageContactPayload(contactDtoToCreateContactDonald, false);

        //this creates a contact with the before each user
        createContact(jsonPayloadObama, jwtTokenObama, status().isOk());


        //use the Barak's jwt token to try and update Donald's contact
        // patch at /api/v1/contact/update-contact/1
        MvcResult updateResult = updateContact(jsonPayloadDonald, idObamaContactStr, jwtTokenObama, status().isBadRequest());

        //assert
        assertTrue(updateResult.getResponse().getContentAsString().contains("User doesn't have permissions to perform this action."));
    }



    @Test
    @Transactional
    public void getAllSelfContacts_thenReturns200() throws Exception {
        //create contact of before each user
        // prep json payload
        var contactDtoToCreateContactObama = DtoContact.builder()
                .name("name")
                .lastName("Last Name")
                .phoneNumber("0893475000")
                .nameOfCompany("Name of Company")
                .address("address")
                .email("email@gmail.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("Comment")
                .build();
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates 5 contacts with the before each user
        for (int i = 0; i < 5 ; i++){

            createContact(jsonPayloadDonald, jwtToken, status().isOk());
        }

        MvcResult listOfContacts = getAllContacts(jwtToken);
        String responseContent = listOfContacts.getResponse().getContentAsString();

        JsonNode jsonResponse = convertFromStringToJson(responseContent);

        assertTrue(jsonResponse.isArray());

    }


    @Test
    @Transactional
    public void getNoContacts_thenReturns200() throws Exception {
        //create contact of before each user

        MvcResult listOfContacts = getAllContacts(jwtToken);
        String responseContent = listOfContacts.getResponse().getContentAsString();

        JsonNode jsonResponse = convertFromStringToJson(responseContent);

        assertTrue(jsonResponse.isEmpty());

    }


    @Test
    @Transactional
    public void deleteContact_thenReturns200() throws Exception {
        //create contact of before each user
        // prep json payload
        var contactDtoToCreateContactObama = DtoContact.builder()
                .name("name")
                .lastName("Last Name")
                .phoneNumber("0893475000")
                .nameOfCompany("Name of Company")
                .address("address")
                .email("email@gmail.com")
                .fax("+1-907-555-1234")
                .mobileNumber("0574965333")
                .comment("Comment")
                .build();
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        MvcResult createdContactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        String response = createdContactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        //delete that contact
        MvcResult deleteResult = deleteContact(idObamaContactStr, jwtToken, status().isNoContent());
        assertEquals(204, deleteResult.getResponse().getStatus());
    }



    @Test
    @Transactional
    public void deleteForeignContact_thenReturns400() throws Exception {
        //create contact of before each user
        // prep json payload
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));


        //create new user
        //create user
        var barakObama = RegisterRequest.builder()
                .firstName("Barak")
                .lastName("Obama")
                .email("BarakObama@usa.com")
                .password("!IlOvED0n41d7RumP")
                .build();
        MvcResult registerResult = createUser(barakObama);

        //extract jwt token
        String jwtTokenObama = extractJwtToken(registerResult.getResponse().getContentAsString());


        //use the Barak's jwt token to try and update Donald's contact
        // patch at /api/v1/contact/update-contact/1
        MvcResult deleteResult = deleteContact(idObamaContactStr, jwtTokenObama, status().isBadRequest());

        JsonNode jsonResult = convertFromStringToJson(deleteResult.getResponse().getContentAsString());

        assertEquals("User doesn't have permissions to perform this action.", jsonResult.get("message").asText());
    }

    @Test
    @Transactional
    public void deleteNoContact_thenReturns404() throws Exception {

        // patch at /api/v1/contact/update-contact/1
        MvcResult deleteResult = deleteContact("1", jwtToken, status().isNotFound());
        JsonNode jsonResult = convertFromStringToJson(deleteResult.getResponse().getContentAsString());

        assertEquals("No contact with id 1 found.", jsonResult.get("message").asText());
    }





    @Test
    @Transactional
    public void createCustomRow_thenExpects200() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get a hold of the newly created custom row id
        JsonNode createdCustomRowJsonNode = convertFromStringToJson(createCustomRowResult.getResponse().getContentAsString());
        Integer newlyCreatedCustomRowId = Integer.parseInt(createdCustomRowJsonNode.get("id").asText());



        // Create and configure the ObjectMapper instance (consider reusing if possible)
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert the response string to JsonNode
        JsonNode actualResultNode = objectMapper.readTree(createCustomRowResult.getResponse().getContentAsString());

        // Convert the expected JSON string to JsonNode
        JsonNode expectedResultNode = objectMapper.readTree(jsonPayload);

        // Add the newly created id to the expected result node
        ((ObjectNode) expectedResultNode).put("id", newlyCreatedCustomRowId);

        // Now compare the two JsonNode objects
        assertEquals(expectedResultNode, actualResultNode, "The expected result does not match the actual result.");

    }


    @Test
    @Transactional
    public void createCustomRowWithEmptyId_thenExpects500() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        var customRow = DtoCustomRow.builder()
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isInternalServerError());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("The given id must not be null", actualMessage);
    }


    @Test
    @Transactional
    public void createCustomRowWithNegativeId_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        var customRow = DtoCustomRow.builder()
                .contactId(-1)
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isBadRequest());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("must be greater than 0.", actualMessage);
    }


    @Test
    @Transactional
    public void createCustomRowWithMissingName_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isBadRequest());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("must not be blank.", actualMessage);
    }



    @Test
    @Transactional
    public void createCustomRowWithMissingField_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get result to extract id
        String response = contactResult.getResponse().getContentAsString();

        //get created contact id
        String idObamaContactStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(","));

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Obama")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isBadRequest());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("must not be blank.", actualMessage);
    }


    @Test
    @Transactional
    public void createCustomRowWithMissingAllFields_thenExpects400() throws Exception {

        var customRow = DtoCustomRow.builder().build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isBadRequest());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("must not be blank. must not be blank.", actualMessage);
    }

    @Test
    @Transactional
    public void createCustomRowWithNoJsonPayload_thenExpects500() throws Exception {

        //prep request
        String jsonPayload = "";

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isInternalServerError());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertEquals("Required request body is missing", actualMessage);
    }



    @Test
    @Transactional
    public void createCustomRowWithMultipleMessagesResponse_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        createContact(jsonPayloadDonald, jwtToken, status().isOk());


        var customRow = DtoCustomRow.builder()
                .contactId(-1)
                .customName("  ")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isBadRequest());

        String actualResult = createCustomRowResult.getResponse().getContentAsString();

        JsonNode responseJsonNode = convertFromStringToJson(actualResult);
        String actualMessage = responseJsonNode.get("message").asText();
        assertTrue(actualMessage.contains("must be greater than 0") && actualMessage.contains("must not be blank."));
    }



    @Test
    @Transactional
    public void updateCustomRow_thenExpects200() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("id").asText();

        customRow.setCustomName("Updated Name3");
        customRow.setCustomField("Updated Custom Field4");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isOk());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertAll("Custom row validations",
                () -> assertEquals(customRow.getCustomName(), updateResultJsonNode.get("customName").asText(), "Custom name mismatch"),
                () -> assertEquals(customRow.getCustomField(), updateResultJsonNode.get("customField").asText(), "Custom field mismatch"),
                () -> assertEquals(idObamaContactStr, updateResultJsonNode.get("contactId").asText(), "Custom field mismatch")

        );


    }



    @Test
    @Transactional
    public void updateCustomRowName_thenExpects200() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("id").asText();

        customRow.setCustomName("Updated Name2");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isOk());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertAll("Custom row validations",
                () -> assertEquals(customRow.getCustomName(), updateResultJsonNode.get("customName").asText(), "Custom name mismatch"),
                () -> assertEquals(customRow.getCustomField(), updateResultJsonNode.get("customField").asText(), "Custom field mismatch"),
                () -> assertEquals(idObamaContactStr, updateResultJsonNode.get("contactId").asText(), "Custom field mismatch")

        );


    }


    @Test
    @Transactional
    public void updateCustomRowField_thenExpects200() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("contactId").asText();

        customRow.setCustomField("Updated Field1");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isOk());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertAll("Custom row validations",
                () -> assertEquals(customRow.getCustomName(), updateResultJsonNode.get("customName").asText(), "Custom name mismatch"),
                () -> assertEquals(customRow.getCustomField(), updateResultJsonNode.get("customField").asText(), "Custom field mismatch"),
                () -> assertEquals(idObamaContactStr, updateResultJsonNode.get("contactId").asText(), "Custom field mismatch")

        );


    }



    @Test
    @Transactional
    public void updateCustomRowName_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("id").asText();

        //update with white space
        customRow.setCustomName(" ");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isBadRequest());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertEquals("Incorrect request body",updateResultJsonNode.get("message").asText(), "Custom name mismatch");


        //update with white the same name
        customRow.setCustomName("What I think about Barak Obama");
        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();
        updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isBadRequest());
        updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertEquals("Incorrect request body",updateResultJsonNode.get("message").asText(), "Custom name mismatch");

    }


    @Test
    @Transactional
    public void updateCustomRowField_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();
        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("contactId").asText();

        //update with white space
        customRow.setCustomField(" ");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isBadRequest());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertEquals("Incorrect request body",updateResultJsonNode.get("message").asText(), "Custom name mismatch");


        //update with white the same name
        customRow.setCustomField("I don't like him");
        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();
        updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isBadRequest());
        updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());

        assertEquals("Incorrect request body",updateResultJsonNode.get("message").asText(), "Custom name mismatch");

    }


    @Test
    @Transactional
    public void updateCustomRowWithNullAsNameAndField_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();

        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("id").asText();

        //set the name and fie to null - expected to get Incorrect request body
        customRow.setCustomField(null);
        customRow.setCustomName(null);

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isBadRequest());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());


        assertEquals("Incorrect request body",updateResultJsonNode.get("message").asText(), "Custom name mismatch");

    }


    @Test
    @Transactional
    public void updateCustomRowWithMissingRequestData_thenExpects400() throws Exception {
        //create contact with the before each user
        var contactDtoToCreateContactObama = DtoContact.builder()
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
        String jsonPayloadDonald = manageContactPayload(contactDtoToCreateContactObama, false);

        //this creates a contact with the before each user
        MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

        //get id with json node
        JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

        String idObamaContactStr = jsonNodeObama.get("id").asText();

        var customRow = DtoCustomRow.builder()
                .contactId(Integer.parseInt(idObamaContactStr))
                .customName("What I think about Barak Obama")
                .customField("I don't like him")
                .build();

        //prep request
        String jsonPayload = convertFromObjectToJson(customRow).toString();

        //create custom row to that contact: data needed: contactId, customName, customField
        MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

        //get id custom row with json node
        JsonNode jsonNodeCustomRow = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

        String idCustomRowStr = jsonNodeCustomRow.get("contactId").asText();



        //prep request to update
        jsonPayload = "";

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, jwtToken, status().isInternalServerError());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());


        assertEquals("Required request body is missing",updateResultJsonNode.get("message").asText(), "Custom name mismatch");

    }














    public JsonNode convertFromStringToJson(String jsonStringPayload){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonStringPayload);
            // Now you can work with jsonResponse as a JsonNode
            // For example, you can check the size of an array, specific fields, etc.
        } catch (IOException e) {
            fail("Failed to parse the response content as JSON");
            return null;
        }
    }

    public JsonNode convertFromObjectToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            fail("Failed to convert the object to JSON");
            return null;
        }
    }

    public MvcResult deleteContact(String contactId, String jwtToken, ResultMatcher expectedResult) throws Exception {
        return mockMvc.perform(delete("/api/v1/contact/delete-contact/" + contactId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedResult)
                .andReturn(); // Get the result of the executed request
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


    public MvcResult createCustomRow( String jsonPayload, String jwtToken, ResultMatcher expectedResult) throws Exception {
        return mockMvc.perform(post("/api/v1/custom-row/create-custom-row")
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

    public MvcResult updateCustomRow( String jsonPayload, String contactId, String jwtToken, ResultMatcher expectedResult) throws Exception {
        return mockMvc.perform(patch("/api/v1/custom-row/update-custom-row/" + contactId)
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

    public MvcResult getAllContacts(String jwtToken) throws Exception {
        return mockMvc.perform(get("/api/v1/contact/get-all-contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
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
