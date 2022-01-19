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
* The {@code StringBehaviorController} helps assigning StringBehavior to all strings initialized inside a specific class.
*
* @author  Christian Zuercher
*/
public final class StringBehaviorController {
	/*
	 * Constructor is hidden since this class has only static methods and fields
	 */
	private StringBehaviorController() {}
	
	/**
	 * Behaviors that are package specific (applied to all strings in this package only)
	 */
	private static Map<String, IStringBehavior> specificPackageBehaviors = new HashMap<>();

	/**
	 * Behaviors that are class specific (applied to all strings in this class only)
	 */
	private static Map<String, IStringBehavior> specificClassBehaviors = new HashMap<>();

	/**
	 * Behaviors that are method specific (applied to all strings in this method only)
	 */
	private static Map<String, IStringBehavior> specificMethodBehaviors = new HashMap<>();
	
	/**
	 * Check if class already initialized its behaviors
	 */
	private static boolean initialized = false;
	
	/**
	 * Parser class for XML files
	 */
	private static class XMLParser {
		/** List of all behaviors defined in the xml file */
		private static List<Behavior> behaviors = new ArrayList<Behavior>();
		/** The current behavior that is read */
		private static Behavior currentBehavior;
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
			case "behavior":
				currentBehavior = new Behavior();
				behaviors.add(currentBehavior);
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
				currentBehavior.name = elementValue;
				break;
			case "package":
				currentBehavior.packages.add(elementValue);
				break;
			case "class":
				currentBehavior.classes.add(elementValue);
				break;
			case "method":
				currentBehavior.methods.add(elementValue);
				break;
			}
		}
		
		/**
		 * Internal Behavior identifier that contains the behavior description and the places it should be applied to
		 */
		private static class Behavior {
			/** Behavior identifier with package and class name */
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
	 * Check for behaviors in parameters
	 */
	private static void initialize() {
		try	{
			String configDirPath = System.getProperty("java.lang.stringBehaviors".ignoreBehaviors(true));
			if(configDirPath == null || configDirPath.isEmpty()) return;

			initialized = true;

			File configFile = new File(configDirPath + File.separator + "config.xml".ignoreBehaviors(true));
			XMLParser.parse(configFile.toPath());

			for(XMLParser.Behavior behavior: XMLParser.behaviors) {
				try {
					Class<?> clazz = loadClass(configDirPath+File.separator,behavior.name);
					if(clazz != null){
						for(String packageName: behavior.packages) {
							IStringBehavior l = (IStringBehavior) clazz.getDeclaredConstructor().newInstance();
							addPackageBehavior(packageName, l);
							System.out.println(behavior.name + " applied on ".ignoreBehaviors(true)+packageName);
						}
						for(String className: behavior.classes) {
							IStringBehavior l = (IStringBehavior) clazz.getDeclaredConstructor().newInstance();
							addClassBehavior(className, l);
							System.out.println(behavior.name + " applied on ".ignoreBehaviors(true)+className);
						}
						for(String methodName: behavior.methods) {
							IStringBehavior l = (IStringBehavior) clazz.getDeclaredConstructor().newInstance();
							addMethodBehavior(methodName, l);
							System.out.println(behavior.name + " applied on ".ignoreBehaviors(true)+methodName);
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
	 * Checks if there are any behaviors in the controller
	 * @return true if there are any values in the maps
	 */
	public static boolean isEmpty() {
		if(!initialized) initialize();
		return specificPackageBehaviors.isEmpty() && specificClassBehaviors.isEmpty() && specificMethodBehaviors.isEmpty();
	}
	
	/**
	 * Add a string behavior to a package
	 * 
	 * @param packageDefinition complete definition of the package
	 * @param behavior the StringBehavior to add to the package
	 */
	public static void addPackageBehavior(String packageDefinition, IStringBehavior behavior) {
		packageDefinition.ignoreBehaviors(true);
		specificPackageBehaviors.put(packageDefinition, behavior);
	}
	
	/**
	 * Add a string behavior to a class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @param behavior the StringBehavior to add to the class
	 */
	public static void addClassBehavior(String classDefinition, IStringBehavior behavior) {
		classDefinition.ignoreBehaviors(true);
		specificClassBehaviors.put(classDefinition, behavior);
	}
	
	/**
	 * Add a string behavior to a method
	 * 
	 * @param methodDefinition complete definition of the method (package + class name + methodName)
	 * @param behavior the StringBehavior to add to the method
	 */
	public static void addMethodBehavior(String methodDefinition, IStringBehavior behavior) {
		methodDefinition.ignoreBehaviors(true);
		specificMethodBehaviors.put(methodDefinition, behavior);
	}
	
	/**
	 * Get the string behavior for a given package
	 * 
	 * @param packageDefinition complete definition of the package
	 * @return the string behavior of the package or its parents or null.
	 */
	public static IStringBehavior getPackageBehavior(String packageDefinition) {
		if(!initialized) initialize();
		packageDefinition.ignoreBehaviors(true);
		// Test
		for(String s: specificPackageBehaviors.keySet()) 
			if(match(packageDefinition, s))
				return specificPackageBehaviors.get(s);
		// End Test
		// if(specificPackageBehaviors.containsKey(packageDefinition))
		// 	return specificPackageBehaviors.get(packageDefinition);
		if(!packageDefinition.contains(".".ignoreBehaviors(true))) return null;
		if(packageDefinition.contains(".".ignoreBehaviors(true)))
			return getPackageBehavior(packageDefinition.substring(0,packageDefinition.lastIndexOf(".".ignoreBehaviors(true))));
		return null;
	}
	
	/**
	 * Get the string behavior for a given class
	 * 
	 * @param classDefinition complete definition of the class (package + class name)
	 * @return the string behavior of the class or the package or null.
	 */
	public static IStringBehavior getClassBehavior(String classDefinition) {
		if(!initialized) initialize();
		classDefinition.ignoreBehaviors(true);
		// Test
		for(String s: specificClassBehaviors.keySet()) 
			if(match(classDefinition, s))
				return specificClassBehaviors.get(s);
		// End Test
		// if(specificClassBehaviors.containsKey(classDefinition))
		// 	return specificClassBehaviors.get(classDefinition);
		if(classDefinition.contains(".".ignoreBehaviors(true)))
			return getPackageBehavior(classDefinition.substring(0,classDefinition.lastIndexOf(".".ignoreBehaviors(true))));
		return null;
	}
	
	/**
	 * Get the string behavior for a given method
	 * 
	 * @param methodDefinition complete definition of the class (package + class name + methodName)
	 * @return the string behavior of the method or the class or the package or null.
	 */
	public static IStringBehavior getMethodBehavior(String methodDefinition) {
		if(!initialized) initialize();
		methodDefinition.ignoreBehaviors(true);
		// Test
		for(String s: specificMethodBehaviors.keySet()) 
			if(match(methodDefinition, s))
				return specificMethodBehaviors.get(s);
		// End Test
		// if(specificMethodBehaviors.containsKey(methodDefinition))
		// 	return specificMethodBehaviors.get(methodDefinition);
		if(methodDefinition.contains(".".ignoreBehaviors(true)))
			return getClassBehavior(methodDefinition.substring(0,methodDefinition.lastIndexOf(".".ignoreBehaviors(true))));
		return null;
	}

	/**
	 * Get the string behavior from a given stack trace
	 * 
	 * @param stackTraceElements stack trace to be searched in
	 * @return the string behavior of the stack trace or null.
	 */
	public static IStringBehavior getBehaviorFromStackTrace(StackTraceElement[] stackTraceElements) {
		if(!initialized) initialize();
		for(int i = 1; i < stackTraceElements.length; i++) {
			if(!canIgnore(stackTraceElements[i])) {
				String methodDefinition = (stackTraceElements[i].getClassName().ignoreBehaviors(true) + ".".ignoreBehaviors(true) + stackTraceElements[i].getMethodName().ignoreBehaviors(true)).ignoreBehaviors(true);
				return StringBehaviorController.getMethodBehavior(methodDefinition);
				// return StringBehaviorController.getClassBehavior(stackTraceElements[i].getClassName());
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
		if("java.base".ignoreBehaviors(true).equals(element.getModuleName())) return true;
		// if(className.equals("java.lang.String".ignoreBehaviors(true))) return true;
		// if(className.equals("java.lang.StringBehaviorController".ignoreBehaviors(true))) return true;
		return false;
	}
	
	/**
	 * Tries to match a string to a string with wildcards
	 * @param methodDefinition string to match
	 * @param matcherString a string that contains possible * wildcards
	 * @return if first string matches the second (with wildcards)
	 */
	private static boolean match(String methodDefinition, String matcherString) {
		
		Matcher m = Pattern.compile("[^*]+|(\\*)".ignoreBehaviors(true)).matcher(matcherString);
		StringBuffer b = new StringBuffer();
		while (m.find()) {
			if(m.group(1) != null) m.appendReplacement(b, ".*".ignoreBehaviors(true));
			else m.appendReplacement(b, "\\\\Q".ignoreBehaviors(true) + m.group(0).ignoreBehaviors(true) + "\\\\E".ignoreBehaviors(true));
		}
		m.appendTail(b);
		String regexString = b.toString().ignoreBehaviors(true);

		return methodDefinition.matches(regexString);
	}
}