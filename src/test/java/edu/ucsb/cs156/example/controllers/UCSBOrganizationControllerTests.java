package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "ATHLETICS"))
        .andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ATHLETICS")
                .param("orgTranslationShort", "Athletics")
                .param("orgTranslation", "Intercollegiate Athletics")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ATHLETICS")
                .param("orgTranslationShort", "Athletics")
                .param("orgTranslation", "Intercollegiate Athletics")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    UCSBOrganization incoming =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "ATHLETICS")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    UCSBOrganization incoming =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "ATHLETICS")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "ATHLETICS").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "ATHLETICS").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_organizations() throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    UCSBOrganization registrar =
        UCSBOrganization.builder()
            .orgCode("REGISTRAR")
            .orgTranslationShort("Registrar")
            .orgTranslation("Office of the Registrar")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(athletics, registrar));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS")).thenReturn(Optional.of(athletics));

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("id", "ATHLETICS"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    String expectedJson = mapper.writeValueAsString(athletics);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_orgCode_when_the_orgCode_exists()
      throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS")).thenReturn(Optional.of(athletics));

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("orgCode", "ATHLETICS"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    String expectedJson = mapper.writeValueAsString(athletics);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_gets_404_when_the_id_does_not_exist() throws Exception {
    when(ucsbOrganizationRepository.findById("NOTREAL")).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("id", "NOTREAL"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("NOTREAL");
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id NOTREAL not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_organization() throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS")).thenReturn(Optional.of(athletics));

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("id", "ATHLETICS").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    verify(ucsbOrganizationRepository, times(1)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("record ATHLETICS deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_organization_by_orgCode() throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS")).thenReturn(Optional.of(athletics));

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("orgCode", "ATHLETICS").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    verify(ucsbOrganizationRepository, times(1)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("record ATHLETICS deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    when(ucsbOrganizationRepository.findById("NOTREAL")).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("id", "NOTREAL").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("NOTREAL");
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id NOTREAL not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_an_existing_organization() throws Exception {
    UCSBOrganization existingOrganization =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    UCSBOrganization incoming =
        UCSBOrganization.builder()
            .orgCode("SHOULD_NOT_CHANGE")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    UCSBOrganization expectedOrganization =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS"))
        .thenReturn(Optional.of(existingOrganization));
    when(ucsbOrganizationRepository.save(expectedOrganization)).thenReturn(expectedOrganization);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("id", "ATHLETICS")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incoming))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    verify(ucsbOrganizationRepository, times(1)).save(expectedOrganization);
    String expectedJson = mapper.writeValueAsString(expectedOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_an_existing_organization_by_orgCode() throws Exception {
    UCSBOrganization existingOrganization =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    UCSBOrganization incoming =
        UCSBOrganization.builder()
            .orgCode("SHOULD_NOT_CHANGE")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    UCSBOrganization expectedOrganization =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics Updated")
            .orgTranslation("Updated Athletics")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById("ATHLETICS"))
        .thenReturn(Optional.of(existingOrganization));
    when(ucsbOrganizationRepository.save(expectedOrganization)).thenReturn(expectedOrganization);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("orgCode", "ATHLETICS")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incoming))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ATHLETICS");
    verify(ucsbOrganizationRepository, times(1)).save(expectedOrganization);
    String expectedJson = mapper.writeValueAsString(expectedOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_update_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    UCSBOrganization incoming =
        UCSBOrganization.builder()
            .orgCode("NOTREAL")
            .orgTranslationShort("Not Real")
            .orgTranslation("Not Real Organization")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById("NOTREAL")).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("id", "NOTREAL")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incoming))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("NOTREAL");
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id NOTREAL not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    UCSBOrganization athletics =
        UCSBOrganization.builder()
            .orgCode("ATHLETICS")
            .orgTranslationShort("Athletics")
            .orgTranslation("Intercollegiate Athletics")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(athletics)).thenReturn(athletics);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "ATHLETICS")
                    .param("orgTranslationShort", "Athletics")
                    .param("orgTranslation", "Intercollegiate Athletics")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).save(athletics);
    String expectedJson = mapper.writeValueAsString(athletics);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
