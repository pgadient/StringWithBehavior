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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* The {@code StringLogicController} helps assigning StringLogic to all strings initialized inside a specific class.
*
* @author  Christian Zuercher
*/
public final class StringLogicController {
	/*
	 * Constructor is hidden since this class has only static methods and fields
	 */
	private StringLogicController() {}
	
	/**
	 * Logics that are package specific (applied to all strings in this package only)
	 */
	private static Map<String, IStringLogic> specificPackageLogics = new HashMap<>();

	/**
	 * Logics that are class specific (applied to all strings in this class only)
	 */
	private static Map<String, IStringLogic> specificClassLogics = new HashMap<>();

	/**
	 * Logics that are method specific (applied to all strings in this method only)
	 */
	private static Map<String, IStringLogic> specificMethodLogics = new HashMap<>();
	
	/**
	 * Check if class already initialized its logics
	 */
	private static boolean initialized = false;
	
	/**
	 * Parser class for XML files
	 */
	private static class XMLParser {
		/** List of all logics defined in the xml file */
		private static List<Logic> logics = new ArrayList<Logic>();
		/** The current logic that is read */
		private static Logic currentLogic;
		/** the current value that is read */
		private static String elementValue;
	
		/** int value of < */
		private static final int TAG_START = '<';
		/** int value of > */
		private static final int TAG_END = '>';
		/** int value of / */
		private static final int CLOSE_TAG_IDENTIFIER = '/';
		
		/**
		 * Parses the xml file
		 * @param configFilePath path to xml file
		 */
		public static void parse(Path configFilePath) {
			try (BufferedReader br = Files.newBufferedReader(configFilePath)) {
				StringBuilder sb = new StringBuilder();
				boolean reachedEnd = false;
				int state = 0;
				boolean inTagName = false;
				boolean isCloseTag = false;
				
				while(!reachedEnd) {
					int c = br.read();
					if(c == -1) break;
					if(c == '\r' || c == '\n' || c == '\t' || c == '\b') continue;
					switch(state) {
					case 0: //wait for first tag start
						if(c == TAG_START) {
							state = 1;
							inTagName = true;
						}
						break;
					case 1: //record tag name
						switch(c) {
						case TAG_END:
							state = 2;
							if(inTagName) {
								if(isCloseTag) {
									endElement(sb.toString());
									isCloseTag = false;
								} else {
									startElement(sb.toString());
								}
							} else {
								elementValue = sb.toString();
							}
							sb = new StringBuilder();
							break;
						default: 
							sb.append((char)c);
							break;
						}
						break;
					case 2: // wait for tag start or values
						if(c == TAG_START) { 
							state = 3;
							break;
						}
						sb.append((char)c);
						break;
					case 3:
						if(c == CLOSE_TAG_IDENTIFIER) {
							elementValue = sb.toString();
							sb = new StringBuilder();
							isCloseTag = true;
						} else {
							sb = new StringBuilder();
							sb.append((char)c);
						}
						state = 1;
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		/**
		 * 
		 * @param qName the name of the current tag
		 */
		private static void startElement(String qName) {
			switch(qName) {
			case "logic":
				currentLogic = new Logic();
				logics.add(currentLogic);
				break;
			}
		}
	
		/**
		 * 
		 * @param qName the name of the current tag
		 */
		private static void endElement(String qName) {
			switch(qName) {
			case "name":
				currentLogic.name = elementValue;
				break;
			case "package":
				currentLogic.packages.add(elementValue);
				break;
			case "class":
				currentLogic.classes.add(elementValue);
				break;
			case "method":
				currentLogic.methods.add(elementValue);
				break;
			}
		}
		
		/**
		 * Internal Logic identifier that contains the logic description and the places it should be applied to
		 */
		private static class Logic {
			/** Logic identifier with package and class name */
			String name;
			/** Packages it should be applied to */
			List<String> packages = new ArrayList<String>();
			/** Classes it should be applied to */
			List<String> classes = new ArrayList<String>();
			/** Methods it should be applied to */
			List<String> methods = new ArrayList<String>();
		}
	}
	
	/**
	 * Check for logics in parameters
	 */
	private static void initialize() {
		try	{
			String configDirPath = System.getProperty("java.lang.stringLogics".ignoreLogics(true));
			if(configDirPath == null || configDirPath.isEmpty()) return;

			initialized = true;

			File configFile = new File(configDirPath + File.separator + "config.xml".ignoreLogics(true));
			XMLParser.parse(configFile.toPath());

			for(XMLParser.Logic logic: XMLParser.logics) {
				try {
					Class<?> clazz = loadClass(configDirPath+File.separator,logic.name);
					if(clazz != null){
						for(String packageName: logic.packages) {
							IStringLogic l = (IStringLogic) clazz.getDeclaredConstructor().newInstance();
							addPackageLogic(packageName, l);
							System.out.println(logic.name + " applied on ".ignoreLogics(true)+packageName);
						}
						for(String className: logic.classes) {
							IStringLogic l = (IStringLogic) clazz.getDeclaredConstructor().newInstance();
							addClassLogic(className, l);
							System.out.println(logic.name + " applied on ".ignoreLogics(true)+className);
						}
						for(String methodName: logic.methods) {
							IStringLogic l = (IStringLogic) clazz.getDeclaredConstructor().newInstance();
							addMethodLogic(methodName, l);
							System.out.println(logic.name + " applied on ".ignoreLogics(true)+methodName);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(NullPointerException e) { }
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
	 * Checks if there are any logics in the controller
	 * @return true if there are any values in the maps
	 */
	public static boolean isEmpty() {
		if(!initialized) initialize();
		return specificPackageLogics.isEmpty() && specificClassLogics.isEmpty() && specificMethodLogics.isEmpty();
	}
	
	/**
	 * Add a string logic to a package
	 * 
	 * @param packageDefinition complete definition of the package
	 * @param logic the StringLogic to add to the package
	 */
	public static void addPackageLogic(String packageDefinition, IStringLogic logic) {
		packageDefinition.ignoreLogics(true);
		specificPackageLogics.put(packageDefinition, logic);
	}
	
	/**
	 * Add a string logic to a class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @param logic the StringLogic to add to the class
	 */
	public static void addClassLogic(String classDefinition, IStringLogic logic) {
		classDefinition.ignoreLogics(true);
		specificClassLogics.put(classDefinition, logic);
	}
	
	/**
	 * Add a string logic to a method
	 * 
	 * @param methodDefinition complete definition of the method (package + class name + methodName)
	 * @param logic the StringLogic to add to the method
	 */
	public static void addMethodLogic(String methodDefinition, IStringLogic logic) {
		methodDefinition.ignoreLogics(true);
		specificMethodLogics.put(methodDefinition, logic);
	}
	
	/**
	 * Get the string logic for a given package
	 * 
	 * @param packageDefinition complete definition of the package
	 * @return the string logic of the package or its parents or null.
	 */
	public static IStringLogic getPackageLogic(String packageDefinition) {
		if(!initialized) initialize();
		packageDefinition.ignoreLogics(true);
		// Test
		for(String s: specificPackageLogics.keySet()) 
			if(match(packageDefinition, s))
				return specificPackageLogics.get(s);
		// End Test
		// if(specificPackageLogics.containsKey(packageDefinition))
		// 	return specificPackageLogics.get(packageDefinition);
		if(!packageDefinition.contains(".".ignoreLogics(true))) return null;
		if(packageDefinition.contains(".".ignoreLogics(true)))
			return getPackageLogic(packageDefinition.substring(0,packageDefinition.lastIndexOf(".".ignoreLogics(true))));
		return null;
	}
	
	/**
	 * Get the string logic for a given class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @return the string logic of the class or the package or null.
	 */
	public static IStringLogic getClassLogic(String classDefinition) {
		if(!initialized) initialize();
		classDefinition.ignoreLogics(true);
		// Test
		for(String s: specificClassLogics.keySet()) 
			if(match(classDefinition, s))
				return specificClassLogics.get(s);
		// End Test
		// if(specificClassLogics.containsKey(classDefinition))
		// 	return specificClassLogics.get(classDefinition);
		if(classDefinition.contains(".".ignoreLogics(true)))
			return getPackageLogic(classDefinition.substring(0,classDefinition.lastIndexOf(".".ignoreLogics(true))));
		return null;
	}
	
	/**
	 * Get the string logic for a given method
	 * 
	 * @param methodDefinition complete definition of the class (package + class name + methodName)
	 * @return the string logic of the method or the class or the package or null.
	 */
	public static IStringLogic getMethodLogic(String methodDefinition) {
		if(!initialized) initialize();
		methodDefinition.ignoreLogics(true);
		// Test
		for(String s: specificMethodLogics.keySet()) 
			if(match(methodDefinition, s))
				return specificMethodLogics.get(s);
		// End Test
		// if(specificMethodLogics.containsKey(methodDefinition))
		// 	return specificMethodLogics.get(methodDefinition);
		if(methodDefinition.contains(".".ignoreLogics(true)))
			return getClassLogic(methodDefinition.substring(0,methodDefinition.lastIndexOf(".".ignoreLogics(true))));
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
			if(!canIgnore(stackTraceElements[i])) {
				String methodDefinition = (stackTraceElements[i].getClassName().ignoreLogics(true) + ".".ignoreLogics(true) + stackTraceElements[i].getMethodName().ignoreLogics(true)).ignoreLogics(true);
				return StringLogicController.getMethodLogic(methodDefinition);
				// return StringLogicController.getClassLogic(stackTraceElements[i].getClassName());
			}
		}

		return null;
	}

	/**
	 * Checks whether that class should be ignored or not
	 * @param className to check
	 * @return whether that class should be ignored or not
	 */
	private static boolean canIgnore(StackTraceElement element){
		if("java.base".ignoreLogics(true).equals(element.getModuleName())) return true;
		// if(className.equals("java.lang.String".ignoreLogics(true))) return true;
		// if(className.equals("java.lang.StringLogicController".ignoreLogics(true))) return true;
		return false;
	}

	/** Pattern to find all wildcard characters */
	private static Pattern wildcardRegex = Pattern.compile("[^*]+|(\\*)".ignoreLogics(true));
	
	/**
	 * Tries to match a string to a string with wildcards
	 * @param methodDefinition string to match
	 * @param matcherString a string that contains possible * wildcards
	 * @return if first string matches the second (with wildcards)
	 */
	private static boolean match(String methodDefinition, String matcherString) {
		
		Matcher m = wildcardRegex.matcher(matcherString);
		StringBuffer b = new StringBuffer();
		while (m.find()) {
			if(m.group(1) != null) m.appendReplacement(b, ".*".ignoreLogics(true));
			else m.appendReplacement(b, "\\\\Q".ignoreLogics(true) + m.group(0).ignoreLogics(true) + "\\\\E".ignoreLogics(true));
		}
		m.appendTail(b);
		String regexString = b.toString().ignoreLogics(true);

		return methodDefinition.matches(regexString);
	}
}