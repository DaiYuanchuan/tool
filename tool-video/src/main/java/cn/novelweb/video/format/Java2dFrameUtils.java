package cn.novelweb.video.format;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;

/**
 * <p>重写原 {@link org.bytedeco.javacv.Java2DFrameUtils} 方法</p>
 * <p>2021-06-28 13:29</p>
 *
 * @author Dan
 **/
public class Java2dFrameUtils {

    private static final Java2DFrameConverter BI_CONV = new Java2DFrameConverter();

    /**
     * Clones (deep copies the data) of a {@link BufferedImage}. Necessary when
     * converting to BufferedImages from JavaCV types to avoid re-using the same
     * memory locations.
     *
     * @param source BufferedImage
     * @return BufferedImage
     */
    public static BufferedImage deepCopy(BufferedImage source) {
        return Java2DFrameConverter.cloneBufferedImage(source);
    }

    public synchronized static BufferedImage toBufferedImage(Frame src) {
        return deepCopy(BI_CONV.getBufferedImage(src.clone()));
    }

}
