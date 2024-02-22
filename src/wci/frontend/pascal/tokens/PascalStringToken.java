package wci.frontend.pascal.tokens;

import wci.frontend.Source;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalTokenType;

public class PascalStringToken extends PascalToken
{
    public PascalStringToken(Source source)
            throws Exception
    {
        super(source);
    }

    protected void extract()
            throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        char currentChar = nextChar();  // consume initial quote
        textBuffer.append('\'');

        // Get string characters.
        do {
            // Replace any whitespace character with a blank.
            if (Character.isWhitespace(currentChar))
            {
                currentChar = ' ';
            }

            if ((currentChar != '\'') && (currentChar != Source.EOF))
            {
                textBuffer.append(currentChar);
                valueBuffer.append(currentChar);
                currentChar = nextChar();  // consume character
            }

            // Quote?  Each pair of adjacent quotes represents a single-quote.
            if (currentChar == '\'')
            {
                while ((currentChar == '\'') && (peekChar() == '\''))
                {
                    textBuffer.append("''");
                    valueBuffer.append(currentChar); // append single-quote
                    currentChar = nextChar();        // consume pair of quotes
                    currentChar = nextChar();
                }
            }
        } while ((currentChar != '\'') && (currentChar != Source.EOF));

        if (currentChar == '\'')
        {
            nextChar();  // consume final quote
            textBuffer.append('\'');

            type = PascalTokenType.STRING;
            value = valueBuffer.toString();
        }
        else
        {
            type = PascalTokenType.ERROR;
            value = PascalErrorCode.UNEXPECTED_EOF;
        }

        text = textBuffer.toString();
    }
}

