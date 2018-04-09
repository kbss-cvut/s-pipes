package cz.cvut.sempipes.sutime;


import java.util.Calendar;

public class AnnforModel {

    private Calendar dateBegin;
    private Calendar dateEnd;
    private String dateType;
    private String dateExtracted;

    public Calendar getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Calendar dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public AnnforModel(Calendar dateBegin, Calendar dateEnd, String dateType, String dateExtracted) {

        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.dateType = dateType;
        this.dateExtracted = dateExtracted;
    }

    @Override
    public String toString() {
        return "AnnforModel{" +
                "dateBegin=" + dateBegin +
                ", dateEnd=" + dateEnd +
                ", dateType='" + dateType + '\'' +
                ", dateExtracted='" + dateExtracted + '\'' +
                '}';
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }


    public String getDateExtracted() {
        return dateExtracted;
    }

    public void setDateExtracted(String dateExtracted) {
        this.dateExtracted = dateExtracted;
    }
}
