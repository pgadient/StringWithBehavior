package logic;

public class RegexLogic implements IStringLogic {
    private final String regex;    

    public RegexLogic(String regex) {
        this.regex = regex;
    }

    public String applyOnCreation(String s) {
        if(!s.matches(regex))
            throw new StringNotMatchingLogicException();
        return s;
    }

    public String applyOnRead(String s) {
        return s;
    }

    public String getDescription() {
        return regex;
    }

    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    public boolean recordHistory() {
        return true;
    }
}
