package org.enrique.urlshorteningservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShortUrlControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void fullFlow_create_get_stats_update_delete_redirect() throws Exception {
        // Create
        String createBody = "{\"url\":\"https://example.org/it/path\"}";
        MvcResult createRes = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.accessCount").doesNotExist())
                .andReturn();
        JsonNode created = objectMapper.readTree(createRes.getResponse().getContentAsString());
        String code = created.get("shortCode").asText();

        // Get (increments access count but response omits accessCount)
        mockMvc.perform(get("/shorten/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.accessCount").doesNotExist());

        // Stats (should be 1)
        MvcResult stats1 = mockMvc.perform(get("/shorten/" + code + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.accessCount").value(1))
                .andReturn();
        assertThat(objectMapper.readTree(stats1.getResponse().getContentAsString()).get("accessCount").asInt()).isEqualTo(1);

        // Redirect (302 and increment)
        mockMvc.perform(get("/r/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.org/it/path"));

        // Stats (should be 2)
        mockMvc.perform(get("/shorten/" + code + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCount").value(2));

        // Update URL
        String updateBody = "{\"url\":\"https://example.org/it/updated\"}";
        mockMvc.perform(put("/shorten/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.org/it/updated"))
                .andExpect(jsonPath("$.accessCount").doesNotExist());

        // Delete
        mockMvc.perform(delete("/shorten/" + code))
                .andExpect(status().isNoContent());

        // Get after delete -> 404
        mockMvc.perform(get("/shorten/" + code))
                .andExpect(status().isNotFound());
    }
}
