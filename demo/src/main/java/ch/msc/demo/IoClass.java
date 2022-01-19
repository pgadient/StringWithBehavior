package ch.msc.demo;

public class IoClass {
    public static void tryToPrint(String original) {
        System.out.println(original);
        System.out.println(original.substring(1, original.length() - 1));
        System.out.println(original.toUpperCase().repeat(2));
        System.out.println(original.replace("s", "5"));
    }

    public static String modifyOriginalAndRevertChange(String original) {
        StringBuilder builder = new StringBuilder();
        builder.append(original);
        builder.append("       A        ");
        original = builder.toString();              // "password       A        "
        original = original.substring(4);           // "word       A        "
        original = original.split("A")[0];          // "word       "
        original = original.trim();                 // "word"
        original = original.replace("w", "passw");  // "password"
        return original;
    }
}
