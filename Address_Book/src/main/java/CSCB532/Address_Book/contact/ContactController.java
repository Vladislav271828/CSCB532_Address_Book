package CSCB532.Address_Book.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }
    @PostMapping("/create-contact")
    public ResponseEntity<DtoContact> createContact(
            @RequestBody DtoContact dtoContact){
        return ResponseEntity.ok(contactService.createContact(dtoContact));
    }

    @PutMapping("/update-contact/{contactId}")
    public ResponseEntity<DtoContact> updateContact(
            @PathVariable Integer contactId,
            @RequestBody DtoContact dtoContact) {

        DtoContact updatedContact = contactService.updateContact(contactId, dtoContact);

        if (updatedContact != null) {
            return ResponseEntity.ok(updatedContact);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-contacts")
    public ResponseEntity<List<DtoContact>> getAllContacts() {
        List<DtoContact> contacts = contactService.getAllContactsForLoggedInUser();
        if (contacts != null && !contacts.isEmpty()) {
            return ResponseEntity.ok(contacts);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/delete-contact/{contactId}")
    public ResponseEntity<String> deleteContact(@PathVariable Integer contactId) {
        String deletionMessage = contactService.deleteContactById(contactId);

        if (deletionMessage != null) {
            return ResponseEntity.ok(deletionMessage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
