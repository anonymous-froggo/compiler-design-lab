package edu.kit.kastel.vads.compiler.parser.ast.statements;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.TreeVisitor;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.expressions.ExpressionTree;

import org.jspecify.annotations.Nullable;

public record DeclTree(TypeTree type, NameTree name, @Nullable ExpressionTree initializer)
    implements StatementTree
{

    @Override
    public Span span() {
        if (initializer() != null) {
            return type().span().merge(initializer().span());
        }
        return type().span().merge(name().span());
    }

    @Override
    public <T, R> R accept(TreeVisitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
