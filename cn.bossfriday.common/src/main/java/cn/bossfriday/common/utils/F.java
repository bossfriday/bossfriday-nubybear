package cn.bossfriday.common.utils;

public class F {
	
	public static interface Action0 {
        public void invoke();
    }

    public static interface Action1<T> {
    	public void invoke(T arg);
    }
    
    public static interface Action2<T1,T2> {
    	public void invoke(T1 arg1, T2 arg2);
    }
    
    public static interface Action3<T1,T2,T3> {
    	public void invoke(T1 arg1, T2 arg2, T3 arg3);
    }
    
    public static interface Action4<T1,T2,T3, T4> {
    	public void invoke(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
    }
    
    public static interface Function0<R> {
    	public R invoke();
    }

    public static interface Function1<T,R> {
    	public R invoke(T arg);
    }
    
    public static interface Promise<V> extends Action1<V>{
    	
    	public void invokeWithThrowable(Throwable e);
    }
}
