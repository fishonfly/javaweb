package org.smart4j.chapter2.helper;





import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.chapter2.util.PropsUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DatabaseHelper {
    private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    private static final QueryRunner QUERY_RUNNER;
    private static final ThreadLocal<Connection> CONNECTION_HOLDER;
    private static final BasicDataSource DATA_SOURCE;

    static {
        Properties conf=PropsUtil.loadProps("config.properties");

        CONNECTION_HOLDER=new ThreadLocal<Connection>();
        QUERY_RUNNER=new QueryRunner();

        DRIVER=conf.getProperty("jdbc.driver");
        URL=conf.getProperty("jdbc.url");
        USERNAME=conf.getProperty("jdbc.username");
        PASSWORD=conf.getProperty("jdbc.password");

        DATA_SOURCE=new BasicDataSource();
        DATA_SOURCE.setDriverClassName(DRIVER);
        DATA_SOURCE.setUrl(URL);
        DATA_SOURCE.setUsername(USERNAME);
        DATA_SOURCE.setPassword(PASSWORD);


        try{
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            LOGGER.error("can't load jdbc driver",e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection(){
        Connection conn=CONNECTION_HOLDER.get();
        if(conn==null) {
            try {
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get sql connection failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);//<2>
            }
        }
        return conn;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection(){
        Connection conn=CONNECTION_HOLDER.get(); //<1>
        if(conn != null){
            try{
                conn.close();
            }catch (SQLException e){
                LOGGER.error("clese database failure",e);
                throw new RuntimeException();
            }finally {
                CONNECTION_HOLDER.remove(); //<3>
            }
        }
    }

    /**
     * 查询实体列表
     */

    public static<T>List<T>queryEntityList(Class<T>entityClass, String sql, Object...params){
        List<T> entityList;
        Connection conn=getConnection();
        try{
            entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<T>(entityClass),params);
        }catch (SQLException e){
            LOGGER.error("query entity list failure",e);
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return entityList;

    }

    /**
     *查询实体
     */
    public static <T> T queryEntity(Class<T>entityClass, String sql, Object...params){
        T entity;
        try{
            Connection conn=getConnection();
            entity=QUERY_RUNNER.query(conn, sql, new BeanHandler<T>(entityClass),params);
        } catch (SQLException e){
            LOGGER.error("query entity failure",e);
            throw new RuntimeException();
        }finally {
            closeConnection();
        }
        return entity;
    }

    /**
     * 执行查询语句
     */
    public static List<Map<String,Object>> executeQuery(String sql, Object...params){
        List<Map<String,Object>> result;
        try{
            Connection conn=getConnection();
            result=QUERY_RUNNER.query(conn,sql,new MapListHandler(),params);
        } catch (SQLException e){
            LOGGER.error("execute query failureee");
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 执行更新语句
     */
    public static int executeUpdate(String sql,Object...params){
        int rows=0;
        try{
            Connection conn=getConnection();
            rows=QUERY_RUNNER.update(conn,sql,params);
        }catch (SQLException e){
            LOGGER.error("execute update failure");
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return rows;
    }

    /**
     * 执行 SQL 文件
     */
    public static void executeSqlFile(String filePath) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String sql;
            while ((sql = reader.readLine()) != null) {
                executeUpdate(sql);
            }
        } catch (Exception e) {
            LOGGER.error("execute sql file failure", e);
            throw new RuntimeException(e);
        }
    }






}
