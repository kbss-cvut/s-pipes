package cz.cvut.spipes.modules;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Slf4j
public class UserQuery {

    private static final Map<String, String> DATE_FORMAT_REGEXPS;

    static {
        DATE_FORMAT_REGEXPS = new HashMap<String, String>() {
            {
                put("^([1-2]\\d{3})|(\\d{1,3})$", "yyyy");
                put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "dd.MM.yyyy");
                put("^\\d{1,2}\\/\\d{4}$", "MM/yyyy");
            }
        };
    }

    private List<Date> dates = new ArrayList<>();

    private List<String> keywords = new ArrayList<>();

    /**
     * Parses a free text input into a UserQuery object.
     *
     * @param userInput input text
     * @return user query
     */
    public static UserQuery parse(final String userInput) {
        final UserQuery userQuery = new UserQuery();

        final String[] keywords = userInput.split("\\s");

        for (final String keyword : keywords) {
            boolean isDate = false;
            for (String format : DATE_FORMAT_REGEXPS.keySet()) {
                if (!keyword.matches(format)) {
                    continue;
                }
                try {
                    DateFormat parser = new SimpleDateFormat(DATE_FORMAT_REGEXPS.get(format));
                    Date date = parser.parse(keyword);
                    userQuery.getDates().add(date);
                    isDate = true;
                    break;
                } catch (ParseException e) {
                    log.warn(MessageFormat.format(
                        "Date/time format {} matched {}, yet parsing into a Date object failed.",
                        format, keyword),e);
                }
            }
            if (!isDate) {
                userQuery.getKeywords().add(keyword);
            }
        }

        return userQuery;
    }

    public List<Date> getDates() {
        return dates;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Gets regular expression to match all keywords from the keywords list.
     *
     * @return the regular expression as a string
     */
    public String getKeywordRegex() {
        Stream<String> stream = getKeywords().stream().map(s -> "(" + s + ")");
        String regex = stream.collect(Collectors.joining("|"));
        if (regex.isEmpty()) {
            regex = ".*";
        }
        return regex;
    }
}
