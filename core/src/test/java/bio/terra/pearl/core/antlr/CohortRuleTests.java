package bio.terra.pearl.core.antlr;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CohortRuleTests {
    @Test
    public void testBasicLexing() {
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString("{foo} = 'yes'"));
        List<? extends Token> tokens = lexer.getAllTokens();
        assertThat(tokens, hasSize(3));
        assertThat(tokens.stream().map(Token::getText).toList(),
                contains("{foo}", "=", "'yes'"));
        assertThat(tokens.stream().map(Token::getType).toList(),
                contains(CohortRuleLexer.VARIABLE, CohortRuleLexer.OPERATOR, CohortRuleLexer.STRING));
    }

    @Test
    public void testBasicParsing() {
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString("{foo} = 'yes'"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CohortRuleParser parser = new CohortRuleParser(tokens);
        CohortRuleParser.ExprContext exp = parser.expr();
        assertThat(exp.term(0).getText(), equalTo("{foo}"));
        assertThat(exp.term(1).getText(), equalTo("'yes'"));
        assertThat(exp.OPERATOR().getText(), equalTo("="));
    }

    @Test
    public void testCompoundParsing() {
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString("{foo} = 'yes' && {bar} = 'no'"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CohortRuleParser parser = new CohortRuleParser(tokens);
        CohortRuleParser.ExprContext exp = parser.expr();
        assertThat(exp.term().size(), equalTo(0));
        assertThat(exp.JOINER().getText(), equalTo("&&"));
        assertThat(exp.expr(0).getText(), equalTo("{foo}='yes'"));
        assertThat(exp.expr(1).getText(), equalTo("{bar}='no'"));
    }
}
