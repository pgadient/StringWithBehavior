package cz.logics;

import java.util.Arrays;

public class RestrictedReadLogic implements IStringLogic {
    private final String[] allowedClasses, ignoreClasses;

    public RestrictedReadLogic(String[] allowedClasses, String[] ignoreClasses) {
        this.allowedClasses = allowedClasses;
        this.ignoreClasses = new String[ignoreClasses.length + 2];
        this.ignoreClasses[0] = "java.lang.String";
        this.ignoreClasses[1] = "cz.logics.RestrictedReadLogic";
        System.arraycopy(ignoreClasses, 0, this.ignoreClasses, 2, ignoreClasses.length);
        System.out.println(Arrays.toString(this.ignoreClasses));
    }

    public RestrictedReadLogic(String[] allowedClasses) {
        this.allowedClasses = allowedClasses;
        this.ignoreClasses = new String[] {"java.lang.String", "cz.logics.RestrictedReadLogic"};
    }

    @Override
    public String applyBeforeToString(String s) throws StringNotMatchingLogicException {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for(int i = 1; i < trace.length; i++){
            boolean ignore = false;
            for(int j = 0; j < ignoreClasses.length; j++) {
                if(ignoreClasses[j].equals(trace[i].getClassName())) ignore = true;
            }
            if(!ignore) {
                for(int j = 0; j < allowedClasses.length; j++) {
                    //throw error if any unauthorized class is in trace until allowed class
                    //if(allowedClasses[j].equals(trace[i].getClassName())) throw new StringNotMatchingLogicException("String was leaked in unauthorized class");
                    
                    // ok if allowed class is in trace
                    if(allowedClasses[j].equals(trace[i].getClassName())) return null;
                }
            }
        }
        throw new StringNotMatchingLogicException("String was leaked in unauthorized class");
    }

    @Override
    public boolean applyOnInitialization(String s) throws StringNotMatchingLogicException {
        return true;
    }

    @Override
    public String getDescription() {
        return "Logic that prohibits toString in wrong contextes.";
    }

    @Override
    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    @Override
    public boolean recordHistory() {
        return false;
    }
    
}