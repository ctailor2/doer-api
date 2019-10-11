package integration;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EndToEndApplicationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    private UserService userService;

    @Autowired
    private ListService listService;

    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        user = userService.create("user");
    }

    @Test
    public void hasTheDefaultListSet() {
        ReadOnlyTodoList list = listService.getDefault(user);

        assertThat(list.getProfileName()).isEqualTo("default");
    }

    @Test
    public void includesTheDefaultList() {
        ReadOnlyTodoList list = listService.getDefault(user);

        List<ListOverview> listOverviews = listService.getOverviews(user);

        assertThat(listOverviews).contains(new ListOverview(list.getUserId(), list.getListId(), list.getProfileName(), 0, Date.from(Instant.EPOCH)));
    }

    @Test
    public void createsAList() {
        String listName = "someName";
        listService.create(user, listName);

        List<ListOverview> listOverviews = listService.getOverviews(user);

        List<String> listNames = listOverviews.stream().map(ListOverview::getName).collect(Collectors.toList());
        assertThat(listNames).contains(listName);
    }
}
