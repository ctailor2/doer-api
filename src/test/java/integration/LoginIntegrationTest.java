package integration;

import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 8/28/16.
 */
public class LoginIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private MvcResult mvcResult;
    private final String content =
            "{\n" +
                    "  \"email\": \"test@email.com\",\n" +
                    "  \"password\": \"password\"\n" +
                    "}";

    @Autowired
    private UserSessionsService userSessionsService;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/login")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void login_whenUserWithEmailRegistered_correctPassword_respondsWithUserEntity_withLoginFields_respondsWithSessionTokenEntity_withSessionTokenFields() throws Exception {
        UserEntity registeredUserEntity = UserEntity.builder()
                .email("test@email.com")
                .password("password")
                .build();
        userSessionsService.signup(registeredUserEntity);

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserEntity userEntity = mapper.readValue(contentAsString, UserEntity.class);

        assertThat(userEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(userEntity.getSessionTokenEntity().getToken()).isNotEmpty();
    }
}
