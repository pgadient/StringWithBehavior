package logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimitiveObsessionLogLogic implements IStringLogic {
	private final Path path = Paths.get("/home/jacktraror/Desktop/logs/POLlog.log");
	
	public PrimitiveObsessionLogLogic(){
		try {
			Path pathParent = path.getParent();
			if (!Files.exists(pathParent)) {
				Files.createDirectories(pathParent);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
    private void writeToFile(String s) {
        try {
			List<String> list = new ArrayList<String>(Arrays.asList(s));
			for(StackTraceElement ste : Thread.currentThread().getStackTrace())
	            list.add(ste.toString());
			list.add("\n");
			Files.write(path, list, StandardCharsets.UTF_8,
				Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
		} catch (final IOException e) {
			e.printStackTrace();
		}
    }

    public String applyOnRead(String s) {
        return s;
    }

    public String applyOnCreation(String s) {
        if(s.matches("(\\d+)|(\\.\\d+)|(\\d*\\.\\d+)"))
			writeToFile(s);
        return s;
    }

    public String getDescription() {
        return "Writes log whenever it encounters a string over the threashhold";
    }

    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }
    
    public boolean recordHistory() {
        return false;
    }
}
