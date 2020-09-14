package integration;

import com.doerapispring.web.Error;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthenticatedEndpointsIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Test
    public void hitEndpoint_withNoSessionTokenHeader_returnsError() throws Exception {
        String responseContent = mockMvc.perform(post("/v1/logout")
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Error error = mapper.readValue(responseContent, new TypeReference<Error>() {
        });
        assertThat(error.getStatus()).isEqualTo("401");
        assertThat(error.getMessage()).isEqualTo("Authentication required");
    }
}
