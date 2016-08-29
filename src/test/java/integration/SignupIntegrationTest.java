package integration;

import com.doerapispring.apiTokens.SessionTokenRepository;
import com.doerapispring.users.User;
import com.doerapispring.users.UserRepository;
import com.doerapispring.userSessions.UserSessionResponseWrapper;
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

    private String content =
            "{\n" +
                    "  \"user\": {\n" +
                    "    \"email\": \"test@email.com\",\n" +
                    "    \"password\": \"password\",\n" +
                    "    \"passwordConfirmation\": \"password\"\n" +
                    "  }\n" +
                    "}";

    private MvcResult mvcResult;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/signup")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void signup_whenUserWithEmailDoesNotExist_createsUser_withSignupFields() throws Exception {
        doPost();

        User user = userRepository.findByEmail("test@email.com");
        assertThat(user).isNotNull();
    }

    @Test
    public void signup_whenUserWithEmailDoesNotExist_createsSessionToken_forUser() throws Exception {
        doPost();

        User user = userRepository.findByEmail("test@email.com");
        assertThat(sessionTokenRepository.findByUserId(user.id).size()).isEqualTo(1);
    }

    @Test
    public void signup_whenUserWithEmailDoesNotExist_respondsWithUserEntity_withSignupFields() throws Exception {
        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserSessionResponseWrapper userSessionResponseWrapper = mapper.readValue(contentAsString, UserSessionResponseWrapper.class);

        assertThat(userSessionResponseWrapper.getUser().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    public void signup_whenUserWithEmailDoesNotExist_respondsWithSessionTokenEntity_withSessionTokenFields() throws Exception {
        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserSessionResponseWrapper userSessionResponseWrapper = mapper.readValue(contentAsString, UserSessionResponseWrapper.class);

        assertThat(userSessionResponseWrapper.getSessionToken().getToken()).isNotEmpty();
    }
}
