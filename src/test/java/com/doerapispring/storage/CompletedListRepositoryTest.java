package com.doerapispring.storage;

import com.doerapispring.domain.CompletedList;
import com.doerapispring.domain.CompletedTodo;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompletedListRepositoryTest {
    private ObjectRepository<CompletedList, String> completedListRepository;

    @Mock
    private UserDAO mockUserDAO;

    @Mock
    private TodoDao mockTodoDAO;

    @Before
    public void setUp() throws Exception {
        completedListRepository = new CompletedListRepository(mockTodoDAO);
    }

    @Test
    public void find_callsTodoDao() throws Exception {
        completedListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockTodoDAO).findFinishedByUserEmail("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsCompletedListWithNoTodos() throws Exception {
        when(mockTodoDAO.findFinishedByUserEmail(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("userIdentifier");
        Optional<CompletedList> completedListOptional = completedListRepository.find(uniqueIdentifier);

        assertThat(completedListOptional.isPresent()).isTrue();
        CompletedList completedList = completedListOptional.get();
        assertThat(completedList.getIdentifier()).isEqualTo(uniqueIdentifier);
        assertThat(completedList.getTodos()).isEmpty();
    }

    @Test
    public void find_whenThereAreTodos_returnsCompletedListWithTodos() throws Exception {
        String userEmail = "thisUserEmail";
        Date completedAt = new Date();
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it now")
                .updatedAt(completedAt)
                .build();
        when(mockTodoDAO.findFinishedByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<CompletedList> completedListOptional = completedListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(completedListOptional.isPresent()).isTrue();
        CompletedList completedList = completedListOptional.get();
        assertThat(completedList.getTodos().size()).isEqualTo(1);
        assertThat(completedList.getTodos()).contains(new CompletedTodo("do it now", completedAt));
    }
}