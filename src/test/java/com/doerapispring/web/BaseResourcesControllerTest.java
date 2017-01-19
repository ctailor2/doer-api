package com.doerapispring.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class BaseResourcesControllerTest {
    private MockMvc mockMvc;
    private BaseResourcesController baseResourcesController;

    @Before
    public void setUp() throws Exception {
        baseResourcesController = new BaseResourcesController(new MockHateoasLinkGenerator());
        mockMvc = MockMvcBuilders
                .standaloneSetup(baseResourcesController)
                .build();
    }

    @Test
    public void baseResources_mapping() throws Exception {
        mockMvc.perform(get("/v1/baseResources"))
                .andExpect(status().isOk());
    }

    @Test
    public void baseResources_includesLinks() throws Exception {
        ResponseEntity<BaseResourcesResponse> responseEntity = baseResourcesController.baseResources();

        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/baseResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/login").withRel("login"),
                new Link(MOCK_BASE_URL + "/signup").withRel("signup"));
    }
}