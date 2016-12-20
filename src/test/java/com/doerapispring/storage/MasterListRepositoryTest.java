package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private DomainRepository<MasterList, String> masterListRepository;

    @Mock
    private TodoDao todoDao;

    @Before
    public void setUp() throws Exception {
        masterListRepository = new MasterListRepository(todoDao);
    }

    @Test
    public void find_callsTodoDao() throws Exception {
        masterListRepository.find(new UserIdentifier("somethingSecret"));

        verify(todoDao).findByUserEmail("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsEmptyOptional() throws Exception {
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.emptyList());

        Optional<MasterList> masterListOptional = masterListRepository.find(new UserIdentifier("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isFalse();
    }

    @Test
    public void find_whenThereAreImmediateTodos_returnsMasterListWithTodosInCorrespondingLists() throws Exception {
        String userEmail = "thisUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it now")
                .active(true)
                .build();
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UserIdentifier("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getImmediateList()).isNotNull();
        assertThat(masterList.getPostponedList()).isNotNull();
        assertThat(masterList.getImmediateList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getImmediateList().getTodos().get(0))
                .isEqualTo(new Todo(new UserIdentifier(userEmail), "do it now", ScheduledFor.now));
    }

    @Test
    public void find_whenThereArePostponedTodos_returnsMasterListWithTodosInCorrespondingLists() throws Exception {
        String userEmail = "thatUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it later")
                .active(false)
                .build();
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UserIdentifier("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getImmediateList()).isNotNull();
        assertThat(masterList.getPostponedList()).isNotNull();
        assertThat(masterList.getPostponedList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getPostponedList().getTodos().get(0))
                .isEqualTo(new Todo(new UserIdentifier(userEmail), "do it later", ScheduledFor.later));
    }
}