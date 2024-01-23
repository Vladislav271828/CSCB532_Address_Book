package CSCB532.Address_Book.contact;

import jakarta.validation.Valid;
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

    @PostMapping("/search-contact")
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

    @PatchMapping("{contactId}/remove-label/{labelId}")
    public ResponseEntity<DtoContact> removeLabelFromContact(
            @PathVariable Integer contactId,
            @PathVariable Integer labelId) {
        contactService.removeLabelFromContact(contactId, labelId);
        return ResponseEntity.noContent().build();

    }


//    @GetMapping("/get-contacts-with-most-common-label-as-user")
//    public ResponseEntity<List<DtoContact>> getContactsWithMostCommonLabel() {
//        return ResponseEntity.ok(contactService.getContactsWithMostCommonLabelByUserId());
//    }

    @GetMapping("/get-contacts-with-label/{labelId}")
    public ResponseEntity<List<DtoContact>> getContactsWithLabel(
            @PathVariable Integer labelId) {
        return ResponseEntity.ok(contactService.getContactsWithLabel(labelId));
    }
}
