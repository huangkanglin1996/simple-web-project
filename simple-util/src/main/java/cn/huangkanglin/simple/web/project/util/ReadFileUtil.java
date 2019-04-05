/**
 * Project Name:zxb-common-generator
 * File Name:ReadFileUtil.java
 * Package Name:com.zhaoxuebang.common.generator.util
 * Date:2018年2月10日下午2:13:46
 * Copyright (c) 2018, hkl@zhaoxuebang.com All Rights Reserved.
 */

package cn.huangkanglin.simple.web.project.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ClassName:ReadFileUtil <br/>
 * Function: TODO <br/>
 * Date: 2018年2月10日 下午2:13:46 <br/>
 *
 * @author hkl
 * @version
 * @since JDK 1.8
 * @see
 */
public class ReadFileUtil {

    /**
     * 如果前缀加上classpath:则表示资源放在类路径下面
     *
     * @param path 文件路径
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getInputSteam(String path) throws FileNotFoundException {
        if (path == null || path.trim().equals("")) {
            throw new NullPointerException("you input filePath is null...");
        }
        // 验证是文件名否在类路径下
        String regex = "(?i)^(classPath:)(.)*";
        boolean isClassPath = path.matches(regex);
        InputStream in = null;
        if (isClassPath) {
            path = path.replaceAll("^(?i)^(classPath:)", "");
            in = ReadFileUtil.class.getClassLoader().getResourceAsStream(path);
        } else {
            in = new FileInputStream(path);
        }
        return in;
    }

    /**
     * 通过文件获取Properties对象
     *
     * @author hkl
     * @param path 文件路径
     * @return
     * @throws IOException
     */
    public static Properties getProperties(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(getInputSteam(path));
        return properties;
    }
}
