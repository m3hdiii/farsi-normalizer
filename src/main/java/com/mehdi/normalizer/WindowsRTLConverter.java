package com.mehdi.normalizer;

import com.ibm.icu.text.Bidi;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mehdi Afsari kashi
 * @version 1.0.0
 * @since 1.0.0
 * <p/>
 * Creation Date : 2016/06/11
 */
public class WindowsRTLConverter implements FarsiNormalizable {
    private final static Logger log = Logger.getLogger(WindowsRTLConverter.class);

    protected static char CHAR_LRM = '\u200E';
    protected static char CHAR_RLM = '\u200F';

    protected static char CHAR_LRI = '\u2066';
    protected static char CHAR_RLI = '\u2067';
    protected static char CHAR_PDI = '\u2069';

    protected static char CHAR_LRO = '\u202D';
    protected static char CHAR_RLO = '\u202E';

    protected static char CHAR_LRE = '\u202A';
    protected static char CHAR_RLE = '\u202B';
    protected static char CHAR_PDF = '\u202C';

    @Override
    public String normalize(String s) {
        return normalizedTextWithWindows(s);
    }

    private enum Direction {
        RTL, LTR, CONTROL, NONE
    }

    /**
     * use this method for normalized Text With Windows in java's text components. this method use directional chars for this
     * and convert latin digit to persian digit.
     *
     * @param str input string
     * @return normalized Text With Windows
     * @see #originalText(String)
     */
    public String normalizedTextWithWindows(String str) {
        if (str == null)
            return null;

        log.info("str = '" + str + "'");

        if (!checkInputStr(str)) {
            log.warn("input str was null or empty!!");
            return str;
        }

        String normalizedInJava = normalizedInJava(str);

        if (log.isTraceEnabled()) {
            log.trace("normalizedInJava = '" + normalizedInJava + "'");
        }

        StringBuilder stringBuilder = new StringBuilder();
        Bidi bidi = new Bidi();
        bidi.setReorderingOptions(Bidi.REORDER_RUNS_ONLY);
        bidi.setReorderingMode(Bidi.OPTION_INSERT_MARKS);
        bidi.setPara(normalizedInJava, Bidi.MIXED, null);

        Direction direction;
        Direction baseDirection = Direction.NONE;
        Direction preDirection = Direction.NONE;

        for (int i = 0; i < bidi.countRuns(); i++) {

            String substring = normalizedInJava.substring(bidi.getRunStart(i), bidi.getRunLimit(i));
            String trim = substring;
            char firstChar = trim.charAt(0);
            direction = getDirection(firstChar);

            Direction otherDir = getBaseDirection(trim);
            baseDirection = Direction.NONE.equals(otherDir) ? baseDirection : otherDir;

            if (Direction.NONE.equals(baseDirection)) {
                if (i == 0 && !Direction.CONTROL.equals(direction)) {
                    stringBuilder.append(CHAR_PDF).append(CHAR_RLE);
                }

                if (!Direction.LTR.equals(preDirection)) {
                    if (isContainsSpecialChar(substring)) {
                        substring = insertLRM_Char(substring);
                    } else if (substring.contains("/")) {
                        stringBuilder.append(CHAR_RLM);
                    }
                }
            } else if (!direction.equals(baseDirection) && Direction.RTL.equals(baseDirection) && !Direction.CONTROL.equals(direction)) {
                stringBuilder.append(CHAR_RLM);
            }

            if (log.isTraceEnabled()) {
                log.trace("dir : " + StringUtils.rightPad(direction + "", 4) +
                        " __base_dir : " + StringUtils.rightPad(baseDirection + "", 4) +
                        " __pre_dir : " + StringUtils.rightPad(preDirection + "", 4) +
                        //                    " __LogicalRun(" + StringUtils.leftPad(i + "", 2, '0') + ") = " + StringUtils.rightPad(bidi.getLogicalRun(i).toString(), 19) +
                        " __VisualRun(" + StringUtils.leftPad(i + "", 2, '0') + ") = " + StringUtils.rightPad(bidi.getVisualRun(i).toString(), 19) +
                        " __str : '" + substring + "'");
            }

            stringBuilder.append(substring);

            preDirection = getLastDirection(trim);
        }
        if (log.isTraceEnabled()) {
            log.trace("=============================================");
        }
        String finalStr = stringBuilder.toString();
        return convertEnglishDigitToFarsiDigit(finalStr);
    }

    private String normalizedInJava(String str) {
        if (str == null)
            return null;
        Direction baseDirection = getBaseDirection(str);
        StringBuilder stringBuilder = new StringBuilder(str);
        if (!Direction.RTL.equals(baseDirection)) {
            stringBuilder.insert(0, CHAR_RLE);
            /*if baseDirection equal to Direction.LTR, last whitespace must be trim;
            * but if if baseDirection equal to Direction.NONE, insert Right-to-Left in last whitespace*/
            stringBuilder = correctSpaceDirection(stringBuilder, Direction.LTR.equals(baseDirection));
        }
        return stringBuilder.toString();
    }

    /**
     * use this method for get originalText after normalized Text With Windows via {@link #normalizedTextWithWindows(String)}
     *
     * @param str input string
     * @return original string
     */
    public String originalText(String str) {
        log.info("str = '" + str + "'");

        if (!checkInputStr(str)) {
            log.warn("input str was null or empty!!");
            return str;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (!isControlChar(ch)) {
                stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }

    private boolean checkInputStr(String str) {
        return !(str == null || str.isEmpty());
    }

    private StringBuilder correctSpaceDirection(StringBuilder stringBuilder, boolean trim) {
        Matcher matcher = Pattern.compile(".*\\S+(\\s+)").matcher(stringBuilder.toString());//find all space at end of str
        if (matcher.matches()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            if (!trim) {
                stringBuilder.insert(start, CHAR_RLM);
            } else {
                stringBuilder.delete(start, end);
            }
        }
        return stringBuilder;
    }

    private Direction getBaseDirection(String str) {
        for (char ch : str.toCharArray()) {
            Direction baseDirection = getDirection(ch);
            if (!Direction.NONE.equals(baseDirection) && !Direction.CONTROL.equals(baseDirection)) {
                return baseDirection;
            }
        }
        return Direction.NONE;
    }

    private Direction getLastDirection(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            Direction baseDirection = getDirection(str.charAt(i));
            if (!Direction.NONE.equals(baseDirection)) {
                return baseDirection;
            }
        }
        return Direction.NONE;
    }

    private Direction getDirection(char firstChar) {
        Direction direction;
        if (isFarsiChar(firstChar)) {
            direction = Direction.RTL;
        } else if (isLatinChar(firstChar)) {
            direction = Direction.LTR;
        } else if (isControlChar(firstChar)) {
            direction = Direction.CONTROL;
        } else {
            direction = Direction.NONE;
        }
        return direction;
    }

    protected String insertLRM_Char(String runStr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : runStr.toCharArray()) {
            if (isContainsSpecialChar(ch)) {
                stringBuilder.append(CHAR_LRM);
                stringBuilder.append(ch);
                stringBuilder.append(CHAR_RLM);
            } else {
                stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }

    public static String convertEnglishDigitToFarsiDigit(String value) {
        return StringUtils.isEmpty(value) ? value : value.replaceAll("0", "\u06F0").replaceAll("1", "\u06F1").replaceAll("2", "\u06F2")
                .replaceAll("3", "\u06F3").replaceAll("4", "\u06F4").replaceAll("5", "\u06F5").replaceAll("6", "\u06F6")
                .replaceAll("7", "\u06F7").replaceAll("8", "\u06F8").replaceAll("9", "\u06F9");
    }

    protected boolean isContainsSpecialChar(String str) {
        for (char c : str.toCharArray()) {
            if (isContainsSpecialChar(c)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isContainsSpecialChar(char c) {
        return "-+#$%".contains(c + "");
    }

    protected boolean isFarsiChar(char ch) {
        return (ch >= 0x060c && ch <= 0xfefc && !Character.isDigit(ch) && !isControlChar(ch));//must be farsi char and not digit
    }

    protected boolean isLatinChar(char ch) {
        return (ch >= 0x0041 && ch <= 0x005a) || (ch >= 0x0061 && ch <= 0x007a);
    }

    protected boolean isControlChar(char ch) {
        return CHAR_LRM == ch || CHAR_LRE == ch || CHAR_LRO == ch || CHAR_LRI == ch ||
                CHAR_RLM == ch || CHAR_RLE == ch || CHAR_RLO == ch || CHAR_RLI == ch ||
                CHAR_PDF == ch || CHAR_PDI == ch;
    }
}
