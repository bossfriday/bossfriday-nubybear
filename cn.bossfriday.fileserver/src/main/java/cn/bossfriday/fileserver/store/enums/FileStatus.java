package cn.bossfriday.fileserver.store.enums;

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

    public static void main(String[] args) {
        System.out.println(FileStatus.Deleted.getValue());
    }
}
