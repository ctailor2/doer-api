package com.doerapispring.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ListManagerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void recordView_whenThereAreNoListViews_returnsAListView() throws Exception {
        ListManager listViewManager = new ListManager(new UniqueIdentifier<>("someIdentifier"), Collections.emptyList());

        ListUnlock listUnlock = listViewManager.unlock();
        assertThat(listUnlock).isNotNull();
    }

    @Test
    public void recordView_whenThereAreListViews_whenFirstListViewWasCreatedToday_throwsLockTimerNotExpiredException() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListUnlock> listUnlocks = Collections.singletonList(new ListUnlock());
        ListManager listViewManager = new ListManager(uniqueIdentifier, listUnlocks);

        exception.expect(LockTimerNotExpiredException.class);
        listViewManager.unlock();
    }

    @Test
    public void recordView_whenThereAreListViews_whenFirstListViewWasCreatedBeforeToday_returnsAListView() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListUnlock> listUnlocks = Collections.singletonList(new ListUnlock(new Date(0L)));
        ListManager listViewManager = new ListManager(uniqueIdentifier, listUnlocks);

        ListUnlock listUnlock = listViewManager.unlock();
        assertThat(listUnlock).isNotNull();
    }
}