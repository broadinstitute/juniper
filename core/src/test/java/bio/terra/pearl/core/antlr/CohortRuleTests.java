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
        CohortRuleParser parser = setupParser("{foo} = 'yes'");
        CohortRuleParser.ExprContext exp = parser.expr();
        assertThat(exp.term(0).getText(), equalTo("{foo}"));
        assertThat(exp.term(0).VARIABLE().getText(), equalTo("{foo}"));
        assertThat(exp.term(1).getText(), equalTo("'yes'"));
        assertThat(exp.OPERATOR().getText(), equalTo("="));
    }

    @Test
    public void testCompoundParsing() {
        CohortRuleParser parser = setupParser("{foo} = 'yes' && {bar} = 'no'");
        CohortRuleParser.ExprContext exp = parser.expr();
        assertThat(exp.term().size(), equalTo(0));
        assertThat(exp.AND().getText(), equalTo("&&"));
        assertThat(exp.expr(0).getText(), equalTo("{foo}='yes'"));
        assertThat(exp.expr(1).getText(), equalTo("{bar}='no'"));
    }

    @Test
    public void testThreeExpressionCompoundParsing() {
        CohortRuleParser parser = setupParser("{foo} = 'yes' && {bar} = 'no' && {baz} = 1");
        CohortRuleParser.ExprContext rootExp = parser.expr();
        assertThat(rootExp.term().size(), equalTo(0));
        assertThat(rootExp.expr().size(), equalTo(2));
        assertThat(rootExp.AND().getText(), equalTo("&&"));

        assertThat(rootExp.expr(0).getText(), equalTo("{foo}='yes'&&{bar}='no'"));
        assertThat(rootExp.expr(1).getText(), equalTo("{baz}=1"));
    }

    @Test
    public void testParenthesesParsing() {
        CohortRuleParser parser = setupParser("{foo} = 'yes' && ({bar} = 'no' || {baz} = 1)");
        CohortRuleParser.ExprContext rootExp = parser.expr();
        assertThat(rootExp.term().size(), equalTo(0));
        assertThat(rootExp.expr().size(), equalTo(2));
        assertThat(rootExp.AND().getText(), equalTo("&&"));


        assertThat(rootExp.expr(0).getText(), equalTo("{foo}='yes'"));
        assertThat(rootExp.expr(1).getText(), equalTo("({bar}='no'||{baz}=1)"));
    }

    protected CohortRuleParser setupParser(String input) {
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new CohortRuleParser(tokens);
    }
}
