import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PerformanceTestBehavior4 {
	private static final int n = 1000000;
	private static String[] strings;

	public static void main(String[] args) throws Exception {
		strings = new String[n];

		FileInputStream fstream = new FileInputStream("strings.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		for(int i = 0 ; i < n ; i++) {
			String strLine = br.readLine();
			if(strLine == null){
				strings[i] = "";
			} else {
				strings[i] = strLine;
			}
		}
		
		//Close the input stream
		fstream.close();

		System.setOut(new PrintStream(new OutputStream() { @Override public void write(int arg0) { } }));

		initTest();
		printTest();
		inheritanceTest();
	}
	
	private static void appendToFile(String s) {
		try{
			FileWriter fw = new FileWriter("log\\performanceResultsBehavior4.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(s);
			bw.newLine();
			bw.close();
		}catch(Exception e){
		}		
	}
	
	private static void initTest() {
		long startTime = new Date().getTime();
		
		for(int i = 0; i < n ; i++){
			new String(strings[i]).setLogic(new Behavior4());
		}
		
		long endTime = new Date().getTime();
		appendToFile("Init," + (endTime - startTime) + "," + n);
	}
	
	private static void printTest() {
		long startTime = new Date().getTime();
		
		for(int i = 0; i < n ; i++){
			String s = new String(strings[i]);
			s.setLogic(new Behavior4());
			System.out.println(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Read," + (endTime - startTime) + "," + n);
	}

	private static void inheritanceTest() {
		String s = new String(strings[0]);

		long startTime = new Date().getTime();
		
		s.setLogic(new Behavior4());

		for(int i = 0; i < n ; i++){
			s = new String(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Inheritance," + (endTime - startTime) + "," + n);
	}

	private static class Behavior4 implements IStringLogic {
		private ArrayList<String> outputPackages;
		private Cipher enCipher;
		private SecretKeySpec key;
		private String keyString = "a835zucnpq85tmcÜ093X,4tc£tCAÄ4WT9CAÖ";
	
		public Behavior4() {
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
}