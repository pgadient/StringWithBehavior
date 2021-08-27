package logic;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionOnDemandLogic implements IStringLogic {
    private ArrayList<String> outputPackages;
    private Cipher enCipher;
    private SecretKeySpec key;
    private String keyString = "a835zucnpq85tmcÜ093X,4tc£tCAÄ4WT9CAÖ";

    public EncryptionOnDemandLogic() {
        this.outputPackages = new ArrayList<String>(Arrays.asList("java.net", "java.io"));

        try {
            DESKeySpec dkey = new DESKeySpec(keyString.getBytes());
            key = new SecretKeySpec(dkey.getKey(), "DES");
            enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String applyOnRead(String s) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for(int i = 1; i < stackTraceElements.length; i++) {
			if(!canIgnore(stackTraceElements[i])) {
                for(String op: outputPackages) {
                    if(stackTraceElements[i].getClassName().startsWith(op)){
                        try {
                            enCipher.init(Cipher.ENCRYPT_MODE, key);
                            byte[] encrypted = enCipher.doFinal(s.getBytes());
                            return new String(encrypted, StandardCharsets.UTF_8);
                        } catch (Exception e) { e.printStackTrace(); }
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
        return false;
    }

    public String getDescription() {
        return "Data leaks occur when privateinformation leaves the system. "
                + "With our approach, we cantrack whether a String object with behavior "
                + "leaves the systemand act accordingly. Depending on the severity "
                + "level of theString, the system could report all accesses, or "
                + "only to thosethat leak the information outside of the system. "
                + "Moreover,instead of logging the String itself could also manipulate "
                + "itsvalue, or even terminate the whole application before leakingthe content.";
    }
    
}
