package sample;

public class Hexadecimal_x64 {
    private final static char hexNumbers[] = {'0','1','2','3','4','5',
            '6','7','8','9','A','B','C','D','E','F'};
    public static String integerToHex64(int number1, int number2)
    {
        if (number1 > 255) number1 = number1 - 255;
        if (number2 > 255) number2 = number2 - 255;
        String hex = "" + hexNumbers[number1 / 16] +
                hexNumbers[number1 % 16] +
                hexNumbers[number2 / 16] +
                hexNumbers[number2 % 16];
        return hex;
    }

    public static String integerToHex32(int number)
    {
        if (number > 255) number = number - 255;
        String hex = "" + hexNumbers[number / 16] +
                hexNumbers[number % 16];
        return hex;
    }
}
