package java.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
* The {@code StringLogicController} helps assigning StringLogic to all strings initialized inside a specific class.
*
* @author  Christian Zuercher
*/
public class StringLogicController {
	/**
	 * Logics that are class specific (applied to all strings in this class only)
	 */
	private static Map<String, IStringLogic> specificLogics = new HashMap<>();
	
	/**
	 * Check if class already initialized its logics
	 */
	private static boolean initialized = false;
	
	/**
	 * Check for logics in parameters
	 */
	// private static void initialize() {
	// 	System.out.println("In initialized");
		
	// 	initialized = true;
	// 	String logics = System.getProperty("ch.unibe.scg.cz.stringLogics");
	// 	System.out.println(logics);

	// 	if(logics != null) {
	// 		String[] logicArray = logics.split(";");
	//         for(String logic: logicArray) {
	//         	String[] elements = logic.split(",");
	//         	if(elements.length > 1) {
	// 				try {
	// 					Class<?> clazz = Class.forName(elements[0]);
	// 					if(clazz != null){
	// 						IStringLogic l = (IStringLogic)clazz.getDeclaredConstructor().newInstance();
	// 						for(int i = 1 ; i < elements.length; i++){
	// 							addClassLogic(elements[i], l);
	// 							System.out.println(elements[0] + " applied on "+elements[i]);
	// 						}
	// 					}
	// 				} catch(Exception e) {
	// 					e.printStackTrace();
	// 				}
	//         	}
	//         }
	// 	}
	// }
	private static void initialize() {
		initialized = true;
		String path = System.getProperty("ch.unibe.scg.cz.stringLogics");

		if(path == null || path.isEmpty()) return;

		System.out.println("In initialized");
		List<String> logics = loadFile(path+"/config.csv");
		System.out.println(logics);

		for(String logicString: logics) {
			String[] elements = logicString.split(",");
			if(elements.length > 1) {
				try {
					Class<?> clazz = loadClass(path+"/",elements[0]);
					if(clazz != null){
						IStringLogic l = (IStringLogic) clazz.getDeclaredConstructor().newInstance();
						for(int i = 1 ; i < elements.length; i++){
							addClassLogic(elements[i], l);
							System.out.println(elements[0] + " applied on "+elements[i]);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Load a csv file
	 * @param filePath file path of csv
	 * @return the list of logics as String
	 */
	private static List<String> loadFile(String filePath) {
		List<String> logics = new ArrayList<String>();
        Path pathToFile = Paths.get(filePath);

        // create an instance of BufferedReader
        // using try with resource, Java 7 feature to close resources
        try (BufferedReader br = Files.newBufferedReader(pathToFile)) {

            // read the first line from the text file
            String line = br.readLine();

            // loop until all lines are read
            while (line != null) {
                logics.add(line);

                // read next line before looping
                // if end of file reached, line would be null
                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return logics;
	}

	/**
	 * Load a class
	 * @param path class path
	 * @param definition class definition
	 * @return the loaded class
	 */
	private static Class<?> loadClass(String path, String definition) {
		// Create a File object on the root of the directory containing the class file
		File file = new File(path);

		try {
			// Convert File to a URL
			URL url = file.toURI().toURL();          // file:/path
			URL[] urls = new URL[]{url};

			// Create a new class loader with the directory
			ClassLoader cl = new URLClassLoader(urls);

			// Load in the class; MyClass.class should be located in
			// the directory file:/path/definition
			return cl.loadClass(definition);
		} catch (MalformedURLException | ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Add a string logic to a class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @param logic the StringLogic to add to the class
	 */
	public static void addClassLogic(String classDefinition, IStringLogic logic) {
		specificLogics.put(classDefinition, logic);
	}
	
	/**
	 * Get the string logic for a given class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @return the string logic of the class or null.
	 */
	public static IStringLogic getClassLogic(String classDefinition) {
		if(!initialized) initialize();
		if(specificLogics.containsKey(classDefinition))
			return specificLogics.get(classDefinition);
		return null;
	}

	/**
	 * Get the string logic from a given stack trace
	 * 
	 * @param stackTraceElements stack trace to be searched in
	 * @return the string logic of the stack trace or null.
	 */
	public static IStringLogic getLogicFromStackTrace(StackTraceElement[] stackTraceElements) {
		if(!initialized) initialize();
		for(int i = 1; i < stackTraceElements.length; i++) {
			if(!stackTraceElements[i].getClassName().equals("java.lang.String")) {
				return StringLogicController.getClassLogic(stackTraceElements[i].getClassName());
			}
		}

		return null;
	}
}