package CSCB532.Address_Book.label;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/label")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }


    @PostMapping("/create-label")
    public ResponseEntity<DtoLabel> createLabel(
            @RequestBody DtoLabel dtoLabel) {
        return ResponseEntity.ok(labelService.createLabel(dtoLabel));
    }

    @PatchMapping("/update-label/{labelId}")
    public ResponseEntity<DtoLabel> updateLabel(
            @PathVariable Integer labelId,
            @RequestBody DtoLabel dtoLabel) {

        return ResponseEntity.ok(labelService.updateLabel(labelId, dtoLabel));
    }

    @GetMapping("/get-all-labels")
    public ResponseEntity<List<DtoLabel>> getAllLabels() {

        return ResponseEntity.ok(labelService.getAllLabelsForLoggedInUser());
    }

    @DeleteMapping("/delete-label/{labelId}")
    public ResponseEntity<String> deleteLabel(@PathVariable Integer labelId) {

        return ResponseEntity.ok(labelService.deleteLabelById(labelId));
    }


}
