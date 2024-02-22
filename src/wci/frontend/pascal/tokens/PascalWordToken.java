package wci.frontend.pascal.tokens;

import wci.frontend.Source;
import wci.frontend.pascal.PascalTokenType;

public class PascalWordToken extends PascalToken
{
    public PascalWordToken(Source source) throws Exception
    {
        super(source);
    }

    @Override
    protected void extract() throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        char currentChar = currentChar();

        while (Character.isLetterOrDigit(currentChar))
        {
            textBuffer.append(currentChar);
            currentChar = nextChar();
        }

        text = textBuffer.toString();

        type = (PascalTokenType.RESERVED_WORDS.contains(text.toLowerCase()))
                ? PascalTokenType.valueOf(text.toUpperCase())
                : PascalTokenType.IDENTIFIER;
    }
}
