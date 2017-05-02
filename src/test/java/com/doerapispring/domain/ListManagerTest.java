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
    public void recordView_whenThereAreNoListUnlocks_returnsAListUnlock() throws Exception {
        ListManager listManager = new ListManager(new UniqueIdentifier<>("someIdentifier"), Collections.emptyList());

        ListUnlock listUnlock = listManager.unlock();
        assertThat(listUnlock).isNotNull();
    }

    @Test
    public void recordView_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_throwsLockTimerNotExpiredException() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListUnlock> listUnlocks = Collections.singletonList(new ListUnlock());
        ListManager listManager = new ListManager(uniqueIdentifier, listUnlocks);

        exception.expect(LockTimerNotExpiredException.class);
        listManager.unlock();
    }

    @Test
    public void recordView_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsAListUnlock() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        List<ListUnlock> listUnlocks = Collections.singletonList(new ListUnlock(new Date(0L)));
        ListManager listManager = new ListManager(uniqueIdentifier, listUnlocks);

        ListUnlock listUnlock = listManager.unlock();
        assertThat(listUnlock).isNotNull();
    }
}