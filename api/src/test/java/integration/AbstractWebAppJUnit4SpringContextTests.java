package integration;

import com.doerapispring.DoerApiSpringApplication;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(classes = DoerApiSpringApplication.class)
@WebAppConfiguration
@ActiveProfiles(value = "test")
@Transactional
abstract class AbstractWebAppJUnit4SpringContextTests extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected JdbcTemplate jdbcTemplate; // For asserting against rows/cols I think?

    protected MockMvc mockMvc;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .defaultRequest(get("/")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .build();
    }
}
