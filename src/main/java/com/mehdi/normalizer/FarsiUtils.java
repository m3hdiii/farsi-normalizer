package com.mehdi.normalizer;

/**
 * @author Mehdi Afsari kashi
 * @version 1.0.0
 * @since 1.0.0
 * <p/>
 * Creation Date : 2016/06/11
 */
public class FarsiUtils implements FarsiNormalizable {

    private boolean farsiDigit;
    private boolean replaceArKafToFa;
    private boolean replaceArYahToFa;
    private static char CHAR_RLM = '\u200F';
    private static char CHAR_RLE = '\u202B';

    public FarsiUtils() {
        this(true, true, false);
    }

    public FarsiUtils(boolean farsiDigit,
                      boolean replaceArKafToFa, boolean replaceArYahToFa) {
        this.farsiDigit = farsiDigit;
        this.replaceArKafToFa = replaceArKafToFa;
        this.replaceArYahToFa = replaceArYahToFa;
    }

    public String normalizeFarsiText(String ANo) {
        String ValidStr = "ابپتثجچحخدذرزژسشصضطظعغفقكکگلمنوهيی";
        int FPos, LPos, i, j, k;
        boolean flgEnterIntSection, LastWasChar;
        String Result = "";
        if (ANo == null)
            return Result;
        flgEnterIntSection = false;
        StringBuilder sbArrestNo = new StringBuilder(ANo);
        i = sbArrestNo.length() - 1;
        while (i >= 0) {
            if (replaceArKafToFa)
                if (sbArrestNo.charAt(i) == 'ك') sbArrestNo.setCharAt(i, 'ک');
            if (replaceArYahToFa)
                if (sbArrestNo.charAt(i) == 'ي') sbArrestNo.setCharAt(i, 'ی'); //Not in Delphi Code.

            if (StrToIntDef(Character.toString(sbArrestNo.charAt(i)), -1) == -1) {
                if (ValidStr.indexOf(sbArrestNo.charAt(i)) > -1) {
                    j = 0;
                    while (((i - j) > -1) && (ValidStr.indexOf(sbArrestNo.charAt(i - j)) > -1))
                        j++;

                    if (j > 1) {
                        k = j;
                        while (j > 0) {
                            Result += sbArrestNo.charAt(i - j + 1);
                            j--;
                        }
                        i = i - k + 1;
                    } else {
                        Result += sbArrestNo.charAt(i);
                        Result += CHAR_RLE;
                    }
                } else { //if (ValidStr.indexOf(sbArrestNo.charAt(i)) > -1)
                    if (sbArrestNo.charAt(i) == '-')
                        Result += CHAR_RLM;
                    Result += sbArrestNo.charAt(i);
                    if ((sbArrestNo.charAt(i) == '.') || (sbArrestNo.charAt(i) == '/'))
                        Result += CHAR_RLE;
                    else if (sbArrestNo.charAt(i) == '-')
                        Result += CHAR_RLM;
                }
                i--;
                flgEnterIntSection = false;
            } else { // if (StrToIntDef(sbArrestNo.substring(i, i + 1), -1) == -1)
                FPos = i;
                j = i;
                while ((j > -1) && ((!flgEnterIntSection) || ((StrToIntDef(Character.toString(sbArrestNo.charAt(j)), -1) != -1)))) {
                    flgEnterIntSection = true;
                    while ((j > -1) && (StrToIntDef(Character.toString(sbArrestNo.charAt(j)), -1) != -1))
                        j--;
                    if ((j > -1) && ((sbArrestNo.charAt(j) == '.') || (sbArrestNo.charAt(j) == '/')))
                        if ((j > 0) && (StrToIntDef(Character.toString(sbArrestNo.charAt(j - 1)), -1) != -1))
                            j--;
                }
                LPos = j + 1;
                for (j = LPos; j <= FPos; j++)
                    if (StrToIntDef(Character.toString(sbArrestNo.charAt(j)), -1) == -1)
                        Result += sbArrestNo.charAt(j);
                    else if (farsiDigit)
                        Result += convertEnDigitToFaDigit(Character.toString(sbArrestNo.charAt(j)));
                    else
                        Result += Character.toString(sbArrestNo.charAt(j));
                i = LPos - 1;
            }
        } //while
        return Result;
    }

    public String convertEnDigitToFaDigit(String value) {
        return (value == null) ? value : value.replaceAll("0", "\u06F0")
                .replaceAll("1", "\u06F1").replaceAll("2", "\u06F2").replaceAll("3", "\u06F3")
                .replaceAll("4", "\u06F4").replaceAll("5", "\u06F5").replaceAll("6", "\u06F6")
                .replaceAll("7", "\u06F7").replaceAll("8", "\u06F8").replaceAll("9", "\u06F9");
    }

    public String convertFaDigitToEnDigit(String value) {
        return (value == null) ? value : value.replaceAll("\u06F0", "0")
                .replaceAll("\u06F1", "1").replaceAll("\u06F2", "2").replaceAll("\u06F3", "3")
                .replaceAll("\u06F4", "4").replaceAll("\u06F5", "5").replaceAll("\u06F6", "6")
                .replaceAll("\u06F7", "7").replaceAll("\u06F8", "8").replaceAll("\u06F9", "9");
    }

    public String clearExtraChars(String value) {
        return (value == null) ? value : value.replaceAll(String.valueOf(CHAR_RLE), "")
                .replaceAll(String.valueOf(CHAR_RLM), "");

    }

    private int StrToIntDef(String Str, int Default) {
        try {
            return Integer.parseInt(Str);
        } catch (NumberFormatException nfe) {
            return Default;
        }
    }

    @Override
    public String normalize(String s) {
        return normalizeFarsiText(s);
    }
}
