import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

public class CreateStrings {
    public static void main(String[] args){
        File f = new File("strings.txt");
        try {
            f.createNewFile();
        }catch (IOException e) {
            e.printStackTrace();
        }

        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        
        for(int n = 0 ; n < 1000000; n++){
            int targetStringLength = 10 + random.nextInt(15);

            String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
                
            generatedString += "\n";

            try {
                Files.write(f.toPath(), generatedString.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
