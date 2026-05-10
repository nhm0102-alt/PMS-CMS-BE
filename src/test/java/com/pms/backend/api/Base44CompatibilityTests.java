package com.pms.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Base44CompatibilityTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void entitiesFilterByIdWorks() throws Exception {
        String created = mockMvc.perform(post("/entities/Property")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("name", "Hotel A"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(created);
        String id = json.get("id").asText();

        mockMvc.perform(get("/entities/Property")
                        .param("q", "{\"id\":\"" + id + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void inviteUserEndpointExists() throws Exception {
        mockMvc.perform(post("/api/users/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", "test@example.com",
                                "role", "user"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invited").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void uploadFileEndpointExists() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                "image/png",
                "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        String body = mockMvc.perform(multipart("/integrations/Core/UploadFile")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_url").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String fileUrl = objectMapper.readTree(body).get("file_url").asText();
        mockMvc.perform(get(fileUrl))
                .andExpect(status().isOk())
                .andExpect(content().bytes("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void invoicesAliasWorks() throws Exception {
        String created = mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "property_id", "p1",
                                "invoice_number", "INV001",
                                "status", "draft",
                                "total_amount", 100
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.balance_due").value(100))
                .andExpect(jsonPath("$.paid_amount").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/invoices")
                        .param("property_id", "p1")
                        .param("sort_by", "-created_date")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void reservationBalanceAndNightsComputed() throws Exception {
        String created = mockMvc.perform(post("/entities/Reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "property_id", "p1",
                                "reservation_number", "RES001",
                                "guest_name", "Guest A",
                                "check_in_date", "2026-05-01",
                                "check_out_date", "2026-05-03",
                                "status", "confirmed",
                                "total_amount", 100,
                                "deposit_amount", 20
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nights").value(2))
                .andExpect(jsonPath("$.balance_due").value(80))
                .andExpect(jsonPath("$.paid_amount").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(put("/entities/Reservation/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "paid_amount", 50
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance_due").value(30));
    }

    @Test
    void reservationsRestEndpointWorks() throws Exception {
        String created = mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "property_id", "p2",
                                "reservation_number", "RESREST001",
                                "guest_name", "Guest B",
                                "check_in_date", "2026-05-10",
                                "check_out_date", "2026-05-12",
                                "status", "confirmed",
                                "total_amount", 200
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/reservations")
                        .param("property_id", "p2")
                        .param("sort_by", "-check_in_date")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void propertiesRestEndpointWorks() throws Exception {
        String created = mockMvc.perform(post("/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "name", "Hotel Rest",
                                "status", "active"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/properties")
                        .param("id", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
    }
}
