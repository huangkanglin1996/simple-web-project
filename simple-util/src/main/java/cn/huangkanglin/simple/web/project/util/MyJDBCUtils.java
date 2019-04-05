package cn.huangkanglin.simple.web.project.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;


/**
 * Created by LIUCIBIN on 2017/4/29.<br>
 * Update by Huangkanglin on 2018/10/24<br>
 * JDBC通用工具类,提供CRUD操作，结果集映射到JavaBean对象,Map对象,JavaType对象操作，简化executeUpdate，executeQuery，executeBatch
 */
public class MyJDBCUtils {

    /**
     * 新增一条tableName的记录，以bean里面get方法不为null的插入
     *
     * @param connection
     * @param tableName
     * @param bean
     * @return
     */
    public static <T> int saveBean(Connection connection, String tableName, T bean) throws SQLException {
        int saveLine = 0;
        // 获取类型
        Class<?> clazz = (Class<?>) bean.getClass();
        Map<String, String> valueMap = new HashMap<String, String>();// 插入的参数集合
        // 获取所有get方法
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().substring(0, 3).equals("get")) {
                String propertyName = methods[i].getName().replace("get", "");
                try {
                    if (methods[i].invoke(bean, null) != null) {
                        // 对参数中的 \ 与 ' 进行转义防止sql注入
                        valueMap.put(propertyName,
                                methods[i].invoke(bean, null).toString().replace("\\", "\\\\").replace("'", "\\'"));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        // 创建SQL
        StringBuilder sql = new StringBuilder("insert into ").append(tableName).append(" ( ");
        StringBuilder values = new StringBuilder(" values ( ");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            // 拼接属性名称
            sql.append(entry.getKey() + ",");
            // 拼接属性值
            values.append("'" + entry.getValue() + "',");
        }
        // 去掉后面那个分号并拼接sql
        sql.deleteCharAt(sql.length() - 1).append(")");
        values.deleteCharAt(values.length() - 1).append(")");
        sql.append(values);
        // 执行sql语句
        System.out.println(sql);
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        saveLine = preparedStatement.executeUpdate();
        preparedStatement.close();
        return saveLine;
    }

    /**
     * 在tableName表中更新以conditionPropertys中的属性为条件的对象
     *
     * @param connection
     * @param tableName
     * @param bean
     * @param conditionPropertys
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> int updateBean(Connection connection, String tableName, T bean, String... conditionPropertys)
            throws SQLException {
        int updateLine = 0;
        StringBuffer sql = new StringBuffer();// sql语句
        String propertyName = null;// 属性名称
        List<String> ignorePropertylList = null;// 忽略参数数组转换为List
        List<String> conditionKeyPropertyList = Arrays.asList(conditionPropertys);// 组件参数数组转换为List
        Map<String, String> valueMap = new HashMap<String, String>();// 插入的参数集合
        // 获取所有get方法
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().substring(0, 3).equals("get")) {
                propertyName = methods[i].getName().substring(3, 4).toLowerCase() + methods[i].getName().substring(4);
                try {
                    if (methods[i].invoke(bean, null) != null) {
                        // 对参数中的 \ 与 ' 进行转义防止sql注入
                        valueMap.put(propertyName,
                                methods[i].invoke(bean, null).toString().replace("\\", "\\\\").replace("'", "\\'"));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        // 创建sql语句
        sql.append("update " + tableName + " set ");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            if (!conditionKeyPropertyList.contains(entry.getKey())) {
                sql.append(entry.getKey() + " = '" + entry.getValue() + "',");
            }
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" where ");
        for (String conditionKeyProperty : conditionKeyPropertyList) {
            sql.append(conditionKeyProperty + " = '" + valueMap.get(conditionKeyProperty) + "' and ");
        }
        sql.delete(sql.length() - 5, sql.length());
        // 执行sql语句
        System.out.println(sql);
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        updateLine = preparedStatement.executeUpdate();
        preparedStatement.close();
        return updateLine;
    }

    public static <T> int updateBean(Connection connection, T bean, String[] conditionPropertys) throws SQLException {
        String tableName = bean.getClass().getSimpleName();
        return updateBean(connection, tableName, bean, conditionPropertys);
    }

    /**
     * 在tableName表中更新以conditionPropertys中的属性为条件的对象,无论对象的某个属性是否为NULL都将更新到数据库中
     *
     * @param connection
     * @param tableName
     * @param bean
     * @param conditionPropertys
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> int updateFullBean(Connection connection, String tableName, T bean, String[] conditionPropertys)
            throws SQLException {
        int updateLine = 0;
        StringBuffer sql = new StringBuffer();// sql语句
        String propertyName = null;// 属性名称
        List<String> ignorePropertylList = null;// 忽略参数数组转换为List
        List<String> conditionKeyPropertyList = Arrays.asList(conditionPropertys);
        ;// 组件参数数组转换为List
        Map<String, Object> valueMap = new HashMap<String, Object>();// 插入的参数集合
        // 获取所有get方法
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().substring(0, 3).equals("get")) {
                propertyName = methods[i].getName().substring(3, 4).toLowerCase() + methods[i].getName().substring(4);
                try {
                    valueMap.put(propertyName, methods[i].invoke(bean, null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        // 创建sql语句
        sql.append("update " + tableName + " set ");
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if (!conditionKeyPropertyList.contains(entry.getKey())) {
                if (entry.getValue() == null) {
                    sql.append(entry.getKey() + " = null,");
                } else {
                    // 对参数中的 \ 与 ' 进行转义防止sql注入
                    sql.append(entry.getKey() + " = '"
                            + entry.getValue().toString().replace("\\", "\\\\").replace("'", "\\'") + "',");
                }
            }
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" where ");
        for (String conditionKeyProperty : conditionKeyPropertyList) {
            if (valueMap.get(conditionKeyProperty) == null) {
                sql.append(conditionKeyProperty + " = null and ");
            } else {
                // 对参数中的 / 与 ' 进行转义防止sql注入
                sql.append(conditionKeyProperty + " = '"
                        + valueMap.get(conditionKeyProperty).toString().replace("\\", "\\\\").replace("'", "\\'")
                        + "' and ");
            }

        }
        sql.delete(sql.length() - 5, sql.length());
        // 执行sql语句
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        updateLine = preparedStatement.executeUpdate();
        preparedStatement.close();
        return updateLine;
    }

    public static <T> int updateFullBean(Connection connection, T bean, String[] conditionPropertys)
            throws SQLException {
        String tableName = bean.getClass().getSimpleName();
        return updateFullBean(connection, tableName, bean, conditionPropertys);
    }

    /**
     * 在tableName表中删除以conditionPropertys中的属性为条件的对象
     *
     * @param connection
     * @param tableName
     * @param bean
     * @param conditionPropertys
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> int deleteBean(Connection connection, String tableName, T bean, String... conditionPropertys)
            throws SQLException {
        int updateLine = 0;
        StringBuffer sql = new StringBuffer();// sql语句
        String propertyName = null;// 属性名称
        List<String> conditionPropertyList = Arrays.asList(conditionPropertys);
        ;// 组件参数数组转换为List
        String methodName = null;
        // 创建sql语句
        sql.append("delete from " + tableName + " where ");
        for (String conditionProperty : conditionPropertys) {
            methodName = "get" + conditionProperty.substring(0, 1).toUpperCase() + conditionProperty.substring(1);
            try {
                // 对参数中的 \ 与 ' 进行转义防止sql注入
                sql.append(conditionProperty + " = '" + bean.getClass().getDeclaredMethod(methodName).invoke(bean)
                        .toString().replace("\\", "\\\\").replace("'", "\\'") + "' and ");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        sql.delete(sql.length() - 5, sql.length());
        System.out.println(sql.toString());
        // 执行sql语句
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        updateLine = preparedStatement.executeUpdate();
        preparedStatement.close();
        return updateLine;
    }

    public static <T> int deleteBean(Connection connection, T bean, String[] conditionPropertys) throws SQLException {
        String tableName = bean.getClass().getSimpleName();
        return deleteBean(connection, tableName, bean, conditionPropertys);
    }

    /**
     * 将查询结果转换为beanClass类型的对象序列
     *
     * @param connection
     * @param querySql
     * @param queryParams
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> List<T> getBeans(Connection connection, String querySql, Object[] queryParams, Class beanClass) {
        List<T> beanList = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(querySql);
            if (queryParams != null) {
                for (int i = 0; i < queryParams.length; i++) {
                    preparedStatement.setObject(i + 1, queryParams[i]);
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            beanList = resultSetToBeans(resultSet, beanClass);
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beanList;
    }

    public static <T> List<T> getBeans(Connection connection, String querySql, Class beanClass) {
        return getBeans(connection, querySql, null, beanClass);
    }

    public static List<Map<String, Object>> getMaps(Connection connection, String querySql) {
        return getMaps(connection, querySql, null);
    }

    /**
     * 将查询结果转换为Map序列
     *
     * @param connection
     * @param sql
     * @param queryParams
     * @return
     */
    public static List<Map<String, Object>> getMaps(Connection connection, String sql, Object... queryParams) {
        List<Map<String, Object>> resultSetList = null;
        System.out.println(sql);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (queryParams != null) {
                for (int i = 0; i < queryParams.length; i++) {
                    preparedStatement.setObject(i + 1, queryParams[i]);
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSetList = resultSetToMaps(resultSet);
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSetList;
    }

    /**
     * 将查询结果转换为Java类型的对象序列
     *
     * @param connection
     * @param sql
     * @param queryParams
     * @param javaTypeClass
     * @param <T>
     * @return
     */
    public static <T> List<T> getJavaTypes(Connection connection, String sql, Object[] queryParams,
                                           Class<?> javaTypeClass) {
        List<T> javaTypList = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (queryParams != null) {
                for (int i = 0; i < queryParams.length; i++) {
                    preparedStatement.setObject(i + 1, queryParams[i]);
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            javaTypList = resultSetToJavaTypes(resultSet, javaTypeClass);
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return javaTypList;
    }

    public static <T> List<T> getJavaTypes(Connection connection, String sql, Class javaTypeClass) {
        return getJavaTypes(connection, sql, null, javaTypeClass);
    }

    /**
     * 将结果集转换为bean对象序列
     *
     * @param resultSet
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> List<T> resultSetToBeans(ResultSet resultSet, Class beanClass) {
        List<T> beanList = null;
        try {
            // 获取创建对象构造方法
            Constructor constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object bean = null;// bean对象
            String propertyName = null;// 属性名称

            // 获取所有set方法
            Method[] methods = beanClass.getDeclaredMethods();
            List<Method> setMethodList = new ArrayList<Method>();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().substring(0, 3).equals("set")) {
                    setMethodList.add(methods[i]);
                }
            }
            // 获取结果集中的字段信息
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            List<String> fieldList = new ArrayList<String>();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                fieldList.add(resultSetMetaData.getColumnName(i));
            }

            // 对对象集合进行赋值
            while (resultSet.next()) {
                // 创建对象集合
                if (beanList == null) {
                    beanList = new ArrayList<T>();
                }
                // 创建对象
                bean = constructor.newInstance();
                // 进行赋值
                for (int i = 0; i < setMethodList.size(); i++) {
                    propertyName = setMethodList.get(i).getName().substring(3, 4).toLowerCase()
                            + setMethodList.get(i).getName().substring(4);
                    // 只对存在于结果集中的属性赋值
                    if (fieldList.contains(propertyName)) {
                        setMethodList.get(i).invoke(bean, resultSet.getObject(propertyName));
                    }
                }
                beanList.add((T) bean);
            }
        } catch (Exception e) {
            beanList = null;
            e.printStackTrace();
        }
        return beanList;
    }

    /**
     * 将结果集转换为Map序列
     *
     * @param resultSet
     * @return
     */
    public static List<Map<String, Object>> resultSetToMaps(ResultSet resultSet) {
        List<Map<String, Object>> resultSetList = null;
        try {
            Map<String, Object> objectMap = null;
            // 获取结果集中的字段信息
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            List<String> fieldList = new ArrayList<String>();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                fieldList.add(resultSetMetaData.getColumnName(i));
            }
            // 对对象集合进行赋值
            while (resultSet.next()) {
                if (resultSetList == null) {
                    resultSetList = new ArrayList<Map<String, Object>>();
                }
                objectMap = new HashMap<String, Object>();
                for (String field : fieldList) {
                    objectMap.put(field, resultSet.getObject(field));
                }
                resultSetList.add(objectMap);
            }
        } catch (Exception e) {
            resultSetList = null;
            e.printStackTrace();
        }
        return resultSetList;
    }

    /**
     * 将结果集转换为Java类型序列
     *
     * @param resultSet
     * @return
     */
    public static <T> List<T> resultSetToJavaTypes(ResultSet resultSet, Class javaTypeClass) {
        List<T> javaTypeList = null;
        try {
            // 对对象集合进行赋值
            while (resultSet.next()) {
                // 创建对象集合
                if (javaTypeList == null) {
                    javaTypeList = new ArrayList<T>();
                }
                javaTypeList.add((T) resultSet.getObject(1, javaTypeClass));
            }
        } catch (Exception e) {
            javaTypeList = null;
            e.printStackTrace();
        }
        return javaTypeList;
    }

    /**
     * 执行connection的executeUpdate操作
     *
     * @param connection
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(Connection connection, String sql, Object[] params) {
        int updateLine = 0;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            updateLine = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateLine;
    }

    public static int executeUpdate(Connection connection, String sql) {
        return executeUpdate(connection, sql, null);
    }

    /**
     * 执行connection的executeBatch操作
     *
     * @param connection
     * @param sql
     * @param batchParams
     * @return
     */
    public static int[] executeBatch(Connection connection, String sql, List<Object[]> batchParams) {
        int[] updateLines = {};
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                preparedStatement.addBatch();

            }
            updateLines = preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateLines;
    }

    public static int[] executeBatch(Connection connection, List<String> batchSqls) {
        int[] updateLines = {};
        try {
            Statement statement = connection.createStatement();
            for (String batchSql : batchSqls) {
                statement.addBatch(batchSql);
            }
            updateLines = statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateLines;
    }

    /**
     * count查询获得本次sql查询获得的对象
     *
     * @param connection
     * @param sql
     * @param values
     * @return
     */
    public static Integer countSqlResult(Connection connection, String sql, Object... values) {
        // 剔除语句中select部分和order by部分
        String s = sql.toUpperCase();
        String countSql = "";
        int count = 0;
        try {
            int j = s.indexOf(" ORDER BY");
            if (j > -1) {
                countSql = sql.substring(0, j);
            } else {
                countSql = sql;
            }
            countSql = "SELECT COUNT(*) AS count  FROM (" + countSql + ") as javacsru";
            List<Map<String, Object>> maps = getMaps(connection, countSql, values);
            Object countValue = maps.get(0).get("count");
            count = Integer.parseInt(countValue.toString());
        } catch (Exception e) {
        }
        return count;
    }

    /**
     * limit查询查询
     *
     * @param connection
     * @param sql
     * @param startIndex
     * @param pageSize
     * @param values
     * @return
     */
    public static List<Map<String, Object>> getMaps(Connection connection, String sql, int startIndex, int pageSize,
                                                    Object... values) {
        sql = sql + " LIMIT " + startIndex + "," + pageSize;
        return getMaps(connection, sql, values);
    }

    /**
     * 返回分页记录
     *
     * @param connection
     * @param pageView
     * @param values
     * @return
     */
    public static PageView<Map<String, Object>> getMaps(Connection connection, String sql,
                                                        PageView<Map<String, Object>> pageView, Object... values) {
        // 1.获取总记录数
        int count = countSqlResult(connection, sql, values);
        List<Map<String, Object>> records = getMaps(connection, sql, pageView.getFirstResult(), pageView.getPageSize(),
                values);

        // 2.封装总记录数
        pageView.setCount(count);
        pageView.setRecords(records);
        return pageView;
    }

}

/**
 * ClassName: PageView <br/>
 * Function: 分页插件 <br/>
 * date: 2018年5月5日 下午11:27:32 <br/>
 *
 * @author hkl
 * @version @param <T>
 */
class PageView<T> implements Serializable {
    /**
     * 分页数据
     */
    private List<T> records;

    /**
     * 页码的开始索引类 这个类包含， startindex 始索 endindex 结束索引 这个数是计算出来
     */
    private PageIndex pageindex;

    /**
     * 总页 这个数是计算出来
     */
    private long pageCount;

    /**
     * 每页显示几条记录
     */
    private int pageSize = 10;

    /**
     * 默认 当前 为第 这个数是计算出来
     */
    private int currentPage = 1;

    /**
     * 总记录数
     */
    private long count;

    /**
     * 从第几条记录
     */
    private int startPage;

    /**
     * 规定显示5个页
     */
    private int pagecode = 5;

    public PageView() {
    }

    /**
     * 要获得记录的 索引 始页
     *
     * @return
     */
    public int getFirstResult() {
        return (this.currentPage - 1) * this.pageSize;
    }

    public int getPagecode() {
        return pagecode;
    }

    public void setPagecode(int pagecode) {
        this.pagecode = pagecode;
    }

    /**
     * 使用构函数，，强制必需输入 每页显示数量 前页
     *
     * @param pageSize 每页显示数量
     */
    public PageView(int pageSize, int currentPage) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

    /**
     * 使用构函数，，强制必需输入 当前
     */
    public PageView(int currentPage) {
        this.currentPage = currentPage;
        startPage = (this.currentPage - 1) * this.pageSize;
    }

    /**
     * 查询结果方法 把 记录数 结果集合 入到pageView对象
     *
     * @param rowCount 总记录数
     * @param records  结果集合
     */

    public void setQueryResult(long rowCount, List<T> records) {
        setCount(rowCount);
        setRecords(records);
    }

    public void setCount(long count) {
        this.count = count;
        setPageCount(this.count % this.pageSize == 0 ? this.count / this.pageSize : this.count / this.pageSize + 1);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public PageIndex getPageindex() {
        return pageindex;
    }

    public void setPageindex(PageIndex pageindex) {
        this.pageindex = pageindex;
    }

    /**
     * 必需输入
     */
    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
        this.pageindex = WebTool.getPageIndex(pagecode, currentPage, pageCount);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int pageNow) {
        if (pageNow < 1) {
            pageNow = 1;
        }
        this.currentPage = pageNow;
    }

    public long getPageCount() {
        return pageCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getCount() {
        return count;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

}

/**
 * ClassName: PageIndex <br/>
 * Function: 页面索引 <br/>
 * date: 2018年5月5日 下午11:30:33 <br/>
 *
 * @author hkl
 */
class PageIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    private long startindex;
    private long endindex;

    public PageIndex(long startindex, long endindex) {
        this.startindex = startindex;
        this.endindex = endindex;
    }

    public long getStartindex() {
        return startindex;
    }

    public void setStartindex(long startindex) {
        this.startindex = startindex;
    }

    public long getEndindex() {
        return endindex;
    }

    public void setEndindex(long endindex) {
        this.endindex = endindex;
    }

}

/**
 * ClassName: WebTool <br/>
 * Function: web工具 <br/>
 * date: 2018年5月5日 下午11:29:16 <br/>
 *
 * @author hkl
 */
class WebTool {
    public static PageIndex getPageIndex(long pagecode, int pageNow, long pageCount) {
        long startpage = pageNow - (pagecode % 2 == 0 ? pagecode / 2 - 1 : pagecode / 2);
        long endpage = pageNow + pagecode / 2;
        if (startpage < 1) {
            startpage = 1;
            if (pageCount >= pagecode) {
                endpage = pagecode;
            } else {
                endpage = pageCount;
            }
        }
        if (endpage > pageCount) {
            endpage = pageCount;
            if ((endpage - pagecode) > 0) {
                startpage = endpage - pagecode + 1;
            } else {
                startpage = 1;
            }
        }
        return new PageIndex(startpage, endpage);
    }
}
