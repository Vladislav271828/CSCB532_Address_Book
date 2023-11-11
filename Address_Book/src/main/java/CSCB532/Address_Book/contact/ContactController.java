package CSCB532.Address_Book.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody DtoContact dtoContact) {
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

        return ResponseEntity.ok(contactService.deleteContactById(contactId));

    }
}
