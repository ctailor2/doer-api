package integration;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class SignupIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    private ObjectRepository<User, String> userRepository;

    @Autowired
    private CredentialsStore userCredentialsRepository;

    private String content =
            "{\n" +
                    "  \"email\": \"test@email.com\",\n" +
                    "  \"password\": \"password\"\n" +
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

        String userIdentifier = "test@email.com";
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(userIdentifier);
        Optional<User> storedUserOptional = userRepository.find(uniqueIdentifier);
        Optional<Credentials> storedCredentialsOptional = userCredentialsRepository.findLatest(userIdentifier);

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(storedUserOptional.isPresent(), is(true));
        assertThat(storedCredentialsOptional.isPresent(), is(true));
        Credentials credentials = storedCredentialsOptional.get();
        assertThat(credentials.getUserIdentifier(), equalTo(userIdentifier));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.token", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.expiresAt", not(isEmptyString())));
    }
}
