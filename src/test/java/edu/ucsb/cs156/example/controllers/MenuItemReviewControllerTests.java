package edu.ucsb.cs156.example.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockBean MenuItemReviewRepository menuItemReviewRepository;
  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isOk());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void all_returns_list_of_reviews() throws Exception {
    MenuItemReview r1 =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger1")
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .comments("A")
            .build();

    MenuItemReview r2 =
        MenuItemReview.builder()
            .id(2)
            .itemId("pizza1")
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .comments("B")
            .build();

    when(menuItemReviewRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(r1, r2)));

    mockMvc
        .perform(get("/api/MenuItemReview/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].itemId").value("burger1"))
        .andExpect(jsonPath("$[0].reviewerEmail").value("a@ucsb.edu"))
        .andExpect(jsonPath("$[0].stars").value(5))
        .andExpect(jsonPath("$[0].comments").value("A"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].itemId").value("pizza1"))
        .andExpect(jsonPath("$[1].reviewerEmail").value("b@ucsb.edu"))
        .andExpect(jsonPath("$[1].stars").value(4))
        .andExpect(jsonPath("$[1].comments").value("B"));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview").param("id", "1")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_by_id_success() throws Exception {
    MenuItemReview r =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger1")
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .comments("A")
            .build();

    when(menuItemReviewRepository.findById(1L)).thenReturn(java.util.Optional.of(r));

    mockMvc
        .perform(get("/api/MenuItemReview").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemId").value("burger1"))
        .andExpect(jsonPath("$.reviewerEmail").value("a@ucsb.edu"))
        .andExpect(jsonPath("$.stars").value(5))
        .andExpect(jsonPath("$.comments").value("A"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_by_id_not_found() throws Exception {
    when(menuItemReviewRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(get("/api/MenuItemReview").param("id", "999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("MenuItemReview")))
        .andExpect(content().string(containsString("999")));
  }

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
  public void admin_can_post() throws Exception {
    MenuItemReview saved =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger")
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .comments("A")
            .build();

    when(menuItemReviewRepository.save(any())).thenReturn(saved);

    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "burger")
                .param("reviewerEmail", "a@ucsb.edu")
                .param("stars", "5")
                .param("comments", "A")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemId").value("burger"))
        .andExpect(jsonPath("$.reviewerEmail").value("a@ucsb.edu"))
        .andExpect(jsonPath("$.stars").value(5))
        .andExpect(jsonPath("$.comments").value("A"));

    ArgumentCaptor<MenuItemReview> captor = ArgumentCaptor.forClass(MenuItemReview.class);
    org.mockito.Mockito.verify(menuItemReviewRepository).save(captor.capture());

    MenuItemReview review = captor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals("burger", review.getItemId());
    org.junit.jupiter.api.Assertions.assertEquals("a@ucsb.edu", review.getReviewerEmail());
    org.junit.jupiter.api.Assertions.assertEquals(5, review.getStars());
    org.junit.jupiter.api.Assertions.assertEquals("A", review.getComments());
  }

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    MenuItemReview updated =
        MenuItemReview.builder()
            .itemId("new")
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .comments("B")
            .build();

    String body = new ObjectMapper().writeValueAsString(updated);

    mockMvc
        .perform(
            put("/api/MenuItemReview")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    MenuItemReview updated =
        MenuItemReview.builder()
            .itemId("new")
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .comments("B")
            .build();

    String body = new ObjectMapper().writeValueAsString(updated);

    mockMvc
        .perform(
            put("/api/MenuItemReview")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_put() throws Exception {
    MenuItemReview existing =
        MenuItemReview.builder()
            .id(1)
            .itemId("old")
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .comments("A")
            .build();

    MenuItemReview updated =
        MenuItemReview.builder()
            .itemId("new")
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .comments("B")
            .build();

    when(menuItemReviewRepository.findById(eq(1L))).thenReturn(java.util.Optional.of(existing));

    String body = new ObjectMapper().writeValueAsString(updated);

    mockMvc
        .perform(
            put("/api/MenuItemReview")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemId").value("new"))
        .andExpect(jsonPath("$.reviewerEmail").value("b@ucsb.edu"))
        .andExpect(jsonPath("$.stars").value(4))
        .andExpect(jsonPath("$.comments").value("B"));

    org.mockito.Mockito.verify(menuItemReviewRepository).save(existing);
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_cannot_edit_review_that_does_not_exist() throws Exception {
    MenuItemReview updated =
        MenuItemReview.builder()
            .itemId("new")
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .comments("B")
            .build();

    String body = new ObjectMapper().writeValueAsString(updated);

    when(menuItemReviewRepository.findById(eq(999L))).thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(
            put("/api/MenuItemReview")
                .param("id", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("MenuItemReview")))
        .andExpect(content().string(containsString("999")));
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/MenuItemReview").param("id", "1").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/MenuItemReview").param("id", "1").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_delete() throws Exception {
    MenuItemReview r =
        MenuItemReview.builder()
            .id(1)
            .itemId("burger")
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .comments("A")
            .build();

    when(menuItemReviewRepository.findById(eq(1L))).thenReturn(java.util.Optional.of(r));

    mockMvc
        .perform(delete("/api/MenuItemReview").param("id", "1").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("MenuItemReview with id 1 deleted"));

    org.mockito.Mockito.verify(menuItemReviewRepository).delete(r);
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void delete_not_found() throws Exception {
    when(menuItemReviewRepository.findById(eq(999L))).thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(delete("/api/MenuItemReview").param("id", "999").with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("MenuItemReview")))
        .andExpect(content().string(containsString("999")));
  }
}
