package im.tretyakov.elasticsearch.rumetaphone.phonetic;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.elasticsearch.common.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encodes a string into a Russian metaphone value.
 * Russian metaphone is an encoding used to relate similar russian names,
 * but can also be used as a general purpose scheme to find word with similar phonemes.
 *
 * For more information, see {@link "http://forum.aeroion.ru/topic461.html"}.
 * This class is thread-safe.

 * @author Dmitry Tretyakov <dmitry@tretyakov.im>
 */
public class RuMetaphoneEncoder implements StringEncoder {

    private static final Pattern INVALID_SYMBOLS_PATTERN = Pattern.compile("[^А-Я\\s\\-]");
    private static final Pattern UNUSED_SYMBOLS_PATTERN = Pattern.compile("[ЬЪ]");
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("[\\s\\-]");
    private static final String TOKEN = ",";
    private static final int SUFFIXES_MAP_CAPACITY = 28;
    private static final int VOWELS_MAP_CAPACITY = 4;
    private static final int DEAF_CONSONANTS_MAP_CAPACITY = 5;
    private static final Map<Pattern, String> SUFFIXES_MAP = new LinkedHashMap<>(SUFFIXES_MAP_CAPACITY);
    private static final Map<Pattern, String> VOWELS_MAP = new LinkedHashMap<>(VOWELS_MAP_CAPACITY);
    private static final Map<Pattern, String> DEAF_CONSONANTS_PATTERN_MAP = new HashMap<>(DEAF_CONSONANTS_MAP_CAPACITY);
    private static final Map<Character, Character> DEAF_CONSONANTS_MAP = new HashMap<>(DEAF_CONSONANTS_MAP_CAPACITY);
    private static final List<Character> VOWELS_AND_SONOROUS_LIST = Arrays.asList('А', 'У', 'И', 'Л', 'М', 'Н');
    private static final char NULL_CHAR = '\u0000';

    static {
        /**
         * Filling russian surname suffixes map
         * Longest suffixes first - the order does matter
         */
        SUFFIXES_MAP.put(Pattern.compile("(ОВСКИЙ)$"), "@");
        SUFFIXES_MAP.put(Pattern.compile("(ЕВСКИЙ)$"), "#");
        SUFFIXES_MAP.put(Pattern.compile("(ОВСКАЯ)$"), "$");
        SUFFIXES_MAP.put(Pattern.compile("(ЕВСКАЯ)$"), "%");
        // Block of second name suffixes
        SUFFIXES_MAP.put(Pattern.compile("(ЕВИЧ)$"), "?");
        SUFFIXES_MAP.put(Pattern.compile("(ОВИЧ)$"), "?");
        SUFFIXES_MAP.put(Pattern.compile("(ЕВНА)$"), "!");
        SUFFIXES_MAP.put(Pattern.compile("(ОВНА)$"), "!");
        // End of block of second name suffixes
        SUFFIXES_MAP.put(Pattern.compile("(ИЕВА)$"), "9");
        SUFFIXES_MAP.put(Pattern.compile("(ЕЕВА)$"), "9");
        SUFFIXES_MAP.put(Pattern.compile("(ОВА)$"), "9");
        SUFFIXES_MAP.put(Pattern.compile("(ЕВА)$"), "9");
        SUFFIXES_MAP.put(Pattern.compile("(ИЕВ)$"), "4");
        SUFFIXES_MAP.put(Pattern.compile("(ЕЕВ)$"), "4");
        SUFFIXES_MAP.put(Pattern.compile("(НКО)$"), "3");
        SUFFIXES_MAP.put(Pattern.compile("(УК)$"), "0");
        SUFFIXES_MAP.put(Pattern.compile("(ЮК)$"), "0");
        SUFFIXES_MAP.put(Pattern.compile("(ИНА)$"), "1");
        SUFFIXES_MAP.put(Pattern.compile("(ИК)$"), "2");
        SUFFIXES_MAP.put(Pattern.compile("(ЕК)$"), "2");
        SUFFIXES_MAP.put(Pattern.compile("(ОВ)$"), "4");
        SUFFIXES_MAP.put(Pattern.compile("(ЕВ)$"), "4");
        SUFFIXES_MAP.put(Pattern.compile("(ЫХ)$"), "5");
        SUFFIXES_MAP.put(Pattern.compile("(ИХ)$"), "5");
        SUFFIXES_MAP.put(Pattern.compile("(АЯ)$"), "6");
        SUFFIXES_MAP.put(Pattern.compile("(АЯ)$"), "7");
        SUFFIXES_MAP.put(Pattern.compile("(ИК)$"), "7");
        SUFFIXES_MAP.put(Pattern.compile("(ИН)$"), "8");

        /**
         * Filling vowels map
         * Longest vowels rows first - the order does matter
         */
        VOWELS_MAP.put(Pattern.compile("(ИО)|(ЙО)|(ИЕ)|(ЙЕ)"), "И");
        VOWELS_MAP.put(Pattern.compile("[ОЫЯ]"), "А");
        VOWELS_MAP.put(Pattern.compile("[Ю]"), "У");
        VOWELS_MAP.put(Pattern.compile("[ЕЁЭ]"), "И");

        /**
         * Filling deaf consonants map with patterns
         */
        DEAF_CONSONANTS_PATTERN_MAP.put(Pattern.compile("Б$"), "П");
        DEAF_CONSONANTS_PATTERN_MAP.put(Pattern.compile("В$"), "Ф");
        DEAF_CONSONANTS_PATTERN_MAP.put(Pattern.compile("Г$"), "К");
        DEAF_CONSONANTS_PATTERN_MAP.put(Pattern.compile("Д$"), "Т");
        DEAF_CONSONANTS_PATTERN_MAP.put(Pattern.compile("З$"), "С");

        /**
         * Filling deaf consonants map with patterns
         */
        DEAF_CONSONANTS_MAP.put('Б', 'П');
        DEAF_CONSONANTS_MAP.put('В', 'Ф');
        DEAF_CONSONANTS_MAP.put('Д', 'Т');
        DEAF_CONSONANTS_MAP.put('Г', 'К');
        DEAF_CONSONANTS_MAP.put('З', 'С');
    }

    /**
     * Encodes a String and returns a String.
     *
     * @param source the String to encode
     * @return the encoded String
     * @throws EncoderException thrown if there is an error condition during the encoding process.
     */
    @Override
    public String encode(final String source) throws EncoderException {
        return this.metaphone(source);
    }

    /**
     * Encodes an "Object" and returns the encoded content as an Object.
     *
     * @param source An object to encode
     * @return An "encoded" Object
     * @throws EncoderException An encoder exception is thrown if the object is not an instance of {@code String}
     */
    @Override
    public Object encode(Object source) throws EncoderException {
        if (!(source instanceof String)) {
            throw new EncoderException("Russian metaphone encode parameter is not of type String");
        }
        return this.metaphone((String) source);
    }

    /**
     * Method which implements phonetic algorithm for words indexing by their spelling
     * using russian pronunciation principles:
     * - Converts given string to uppercase
     * - Removes any non-letter symbols and symbols Ъ and Ь
     * - Transforms russian surname and middle name suffixes to special symbols
     * - Converts sonorous consonants to deaf
     *
     * @param source Source string to encode
     * @return The encoded string
     */
    private String metaphone(final String source) {
        final String value = cleanSource(source);
        if (value == null) {
            return "";
        }
        // Tokenize
        final String[] tokens = DELIMITER_PATTERN.matcher(value).replaceAll(TOKEN).split(TOKEN);
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            final char[] chars = replaceSuffixes(
                replaceVowels(
                    replaceSuffixes(tokens[i], SUFFIXES_MAP)
                ),
                DEAF_CONSONANTS_PATTERN_MAP
            ).toCharArray();
            for (int j = 0; j < chars.length; j++) {
                char current = chars[j];
                final char next = j < chars.length - 1 ? chars[j + 1] : NULL_CHAR;
                final char prev = j > 0 ? chars[j -1] : NULL_CHAR;
                if (prev == current) {
                    continue;
                }
                if (VOWELS_AND_SONOROUS_LIST.indexOf(next) != -1) {
                    result.append(current);
                } else {
                    current = DEAF_CONSONANTS_MAP.getOrDefault(current, current);
                    if (current != prev) {
                        result.append(current);
                    }
                }
            }
            result.append(i != tokens.length - 1 ? " " : "");
        }
        return result.toString().trim();
    }

    /**
     * Cleans source string by removing invalid symbols
     *
     * @param source Source string to clean
     * @return The cleaned string
     */
    @Nullable
    private static String cleanSource(String source) {
        if (source == null) {
            return null;
        }
        source = UNUSED_SYMBOLS_PATTERN.matcher(
            INVALID_SYMBOLS_PATTERN.matcher(
                source.trim().toUpperCase()
            ).replaceAll("")
        ).replaceAll("");
        return source.isEmpty() ? null : source;
    }

    /**
     * This method replaces russian surname suffixes by given pattern with corresponding symbols
     *
     * @param source Source string to replace suffixes in
     * @param patternMap Map with suffixes patterns and replacements
     * @return String with replaced suffixes
     */
    private static String replaceSuffixes(final String source, final Map<Pattern, String> patternMap) {
        for (final Entry<Pattern, String> entry : patternMap.entrySet()) {
            final Matcher matcher = entry.getKey().matcher(source);
            if (matcher.find()) {
                return matcher.replaceFirst(entry.getValue());
            }
        }
        return source;
    }

    /**
     * This method replaces russian vowels and vowels rows with predefined vowels
     * (restricting all vowels to three different)
     *
     * @param source The source string
     * @return The replaced result
     */
    private static String replaceVowels(final String source) {
        String result = source;
        for (final Entry<Pattern, String> entry : VOWELS_MAP.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(entry.getValue());
        }
        return result;
    }
}
