package cn.bossfriday.fileserver.engine.enums;

public enum FileStatus {

    Normal(0),
    Deleted(1);

    private final int value;

    FileStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
