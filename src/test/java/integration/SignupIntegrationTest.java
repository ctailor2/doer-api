package integration;

import com.doerapispring.UserCredentials;
import com.doerapispring.UserCredentialsRepository;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.users.User;
import com.doerapispring.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 8/9/16.
 */

public class SignupIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

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
    public void signup_whenUserWithEmailDoesNotExist_createsUser_storesCredentials_createsSessionToken_respondsWithSessionToken() throws Exception {
        doPost();

        UserIdentifier userIdentifier = new UserIdentifier("test@email.com");
        Optional<User> storedUserOptional = userRepository.find(userIdentifier);
        Optional<UserCredentials> storedUserCredentialsOptional = userCredentialsRepository.find(userIdentifier);

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        SessionToken sessionToken = mapper.readValue(contentAsString, SessionToken.class);

        assertThat(storedUserOptional.isPresent()).isTrue();
        User user = storedUserOptional.get();
        assertThat(storedUserCredentialsOptional.isPresent()).isTrue();
        UserCredentials userCredentials = storedUserCredentialsOptional.get();
        assertThat(userCredentials.getUserIdentifier()).isEqualTo(user.getIdentifier());
        assertThat(sessionToken.getToken()).isNotEmpty();
        assertThat(sessionToken.getExpiresAt()).isInTheFuture();
    }
}
