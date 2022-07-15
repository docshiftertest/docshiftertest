package com.docshifter.core;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class SpelTest {

    @Test
    public void checkContains() {
        String condition = "#result.contains('HIFI')";
        assertTrue(evaluateExpression("hifi", condition));
    }

    boolean evaluateExpression(String result, String condition) {

        result = result.toUpperCase();

        StandardEvaluationContext context = new StandardEvaluationContext();

        if (condition.contains("contains")) {
            context.setVariable("result", result);
        } else {
            context.setVariable("RESULT", result);
            condition = condition.toUpperCase();
        }

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(condition);

        return (Boolean) exp.getValue(context);
    }
}
