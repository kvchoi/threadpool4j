package cn.aofeng.threadpool4j;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * {@link ThreadPoolImpl}的单元测试用例。
 * 
 * @author <a href="mailto:aofengblog@163.com">聂勇</a>
 */
public class ThreadPoolTest {

    private ThreadPoolImpl _threadPool = new ThreadPoolImpl();
    
    @Rule
    public ExpectedException _expectedEx = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        _threadPool._status = ThreadPoolStatus.UNINITIALIZED;
        _threadPool._threadPoolConfig._configFile = ThreadPoolConfig.DEFAULT_CONFIG_FILE;
        _threadPool.init();
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testInit() {
        assertEquals(2, _threadPool._multiThreadPool.size());
        assertTrue(_threadPool._multiThreadPool.containsKey("default"));
        assertTrue(_threadPool._multiThreadPool.containsKey("other"));
        assertTrue(_threadPool._threadPoolConfig._threadStateSwitch);
        assertEquals(60, _threadPool._threadPoolConfig._threadStateInterval);
    }
    
    @Test
    public void testDestroy() {
        // 先销毁加载默认配置的线程池
        _threadPool.destroy();
        assertEquals(ThreadPoolStatus.DESTROYED, _threadPool._status);
        assertNull(_threadPool._threadPoolStateJob);
        assertNull(_threadPool._threadStateJob);
        for (Entry<String, ExecutorService> entry : _threadPool._multiThreadPool.entrySet()) {
            assertTrue(entry.getValue().isShutdown());
        }
        
        // 加载指定配置文件的线程池，初始化后再销毁
        String configFile = "/cn/aofeng/threadpool4j/threadpool4j_1.5.0_closethreadstate.xml";
        _threadPool._threadPoolConfig._configFile = configFile;
        _threadPool.init();
        _threadPool.destroy();
        assertEquals(ThreadPoolStatus.DESTROYED, _threadPool._status);
        assertNull(_threadPool._threadPoolStateJob);
        assertNull(_threadPool._threadStateJob);
        for (Entry<String, ExecutorService> entry : _threadPool._multiThreadPool.entrySet()) {
            assertTrue(entry.getValue().isShutdown());
        }
    }
    
    /**
     * 测试用例：没有默认的线程池'default' <br/>
     * 前置条件：
     * <pre>
     * 1、2.1.0版本的配置文件
     * 2、没有名为default的线程池。
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出IllegalStateException异常
     * </pre>
     */
    @Test
    public void testInit42_1_0_NoDefaultPool() {
        String configFile = "/cn/aofeng/threadpool4j/threadpool4j_2.1.0_no_default_pool.xml";
        
        _expectedEx.expect(IllegalStateException.class);
        _expectedEx.expectMessage( String.format("the default thread pool not exists, please check the config file '%s'", configFile) );
        
        _threadPool.destroy();
        _threadPool._threadPoolConfig._configFile = configFile;
        _threadPool._status = ThreadPoolStatus.UNINITIALIZED;
        _threadPool.init();
    }
    
    /**
     * 测试用例：提交一个异步任务给默认的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitRunnable4TaskIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task is null");
        
        Runnable task = null;
        _threadPool.submit(task);
    }
    
    /**
     * 测试用例：提交一个异步任务给默认的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 线程池default的submit方法被调用1次
     * </pre>
     */
    @Test
    public void testSubmitRunnable() {
        callThreadPool("default");
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为null；线程池为default
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitRunnableString4TaskIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task is null");
        
        Runnable task = null;
        _threadPool.submit(task, "default");
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}；线程池名为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitRunnableString4ThreadpoolNameIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool name is empty");
        
        Runnable task = createRunnable();
        _threadPool.submit(task, null);
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}；线程池名为"ThreadpoolNotExists"，但实际不存在这个线程池
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitRunnableString4ThreadpoolNameNotExists() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool ThreadpoolNotExists not exists");
        
        Runnable task = createRunnable();
        _threadPool.submit(task, "ThreadpoolNotExists");
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 为{@link Runnable}；线程池名为"other"且实际存在
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 线程池other的submit方法被调用1次
     * </pre>
     */
    @Test
    public void testSubmitRunnableString() {
        callThreadPool("other");
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}，提交给线程池名"default"执行
     * </pre>
     * 
     * 测试结果：
     * <pre>
     *  任务对象的run方法被调用1次
     * </pre>
     */
    @Test
    public void testSubmitRunnableString4RunTask() throws InterruptedException {
        Runnable mock = Mockito.mock(Runnable.class);
        Mockito.doNothing().when(mock).run();
        _threadPool.submit(mock, "default");
        Thread.sleep(1000); // 异步操作，需等待一会儿
        
        Mockito.verify(mock, Mockito.times(1)).run();
    }
    
    /**
     * 测试用例：队列满，执行失败处理器 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}，提交给线程池名"default"执行。队列满，抛出{@link RejectedExecutionException}
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 任务对象的run方法被调用1次；失败处理器被执行1次。
     * </pre>
     * @throws InterruptedException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitRunnableStringFailHandler() throws InterruptedException {
        String threadpoolName = "defalut";
        Runnable taskMock = Mockito.mock(Runnable.class);
        FailHandler<Runnable> handlerMock = Mockito.mock(FailHandler.class);
        
        ThreadPoolImpl threadPoolMockImpl = Mockito.spy(_threadPool);
        Mockito.when(threadPoolMockImpl.getThreadPool(threadpoolName)).thenReturn(Executors.newSingleThreadExecutor());
        Mockito.when(threadPoolMockImpl.submit(taskMock, threadpoolName)).thenThrow(RejectedExecutionException.class);
        
        threadPoolMockImpl.submit(taskMock, threadpoolName, handlerMock);
        Thread.sleep(1000); // 异步操作，需等待一会儿
        
        Mockito.verify(taskMock, Mockito.times(1)).run(); // 期望任务的run方法被调用1次
        Mockito.verify(handlerMock, Mockito.times(1)).execute(taskMock); // 期望失败处理器的execute方法被调用1次
    }
    
    private void callThreadPool(String threadpoolName) {
        ExecutorService mock = Mockito.mock(ExecutorService.class);
        Mockito.when(mock.submit(Mockito.any(Runnable.class))).thenReturn(null);
        _threadPool._multiThreadPool.put(threadpoolName, mock);
        _threadPool.submit(createRunnable(), threadpoolName);
        
        Mockito.verify(mock, Mockito.times(1)).submit(Mockito.any(Runnable.class));
    }

    private Runnable createRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // nothing
            }
        };
    }

    /**
     * 测试用例：提交一个异步任务给默认的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitCallable4TaskIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task is null");
        
        Callable<?> task = null;
        _threadPool.submit(task);
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为null；线程池为default
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitCallableString4TaskIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task is null");
        
        Callable<?> task = null;
        _threadPool.submit(task, "default");
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Callable}；线程池名为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitCallableString4ThreadpoolNameIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool name is empty");
        
        Callable<?> task = createCallable();
        _threadPool.submit(task, null);
    }
    
    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Callable}；线程池名为"ThreadpoolNotExists"，但实际不存在这个线程池
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testSubmitCallableString4ThreadpoolNameNotExists() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool ThreadpoolNotExists not exists");
        
        Callable<?> task = createCallable();
        _threadPool.submit(task, "ThreadpoolNotExists");
    }

    /**
     * 测试用例：提交一个异步任务给指定的线程池执行 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Runnable}，提交给线程池名"default"执行
     * </pre>
     * 
     * 测试结果：
     * <pre>
     *  任务对象的run方法被调用1次
     * </pre>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitCallableString4RunTask() throws Exception {
        Callable<List<String>> mock = Mockito.mock(Callable.class);
        Mockito.when(mock.call()).thenReturn(new ArrayList<String>());
        _threadPool.submit(mock, "default");
        Thread.sleep(1000); // 异步操作，需等待一会儿
        
        Mockito.verify(mock, Mockito.times(1)).call();
    }
    
    /**
     * 测试用例：队列满，执行失败处理器 <br/>
     * 前置条件：
     * <pre>
     * 任务对象为{@link Callable}，提交给线程池名"default"执行。队列满，抛出{@link RejectedExecutionException}
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 任务对象的run方法被调用1次；失败处理器被执行1次。
     * </pre>
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitCallableStringFailHandler() throws Exception {
        String threadpoolName = "defalut";
        Callable<Object> taskMock = Mockito.mock(Callable.class);
        FailHandler<Callable<Object>> handlerMock = Mockito.mock(FailHandler.class);
        
        ThreadPoolImpl threadPoolMockImpl = Mockito.spy(_threadPool);
        Mockito.when(threadPoolMockImpl.getThreadPool(threadpoolName)).thenReturn(Executors.newSingleThreadExecutor());
        Mockito.when(threadPoolMockImpl.submit(taskMock, threadpoolName)).thenThrow(RejectedExecutionException.class);
        
        threadPoolMockImpl.submit(taskMock, threadpoolName, handlerMock);
        Thread.sleep(1000); // 异步操作，需等待一会儿
        
        Mockito.verify(taskMock, Mockito.times(1)).call(); // 期望任务的run方法被调用1次
        Mockito.verify(handlerMock, Mockito.times(1)).execute(taskMock); // 期望失败处理器的execute方法被调用1次
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 任务列表为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4TaskListIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task list is null or empty");
        
        Collection<Callable<Integer>> tasks = null;
        _threadPool.invokeAll(tasks, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 任务列表为空列表（容量为0）
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4TaskListIsEmpty() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("task list is null or empty");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        _threadPool.invokeAll(tasks, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 超时时间等于0
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4TimeoutEqualsZero() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("timeout less than or equals zero");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        _threadPool.invokeAll(tasks, 0, TimeUnit.SECONDS);
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 超时时间小于0
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4TimeoutLessThanZero() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("timeout less than or equals zero");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        _threadPool.invokeAll(tasks, -1, TimeUnit.SECONDS);
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 线程池名称为null
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4ThreadpoolNameIsNull() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool name is empty");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        _threadPool.invokeAll(tasks, 2, TimeUnit.SECONDS, null);
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 线程池名称为"   "
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4ThreadpoolNameIsEmpty() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool name is empty");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        _threadPool.invokeAll(tasks, 2, TimeUnit.SECONDS, "   ");
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 线程池名称为"   "
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 抛出{@link IllegalArgumentException}异常
     * </pre>
     */
    @Test
    public void testInvokeAll4ThreadpoolNotExists() {
        _expectedEx.expect(IllegalArgumentException.class);
        _expectedEx.expectMessage("thread pool ThreadPoolNotExists not exists");
        
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        _threadPool.invokeAll(tasks, 2, TimeUnit.SECONDS, "ThreadPoolNotExists");
    }
    
    /**
     * 测试用例：在线程池"default"中执行多个需要返回值的异步任务，并设置超时时间 <br/>
     * 前置条件：
     * <pre>
     * 1、所有参数均符合要求。
     * 2、线程池存在。
     * 3、执行两个异步任务。
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 两个异步任务均返回正确的执行结果
     * </pre>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testInvokeAll() throws InterruptedException, ExecutionException {
        Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
        tasks.add(createCallable());
        tasks.add(createCallable());
        List<Future<Integer>> futures = _threadPool.invokeAll(tasks, 2, TimeUnit.SECONDS);
        int result = 0;
        for (Future<Integer> future : futures) {
            result += future.get();
        }
        
        assertEquals(18, result);
    }
    
    /**
     * 测试用例：查询指定名称的线程池是否存在 <br/>
     * 前置条件：
     * <pre>
     * 线程池"ThreadPoolNotExists"不存在
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 返回false
     * </pre>
     */
    @Test
    public void testIsExists4NotExists() {
        assertFalse(_threadPool.isExists("ThreadPoolNotExists"));
    }
    
    /**
     * 测试用例：查询指定名称的线程池是否存在 <br/>
     * 前置条件：
     * <pre>
     * 线程池"default"存在
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 返回true
     * </pre>
     */
    @Test
    public void testIsExists4Exists() {
        assertTrue(_threadPool.isExists("default"));
    }
    
    private Callable<Integer> createCallable() {
        return new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return 9;
            }
            
        };
    }

}
