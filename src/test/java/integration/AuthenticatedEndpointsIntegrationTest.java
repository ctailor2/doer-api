package integration;

import com.doerapispring.Error;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthenticatedEndpointsIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/logout")
                .headers(httpHeaders))
                .andReturn();
    }

    @Test
    public void hitEndpoint_withNoSessionTokenHeader_returnsError() throws Exception {
        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        Error error = mapper.readValue(response.getContentAsString(), new TypeReference<Error>() {
        });
        assertThat(error.getStatus()).isEqualTo("401");
        assertThat(error.getMessage()).isEqualTo("Authentication required");
    }
}
