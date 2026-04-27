package edu.ucsb.cs156.example.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockBean MenuItemReviewRepository menuItemReviewRepository;

  @MockBean UserRepository userRepository;

  // Tests for GET /api/MenuItemReview/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void all_returns_list_of_reviews() throws Exception {
    MenuItemReview review1 =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger1")
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .comments("Amazing!")
            .build();

    MenuItemReview review2 =
        MenuItemReview.builder()
            .id(2)
            .itemId("pizza1")
            .reviewerEmail("test2@ucsb.edu")
            .stars(4)
            .comments("Pretty good")
            .build();

    ArrayList<MenuItemReview> expectedReviews = new ArrayList<>(Arrays.asList(review1, review2));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    mockMvc
        .perform(get("/api/MenuItemReview/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].itemId").value("burger1"))
        .andExpect(jsonPath("$[0].reviewerEmail").value("test1@ucsb.edu"))
        .andExpect(jsonPath("$[0].stars").value(5))
        .andExpect(jsonPath("$[0].comments").value("Amazing!"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].itemId").value("pizza1"))
        .andExpect(jsonPath("$[1].reviewerEmail").value("test2@ucsb.edu"))
        .andExpect(jsonPath("$[1].stars").value(4))
        .andExpect(jsonPath("$[1].comments").value("Pretty good"));
  }

  // Tests for GET /api/MenuItemReview?id=...

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview").param("id", "1")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_id_exists() throws Exception {
    MenuItemReview review =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger1")
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .comments("Amazing!")
            .build();

    when(menuItemReviewRepository.findById(1L)).thenReturn(java.util.Optional.of(review));

    mockMvc
        .perform(get("/api/MenuItemReview").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemId").value("burger1"))
        .andExpect(jsonPath("$.reviewerEmail").value("test@ucsb.edu"))
        .andExpect(jsonPath("$.stars").value(5))
        .andExpect(jsonPath("$.comments").value("Amazing!"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_when_id_does_not_exist() throws Exception {
    when(menuItemReviewRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc.perform(get("/api/MenuItemReview").param("id", "999")).andExpect(status().isNotFound());
  }

  // Tests for POST /api/MenuItemReview/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "burger1")
                .param("reviewerEmail", "test@ucsb.edu")
                .param("stars", "5")
                .param("comments", "Amazing!")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "burger1")
                .param("reviewerEmail", "test@ucsb.edu")
                .param("stars", "5")
                .param("comments", "Amazing!")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_users_can_post_a_new_review() throws Exception {
    MenuItemReview savedReview =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger1")
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .comments("Amazing!")
            .build();

    when(menuItemReviewRepository.save(any())).thenReturn(savedReview);

    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "burger1")
                .param("reviewerEmail", "test@ucsb.edu")
                .param("stars", "5")
                .param("comments", "Amazing!")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemId").value("burger1"))
        .andExpect(jsonPath("$.reviewerEmail").value("test@ucsb.edu"))
        .andExpect(jsonPath("$.stars").value(5))
        .andExpect(jsonPath("$.comments").value("Amazing!"));

    ArgumentCaptor<MenuItemReview> reviewCaptor = ArgumentCaptor.forClass(MenuItemReview.class);

    org.mockito.Mockito.verify(menuItemReviewRepository).save(reviewCaptor.capture());

    MenuItemReview review = reviewCaptor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals("burger1", review.getItemId());
    org.junit.jupiter.api.Assertions.assertEquals("test@ucsb.edu", review.getReviewerEmail());
    org.junit.jupiter.api.Assertions.assertEquals(5, review.getStars());
    org.junit.jupiter.api.Assertions.assertEquals("Amazing!", review.getComments());
  }
}
