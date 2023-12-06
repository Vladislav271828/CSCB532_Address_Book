package CSCB532.Address_Book.contact;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.List;
@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/create-contact")
    public ResponseEntity<DtoContact> createContact(
            @Valid @RequestBody DtoContact dtoContact) {
        return ResponseEntity.ok(contactService.createContact(dtoContact));
    }

    @PatchMapping("/update-contact/{contactId}")
    public ResponseEntity<DtoContact> updateContact(
            @PathVariable Integer contactId,
            @RequestBody DtoContact dtoContact) {

        return ResponseEntity.ok(contactService.updateContact(contactId, dtoContact));

    }

    @GetMapping("/get-all-contacts")
    public ResponseEntity<List<DtoContact>> getAllContacts() {

        return ResponseEntity.ok(contactService.getAllContactsForLoggedInUser());

    }

    @DeleteMapping("/delete-contact/{contactId}")
    public ResponseEntity<String> deleteContact(@PathVariable Integer contactId) {
        contactService.deleteContactById(contactId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/search-contact")
    public ResponseEntity<List<DtoContact>> searchContact(
            @RequestBody DtoContact dtoContact) {

        return ResponseEntity.ok(contactService.searchContacts(dtoContact));

    }

    @PatchMapping("{contactId}/add-label/{labelId}")
    public ResponseEntity<DtoContact> addLabelToContact(
            @PathVariable Integer contactId,
            @PathVariable Integer labelId) {

        return ResponseEntity.ok(contactService.addLabelToContact(contactId, labelId));

    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportAllContactsToCSV() {
        String csvContent = contactService.exportAllContactsToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");
        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }

    @GetMapping("/export/json")
    public ResponseEntity<String> exportContactsToJSON() {
        try {
            String jsonContent = contactService.exportAllContactsToJSON();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.json");
            return new ResponseEntity<>(jsonContent, headers, HttpStatus.OK);
        } catch (ExportException e) {
            return new ResponseEntity<>("Error exporting contacts to JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @CrossOrigin
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportContactsToExcel() {
        try {
            byte[] excelContent = contactService.exportAllContactsToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.xlsx");
            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            // Handle exception appropriately, e.g., return an error response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
