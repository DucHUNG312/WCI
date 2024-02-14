package wci.frontend.pascal;

import wci.frontend.EofToken;
import wci.frontend.Scanner;
import wci.frontend.Source;
import wci.frontend.Token;
import static wci.frontend.Source.EOF;

public class PascalScanner extends Scanner
{
    public PascalScanner(Source source)
    {
        super(source);
    }

    @Override
    protected Token extractToken() throws Exception
    {
        Token token;
        char currentChar = currentChar();

        if(currentChar == EOF)
        {
            token = new EofToken(source);
        }
        else
        {
            token = new Token(source);
        }

        return token;
    }
}
