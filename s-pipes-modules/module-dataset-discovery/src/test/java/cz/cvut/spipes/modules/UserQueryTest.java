package cz.cvut.spipes.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class UserQueryTest {

    @Test
    public void parseOneYearTwoKeywords() throws Exception {
        String s = "zakon osoba 1990";

        UserQuery q = UserQuery.parse(s);
        assertEquals(1, q.getDates().size());
        assertEquals(1990, 1900 + q.getDates().iterator().next().getYear());
        assertEquals(2, q.getKeywords().size());
    }

    @Test
    public void parseOneDayTwoKeywords() throws Exception {
        String s = "zakon 1.1.2015";

        UserQuery q = UserQuery.parse(s);
        assertEquals(1, q.getDates().size());
        assertEquals(1, q.getDates().iterator().next().getDate());
        assertEquals(1, 1 + q.getDates().iterator().next().getMonth());
        assertEquals(2015, 1900 + q.getDates().iterator().next().getYear());
        assertEquals(1, q.getKeywords().size());
    }

    @Test
    public void keywordRegex() throws Exception {
        String s = "zakon osoba 1990";
        UserQuery q = UserQuery.parse(s);
        assertEquals("(zakon)|(osoba)", q.getKeywordRegex());
    }
}