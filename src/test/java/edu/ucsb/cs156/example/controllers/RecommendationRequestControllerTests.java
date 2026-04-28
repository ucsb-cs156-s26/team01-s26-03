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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(200));
  }

  // Authorization tests for /post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/RecommendationRequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/RecommendationRequest/post")).andExpect(status().is(403));
  }

  // Tests for GET /all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

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

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for POST /post

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
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

    verify(recommendationRequestRepository, times(1)).save(req1);
    String expectedJson = mapper.writeValueAsString(req1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /?id=...

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_getById_returns_error_when_id_does_not_exist() throws Exception {

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_getById_returns_request_when_id_exists() throws Exception {

    LocalDateTime dr = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn = LocalDateTime.parse("2026-05-20T00:00:00");

    RecommendationRequest req =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad School")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(req));

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(req);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for PUT /?id=...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
    LocalDateTime dr1 = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn1 = LocalDateTime.parse("2026-05-20T00:00:00");
    LocalDateTime dr2 = LocalDateTime.parse("2026-04-21T00:00:00");
    LocalDateTime dn2 = LocalDateTime.parse("2026-05-21T00:00:00");

    RecommendationRequest reqOrig =
        RecommendationRequest.builder()
            .requesterEmail("orig@ucsb.edu")
            .professorEmail("proforig@ucsb.edu")
            .explanation("Orig Explanation")
            .dateRequested(dr1)
            .dateNeeded(dn1)
            .done(false)
            .build();

    RecommendationRequest reqEdited =
        RecommendationRequest.builder()
            .requesterEmail("new@ucsb.edu")
            .professorEmail("profnew@ucsb.edu")
            .explanation("New Explanation")
            .dateRequested(dr2)
            .dateNeeded(dn2)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(reqEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(reqOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1)).save(reqEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    LocalDateTime dr1 = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn1 = LocalDateTime.parse("2026-05-20T00:00:00");

    RecommendationRequest reqEdited =
        RecommendationRequest.builder()
            .requesterEmail("new@ucsb.edu")
            .professorEmail("profnew@ucsb.edu")
            .explanation("New Explanation")
            .dateRequested(dr1)
            .dateNeeded(dn1)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(reqEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  // Tests for DELETE

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_request() throws Exception {
    LocalDateTime dr = LocalDateTime.parse("2026-04-20T00:00:00");
    LocalDateTime dn = LocalDateTime.parse("2026-05-20T00:00:00");

    RecommendationRequest req =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad School")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(req));

    MvcResult response =
        mockMvc
            .perform(delete("/api/RecommendationRequest?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(req);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existent_request_and_gets_right_error_message()
      throws Exception {
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/RecommendationRequest?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }
}
