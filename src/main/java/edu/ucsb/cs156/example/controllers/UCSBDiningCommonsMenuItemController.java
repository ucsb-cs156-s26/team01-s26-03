package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbdiningcommonsmenuitems")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    return ucsbDiningCommonsMenuItemRepository.findAll();
  }

  @Operation(summary = "Get a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    return ucsbDiningCommonsMenuItemRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));
  }

  @Operation(summary = "Create a new UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    return ucsbDiningCommonsMenuItemRepository.save(item);
  }

  @Operation(summary = "Delete a UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(item);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }

  @Operation(summary = "Update a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    item.setDiningCommonsCode(incoming.getDiningCommonsCode());
    item.setName(incoming.getName());
    item.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(item);

    return item;
  }
}
