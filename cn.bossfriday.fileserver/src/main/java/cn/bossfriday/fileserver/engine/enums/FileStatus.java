package cn.bossfriday.fileserver.engine.enums;

public enum FileStatus {

    Normal((byte) 0),
    Deleted((byte) 1);

    private final byte value;

    FileStatus(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
