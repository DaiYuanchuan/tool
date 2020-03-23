package cn.novelweb.tool.upload.fastdfs.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 * <p>2020-02-03 23:27</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class Log {

    public static Boolean debugLog = true;

    public static void debug(String s, Object o, Object o1) {
        if (debugLog) {
            log.debug(s, o, o1);
        }
    }

    public static void debug(String s, Object... objects) {
        if (debugLog) {
            log.debug(s, objects);
        }
    }

    public static void debug(String s, Throwable throwable) {
        if (debugLog) {
            log.debug(s, throwable);
        }
    }

    public static void debug(String s, Object o) {
        if (debugLog) {
            log.debug(s, o);
        }
    }

    public static void debug(org.slf4j.Marker marker, String s) {
        if (debugLog) {
            log.debug(marker, s);
        }
    }

    public static void debug(org.slf4j.Marker marker, String s, Object o) {
        if (debugLog) {
            log.debug(marker, s, o);
        }
    }

    public static void debug(org.slf4j.Marker marker, String s, Object o, Object o1) {
        if (debugLog) {
            log.debug(marker, s, o, o1);
        }
    }

    public static void debug(org.slf4j.Marker marker, String s, Object... objects) {
        if (debugLog) {
            log.debug(marker, s, objects);
        }
    }

    public static void debug(org.slf4j.Marker marker, String s, Throwable throwable) {
        if (debugLog) {
            log.debug(marker, s, throwable);
        }
    }
}
