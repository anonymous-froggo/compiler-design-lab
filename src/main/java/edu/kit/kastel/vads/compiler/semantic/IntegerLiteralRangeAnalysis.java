package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.parser.ast.expressions.NumberLiteralTree;
import edu.kit.kastel.vads.compiler.semantic.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.semantic.visitor.Unit;

public class IntegerLiteralRangeAnalysis implements NoOpVisitor<Namespace<Void>> {

    @Override
    public Unit visit(NumberLiteralTree literalTree, Namespace<Void> data) {
        literalTree.parseValue()
            .orElseThrow(
                () -> new SemanticException("invalid integer literal " + literalTree.value())
            );
        return NoOpVisitor.super.visit(literalTree, data);
    }
}
