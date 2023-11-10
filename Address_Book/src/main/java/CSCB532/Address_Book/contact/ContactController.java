package CSCB532.Address_Book.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
