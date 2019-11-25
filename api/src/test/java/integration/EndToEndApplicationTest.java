package integration;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;
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
        TodoListReadModel list = listService.getDefault(user);

        assertThat(list.getProfileName()).isEqualTo("default");
    }

    @Test
    public void includesTheDefaultList() {
        TodoListReadModel list = listService.getDefault(user);

        List<ListId> todoListIds = listService.getAll(user).stream()
            .map(TodoList::getListId)
            .collect(toList());

        assertThat(todoListIds).contains(list.getListId());
    }

    @Test
    public void createsAList() {
        String listName = "someName";
        listService.create(user, listName);

        List<TodoList> todoLists = listService.getAll(user);

        List<String> listNames = todoLists.stream().map(TodoList::getName).collect(toList());
        assertThat(listNames).contains(listName);
    }
}
