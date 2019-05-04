package cz.cvut.spipes.modules;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserQueryTest {

    @Test
    public void parseOneYearTwoKeywords() throws Exception {
        String s = "zakon osoba 1990";

        UserQuery q = UserQuery.parse(s);
        Calendar cal = getCalendar(q.getDates().iterator().next());
        assertEquals(1, q.getDates().size());
        assertEquals(1990, cal.get(Calendar.YEAR));
        assertEquals(2, q.getKeywords().size());
    }

    @Test
    public void parseOneDayTwoKeywords() throws Exception {
        String s = "zakon 1.1.2015";

        UserQuery q = UserQuery.parse(s);
        Calendar cal = getCalendar(q.getDates().iterator().next());
        assertEquals(1, q.getDates().size());
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, 1 + cal.get(Calendar.MONTH));
        assertEquals(2015, cal.get(Calendar.YEAR));
        assertEquals(1, q.getKeywords().size());
    }

    @Test
    public void keywordRegex() throws Exception {
        String s = "zakon osoba 1990";
        UserQuery q = UserQuery.parse(s);
        assertEquals("(zakon)|(osoba)", q.getKeywordRegex());
    }

    private Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}