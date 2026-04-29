package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /** Get all MenuItemReviews */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    return menuItemReviewRepository.findAll();
  }

  /** Get a single MenuItemReview by id */
  @Operation(summary = "Get a single menu item review")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(@Parameter(name = "id") @RequestParam Long id) {
    return menuItemReviewRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
  }

  /** Create a new MenuItemReview */
  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam Long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "dateReviewed")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments)
      throws JsonProcessingException {

    MenuItemReview review = new MenuItemReview();
    review.setItemId(itemId);
    review.setReviewerEmail(reviewerEmail);
    review.setStars(stars);
    review.setDateReviewed(dateReviewed);
    review.setComments(comments);

    return menuItemReviewRepository.save(review);
  }

  /** Update a single MenuItemReview */
  @Operation(summary = "Update a single menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    review.setItemId(incoming.getItemId());
    review.setReviewerEmail(incoming.getReviewerEmail());
    review.setStars(incoming.getStars());
    review.setDateReviewed(incoming.getDateReviewed());
    review.setComments(incoming.getComments());

    menuItemReviewRepository.save(review);

    return review;
  }

  /** Delete a MenuItemReview */
  @Operation(summary = "Delete a menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteMenuItemReview(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReviewRepository.delete(review);
    return genericMessage("MenuItemReview with id %s deleted".formatted(id));
  }
}
