package cz.cvut.sempipes.modules;

import org.junit.Assert;
import org.junit.Test;

public class UserQueryTest {

    @Test
    public void parseOneYearTwoKeywords() throws Exception {
        String s = "zakon osoba 1990";

        UserQuery q  = UserQuery.parse(s);
        Assert.assertEquals(1, q.getDates().size());
        Assert.assertEquals(1990, 1900+q.getDates().iterator().next().getYear());
        Assert.assertEquals(2, q.getKeywords().size());
    }

    @Test
    public void parseOneDayTwoKeywords() throws Exception {
        String s = "zakon 1.1.2015";

        UserQuery q  = UserQuery.parse(s);
        Assert.assertEquals(1, q.getDates().size());
        Assert.assertEquals(1, q.getDates().iterator().next().getDate());
        Assert.assertEquals(1, 1+q.getDates().iterator().next().getMonth());
        Assert.assertEquals(2015, 1900+q.getDates().iterator().next().getYear());
        Assert.assertEquals(1, q.getKeywords().size());
    }

    @Test
    public void keywordRegex() throws Exception {
        String s = "zakon osoba 1990";
        UserQuery q  = UserQuery.parse(s);
        Assert.assertEquals("(zakon)|(osoba)", q.getKeywordRegex());
    }
}