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

public class SQLdetectionLogLogic implements IStringLogic  {
    private final Path path = Paths.get("/home/jacktraror/Desktop/logs/SQLiLog.log");
	
	public SQLdetectionLogLogic(){
		try {
			Path pathParent = path.getParent();
			if (!Files.exists(pathParent)) {
				Files.createDirectories(pathParent);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
    private void writeToFile(String title, String s) {
        try {
			List<String> list = new ArrayList<String>(Arrays.asList(title,s));
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
        if(s.matches("(?i)\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|SELECT|UPDATE|UNION( +ALL){0,1})\\b.*"))
            writeToFile("Possible SQL statement detected",s);
        if(s.matches(".*'(''|[^'])*'.*") || s.matches(".*\"(\"\"|[^\"])*\".*"))
            writeToFile("Possible text block injection detected",s);
        if(s.matches("\\s*'*\\s*(?i)\\bOR\\b.*=.*"))
            writeToFile("Possible injection with 'or =' detected",s);
        if(s.matches(".*(\\s*;\\s*)+(?i)\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|SELECT|UPDATE|UNION( +ALL){0,1})\\b.*"))
            writeToFile("Possible SQL injection detected",s);
        return s;
    }

    public String getDescription() {
        return "Writes log whenever it encounters something related to SQL injection";
    }

    public boolean inheritToChild(StringTransformType stt) {
        return true;
    }

    public boolean recordHistory() {
        return false;
    }
}
