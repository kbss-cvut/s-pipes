package cz.cvut.spipes.modules.template;

import cz.cvut.spipes.modules.exception.InvalidTemplateException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the first two levels of RFC6570.
 * <p>
 * This implementation of the String Template has been heavily inspired by the one that is part of the
 * project Linkedpipes ETL developed by the Linkedpipes ETL Team
 * (<a href="https://etl.linkedpipes.com/team/">ETL Team</a>).
 *
 * @see <a href="https://tools.ietf.org/html/rfc6570#section-2">RFC6570 section 2</a>
 * @see <a href="https://etl.linkedpipes.com/">Linkedpipes ETL</a>
 */
public class StringTemplate {

    public static final String TABLE_RESOURCE_REF = "__A83N48X1_TABLE_URI__";

    private interface Token {

        String process(List<String> row);
    }

    /**
     * The template is just a fixed string (aka level 0).
     */
    private static class TokenString implements Token {

        private final String string;

        private TokenString(String string) {
            this.string = string;
        }

        @Override
        public String process(List<String> row) {
            return string;
        }
    }

    /**
     * Level 1 template.
     * Values in URI are encoded to make valid URL.
     */
    private static class TokenSimpleExpansion implements Token {

        private final int index;

        private TokenSimpleExpansion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            final String value = row.get(index);

            if (value == null) {
                return null;
            }

            return encodeString(value);
        }
    }

    /**
     * Level 2 template.
     * Values starting with operator {@code +} can include reserved URI characters.
     */
    private static class TokenReservedExpansion implements Token {

        private final int index;

        private TokenReservedExpansion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            return row.get(index);
        }
    }

    /**
     * Level 2 template.
     * Character {@code #} at the start of the value is preserved in its place.
     */
    private static class TokenFragmentExpansion implements Token {

        private final int index;

        private TokenFragmentExpansion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            final String value = row.get(index);

            if (value == null) {
                return null;
            }

            return "#" + encodeString(value);
        }
    }

    private final String template;
    private final List<Token> tokens = new LinkedList<>();

    StringTemplate(String template) {
        this.template = template;
    }

    /**
     * Initializes this template with the URI and header of the table.
     *
     * @param tableUri URI of currently parsed table.
     * @param header Names of columns headers.
     */
    public void initialize(String tableUri, List<String> header) throws InvalidTemplateException {
        tokens.clear();

        // Parse inner template;
        String toParse = template;
        while (!toParse.isEmpty()) {
            int left = indexOfUnescape(toParse, '{');
            int right = indexOfUnescape(toParse, '}');

            if (left == -1 && right == -1) {
                tokens.add(new TokenString(toParse));
                break;
            }

            // There is { or } in the string.
            if (right == -1 || (left != -1 && left < right)) {
                // { -> string
                final String value = toParse.substring(0, left);
                toParse = toParse.substring(left + 1);

                if (!value.isEmpty()) {
                    tokens.add(new TokenString(value));
                } else {
                    // It can be empty, if for example, string starts
                    // with { or there is }{ as substring
                }
            } else if (left == -1 || (right != -1 && right < left)) {
                // } --> name
                String name = toParse.substring(0, right);
                // Revert escaping of { } in the pattern.
                name = name.replaceAll("\\\\\\{", "\\{")
                        .replaceAll("\\\\}", "\\}");
                toParse = toParse.substring(right + 1);

                // Now name contains the pattern, so we can create token.
                if (name.equals(TABLE_RESOURCE_REF)) {
                    // Special token with table resource URI.
                    tokens.add(new TokenString(tableUri));
                } else {
                    tokens.add(createToken(name, header));
                }
            } else {
                throw new InvalidTemplateException("Invalid template '" + template + "'");
            }
        }
    }

    /**
     * Processes all the tokens in this template and returns the corresponding URI for the given row.
     *
     * @return final URI as a {@link String}
     */
    public String process(List<String> row) {
        final StringBuilder result = new StringBuilder(20);

        for (Token token : tokens) {
            String newString = token.process(row);

            if (newString == null) {
                // If any token returns null, we do not publish - i.e. we
                // assume all to be mandatory.
                return null;
            } else {
                result.append(newString);
            }
        }

        return result.toString();
    }

    private static String encodeString(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    /**
     * Return index of first un-escaped occurrence of given character in given
     * string.
     */
    private static int indexOfUnescape(String str, char toFind) {
        for (int i = 0; i < str.length(); ++i) {
            final char current = str.charAt(i);
            if (current == toFind) {
                // we find the one
                return i;
            } else if (current == '\\') {
                // skip next
                i++;
            }
            // continue the search
        }
        // not found
        return -1;
    }

    /**
     * Create a token based on the template.
     */
    private static Token createToken(String template, List<String> header)
            throws InvalidTemplateException {
        if (template.startsWith("+")) {
            return new TokenReservedExpansion(getIndexForTemplate(
                    template.substring(1), header));
        } else if (template.startsWith("#")) {
            return new TokenFragmentExpansion(getIndexForTemplate(
                    template.substring(1), header));
        } else {
            return new TokenSimpleExpansion(getIndexForTemplate(
                    template, header));
        }
    }

    /**
     * Find the index of the given name in the header.
     */
    private static int getIndexForTemplate(String template, List<String> header)
            throws InvalidTemplateException {
        int value = header.indexOf(template);

        if (value == -1) {
            throw new InvalidTemplateException("Missing template in header '" + template + "'");
        } else {
            return value;
        }
    }
}
