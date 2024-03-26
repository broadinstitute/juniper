package bio.terra.pearl.core.service.rule;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * By default, Antlr will silently print out syntax errors without throwing an exception. This class
 * overrides the default behavior to throw an exception when a syntax error is encountered. It must
 * throw a ParseCancellationException to stop the parsing process.
 */
public class RuleParsingErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
