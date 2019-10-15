package integration;

import com.doerapispring.domain.ListService;
import com.doerapispring.domain.TodoList;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        TodoList list = listService.getDefault(user);

        assertThat(list.getName()).isEqualTo("default");
    }

    @Test
    public void includesTheDefaultList() {
        TodoList list = listService.getDefault(user);

        List<TodoList> todoLists = listService.getAll(user);

        assertThat(todoLists).contains(list);
    }

    @Test
    public void createsAList() {
        String listName = "someName";
        listService.create(user, listName);

        List<TodoList> todoLists = listService.getAll(user);

        List<String> listNames = todoLists.stream().map(TodoList::getName).collect(Collectors.toList());
        assertThat(listNames).contains(listName);
    }
}
