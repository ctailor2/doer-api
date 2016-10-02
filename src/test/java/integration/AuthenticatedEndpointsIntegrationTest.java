package integration;

import com.doerapispring.ErrorEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 9/30/16.
 */
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
        ErrorEntity errorEntity = mapper.readValue(response.getContentAsString(), new TypeReference<ErrorEntity>() {
        });
        assertThat(errorEntity.getStatus()).isEqualTo("401");
        assertThat(errorEntity.getMessage()).isEqualTo("Authentication required");
    }
}
