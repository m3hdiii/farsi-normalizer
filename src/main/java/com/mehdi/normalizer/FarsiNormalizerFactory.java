package com.mehdi.normalizer;

/**
 * @author Mehdi Afsari kashi
 * @version 1.0.0
 * @since 1.0.0
 * <p/>
 * Creation Date : 2016/06/11
 */
public class FarsiNormalizerFactory {
    public static FarsiNormalizable create() {
        if (Boolean.TRUE.toString().equals(System.getProperty(FarsiNormalizable.class.getName()))) {
            return new WindowsRTLConverter();
        } else {
            return new FarsiUtils();
        }
    }
}
