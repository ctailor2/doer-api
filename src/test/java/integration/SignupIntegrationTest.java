package integration;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenRepository;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import com.doerapispring.users.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 8/9/16.
 */

public class SignupIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionTokenRepository sessionTokenRepository;

    @Autowired
    private UserService userService;

    private String content =
            "{\n" +
                    "  \"identifier\": \"test@email.com\",\n" +
                    "  \"credentials\": \"password\"\n" +
                    "}";

    private MvcResult mvcResult;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/signup")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void signup_whenUserWithEmailDoesNotExist_createsUser_createsSessionToken_respondsWithBoth() throws Exception {
        doPost();

        UserEntity userEntity = userRepository.findByEmail("test@email.com");

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        SessionToken sessionToken = mapper.readValue(contentAsString, SessionToken.class);

        assertThat(userEntity).isNotNull();
        assertThat(sessionTokenRepository.findActiveByUserEmail(userEntity.email)).isNotNull();
        assertThat(sessionToken.getToken()).isNotEmpty();
        assertThat(sessionToken.getExpiresAt()).isInTheFuture();
    }
}
