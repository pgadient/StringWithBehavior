package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataLeakLogLogic implements IStringLogic {
    private Logger logger;  
    private FileHandler fh;
    private ArrayList<String> outputPackages;

    public DataLeakLogLogic(String className) {
        this.outputPackages = new ArrayList<String>(Arrays.asList("java.net", "java.io"));
        this.logger = Logger.getLogger(className);
        try{
            this.fh = new FileHandler(className+".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public String applyOnRead(String s) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int i = 1; i < stackTraceElements.length; i++) {
			if(!canIgnore(stackTraceElements[i])) {
                for(String op: outputPackages) {
                    if(stackTraceElements[i].getClassName().startsWith(op)){
                        StringJoiner sj = new StringJoiner("\n");
                        sj.add("String was leaked inside class "+stackTraceElements[i].getClassName());
                        for(StackTraceElement ele : stackTraceElements)
                            sj.add(ele.toString());
                        logger.warning(sj.toString());
                        return s;
                    }
                }
			}
		}
        return s;
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
        return "...";
    }
    
}
