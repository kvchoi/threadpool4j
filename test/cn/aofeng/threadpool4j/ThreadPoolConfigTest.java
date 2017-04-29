package cn.aofeng.threadpool4j;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ThreadPoolConfig}的单元测试用例。
 * 
 * @author <a href="mailto:aofengblog@163.com">聂勇</a>
 */
public class ThreadPoolConfigTest {

    private ThreadPoolConfig _threadPoolConfig = new ThreadPoolConfig();
    
    @Before
    public void setUp() throws Exception {
        _threadPoolConfig._configFile = "/biz/threadpool4j.xml";
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * 测试用例：读取线程池配置文件 <br/>
     * 前置条件：
     * <pre>
     * 1.0.0版本的配置文件，没有线程状态输出开关（threadstate节点）
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 1、有两个线程池的配置信息，分别是default和other。
     * 2、线程状态输出开关的值为false。
     * </pre>
     */
    @Test
    public void testInit41_0_0() {
        _threadPoolConfig._configFile = "/cn/aofeng/threadpool4j/threadpool4j_1.0.0.xml";
        _threadPoolConfig.init();
        assertEquals(2, _threadPoolConfig._multiThreadPoolInfo.size());
        
        // default线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("default"));
        ThreadPoolInfo defaultInfo = _threadPoolConfig._multiThreadPoolInfo.get("default");
        assertEquals(30, defaultInfo.getCoreSize());
        assertEquals(150, defaultInfo.getMaxSize());
        assertEquals(5, defaultInfo.getThreadKeepAliveTime());
        assertEquals(100000, defaultInfo.getQueueSize());
        
        // other线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("other"));
        ThreadPoolInfo otherInfo = _threadPoolConfig._multiThreadPoolInfo.get("other");
        assertEquals(10, otherInfo.getCoreSize());
        assertEquals(100, otherInfo.getMaxSize());
        assertEquals(10, otherInfo.getThreadKeepAliveTime());
        assertEquals(10000, otherInfo.getQueueSize());
        
        // 线程池状态收集配置信息
        assertFalse(_threadPoolConfig._threadPoolStateSwitch);
        assertEquals(60, _threadPoolConfig._threadPoolStateInterval);
        
        // 线程状态收集配置信息
        assertFalse(_threadPoolConfig._threadStateSwitch);
        assertEquals(60, _threadPoolConfig._threadStateInterval);
        
        // 线程堆栈收集配置信息
        assertFalse(_threadPoolConfig._threadStackSwitch);
        assertEquals(60, _threadPoolConfig._threadStackInterval);
    }
    
    /**
     * 测试用例：读取线程池配置文件 <br/>
     * 前置条件：
     * <pre>
     * 1、1.5.0版本的配置文件
     * 2、有线程池状态输出开关（threadpoolstate节点），配置为on
     * 3、有线程状态输出开关（threadstate节点），配置为on
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 1、有两个线程池的配置信息，分别是default和other。
     * 2、线程池状态输出开关的值为true，输出间隔为120秒。
     * 3、线程状态输出开关的值为true，输出间隔为180秒。
     * </pre>
     */
    @Test
    public void testInit41_5_0() {
        _threadPoolConfig._configFile = "/cn/aofeng/threadpool4j/threadpool4j_1.5.0.xml";
        _threadPoolConfig.init();
        assertEquals(2, _threadPoolConfig._multiThreadPoolInfo.size());
        
        // default线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("default"));
        ThreadPoolInfo defaultInfo = _threadPoolConfig._multiThreadPoolInfo.get("default");
        assertEquals(30, defaultInfo.getCoreSize());
        assertEquals(150, defaultInfo.getMaxSize());
        assertEquals(5, defaultInfo.getThreadKeepAliveTime());
        assertEquals(100000, defaultInfo.getQueueSize());
        
        // other线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("other"));
        ThreadPoolInfo otherInfo = _threadPoolConfig._multiThreadPoolInfo.get("other");
        assertEquals(10, otherInfo.getCoreSize());
        assertEquals(100, otherInfo.getMaxSize());
        assertEquals(10, otherInfo.getThreadKeepAliveTime());
        assertEquals(10000, otherInfo.getQueueSize());
        
        // 线程池状态统计配置信息
        assertTrue(_threadPoolConfig._threadPoolStateSwitch);
        assertEquals(120, _threadPoolConfig._threadPoolStateInterval);
        
        // 线程状态统计配置信息
        assertTrue(_threadPoolConfig._threadStateSwitch);
        assertEquals(180, _threadPoolConfig._threadStateInterval);
        
        // 线程堆栈收集配置信息
        assertFalse(_threadPoolConfig._threadStackSwitch);
        assertEquals(60, _threadPoolConfig._threadStackInterval);
    }
    
    /**
     * 测试用例：读取线程池配置文件 <br/>
     * 前置条件：
     * <pre>
     * 1、1.5.0版本的配置文件
     * 2、有线程池状态输出开关（threadpoolstate节点），配置为on
     * 3、有线程状态输出开关（threadstate节点），配置为off
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 1、有两个线程池的配置信息，分别是default和hello。
     * 2、线程池状态输出开关的值为true，输出间隔为120秒。
     * 3、线程状态输出开关的值为false，输出间隔为100秒。
     * </pre>
     */
    @Test
    public void testInit41_5_0CloseThreadStateSwitch() {
        _threadPoolConfig._configFile = "/cn/aofeng/threadpool4j/threadpool4j_1.5.0_closethreadstate.xml";
        _threadPoolConfig.init();
        assertEquals(2, _threadPoolConfig._multiThreadPoolInfo.size());
        
        // default线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("default"));
        ThreadPoolInfo defaultInfo = _threadPoolConfig._multiThreadPoolInfo.get("default");
        assertEquals(30, defaultInfo.getCoreSize());
        assertEquals(150, defaultInfo.getMaxSize());
        assertEquals(5, defaultInfo.getThreadKeepAliveTime());
        assertEquals(100000, defaultInfo.getQueueSize());
        
        // hello线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("hello"));
        ThreadPoolInfo otherInfo = _threadPoolConfig._multiThreadPoolInfo.get("hello");
        assertEquals(10, otherInfo.getCoreSize());
        assertEquals(100, otherInfo.getMaxSize());
        assertEquals(10, otherInfo.getThreadKeepAliveTime());
        assertEquals(10000, otherInfo.getQueueSize());
        
        // 线程池状态统计配置信息
        assertFalse(_threadPoolConfig._threadPoolStateSwitch);
        assertEquals(120, _threadPoolConfig._threadPoolStateInterval);
        
        // 线程状态统计配置信息
        assertFalse(_threadPoolConfig._threadStateSwitch);
        assertEquals(100, _threadPoolConfig._threadStateInterval);
        
        // 线程堆栈收集配置信息
        assertFalse(_threadPoolConfig._threadStackSwitch);
        assertEquals(60, _threadPoolConfig._threadStackInterval);
    }
    
    /**
     * 测试用例：读取线程池配置文件 <br/>
     * 前置条件：
     * <pre>
     * 1、2.2.0版本的配置文件
     * 2、有线程池状态输出开关（threadpoolstate节点），配置为on
     * 3、有线程状态输出开关（threadstate节点），配置为on
     * </pre>
     * 
     * 测试结果：
     * <pre>
     * 1、有两个线程池的配置信息，分别是default和other。
     * 2、线程池状态输出开关的值为true，输出间隔为120秒。
     * 3、线程状态输出开关的值为true，输出间隔为180秒。
     * </pre>
     */
    @Test
    public void testInit42_5_0() {
        _threadPoolConfig._configFile = "/cn/aofeng/threadpool4j/threadpool4j_2.5.0.xml";
        _threadPoolConfig.init();
        assertEquals(2, _threadPoolConfig._multiThreadPoolInfo.size());
        
        // default线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("default"));
        ThreadPoolInfo defaultInfo = _threadPoolConfig._multiThreadPoolInfo.get("default");
        assertEquals(10, defaultInfo.getCoreSize());
        assertEquals(100, defaultInfo.getMaxSize());
        assertEquals(15, defaultInfo.getThreadKeepAliveTime());
        assertEquals(100000, defaultInfo.getQueueSize());
        
        // other线程池配置信息
        assertTrue(_threadPoolConfig._multiThreadPoolInfo.containsKey("other"));
        ThreadPoolInfo otherInfo = _threadPoolConfig._multiThreadPoolInfo.get("other");
        assertEquals(20, otherInfo.getCoreSize());
        assertEquals(200, otherInfo.getMaxSize());
        assertEquals(25, otherInfo.getThreadKeepAliveTime());
        assertEquals(200000, otherInfo.getQueueSize());
        
        // 线程池状态统计配置信息
        assertTrue(_threadPoolConfig._threadPoolStateSwitch);
        assertEquals(160, _threadPoolConfig._threadPoolStateInterval);
        
        // 线程状态统计配置信息
        assertFalse(_threadPoolConfig._threadStateSwitch);
        assertEquals(260, _threadPoolConfig._threadStateInterval);
        
        // 线程堆栈收集配置信息
        assertFalse(_threadPoolConfig._threadStackSwitch);
        assertEquals(360, _threadPoolConfig._threadStackInterval);
    }

    @Test
    public void testContainsPool() {
        _threadPoolConfig._configFile = "/cn/aofeng/threadpool4j/threadpool4j_2.1.0_no_default_pool.xml";
        _threadPoolConfig.init();
        
        // 配置不为空，但指定的线程池不存在
        assertFalse( _threadPoolConfig.containsPool("NOT_EXISTS_1") );
        
        // 配置不为空，指定的线程池存在
        assertTrue( _threadPoolConfig.containsPool("hello") );
        
        // 配置为空，任何线程池都不存在
        _threadPoolConfig._multiThreadPoolInfo = new HashMap<String, ThreadPoolInfo>();
        assertEquals(0, _threadPoolConfig._multiThreadPoolInfo.size());
        assertFalse(_threadPoolConfig.containsPool("hello"));
    }
    
}
