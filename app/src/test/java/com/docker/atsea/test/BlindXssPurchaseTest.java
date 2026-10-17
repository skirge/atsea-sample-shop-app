package com.docker.atsea.test;

import java.util.Base64;
import javax.persistence.EntityManagerFactory;
import javax.servlet.Filter;
import javax.sql.DataSource;

import com.docker.atsea.controller.AdminPurchaseController;
import com.docker.atsea.configuration.WebConfiguration;
import com.docker.atsea.configuration.AdminAccountInitializer;
import com.docker.atsea.controller.PurchaseController;
import com.docker.atsea.model.Customer;
import com.docker.atsea.model.Purchase;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.repositories.PurchaseRepository;
import com.docker.atsea.security.JwtFilter;
import com.docker.atsea.security.SecurityConfiguration;
import com.docker.atsea.service.CustomerService;
import com.docker.atsea.service.CustomerServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = BlindXssPurchaseTest.Config.class)
public class BlindXssPurchaseTest {
    private static final String PAYLOAD = "<img src=x onerror=\"document.title='blind-xss'\">";
    @Autowired WebApplicationContext context;
    @Autowired Filter springSecurityFilterChain;
    @Autowired PurchaseRepository purchases;
    @Autowired CustomerRepository customers;
    @Autowired AdminAccountInitializer adminAccountInitializer;
    private MockMvc mvc;
    private Customer buyer;

    @Before
    public void setUp() {
        purchases.deleteAll();
        customers.deleteAll();
        buyer = customers.save(customer("buyer", "USER"));
        adminAccountInitializer.run();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .addFilter(new JwtFilter(), "/purchase/*").build();
    }

    private Customer customer(String username, String role) {
        return new Customer(null, username, "Dojo street", username + "@example.test",
                "123456", username, "dojo-password", true, role);
    }

    private String basic(String username) {
        String password = "admin".equals(username) ? "admin123!" : "dojo-password";
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private String token() {
        return "Bearer " + Jwts.builder().setSubject("buyer").claim("aud", "admin")
                .signWith(SignatureAlgorithm.HS256, "c2VjcmV0").compact();
    }

    @Test
    public void startupCreatesEnabledAdminWithoutDuplicatingAccounts() {
        customers.deleteAll();
        adminAccountInitializer.run();
        Customer admin = customers.findByUserName("admin");
        assertEquals("admin123!", admin.getPassword());
        assertEquals("ADMIN", admin.getRole());
        assertTrue(admin.getEnabled());
        adminAccountInitializer.run();
        assertEquals(1, customers.count());
        assertEquals(admin.getCustomerId(), customers.findByUserName("admin").getCustomerId());
    }

    @Test
    public void startupPreservesExistingAdminAccount() {
        Customer admin = customers.findByUserName("admin");
        admin.setPassword("changed-password");
        admin.setEnabled(false);
        customers.save(admin);
        adminAccountInitializer.run();
        Customer existing = customers.findByUserName("admin");
        assertEquals("changed-password", existing.getPassword());
        assertFalse(existing.getEnabled());
        assertEquals(2, customers.count());
    }

    @Test
    public void customerInputIsStoredAndRenderedAsHtmlForAdmin() throws Exception {
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                new java.util.LinkedHashMap<String, Object>() {{
                    put("firstName", PAYLOAD);
                    put("lastName", "Customer");
                    put("company", "Dojo");
                    put("title", "Buyer");
                    put("address", "Training street");
                    put("city", "Test city");
                    put("customerId", -100);
                    put("purchaseId", 999);
                    put("cardNumber", "not-stored");
                    put("cvv", "not-stored");
                    put("productsOrdered", java.util.Collections.singletonMap("1", 2));
                }});
        mvc.perform(post("/purchase/").header("Authorization", token())
                .contentType("application/json").accept("application/json").content(json))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").exists())
                .andExpect(content().string(not(containsString("<img"))));
        Purchase saved = purchases.findAll().get(0);
        assertEquals(PAYLOAD, saved.getFirstName());
        assertEquals(buyer.getCustomerId(), saved.getCustomerId());
        assertNotEquals(Long.valueOf(999), saved.getPurchaseId());
        assertNotNull(saved.getPurchasedAt());
        assertEquals(Integer.valueOf(2), saved.getProductsOrdered().get(1));
        mvc.perform(get("/admin/purchases").accept("text/html").header("Authorization", basic("admin")))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString(PAYLOAD)))
                .andExpect(content().string(containsString("Training street")));
    }

    @Test
    public void purchaseListRequiresDatabaseAdminAuthority() throws Exception {
        for (String path : new String[] {"/admin/purchases", "/admin/purchases/", "/ADMIN/PURCHASES"}) {
            mvc.perform(get(path).accept("text/html")).andExpect(status().isUnauthorized());
            mvc.perform(get(path).accept("text/html").header("Authorization", basic("buyer")))
                    .andExpect(status().isForbidden());
            mvc.perform(get(path).accept("text/html").header("Authorization", token()))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/admin/purchases").accept("text/html").header("Authorization", basic("admin")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("No purchases yet.")));
    }

    @Test
    public void customerPurchaseListReturnsOnlyOwnPurchasesNewestFirst() throws Exception {
        Customer other = customers.save(customer("other-buyer", "USER"));
        Purchase first = new Purchase();
        first.setCustomerId(buyer.getCustomerId());
        first.setCompany(PAYLOAD);
        purchases.save(first);
        Purchase second = new Purchase();
        second.setCustomerId(buyer.getCustomerId());
        purchases.save(second);
        Purchase privatePurchase = new Purchase();
        privatePurchase.setCustomerId(other.getCustomerId());
        privatePurchase.setCompany("Other customer's private company");
        purchases.save(privatePurchase);

        mvc.perform(get("/purchase/").header("Authorization", token()).accept("application/json")
                .param("customerId", other.getCustomerId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].purchaseId").value(second.getPurchaseId().intValue()))
                .andExpect(jsonPath("$[1].purchaseId").value(first.getPurchaseId().intValue()))
                .andExpect(jsonPath("$[1].company").value(PAYLOAD))
                .andExpect(content().string(not(containsString("Other customer's private company"))));
    }

    @Test
    public void customerWithoutPurchasesReceivesEmptyList() throws Exception {
        mvc.perform(get("/purchase/").header("Authorization", token()).accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void customerPurchaseListRequiresVerifiedCustomerClaims() {
        PurchaseController controller = context.getBean(PurchaseController.class);
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED,
                controller.listPurchases(new org.springframework.mock.web.MockHttpServletRequest()).getStatusCode());
    }

    @Test
    public void invalidCartDoesNotCreatePurchase() throws Exception {
        for (String cart : new String[] {"{}", "null", "{\"1\":0}", "{\"1\":-1}", "{\"1\":null}"}) {
            mvc.perform(post("/purchase/").header("Authorization", token())
                    .contentType("application/json").content("{\"productsOrdered\":" + cart + "}"))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0, purchases.count());
    }

    @Test
    public void deletedOrDisabledCustomerCannotPurchase() throws Exception {
        buyer.setEnabled(false);
        customers.save(buyer);
        mvc.perform(get("/purchase/").header("Authorization", token()).accept("application/json"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/purchase/").header("Authorization", token())
                .contentType("application/json").content("{\"productsOrdered\":{\"1\":1}}"))
                .andExpect(status().isUnauthorized());
        customers.delete(buyer);
        mvc.perform(get("/purchase/").header("Authorization", token()).accept("application/json"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/purchase/").header("Authorization", token())
                .contentType("application/json").content("{\"productsOrdered\":{\"1\":1}}"))
                .andExpect(status().isUnauthorized());
        assertEquals(0, purchases.count());
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories("com.docker.atsea.repositories")
    @Import({SecurityConfiguration.class, WebConfiguration.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.docker.atsea.model");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(java.util.Collections.singletonMap("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean JpaTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean CustomerService customers() { return new CustomerServiceImpl(); }
        @Bean AdminAccountInitializer adminAccountInitializer(CustomerService customers) {
            return new AdminAccountInitializer(customers);
        }
        @Bean PurchaseController purchaseController(PurchaseRepository purchases, CustomerService customers) {
            return new PurchaseController(purchases, customers);
        }
        @Bean AdminPurchaseController adminPurchaseController(PurchaseRepository purchases) {
            return new AdminPurchaseController(purchases);
        }
        @Bean ThymeleafViewResolver viewResolver(WebApplicationContext context) {
            SpringResourceTemplateResolver templates = new SpringResourceTemplateResolver();
            templates.setApplicationContext(context);
            templates.setPrefix("classpath:/templates/");
            templates.setSuffix(".html");
            templates.setTemplateMode("HTML");
            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(templates);
            ThymeleafViewResolver views = new ThymeleafViewResolver();
            views.setTemplateEngine(engine);
            views.setContentType("text/html;charset=UTF-8");
            return views;
        }
    }
}
