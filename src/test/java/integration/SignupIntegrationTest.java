package integration;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.SessionTokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
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

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        SessionTokenDTO sessionToken = mapper.readValue(contentAsString, SessionTokenDTO.class);

        assertThat(storedUserOptional.isPresent()).isTrue();
        assertThat(storedCredentialsOptional.isPresent()).isTrue();
        Credentials credentials = storedCredentialsOptional.get();
        assertThat(credentials.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(sessionToken.getToken()).isNotEmpty();
        assertThat(sessionToken.getExpiresAt()).isInTheFuture();
    }
}
