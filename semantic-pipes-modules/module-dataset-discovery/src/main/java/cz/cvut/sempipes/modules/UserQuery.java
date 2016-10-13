package cz.cvut.sempipes.modules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserQuery {
    private static final Map<String, String> DATE_FORMAT_REGEXPS;

    static {
        DATE_FORMAT_REGEXPS = new HashMap<String,String>() {{
            put("^([1-2]\\d{3})|(\\d{1,3})$", "yyyy");
            put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "dd.MM.yyyy");
            put("^\\d{1,2}\\/\\d{4}$", "MM/yyyy");
        }};
    }

    private List<Date> dates = new ArrayList<>();

    private List<String> keywords = new ArrayList<>();

    public List<Date> getDates() {
        return dates;
    }

    public List<String> getKeywords() {
        return keywords;
    }
    public String getKeywordRegex() {
        Stream<String> stream = getKeywords().stream().map(s -> "(" +s +")" );
        String regex = stream.collect(Collectors.joining("|"));
        if (regex.isEmpty()) {
            regex=".*";
        }
        return regex;
    }

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
                }
            }
            if (!isDate) {
                userQuery.getKeywords().add(keyword);
            }
        }

        return userQuery;
    }
}
