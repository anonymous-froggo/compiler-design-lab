package edu.kit.kastel.vads.compiler.parser.ast.statements;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.TreeVisitor;

public record ContinueTree(Span span) implements StatementTree {

    @Override
    public <T, R> R accept(TreeVisitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
