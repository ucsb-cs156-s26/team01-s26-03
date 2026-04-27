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

/** REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all dining commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all UCSB dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    return ucsbDiningCommonsMenuItemRepository.findAll();
  }

  /**
   * Get a single dining commons menu item by id
   *
   * @param id the id of the menu item
   * @return a UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Get a single UCSB dining commons menu item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    return ucsbDiningCommonsMenuItemRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));
  }

  /**
   * Create a new dining commons menu item
   *
   * @param diningCommonsCode the code of the dining commons (e.g. "ortega")
   * @param name the name of the menu item
   * @param station the station where the item is served (e.g. "Entrees")
   * @return the saved UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Create a new UCSB dining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(
              name = "diningCommonsCode",
              description = "Code of the dining commons (e.g. ortega)")
          @RequestParam
          String diningCommonsCode,
      @Parameter(name = "name", description = "Name of the menu item") @RequestParam String name,
      @Parameter(name = "station", description = "Station where the item is served (e.g. Entrees)")
          @RequestParam
          String station)
      throws JsonProcessingException {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    return ucsbDiningCommonsMenuItemRepository.save(item);
  }

  /**
   * Update a single dining commons menu item
   *
   * @param id the id of the menu item to update
   * @param incoming the new values for the menu item
   * @return the updated UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Update a single UCSB dining commons menu item")
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

  /**
   * Delete a single dining commons menu item
   *
   * @param id the id of the menu item to delete
   * @return a message indicating the item was deleted
   */
  @Operation(summary = "Delete a UCSB dining commons menu item")
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
}
