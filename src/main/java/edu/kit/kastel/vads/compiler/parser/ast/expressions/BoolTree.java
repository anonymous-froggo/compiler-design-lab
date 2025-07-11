package edu.kit.kastel.vads.compiler.parser.ast.expressions;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.keywords.BoolKeyword;
import edu.kit.kastel.vads.compiler.lexer.keywords.BoolKeyword.BoolKeywordType;
import edu.kit.kastel.vads.compiler.parser.ast.TreeVisitor;

public record BoolTree(BoolKeyword keyword) implements ExpressionTree {

    @Override
    public Span span() {
        return keyword.span();
    }

    @Override
    public <T, R> R accept(TreeVisitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    public boolean value() {
        return switch (keyword.type()) {
            case BoolKeywordType.TRUE -> true;
            case BoolKeywordType.FALSE -> false;
        };
    }
}
