package cn.bossfriday.common.zk;

public interface ZkChildrenChangeListener {
    /**
     * added
     *
     * @param path
     * @param data
     */
    void added(String path, byte[] data);

    /**
     * updated
     *
     * @param path
     * @param data
     */
    void updated(String path, byte[] data);

    /**
     * removed
     *
     * @param path
     * @param data
     */
    void removed(String path, byte[] data);

    /**
     * connectInitialized
     */
    void connectInitialized();

    /**
     * reconnected
     */
    void reconnected();

    /**
     * suspended
     */
    void suspended();

    /**
     * connectLost
     */
    void connectLost();
}
