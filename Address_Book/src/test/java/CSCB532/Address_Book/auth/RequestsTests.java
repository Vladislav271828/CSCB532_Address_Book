package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.customRow.DtoCustomRow;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RequestsTests {

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
        createContact(jsonPayloadDonald, jwtToken, status().isOk());



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
        createContact(jsonPayloadDonald, jwtToken, status().isOk());


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
    public void updateCustomRowNameWithWhiteSpace_thenExpects400() throws Exception {
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
    public void updateCustomRowFieldWithWhiteSpace_thenExpects400() throws Exception {
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





    @Test
    @Transactional
    public void updateForeignCustomRow_thenExpects400() throws Exception {
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


        //create new user
        var registerObamaDto = RegisterRequest.builder()
                .firstName("Barak")
                .lastName("Obama")
                .email("BarakObama@usa.com")
                .password("!l0V3D0n41D7RuMp")
                .build();
        MvcResult registerObamaResult = createUser(registerObamaDto);

        //extract jwt token
        String newlyCreatedUserJwtToken = extractJwtToken(registerObamaResult.getResponse().getContentAsString());

        //set the name and fie to null - expected to get Incorrect request body
        customRow.setCustomField("Actually I like him very much.");

        //prep request to update
        jsonPayload = convertFromObjectToJson(customRow).toString();

        MvcResult updateResult = updateCustomRow(jsonPayload, idCustomRowStr, newlyCreatedUserJwtToken, status().isBadRequest());
        JsonNode updateResultJsonNode = convertFromStringToJson(updateResult.getResponse().getContentAsString());


        assertEquals("User doesn't have permissions to perform this action.",updateResultJsonNode.get("message").asText(), "No permission to edit this custom row.");

    }






//case where it finds it with 200
@Test
@Transactional
public void getAllCustomRowsForContactId_thenExpects400() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();

    var customRow = DtoCustomRow.builder()
            .contactId(Integer.parseInt(idObamaContactStr))
            .customName("What I think about Barak Obama")
            .customField("I don't like him")
            .build();

    // prep request to create custom row
    String jsonPayload = convertFromObjectToJson(customRow).toString();

    // create 5 custom rows to that contact: data needed: contactId, customName, customField
            for (int i = 0; i < 5; i++){

                createCustomRow(jsonPayload,jwtToken,status().isOk());
            }

    // make a get all request with the contact id
    MvcResult resultCustomRowsForContact = getAllCustomRowsForContactId(jwtToken, idObamaContactStr, status().isOk());

    // convert to json node to later check for content or length
    JsonNode customRowListResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertTrue(customRowListResult.isArray(), "Is not a list.");
}


//case where the custom row id is 404
@Test
@Transactional
public void getAllCustomRowsForContactIdNotFound_thenExpects404() throws Exception {
    // make a get all request with the contact id
    MvcResult resultCustomRowsForContact = getAllCustomRowsForContactId(jwtToken, "99999", status().isNotFound());

    // convert to json node to later check for content or length
    JsonNode customRowListResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("Contact with ID 99999 not found.", customRowListResult.get("message").asText());
}



//case where User doesn't have permissions to perform this action. 400
@Test
@Transactional
public void getAllCustomRowsForForeignContact_thenExpects400() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();

    var customRow = DtoCustomRow.builder()
            .contactId(Integer.parseInt(idObamaContactStr))
            .customName("What I think about Barak Obama")
            .customField("I don't like him")
            .build();

    // prep request to create custom row
    String jsonPayload = convertFromObjectToJson(customRow).toString();

    // create 5 custom rows to that contact: data needed: contactId, customName, customField
    for (int i = 0; i < 5; i++){

        createCustomRow(jsonPayload,jwtToken,status().isOk());
    }


    //create new user
    var registerObamaDto = RegisterRequest.builder()
            .firstName("Barak")
            .lastName("Obama")
            .email("BarakObama@usa.com")
            .password("!l0V3D0n41D7RuMp")
            .build();
    MvcResult registerObamaResult = createUser(registerObamaDto);

    //extract jwt token
    String newlyCreatedUserJwtToken = extractJwtToken(registerObamaResult.getResponse().getContentAsString());




    // make a get all request with the contact id of first user
    MvcResult resultCustomRowsForContact = getAllCustomRowsForContactId(newlyCreatedUserJwtToken, idObamaContactStr, status().isBadRequest());

    // convert to json node to later check for content or length
    JsonNode customRowListResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("User doesn't have permissions to perform this action.", customRowListResult.get("message").asText());
}


//case when deleting with 204
@Test
@Transactional
public void deleteAllCustomRowsForContact_thenExpects204() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();

    var customRow = DtoCustomRow.builder()
            .contactId(Integer.parseInt(idObamaContactStr))
            .customName("What I think about Barak Obama")
            .customField("I don't like him")
            .build();

    // prep request to create custom row
    String jsonPayload = convertFromObjectToJson(customRow).toString();

    // create 5 custom rows to that contact: data needed: contactId, customName, customField
    for (int i = 0; i < 5; i++){

        createCustomRow(jsonPayload,jwtToken,status().isOk());
    }



    // make a get all request with the contact id
    MvcResult resultCreateCustomRowsForContact = getAllCustomRowsForContactId(jwtToken, idObamaContactStr, status().isOk());
    JsonNode customRowListResult = convertFromStringToJson(resultCreateCustomRowsForContact.getResponse().getContentAsString());

    assertTrue(customRowListResult.isArray(),"Is not an array.");

    // delete all custom rows for contact id
    MvcResult resultDeleteCustomRowsForContact = deleteCustomRowsByContactId(jwtToken, idObamaContactStr, status().isNoContent());

    // assert
    assertEquals(204., resultDeleteCustomRowsForContact.getResponse().getStatus());
}



//case when User doesn't have permissions to perform this action.
@Test
@Transactional
public void deleteAllCustomRowsForForeignContact_thenExpects400() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();




    //create new user
    var registerObamaDto = RegisterRequest.builder()
            .firstName("Barak")
            .lastName("Obama")
            .email("BarakObama@usa.com")
            .password("!l0V3D0n41D7RuMp")
            .build();
    MvcResult registerObamaResult = createUser(registerObamaDto);

    //extract jwt token
    String newlyCreatedUserJwtToken = extractJwtToken(registerObamaResult.getResponse().getContentAsString());




    // make a delete all request with the contact id of first user
    MvcResult resultCustomRowsForContact = deleteCustomRowsByContactId(newlyCreatedUserJwtToken, idObamaContactStr, status().isBadRequest());

    // convert to json node to later check for content or length
    JsonNode deleteCustomRowForForeignContactJsonNodeResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("User doesn't have permissions to perform this action.", deleteCustomRowForForeignContactJsonNodeResult.get("message").asText());
}



//case when trying to delete custom rows of contact but there are no custom rows
@Test
@Transactional
public void deleteAllCustomRowsForContactWithNoCustomRows_thenExpects404() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();




    // make a delete all request with the contact id of first user
    MvcResult resultCustomRowsForContact = deleteCustomRowsByContactId(jwtToken, idObamaContactStr, status().isNotFound());

    // convert to json node to later check for content or length
    JsonNode deleteCustomRowForForeignContactJsonNodeResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("No Custom Rows found for contact with id " + idObamaContactStr, deleteCustomRowForForeignContactJsonNodeResult.get("message").asText());
}



//case when contact not found
@Test
@Transactional
public void deleteAllCustomRowsForContactIdNotFound_thenExpects404() throws Exception {

    // make a delete all request with the contact id of first user
    MvcResult resultCustomRowsForContact = deleteCustomRowsByContactId(jwtToken, "99999", status().isNotFound());

    // convert to json node to later check for content or length
    JsonNode deleteCustomRowForForeignContactJsonNodeResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("Contact with ID 99999 not found.", deleteCustomRowForForeignContactJsonNodeResult.get("message").asText());
}



//case when deleting custom row 204
@Test
@Transactional
public void deleteCustomRowForCustomRowId_thenExpects204() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
    JsonNode jsonNodeObama = convertFromStringToJson(contactResult.getResponse().getContentAsString());

    String idObamaContactStr = jsonNodeObama.get("id").asText();

    var customRow = DtoCustomRow.builder()
            .contactId(Integer.parseInt(idObamaContactStr))
            .customName("What I think about Barak Obama")
            .customField("I don't like him")
            .build();

    // prep request to create custom row
    String jsonPayload = convertFromObjectToJson(customRow).toString();

    // create custom row to that contact: data needed: contactId, customName, customField
    MvcResult createdCustomRowResult = createCustomRow(jsonPayload,jwtToken,status().isOk());

    JsonNode createdCustomRowJsonResult = convertFromStringToJson(createdCustomRowResult.getResponse().getContentAsString());

    String customRowIdStr = createdCustomRowJsonResult.get("id").asText();

    // delete created custom rows for custom row id
    MvcResult resultDeleteCustomRowsForContact = deleteCustomRowById(jwtToken, customRowIdStr, status().isNoContent());

    // assert
    assertEquals(204., resultDeleteCustomRowsForContact.getResponse().getStatus());
}

//case when deleting custom row not found
@Test
@Transactional
public void deleteCustomRowForContactIdNotFound_thenExpects404() throws Exception {
    // make a get all request with the contact id
    MvcResult resultCustomRowsForContact = deleteCustomRowById(jwtToken, "99999", status().isNotFound());

    // convert to json node to later check for content or length
    JsonNode customRowListResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("Custom row not found for ID: 99999", customRowListResult.get("message").asText());
}



//case when deleting custom row User doesn't have permissions to perform this action. 400
@Test
@Transactional
public void deleteCustomRowForForeignCustomRow_thenExpects400() throws Exception {
    // create contact with the before each user
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

    // this creates a contact with the before each user
    MvcResult contactResult = createContact(jsonPayloadDonald, jwtToken, status().isOk());

    // get id with json node to create custom row with contactId of this contact
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

    //create new user
    var registerObamaDto = RegisterRequest.builder()
            .firstName("Barak")
            .lastName("Obama")
            .email("BarakObama@usa.com")
            .password("!l0V3D0n41D7RuMp")
            .build();
    MvcResult registerObamaResult = createUser(registerObamaDto);

    //extract jwt token
    String newlyCreatedUserJwtToken = extractJwtToken(registerObamaResult.getResponse().getContentAsString());




    // make a delete all request with the contact id of first user
    MvcResult resultCustomRowsForContact = deleteCustomRowById(newlyCreatedUserJwtToken, idCustomRowStr, status().isBadRequest());

    // convert to json node to later check for content or length
    JsonNode deleteCustomRowForForeignContactJsonNodeResult = convertFromStringToJson(resultCustomRowsForContact.getResponse().getContentAsString());

    // assert
    assertEquals("User doesn't have permissions to perform this action.", deleteCustomRowForForeignContactJsonNodeResult.get("message").asText());
}



//get user info 200
@Test
@Transactional
public void getUserInformation_thenExpect200() throws Exception {
    // use the before user jwt to authenticate the request
    // make the request
    MvcResult userInfoResult = getUserInformation(jwtToken);

    //convert result to jsonNode
    JsonNode jsonNodeUserInfoResult = convertFromStringToJson(userInfoResult.getResponse().getContentAsString());
    String actualFirstName = jsonNodeUserInfoResult.get("firstName").asText();
    String actualLastName = jsonNodeUserInfoResult.get("lastName").asText();
    String actualEmail = jsonNodeUserInfoResult.get("email").asText();


    // assert
    assertAll("Custom row validations",
            () -> assertEquals("Donald", actualFirstName, "First name mismatch"),
            () -> assertEquals("Trump", actualLastName, "Last name mismatch"),
            () -> assertEquals("DonaldTrump@usa.com", actualEmail, "Custom field mismatch")

    );

}


//change password 200
@Test
@Transactional
public void changePassword_thenExpect200() throws Exception {
    // use the before user jwt to authenticate the request

    //new password
    String newPassword = "!(*)QH@WFRH@";

    String jsonRequestPayload = "{\"password\":\""+newPassword+"\"}";


    // make the request
    MvcResult userInfoResult = changePassword(jsonRequestPayload, jwtToken, status().isOk());

    //convert result to jsonNode
    String actualResult = userInfoResult.getResponse().getContentAsString();


    // assert
    assertEquals("Password changed Successfully",actualResult,"Message mismatch");

}


    //change first and last name 200
    @Test
    @Transactional
    public void changeFirstAndLastName_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = "Barak";
        String newLastName = "Obama";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\",\"lastName\":\""+newLastName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals(newFirstName, actualFirstName, "First name mismatch"),
                () -> assertEquals(newLastName, actualLastName, "Last name mismatch")
        );
    }

    //change first name 200
    @Test
    @Transactional
    public void changeFirstName_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = "Barak";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals(newFirstName, actualFirstName, "First name mismatch"),
                () -> assertEquals("Trump", actualLastName, "Last name mismatch")
        );
    }

    //change last name 200
    @Test
    @Transactional
    public void changeLastName_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newLastName = "Obama";

        String jsonRequestPayload = "{\"lastName\":\""+newLastName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals("Donald", actualFirstName, "First name mismatch"),
                () -> assertEquals(newLastName, actualLastName, "Last name mismatch")
        );
    }


    //change first name with same first name Invalid Data provided.
    @Test
    @Transactional
    public void changeFirstNameWithOldFirstName_thenExpect400() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = "Donald";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isBadRequest());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String message = actualResultJsonNode.get("message").asText();

        // assert
        assertEquals("Invalid Data provided.", message, "Message mismatch");
    }

    //change last name with same last name Invalid Data provided.
    @Test
    @Transactional
    public void changeLastNameWithOldLastName_thenExpect400() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newLastName = "Trump";

        String jsonRequestPayload = "{\"lastName\":\""+newLastName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isBadRequest());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String message = actualResultJsonNode.get("message").asText();

        // assert
        assertEquals("Invalid Data provided.", message, "Message mismatch");
    }


    //change first name and have last name the same as last name 200.
    @Test
    @Transactional
    public void changeFirstNameAndHaveLastNameIsOld_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = "Barak";
        String oldLastName = "Trump";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\",\"lastName\":\""+oldLastName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals(newFirstName, actualFirstName, "First name mismatch"),
                () -> assertEquals(oldLastName, actualLastName, "Last name mismatch")
        );
    }


    //change last name and have first name the same as first name 200.
    @Test
    @Transactional
    public void changeLastNameAndHaveFirstNameIsOld_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = "Donald";
        String oldLastName = "Obama";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\",\"lastName\":\""+oldLastName+"\"}";


        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals(newFirstName, actualFirstName, "First name mismatch"),
                () -> assertEquals(oldLastName, actualLastName, "Last name mismatch")
        );
    }

    //change first name with blank
    //change last name with blank
    @Test
    @Transactional
    public void changeFirstAndLastNameWithBlank_thenExpect400() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newFirstName = " ";
        String oldLastName = "  ";

        String jsonRequestPayload = "{\"firstName\":\""+newFirstName+"\",\"lastName\":\""+oldLastName+"\"}";

        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isBadRequest());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String message = actualResultJsonNode.get("message").asText();

        // assert
        assertEquals("Invalid Data provided.", message, "Message mismatch");
    }

    //change with no content Invalid input, at least one name must be provided.
    @Test
    @Transactional
    public void changeFirstAndLastNameWithNoContent_thenExpect400() throws Exception {
        // use the before user jwt to authenticate the request

        String jsonRequestPayload = "{}";

        // make the request
        MvcResult changeNamesResult = changeFirstNameAndLastName(jsonRequestPayload, jwtToken, status().isBadRequest());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String message = actualResultJsonNode.get("message").asText();

        // assert
        assertEquals("Invalid input, at least one name must be provided.", message, "Message mismatch");
    }


    //update email 200
    @Test
    @Transactional
    public void changeEmail_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newEmail = "New_Email@gmail.com";

        String jsonRequestPayload = "{\"email\":\""+newEmail+"\"}";


        // make the request
        MvcResult changeNamesResult = changeEmail(jsonRequestPayload, jwtToken, status().isOk());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        String actualFirstName = actualResultJsonNode.get("firstName").asText();
        String actualLastName = actualResultJsonNode.get("lastName").asText();
        String actualEmail = actualResultJsonNode.get("email").asText();

        // assert
        assertAll("Custom row validations",
                () -> assertEquals("Donald", actualFirstName, "First name mismatch"),
                () -> assertEquals("Trump", actualLastName, "Last name mismatch"),
                () -> assertEquals(newEmail, actualEmail, "Email name mismatch")
        );
    }






    //update email with an old email in the db 400
    @Test
    @Transactional
    public void changeEmailWithOldEmail_thenExpect200() throws Exception {
        // use the before user jwt to authenticate the request

        //new password
        String newEmail = "DonaldTrump@usa.com";

        String jsonRequestPayload = "{\"email\":\""+newEmail+"\"}";


        // make the request
        MvcResult changeNamesResult = changeEmail(jsonRequestPayload, jwtToken, status().isBadRequest());

        //convert result to jsonNode
        String actualResult = changeNamesResult.getResponse().getContentAsString();

        JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

        assertEquals("Email already in use.", actualResultJsonNode.get("message").asText());

    }


//update email with an already existing email in the db 400
@Test
@Transactional
public void changeEmailWithEmailAlreadyInUse_thenExpect200() throws Exception {
    // use the before user jwt to authenticate the request
    //create new user
    //create user
    var newUserDto = RegisterRequest.builder()
            .firstName("Barak")
            .lastName("Obama")
            .email("BarakObama@usa.com")
            .password("A(&*SDHY(A*QHWF")
            .build();
    MvcResult registerResult = createUser(newUserDto);

    String obamaJwt = extractJwtToken(registerResult.getResponse().getContentAsString());

    //new password
    String newEmail = "DonaldTrump@usa.com";

    String jsonRequestPayload = "{\"email\":\""+newEmail+"\"}";


    // make the request
    MvcResult changeNamesResult = changeEmail(jsonRequestPayload, obamaJwt, status().isBadRequest());

    //convert result to jsonNode
    String actualResult = changeNamesResult.getResponse().getContentAsString();

    JsonNode actualResultJsonNode = convertFromStringToJson(actualResult);

    assertEquals("Email already in use.", actualResultJsonNode.get("message").asText());

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

    public MvcResult deleteCustomRowsByContactId(String jwtToken, String contactId, ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(delete("/api/v1/custom-row/delete-custom-row-by-contact-id/" + contactId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult deleteCustomRowById(String jwtToken, String contactId, ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(delete("/api/v1/custom-row/delete-custom-row-by-id/" + contactId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
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

    public MvcResult getUserInformation(String jwtToken) throws Exception{

        return mockMvc.perform(get("/api/v1/user-profile/get-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult changePassword(String jsonRequestPayload, String jwtToken, ResultMatcher resultMatcher) throws Exception{

        return mockMvc.perform(patch("/api/v1/user-profile/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestPayload))
                .andExpect(resultMatcher)
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult changeFirstNameAndLastName(String jsonRequestPayload, String jwtToken, ResultMatcher resultMatcher) throws Exception{

        return mockMvc.perform(patch("/api/v1/user-profile/update-user-names")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestPayload))
                .andExpect(resultMatcher)
                .andReturn(); // Get the result of the executed request
    }

    public MvcResult changeEmail(String jsonRequestPayload, String jwtToken, ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(patch("/api/v1/user-profile/update-email")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestPayload))
                .andExpect(resultMatcher)
                .andReturn(); // Get the result of the executed request
    }
    public MvcResult getAllCustomRowsForContactId(String jwtToken, String contactId, ResultMatcher resultMatcher) throws Exception {

        return mockMvc.perform(get("/api/v1/custom-row/"+contactId+"/get-all-custom-rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
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






}
