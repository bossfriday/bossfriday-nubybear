package cn.bossfriday.common.zk;

/**
 * ZkNodeChangeListener
 *
 * @author chenx
 */
public interface ZkNodeChangeListener {
    
    /**
     * changed
     *
     * @param bytes
     */
    void changed(byte[] bytes);
}
