package CSCB532.Address_Book.customRow;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-row")
public class CustomRowController {
    private final CustomRowService customRowService;

    public CustomRowController(CustomRowService customRowService) {
        this.customRowService = customRowService;
    }


    @PostMapping("/create-custom-row")
    public ResponseEntity<DtoCustomRow> createCustomRow(
            @Valid @RequestBody DtoCustomRow dtoCustomRow) {
        return ResponseEntity.ok(customRowService.createCustomRow(dtoCustomRow));
    }

    @PatchMapping("/update-custom-row/{rowId}")
    public ResponseEntity<DtoCustomRow> updateCustomRow(
            @PathVariable Integer rowId,
            @RequestBody DtoCustomRow dtoCustomRow) {

        return ResponseEntity.ok(customRowService.updateCustomRow(rowId, dtoCustomRow));

    }

    @GetMapping("{contactId}/get-all-custom-rows")
    public ResponseEntity<List<DtoCustomRow>> getCustomRows(
            @PathVariable Integer contactId
    ) {
        return ResponseEntity.ok(customRowService.getCustomRowsByContactId(contactId));
    }

    @DeleteMapping("/delete-custom-row-by-contact-id/{contactId}")
    public ResponseEntity<Void> deleteCustomRowsByContactId(
            @PathVariable Integer contactId) {
        customRowService.deleteCustomRowsByContactId(contactId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-custom-row-by-id/{rowId}")
    public ResponseEntity<Void> deleteCustomRow(
            @PathVariable Integer rowId) {
        customRowService.deleteCustomRowById(rowId);
        return ResponseEntity.noContent().build();
    }
}
