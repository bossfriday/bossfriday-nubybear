package cn.bossfriday.common.zk;

public interface ZkNodeChangeListener {
    /**
     * changed
     *
     * @param bytes
     */
    void changed(byte[] bytes);
}
