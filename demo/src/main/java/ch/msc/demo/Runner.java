package ch.msc.demo;

public class Runner 
{
    public static void main( String[] args )
    {
        String sensitiveData = new String("password", new Behavior());

        System.out.println(sensitiveData);

        IoClass.tryToPrint(sensitiveData);

        if(sensitiveData.equals("password")){
            System.out.println("L13 Value is still password");
        } else {
            System.out.println("L13 Value changed");
        }

        String alteredAndRevertedData = IoClass.modifyOriginalAndRevertChange(sensitiveData);
    
        System.out.println(alteredAndRevertedData);

        if(sensitiveData.equals("password")){
            System.out.println("L23 Value is still password");
        } else {
            System.out.println("L23 Value changed");
        }
    }
}
