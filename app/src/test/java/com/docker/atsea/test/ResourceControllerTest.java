package com.docker.atsea.test;

import com.docker.atsea.controller.ResourceController;
import com.docker.atsea.configuration.WebConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceControllerTest {
    private MockMvc mvc;

    @Before
    public void setUp() {
        TestContentNegotiationConfigurer configurer = new TestContentNegotiationConfigurer();
        new WebConfiguration().configureContentNegotiation(configurer);
        mvc = MockMvcBuilders.standaloneSetup(new ResourceController())
                .setContentNegotiationManager(configurer.buildContentNegotiationManager()).build();
    }

    private static class TestContentNegotiationConfigurer extends ContentNegotiationConfigurer {
        TestContentNegotiationConfigurer() {
            super(new MockServletContext());
        }

        @Override
        public ContentNegotiationManager buildContentNegotiationManager() {
            return super.buildContentNegotiationManager();
        }
    }

    @Test
    public void servesPublicResourceWithoutCredentials() throws Exception {
        mvc.perform(get("/api/resource").param("name", "resource.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("AtSea dojo public resource.\n"));
    }

    @Test
    public void servesResourceWithCurlAcceptHeader() throws Exception {
        mvc.perform(get("/api/resource").param("name", "resource.txt").accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("AtSea dojo public resource.\n"));
    }

    @Test
    public void escapedSlashesExposePrivateClasspathResource() throws Exception {
        mvc.perform(get("/api/resource")
                .param("name", "..::..::resource-controller::private.properties"))
                .andExpect(status().isOk())
                .andExpect(content().string("dojo.secret=escaped-slash-traversal\n"));
    }

    @Test
    public void rejectsLiteralTraversalBeforeDecoding() throws Exception {
        for (String name : new String[] {"../../application.properties", "..\\..\\application.properties"}) {
            mvc.perform(get("/api/resource").param("name", name))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    public void missingResourceReturnsNotFound() throws Exception {
        mvc.perform(get("/api/resource").param("name", "missing.txt"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void directoryReturnsNotFound() throws Exception {
        mvc.perform(get("/api/resource").param("name", "..::"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void missingOrEmptyNameReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/resource")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/resource").param("name", ""))
                .andExpect(status().isBadRequest());
    }
}
