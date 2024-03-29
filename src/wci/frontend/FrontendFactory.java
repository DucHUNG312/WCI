package wci.frontend;

import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalScanner;

public class FrontendFactory
{
    public static Parser createParser(String language, String type, Source source) throws Exception
    {
        if (language.equalsIgnoreCase("Pascal") && type.equalsIgnoreCase("top-down"))
        {
            Scanner scanner = new PascalScanner(source);
            return new PascalParserTD(scanner);
        }
        else if (!language.equalsIgnoreCase("Pascal"))
        {
            throw new Exception("Parser factory: Invalid language &apos;" + language + "&apos;");
        }
        else
        {
            throw new Exception("Parser factory: Invalid type &apos;" + type + "&apos;");
        }
    }
}
