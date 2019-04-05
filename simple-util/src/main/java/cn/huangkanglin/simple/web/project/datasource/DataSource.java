package cn.huangkanglin.simple.web.project.datasource;

import cn.huangkanglin.simple.web.project.util.ReadFileUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @ClassName DataSource
 * @Description TODO
 * @Author hkl
 * @Date 2019/4/5 11:14
 **/
public class DataSource {
    /**
     * 最大连接源
     */
    private static int MAX_ACTIVE = 50;
    /**
     * 数据源
     */
    private static DruidDataSource dataSource;
    /**
     * 数据库连接池
     */
    private static DruidPooledConnection pool;
    /**
     * Statement 对象
     */
    private static PreparedStatement preparedStatement;

    // 初始化数据源
    static {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = ReadFileUtil.getInputSteam("classpath:db.properties");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        String url = null;
        String driverClassName = null;
        String username = null;
        String password = null;
        try {
            prop.load(in);
            url = prop.getProperty("url");
            driverClassName = prop.getProperty("driverClassName");
            username = prop.getProperty("username");
            password = prop.getProperty("password");
            // 配置初始化属性
            dataSource = new DruidDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setUrl(url);
            dataSource.setMaxActive(MAX_ACTIVE);
            pool = dataSource.getConnection();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("不能读取属性文件. " + "请确保db.properties在CLASSPATH指定的路径中");
        }
    }

    /**
     * 获取连接池里面的数据源
     *
     * @return
     * @author hkl
     */
    public static Connection getConnection() {
        return pool.getConnection();
    }

    /**
     * 返回PreparedStatement
     * @param sql 传入的sql
     * @return
     */
    public static PreparedStatement getPreparedStatement(String sql) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * 关闭资源
     *
     * @param conn
     * @author hkl
     */
    public static void closeConnection(Connection conn) {
        // 空对象直接返回
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
