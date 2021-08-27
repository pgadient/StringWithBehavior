package logic;

import java.util.ArrayList;
import java.util.Arrays;

public class AccessRestrictionLogic implements IStringLogic {
    private ArrayList<String> unauthorizedPackages = new ArrayList<String>(Arrays.asList("java.net", "java.io"));

    public String applyOnRead(String s) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int i = 1 ; i < stackTraceElements.length ; i++) {
            for(String pack: unauthorizedPackages) {
                if(stackTraceElements[i].getClassName().startsWith(pack)){
                    throw new StringNotMatchingLogicException();
                }
            }
        }
        return s;
    }

    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    public boolean recordHistory() {
        return false;
    }

    public String getDescription() {
        return "Logic that throws an exception if the String is read in an unauthorized class.";
    }
}
