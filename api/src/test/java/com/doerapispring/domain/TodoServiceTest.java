package com.doerapispring.domain;

import com.doerapispring.storage.IdentityGeneratingObjectRepository;
import com.doerapispring.web.InvalidRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private IdentityGeneratingObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Mock
    private IdentityGeneratingObjectRepository<MasterList, String> mockMasterListRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private UniqueIdentifier<String> uniqueIdentifier;
    private MasterList masterList;

    private CompletedList completedList;
    private String todoIdentifier;
    private String completedTodoIdentifier;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(
            mockCompletedListRepository,
            mockMasterListRepository
        );
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
        todoIdentifier = "todoId";
        completedList = mock(CompletedList.class);
        when(mockCompletedListRepository.find(uniqueIdentifier)).thenReturn(Optional.of(completedList));
        when(mockMasterListRepository.nextIdentifier()).thenReturn(new UniqueIdentifier<>(todoIdentifier));
        completedTodoIdentifier = "completedTodoId";
        when(mockCompletedListRepository.nextIdentifier()).thenReturn(new UniqueIdentifier<>(completedTodoIdentifier));
    }

    @Test
    public void create_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(new User(uniqueIdentifier), task);

        verify(masterList).add(new TodoId(todoIdentifier), task);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void create_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(masterList).add(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.create(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void create_whenMasterListFound_whenListFull_refusesCreate() throws Exception {
        doThrow(new ListSizeExceededException()).when(masterList).add(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(new User(uniqueIdentifier), task);

        verify(masterList).addDeferred(new TodoId(todoIdentifier), task);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void createDeferred_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(masterList).addDeferred(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.createDeferred(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenMasterListNotFound_refusesCreate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.delete(new User(uniqueIdentifier), todoId);

        verify(masterList).delete(todoId);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(new User(uniqueIdentifier), new TodoId("someTodoId"));
    }

    @Test
    public void delete_whenMasterListFound_whenTodoNotFound_refusesDelete() throws Exception {
        doThrow(new TodoNotFoundException()).when(masterList).delete(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(new User(uniqueIdentifier), new TodoId("someTodoId"));
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_savesUsingRepository() throws Exception {
        todoService.displace(new User(uniqueIdentifier), "someTask");

        verify(masterList).displace(new TodoId(todoIdentifier), "someTask");
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void displace_whenMasterListNotFound_refusesDisplace() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(uniqueIdentifier), "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        doThrow(new DuplicateTodoException()).when(masterList).displace(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.displace(new User(uniqueIdentifier), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenListIsNotFull_refusesDisplace() throws Exception {
        doThrow(new ListNotFullException()).when(masterList).displace(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(uniqueIdentifier), "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(uniqueIdentifier), "someTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        String updatedTask = "someOtherTask";
        TodoId todoId = new TodoId("someIdentifier");
        todoService.update(new User(uniqueIdentifier), todoId, updatedTask);

        verify(masterList).update(todoId, updatedTask);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(uniqueIdentifier), new TodoId("someIdentifier"), "someOtherTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        doThrow(new DuplicateTodoException()).when(masterList).update(any(), any());

        assertThatThrownBy(() ->
            todoService.update(new User(uniqueIdentifier), new TodoId("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(masterList).update(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(uniqueIdentifier), new TodoId("someId"), "someTask");
    }

    @Test
    public void update_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(uniqueIdentifier), new TodoId("someId"), "someTask");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_completesUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.complete(new User(uniqueIdentifier), todoId);

        verify(masterList).complete(todoId);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void complete_addsCompletedTaskToCompletedList_savesUsingRepository() throws Exception {
        String completedTask = "completedTask";
        when(masterList.complete(any(TodoId.class))).thenReturn(completedTask);

        todoService.complete(new User(uniqueIdentifier), new TodoId("someIdentifier"));

        verify(mockCompletedListRepository).find(uniqueIdentifier);
        verify(completedList).add(new CompletedTodoId(completedTodoIdentifier), completedTask);
        verify(mockCompletedListRepository).save(completedList);
    }

    @Test
    public void complete_whenCompletedListRepositoryRejectsModel_refusesOperation() throws Exception {
        String completedTask = "completedTask";
        when(masterList.complete(any(TodoId.class))).thenReturn(completedTask);
        doThrow(new AbnormalModelException()).when(mockCompletedListRepository).save(any(CompletedList.class));

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(uniqueIdentifier), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(uniqueIdentifier), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(masterList).complete(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(uniqueIdentifier), new TodoId("someId"));
    }

    @Test
    public void complete_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(uniqueIdentifier), new TodoId("someId"));
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        TodoId sourceIdentifier = new TodoId("sourceIdentifier");
        TodoId destinationIdentifier = new TodoId("destinationIdentifier");
        todoService.move(new User(uniqueIdentifier), sourceIdentifier, destinationIdentifier);

        verify(masterList).move(sourceIdentifier, destinationIdentifier);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(uniqueIdentifier), new TodoId("sourceIdentifier"), new TodoId("destinationIdentifier"));
    }

    @Test
    public void move_whenMasterListFound_whenTodosNotFound_refusesOperation() throws Exception {
        doThrow(new TodoNotFoundException()).when(masterList).move(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(uniqueIdentifier), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void move_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(uniqueIdentifier), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        todoService.pull(new User(uniqueIdentifier));

        verify(masterList).pull();
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.pull(new User(uniqueIdentifier));
    }
}