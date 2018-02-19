package com.doerapispring.web;

public class TodoResourcesDTO {
    private final boolean nowListFull;
    private final boolean laterListLocked;

    public TodoResourcesDTO(boolean nowListFull, boolean laterListLocked) {
        this.nowListFull = nowListFull;
        this.laterListLocked = laterListLocked;
    }

    public boolean doesNowListHaveCapacity() {
        return !isNowListFull();
    }

    public boolean isLaterListUnlocked() {
        return !isLaterListLocked();
    }

    private boolean isNowListFull() {
        return nowListFull;
    }

    private boolean isLaterListLocked() {
        return laterListLocked;
    }
}
