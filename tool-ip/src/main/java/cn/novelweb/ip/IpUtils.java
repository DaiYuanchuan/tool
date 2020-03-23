package cn.novelweb.ip;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.net.NetUtil;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * <p>IP工具类集</p>
 * <p>2019-11-21 12:04</p>
 *
 * @author Dai Yuanchuan
 **/
public class IpUtils {

    /**
     * 通过域名获取IP地址
     *
     * @param hostName 域名信息(注意不要加http或https,类似于cmd的ping命令)
     * @return 返回IP地址，如果未能获取到IP地址则返回域名信息hostName
     */
    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取本机网卡的IP地址和计算机名
     *
     * @return 本机网卡的IP地址和计算机名，获取失败返回null
     */
    public static InetAddress getLocalhost() {
        return NetUtil.getLocalhost();
    }

    /**
     * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个<br>
     * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。<br>
     * 此方法不会抛出异常，获取失败将返回null<br>
     *
     * @return 本机网卡IP地址，获取失败返回<code>null</code>
     */
    public static String getLocalhostStr() {
        return NetUtil.getLocalhostStr();
    }

    /**
     * 隐藏掉IP地址的最后一部分,使用 * 代替
     *
     * @param ip 需要操作的IP地址
     * @return 隐藏部分后的IP
     */
    public static String hideIpPart(String ip) {
        return NetUtil.hideIpPart(ip);
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 返回是否为内网IP地址(true : 内网IP | | false : 不是内网IP)
     */
    public static boolean isInnerIp(String ip) {
        return NetUtil.isInnerIP(ip);
    }

    /**
     * 检测本地端口是否可用<br>
     *
     * @param port 需要被检测的端口
     * @return 返回给定的端口是否可用(true : 可用 | | false : 不可用)
     */
    public static boolean isUsableLocalPort(int port) {
        return NetUtil.isUsableLocalPort(port);
    }

    /**
     * 是否为有效的端口<br>
     * 此方法并不检查端口是否被占用
     * 有效端口是0～65535
     *
     * @param port 需要被检测的端口
     * @return 是否有效(true : 有效 | | false : 无效)
     */
    public static boolean isValidPort(int port) {
        return NetUtil.isValidPort(port);
    }

    /**
     * 获取指定IP地址的实际地理位置<br>
     * 使用内存搜索 memorySearch 算法
     *
     * @param ip 需要查询的IP地址信息
     * @return 返回查询到的地区信息，查询出错时返回null
     */
    public static Region getIpLocationByMemory(String ip) {
        return getIpLocation(ip, "memorySearch");
    }

    /**
     * 获取指定IP地址的实际地理位置<br>
     * 使用 b-tree 算法
     *
     * @param ip 需要查询的IP地址信息
     * @return 返回查询到的地区信息，查询出错时返回null
     */
    public static Region getIpLocationByBtree(String ip) {
        return getIpLocation(ip, "btreeSearch");
    }

    /**
     * 获取指定IP地址的实际地理位置<br>
     * 使用 二进制搜索算法 算法
     *
     * @param ip 需要查询的IP地址信息
     * @return 返回查询到的地区信息，查询出错时返回null
     */
    public static Region getIpLocationByBinary(String ip) {
        return getIpLocation(ip, "binarySearch");
    }

    /**
     * 获取指定IP地址的实际地理位置<br>
     *
     * @param ip        需要查询的IP地址信息
     * @param algorithm 查询算法(不同算法间有时间差异)
     * @return 返回查询到的地区信息，查询出错时返回null
     */
    private static Region getIpLocation(String ip, String algorithm) {
        if (isInnerIp(ip)) {
            String str = "内网IP";
            return Region.builder()
                    .country(str)
                    .province(str)
                    .city(str)
                    .isp(str)
                    .build();
        }
        try {
            DbConfig config = Singleton.get(DbConfig.class);
            final File tempFile = FileUtil.touch(Singleton.get(File.class,"ip2region.db"));
            InputStream inputStream = IpUtils.class.getClassLoader().getResourceAsStream("data/ip2region.db");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                if (inputStream!=null) {
                    org.apache.commons.io.IOUtils.copy(inputStream, out);
                }
            }
            DbSearcher dbSearcher = Singleton.get(DbSearcher.class,
                    config, tempFile);
            Method method = dbSearcher.getClass().getMethod(algorithm, String.class);
            DataBlock dataBlock = (DataBlock) method.invoke(dbSearcher, ip);
            String[] ipData = dataBlock.getRegion().split("\\|");
            return Region.builder()
                    .country(ipData[0])
                    .province(ipData[2])
                    .city(ipData[3])
                    .isp(ipData[4])
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
