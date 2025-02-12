package cz.cvut.spipes;

import org.supercsv.io.Tokenizer;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;

/**
    * <p> This class is a custom implementation of the Tokenizer interface
    * that allows to parse CSV and TSV files with invalid quoting.
    * Any quotes not next to a delimiter or at the start/end of the line should be escaped.</p>
    * <p>Example of invalid quoting in the CSV:
    *  <table>
    *      <thead>
    *          <td>Company Name</td>
    *          <td>Product</td>
    *      </thead>
    *   <tr>
    *     <td>"Albanese Confectionery", </td>
    *     <td>"FRUIT WORMS 2" 4/5LB"</td>
    *     <td>     <--- Invalid quoting</td>
    *   </tr>
    *   <tr>
    *    <td>"Albanese Confectionery", </td>
    *      <td>"FRUIT WORMS 2"" 4/5LB"</td>
    *       <td><--- Valid quoting</td>
    *    </tr>
    * </table>
    * </p>
    * <p> The tokenizer is compliant with following formats: CSV, TSV </p>
    * <p>Notes:
    * In the TSV standard, there is no mention of quotes, but in this implementation, we process
    * the TSV quotes the same way as the CSV quotes.
    * </p>
    * @see <a href="https://www.rfc-editor.org/rfc/rfc4180">CSV</a>
    * @see <a href="https://www.iana.org/assignments/media-types/text/tab-separated-values">TSV</a>

 */
public class InvalidQuotingTokenizer extends Tokenizer {

    public InvalidQuotingTokenizer(Reader reader, CsvPreference preferences) {
        super(reader, preferences);
    }

    @Override
    protected String readLine() throws IOException {
        String line = super.readLine();
        if (line == null) {
            return null;
        }

        final char quote = getPreferences().getQuoteChar();
        final char delimiter = (char) getPreferences().getDelimiterChar();

        // Handle multi-line quoted columns
        boolean inQuotes = false;
        StringBuilder result = new StringBuilder();

        do {
            if (inQuotes) {
                result.append('\n');
            }

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == quote) {
                    boolean isCorrectQuote = (i == 0 || line.charAt(i - 1) == quote || line.charAt(i - 1) == delimiter) ||
                            (i == line.length() - 1 || line.charAt(i + 1) == quote || line.charAt(i + 1) == delimiter);

                    if (isCorrectQuote) {
                        // Toggle the inQuotes flag
                        inQuotes = !inQuotes;
                    } else {
                        // Close the quote
                        result.append(quote);
                    }
                }
                result.append(c);
            }

            if (inQuotes) {
                line = super.readLine();
            }
        } while (inQuotes && line != null);

        return result.toString();
    }

}