package integration;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class SignupIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    @SuppressWarnings("unused")
    private ObjectRepository<User, UserId> userRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CredentialsStore userCredentialsRepository;

    @Test
    public void signup() throws Exception {
        String content = "{\n" +
            "  \"email\": \"test@email.com\",\n" +
            "  \"password\": \"password\"\n" +
            "}";
        mockMvc.perform(post("/v1/signup")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.session", not(isEmptyString())))
                .andExpect(jsonPath("$.session.token", not(isEmptyString())))
                .andExpect(jsonPath("$.session.expiresAt", not(isEmptyString())))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/signup")))
                .andExpect(jsonPath("$._links.root.href", containsString("/v1/resources/root")));

        String userIdentifier = "test@email.com";
        Optional<User> storedUserOptional = userRepository.find(new UserId(userIdentifier));
        Optional<Credentials> storedCredentialsOptional = userCredentialsRepository.findLatest(userIdentifier);

        assertThat(storedUserOptional.isPresent(), is(true));
        assertThat(storedCredentialsOptional.isPresent(), is(true));
        Credentials credentials = storedCredentialsOptional.get();
        assertThat(credentials.getUserIdentifier(), equalTo(userIdentifier));
    }
}
