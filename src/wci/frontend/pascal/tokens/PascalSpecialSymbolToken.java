package wci.frontend.pascal.tokens;
import wci.frontend.Source;
import wci.frontend.pascal.PascalErrorCode;

public class PascalSpecialSymbolToken extends PascalToken
{
    public PascalSpecialSymbolToken(Source source)
            throws Exception
    {
        super(source);
    }

    protected void extract() throws Exception
    {
        char currentChar = currentChar();

        text = Character.toString(currentChar);
        type = null;

        switch (currentChar)
        {

            // Single-character special symbols.
            case '+':  case '-':  case '*':  case '/':  case ',':
            case ';':  case '\'': case '=':  case '(':  case ')':
            case '[':  case ']':  case '{':  case '}':  case '^': {
                nextChar();  // consume character
                break;
            }

            // : or :=
            case ':':
            {
                currentChar = nextChar();  // consume ':';

                if (currentChar == '=')
                {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }

            // < or <= or <>
            case '<':
            {
                currentChar = nextChar();  // consume '<';

                if (currentChar == '=')
                {
                    text += currentChar;
                    nextChar();  // consume '='
                }
                else if (currentChar == '>')
                {
                    text += currentChar;
                    nextChar();  // consume '>'
                }

                break;
            }

            // > or >=
            case '>':
            {
                currentChar = nextChar();  // consume '>';

                if (currentChar == '=')
                {
                    text += currentChar;
                    nextChar();  // consume '='
                }

                break;
            }

            // . or ..
            case '.':
            {
                currentChar = nextChar();  // consume '.';

                if (currentChar == '.')
                {
                    text += currentChar;
                    nextChar();  // consume '.'
                }

                break;
            }

            default:
            {
                nextChar();  // consume bad character
                type = PascalTokenType.ERROR;
                value = PascalErrorCode.INVALID_CHARACTER;
            }
        }

        // Set the type if it wasn't an error.
        if (type == null)
        {
            type = PascalTokenType.SPECIAL_SYMBOLS.get(text);
        }
    }
}
