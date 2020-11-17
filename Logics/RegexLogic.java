package cz.logics;

public class RegexLogic implements IStringLogic {
    private final String regex;    

    public TestLogic(String regex) {
        this.regex = regex;
    }

    @Override public boolean applyOnInitialization(String s) throws StringNotMatchingLogicException {
        if(!s.matches(regex))
            throw new StringNotMatchingLogicException();
        return true;
    }

    @Override public String applyBeforeToString(String s) throws StringNotMatchingLogicException {
        return null;
    }

    @Override public String getDescription() {
        return regex;
    }

    @Override
    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    @Override
    public boolean recordHistory() {
        return true;
    }
}
