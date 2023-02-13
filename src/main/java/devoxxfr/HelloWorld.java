package devoxxfr;

public class HelloWorld {

    private static final String DEFAULT_VALUE = "World";

    public static void main(String[] args) {
        System.out.println(createMessage(args));
    }

    private static String createMessage(String[] args) {
        var name = DEFAULT_VALUE;

        if (args.length != 0) {
            name = args[0];
        }

        return String.format("Hello %s!", name);
    }
}
