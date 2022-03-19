package cn.bossfriday.jmeter.rpc;

public class FooClient {
    private static volatile FooClient instance;

    private FooClient() {

    }

    /**
     * getInstance
     */
    public static FooClient getInstance() {
        if (instance == null) {
            synchronized (FooClient.class) {
                if (instance == null) {
                    instance = new FooClient();
                }
            }
        }

        return instance;
    }
}
