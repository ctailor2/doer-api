package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ListView;
import com.doerapispring.domain.ListViewManager;
import com.doerapispring.domain.UniqueIdentifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListViewRepositoryTest {
    private ListViewRepository listViewRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    ListViewDao mockListViewDao;

    @Mock
    UserDAO mockUserDao;

    @Captor
    ArgumentCaptor<ListViewEntity> listViewEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        listViewRepository = new ListViewRepository(mockListViewDao, mockUserDao);
    }

    @Test
    public void find_callsListViewDao() throws Exception {
        listViewRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockListViewDao).findUserListView("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoListViews_returnsListViewManagerWithNoListViews() throws Exception {
        when(mockListViewDao.findUserListView(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<ListViewManager> listViewOptional = listViewRepository.find(uniqueIdentifier);

        assertThat(listViewOptional.isPresent()).isTrue();
        ListViewManager listView = listViewOptional.get();
        assertThat(listView).isEqualTo(new ListViewManager(uniqueIdentifier, Collections.emptyList()));
    }

    @Test
    public void find_whenThereAreListViews_returnsListViewManagerWithListViews() throws Exception {
        List<ListViewEntity> listViewEntities = Collections.singletonList(
                ListViewEntity.builder()
                        .updatedAt(new Date(0L))
                        .build());
        when(mockListViewDao.findUserListView(any())).thenReturn(listViewEntities);

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<ListViewManager> listViewOptional = listViewRepository.find(uniqueIdentifier);

        assertThat(listViewOptional.isPresent()).isTrue();
        ListViewManager listView = listViewOptional.get();
        List<ListView> listViews = Collections.singletonList(new ListView(new Date(0)));
        assertThat(listView).isEqualTo(new ListViewManager(uniqueIdentifier, listViews));
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDao.findByEmail(any())).thenReturn(userEntity);

        ListView listView = new ListView();
        ListViewManager listViewManager = new ListViewManager(new UniqueIdentifier("listUserIdentifier"), Collections.emptyList());
        listViewRepository.add(listViewManager, listView);

        verify(mockUserDao).findByEmail("listUserIdentifier");
        verify(mockListViewDao).save(listViewEntityArgumentCaptor.capture());
        ListViewEntity todoEntity = listViewEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(mockUserDao.findByEmail(any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        ListView listView = new ListView();
        ListViewManager listViewManager = new ListViewManager(new UniqueIdentifier("nonExistentUser"), Collections.emptyList());
        listViewRepository.add(listViewManager, listView);

        verify(mockUserDao).findByEmail("nonExistentUser");
    }
}