package cz.cvut.spipes;

import org.supercsv.io.Tokenizer;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;

public class CustomTokenizer extends Tokenizer {

    public CustomTokenizer(Reader reader, CsvPreference preferences) {
        super(reader, preferences);
    }

    @Override
    protected String readLine() throws IOException {
        final String line = super.readLine();
        if (line == null) {
            return null;
        }

        final char quote = getPreferences().getQuoteChar();
        final char delimiter = (char) getPreferences().getDelimiterChar();

        // escape all quotes not next to a delimiter (or start/end of line)
        final StringBuilder b = new StringBuilder(line);
        for (int i = b.length() - 1; i >= 0; i--) {
            if (quote == b.charAt(i)) {
                final boolean validCharBefore = i - 1 < 0
                        || b.charAt(i - 1) == delimiter;
                final boolean validCharAfter = i + 1 == b.length()
                        || b.charAt(i + 1) == delimiter;
                if (!(validCharBefore || validCharAfter)) {
                    // escape that quote!
                    b.insert(i, quote);
                }
            }
        }
        return b.toString();
    }
}