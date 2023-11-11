package CSCB532.Address_Book.customRow;

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


    @PostMapping("/{contactId}/create-custom-row")
    public ResponseEntity<DtoCustomRow> createCustomRow(
            @PathVariable Integer contactId,
            @RequestBody DtoCustomRow dtoCustomRow) {
        return ResponseEntity.ok(customRowService.createCustomRow(contactId, dtoCustomRow));
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

    @DeleteMapping("/delete-custom-row/{rowId}")
    public ResponseEntity<DtoCustomRow> deleteCustomRow(
            @PathVariable Integer rowId) {
        return ResponseEntity.ok(customRowService.deleteCustomRowById(rowId));
    }
}
