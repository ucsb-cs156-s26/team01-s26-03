package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
