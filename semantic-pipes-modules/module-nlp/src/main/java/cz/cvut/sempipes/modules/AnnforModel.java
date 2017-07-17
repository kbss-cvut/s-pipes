package cz.cvut.sempipes.modules;


public class AnnforModel {
    public String getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(String dateBegin) {
        this.dateBegin = dateBegin;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public AnnforModel(String dateBegin, String dateEnd, String dateType, String dateExtraction) {
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.dateType = dateType;
        this.dateExtracted = dateExtraction;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    private String dateBegin;
    private String dateEnd;
    private String dateType;
    private String dateExtracted;

    public String getDateExtracted() {
        return dateExtracted;
    }

    public void setDateExtracted(String dateExtracted) {
        this.dateExtracted = dateExtracted;
    }
}
