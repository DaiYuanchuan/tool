package cn.novelweb.tool.upload.fastdfs.utils;

import cn.hutool.core.util.ArrayUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * <p>文件读取</p>
 * <p>2020-02-03 20:55</p>
 *
 * @author Dai Yuanchuan
 **/
public class IniFileReader {

    private Hashtable paramTable;
    private String confFilename;

    public IniFileReader(String confFilename) {
        this.confFilename = confFilename;
        loadFromFile(confFilename);
    }

    public static ClassLoader classLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

    public static InputStream loadFromOsFileSystemOrClasspathAsStream(String filePath) {
        InputStream in = null;
        try {
            // 优先从文件系统路径加载
            if (new File(filePath).exists()) {
                in = new FileInputStream(filePath);
            } else {
                // 从类路径加载
                in = classLoader().getResourceAsStream(filePath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return in;
    }

    public String getConfFilename() {
        return this.confFilename;
    }

    public String getStrValue(String name) {
        Object obj;
        obj = this.paramTable.get(name);
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        return (String) ((ArrayList) obj).get(0);
    }

    public int getIntValue(String name, int defaultValue) {
        String szValue = this.getStrValue(name);
        if (szValue == null) {
            return defaultValue;
        }

        return Integer.parseInt(szValue);
    }

    public boolean getBoolValue(String name, boolean defaultValue) {
        String szValue = this.getStrValue(name);
        if (szValue == null) {
            return defaultValue;
        }

        String[] str = {"yes", "on", "true", "1"};
        return ArrayUtil.contains(str, szValue);
    }

    public String[] getValues(String name) {
        Object obj;
        String[] values;

        obj = this.paramTable.get(name);
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            values = new String[1];
            values[0] = (String) obj;
            return values;
        }

        Object[] objs = ((ArrayList) obj).toArray();
        values = new String[objs.length];
        System.arraycopy(objs, 0, values, 0, objs.length);
        return values;
    }

    private void loadFromFile(String confFilePath) {
        InputStream in = loadFromOsFileSystemOrClasspathAsStream(confFilePath);
        try {
            readToParamTable(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void readToParamTable(InputStream in) {
        this.paramTable = new Hashtable();
        if (in == null) {
            return;
        }
        String line;
        String[] parts;
        String name;
        String value;
        Object obj;
        ArrayList valueList;
        InputStreamReader inReader = null;
        BufferedReader bufferedReader = null;
        try {
            inReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inReader);
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }
                name = parts[0].trim();
                value = parts[1].trim();
                obj = this.paramTable.get(name);
                if (obj == null) {
                    this.paramTable.put(name, value);
                } else if (obj instanceof String) {
                    valueList = new ArrayList();
                    valueList.add(obj);
                    valueList.add(value);
                    this.paramTable.put(name, valueList);
                } else {
                    valueList = (ArrayList) obj;
                    valueList.add(value);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inReader != null) {
                    inReader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
