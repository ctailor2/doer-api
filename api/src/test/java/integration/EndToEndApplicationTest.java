package integration;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

        assertThat(listOverviews).contains(new ListOverview(list.getListId(), list.getProfileName()));
    }
}
