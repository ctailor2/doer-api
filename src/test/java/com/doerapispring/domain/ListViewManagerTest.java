package com.doerapispring.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ListViewManagerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void recordView_whenThereAreNoListViews_returnsAListView() throws Exception {
        ListViewManager listViewManager = new ListViewManager(new UniqueIdentifier<>("someIdentifier"), Collections.emptyList());

        ListView listView = listViewManager.recordView();
        assertThat(listView).isNotNull();
    }

    @Test
    public void recordView_whenThereAreListViews_whenFirstListViewWasCreatedToday_throwsLockTimerNotExpiredException() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListView> listViews = Collections.singletonList(new ListView());
        ListViewManager listViewManager = new ListViewManager(uniqueIdentifier, listViews);

        exception.expect(LockTimerNotExpiredException.class);
        listViewManager.recordView();
    }

    @Test
    public void recordView_whenThereAreListViews_whenFirstListViewWasCreatedBeforeToday_returnsAListView() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListView> listViews = Collections.singletonList(new ListView(new Date(0L)));
        ListViewManager listViewManager = new ListViewManager(uniqueIdentifier, listViews);

        ListView listView = listViewManager.recordView();
        assertThat(listView).isNotNull();
    }
}