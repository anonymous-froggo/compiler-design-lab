package edu.kit.kastel.vads.compiler.lexer.operators;

import edu.kit.kastel.vads.compiler.Span;

public sealed class BinaryOperator implements Operator permits TernaryMiddle {

    public static final int MIN_PRECEDENCE = 0;

    private final BinaryOperatorType type;
    private final Span span;

    public BinaryOperator(BinaryOperatorType type, Span span) {
        this.type = type;
        this.span = span;
    }

    @Override
    public BinaryOperatorType type() {
        return this.type;
    }

    @Override
    public Span span() {
        return this.span;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum BinaryOperatorType implements OperatorType {

        MUL("*", 11),
        DIV("/", 11),
        MOD("%", 11),

        PLUS("+", 10),
        // May actually be a unary minus if it is at the beginning of an atom. It will
        // then be converted to UnaryOperator.UNARY_MINUS in [Parser::parseAtom]
        MINUS("-", 10),

        SHIFT_LEFT("<<", 9),
        SHIFT_RIGHT(">>", 9),

        LESS_THAN("<", 8),
        LESS_THAN_EQ("<=", 8),
        GREATER_THAN(">", 8),
        GREATER_THAN_EQ(">=", 8),

        EQ("==", 7),
        NOT_EQ("!=", 7),

        BITWISE_AND("&", 6),

        BITWISE_XOR("^", 5),

        BITWISE_OR("|", 4),

        LOGICAL_AND("&&", 3),

        LOGICAL_OR("||", 2),

        TERNARY_OPEN("?", 1, Associativity.RIGHT),
        TERNARY_CLOSE(":", 1, Associativity.RIGHT),
        // Represents a TernaryTree as binary infix operator
        TERNARY(null, 1, Associativity.RIGHT);

        private final String value;
        private final int precedence;
        private final Associativity associativity;

        BinaryOperatorType(String value, int precedence) {
            this.value = value;
            this.precedence = precedence;
            this.associativity = Associativity.LEFT;
        }

        BinaryOperatorType(String value, int precedence, Associativity associativity) {
            this.value = value;
            this.precedence = precedence;
            this.associativity = associativity;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public int precedence() {
            return this.precedence;
        }

        public Associativity associativity() {
            return this.associativity;
        }
    }
}
