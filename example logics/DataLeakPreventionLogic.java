package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataLeakPreventionLogic implements IStringLogic {
    public static LeakAction securityLevel = LeakAction.NOTHING;

    public enum LeakAction {
        NOTHING,
        LOG,
        BLOCK
    }

    private Logger logger;  
    private FileHandler fh;
    private LeakAction level;
    private ArrayList<String> outputPackages;

    public DataLeakPreventionLogic(LeakAction level){
        this.level = level;
        this.outputPackages = new ArrayList<String>(Arrays.asList("java.net", "java.io"));
        this.logger = Logger.getLogger(this.getClass().getName());
        try{
            this.fh = new FileHandler(this.getClass().getName()+".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public DataLeakPreventionLogic(){
        this.level = securityLevel;
        this.outputPackages = new ArrayList<String>(Arrays.asList("java.net", "java.io"));
        this.logger = Logger.getLogger(this.getClass().getName());
        try{
            this.fh = new FileHandler(this.getClass().getName()+".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public String applyOnRead(String s) {
        if(level.equals(LeakAction.NOTHING)) return s;

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int i = 1; i < stackTraceElements.length; i++) {
			if(!canIgnore(stackTraceElements[i])) {
				// String methodDefinition = stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName();
                for(String op: outputPackages) {
                    if(stackTraceElements[i].getClassName().startsWith(op)){
                        switch(level){
                            case BLOCK:
                                throw new StringNotMatchingLogicException("The String is not allowed to be leaked in an I/O class! (found inside " + stackTraceElements[i].getClassName() + ")");
                            case LOG:
                                logLeak(stackTraceElements[i].getClassName(), stackTraceElements);
                                return s;
                            case NOTHING: break;
                        }
                    }
                }
			}
		}
        return s;
    }

    private void logLeak(String className, StackTraceElement[] stackTraceElements) {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("String was leaked inside class "+className);
        for(StackTraceElement ele : stackTraceElements)
            sj.add(ele.toString());
        logger.warning(sj.toString());
    }

    private boolean canIgnore(StackTraceElement element){
        if(element.getClassName().equals(this.getClass().getName())) return true;
        for(String op: outputPackages)
            if(element.getClassName().startsWith(op))
                return false;
        if(element.getModuleName() == null) return false;
		if(element.getModuleName().equals("java.base")) return true;
		return false;
	}

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
        return "Encryption has to be performedwith specific APIs at certain places in the code. "
                + "With ourapproach, we can track whether the content is about to leavethe system and "
                + "then encrypt the content before it leaves thesystem. For example, a String object "
                + "with behavior containingsensitive information can be accessed in the entire application "
                + "like an ordinary String, but when it is accessed in classes thatsend information over "
                + "network or store it on disk the data willbe transparently encrypted before it leaves "
                + "the memory. Adeveloper does not any longer need to care about encryptingwhich data, "
                + "instead each String itself knows whether it needsto be encrypted. For example, a "
                + "String containing a passwordwould only leak it securely in an encrypted manner.";
    }
    
}
