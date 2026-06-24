package com.lubj9.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lubj9.urlshortener.dto.CreateUrlRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste end-to-end: cria URL → segue o redirect → consulta estatísticas → deleta.
 *
 * Roda com o contexto Spring completo + H2 em memória. Garante que controllers,
 * services, cache, repositórios e validação estão integrados corretamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fluxo_completo_criar_redirect_stats() throws Exception {
        // 1. Cria URL curta
        CreateUrlRequest req = new CreateUrlRequest("https://www.mackenzie.br", null);
        MvcResult criacao = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl", is("https://www.mackenzie.br")))
                .andReturn();

        String resp = criacao.getResponse().getContentAsString();
        String code = objectMapper.readTree(resp).get("code").asText();

        // 2. Segue o redirect e valida o Location
        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.mackenzie.br"));

        // 3. Consulta estatísticas (tracking é assíncrono — pode ainda não ter chegado no banco)
        mockMvc.perform(get("/api/urls/" + code + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(code)))
                .andExpect(jsonPath("$.originalUrl", is("https://www.mackenzie.br")))
                .andExpect(jsonPath("$.expired", is(false)));

        // 4. Deleta
        mockMvc.perform(delete("/api/urls/" + code))
                .andExpect(status().isNoContent());

        // 5. Redirect agora retorna 404 (porque foi removida do cache via @CacheEvict)
        mockMvc.perform(get("/" + code))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_url_javascript_retorna_400() throws Exception {
        CreateUrlRequest req = new CreateUrlRequest("javascript:alert(1)", null);
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("suportado")));
    }

    @Test
    void redirect_codigo_inexistente_retorna_404() throws Exception {
        mockMvc.perform(get("/abc123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_url_invalida_sem_protocolo_retorna_400() throws Exception {
        CreateUrlRequest req = new CreateUrlRequest("apenas-texto", null);
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
