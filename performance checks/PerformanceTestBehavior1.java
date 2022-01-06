import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

public class PerformanceTestBehavior1 {
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
			FileWriter fw = new FileWriter("log\\performanceResultsBehavior1.txt", true);
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
			new String(strings[i]).setLogic(new Behavior1());
		}
		
		long endTime = new Date().getTime();
		appendToFile("Init," + (endTime - startTime) + "," + n);
	}
	
	private static void printTest() {
		long startTime = new Date().getTime();
		
		for(int i = 0; i < n ; i++){
			String s = new String(strings[i]);
			s.setLogic(new Behavior1());
			System.out.println(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Read," + (endTime - startTime) + "," + n);
	}

	private static void inheritanceTest() {
		String s = new String(strings[0]);

		long startTime = new Date().getTime();

		for(int i = 0; i < n ; i++){
			s = new String(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Inheritance," + (endTime - startTime) + "," + n);
	}

	private static class Behavior1 implements IStringLogic {
		@Override
		public boolean inheritToChild(StringTransformType stt) {
			return false;
		}

		@Override
		public boolean recordHistory() {
			return false;
		}

		@Override
		public String getDescription() {
			return "";
		}
	}
}