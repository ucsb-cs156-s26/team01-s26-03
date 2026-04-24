package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockBean RecommendationRequestRepository recommendationRequestRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest/all"))
        .andExpect(status().is(200)); // logged in users can get all
  }

  // Authorization tests for /post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/RecommendationRequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/RecommendationRequest/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // Tests with mocks for /all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

    // arrange
    LocalDateTime dr1 = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn1 = LocalDateTime.parse("2026-05-20T00:00:00");

    RecommendationRequest req1 =
        RecommendationRequest.builder()
            .requesterEmail("user1@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("Grad School")
            .dateRequested(dr1)
            .dateNeeded(dn1)
            .done(false)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(req1));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests with mocks for /post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
    // arrange
    LocalDateTime dr1 = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn1 = LocalDateTime.parse("2026-05-20T00:00:00");

    RecommendationRequest req1 =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("pconrad@ucsb.edu")
            .explanation("Masters Program")
            .dateRequested(dr1)
            .dateNeeded(dn1)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(req1))).thenReturn(req1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("professorEmail", "pconrad@ucsb.edu")
                    .param("explanation", "Masters Program")
                    .param("dateRequested", "2026-04-20T00:00:00")
                    .param("dateNeeded", "2026-05-20T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(req1);
    String expectedJson = mapper.writeValueAsString(req1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
