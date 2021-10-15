public class Programs {

    public static final String IGNORING_RETURN_VALUE_INSIDE_IF_STATEMENT =
            "public class TestClass {" +
                "public void method(boolean something) {" +
                    "if (something) {" +
                        "java.lang.String someString = \"hei\";" +
                        "someString.toUpperCase();" +
                    "}" +
                "}" +
            "}";

    public static final String EXAMPLE_PROGRAM_SIMPLE_APP = "public class SemiColonAfterIf {\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        int[] list = new int[]{1, 2, 3};\n" +
            "        int a = list.length;\n" +
            "        int b = 1;\n" +
            "        int g = 1000;\n" +
            "\n" +
            "        if (a < 0) ;\n" +
            "        {\n" +
            "            b = 5;\n" +
            "            g = 300;\n" +
            "        }\n" +
            "\n" +
            "\n" +
            "        // A very dangerous mistake\n" +
            "        if (\" \" == \"heu\") {\n" +
            "            launchRocket();\n" +
            "        }\n" +
            "\n" +
            "        if (b == 30) {\n" +
            "            System.out.println(\"hei\");\n" +
            "            java.lang.String str = \"hei\";\n" +
            "            str.toLowerCase();\n" +
            "        }\n" +
            "\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    public static String method() {\n" +
            "        return \"hei\";\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    private static void launchRocket() {\n" +
            "        System.out.println(\"IT IS LAUNCHED! \");\n" +
            "    }\n" +
            "}";
}
