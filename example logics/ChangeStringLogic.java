package logic;

public class ChangeStringLogic implements IStringLogic {

    private final String replacement;

    public ChangeStringLogic(String replacement) {
        this.replacement = replacement;
    }

    public String applyOnRead(String s) { 
        return replacement;
    };

    public String applyOnCreation(String s) {
        return s;
    }

    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    public boolean recordHistory() {
        return true;
    }

    public String getDescription() {
        return "Changes the String to the replacement String and records the history. It is also inherited to everything.";
    }
    
}
