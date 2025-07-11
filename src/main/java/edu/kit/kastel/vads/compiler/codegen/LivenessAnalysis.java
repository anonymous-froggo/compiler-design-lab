package edu.kit.kastel.vads.compiler.codegen;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;
import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorsSkipProj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.Main;
import edu.kit.kastel.vads.compiler.ir.SsaGraph;
import edu.kit.kastel.vads.compiler.ir.nodes.Block;
import edu.kit.kastel.vads.compiler.ir.nodes.BoolNode;
import edu.kit.kastel.vads.compiler.ir.nodes.CallNode;
import edu.kit.kastel.vads.compiler.ir.nodes.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.nodes.Node;
import edu.kit.kastel.vads.compiler.ir.nodes.Phi;
import edu.kit.kastel.vads.compiler.ir.nodes.binary.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.nodes.control.ConditionalJumpNode;
import edu.kit.kastel.vads.compiler.ir.nodes.control.JumpNode;
import edu.kit.kastel.vads.compiler.ir.nodes.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.nodes.unary.UnaryOperationNode;

public class LivenessAnalysis {

    private final SsaGraph graph;

    private final Map<Node, Set<Node>> def = new HashMap<>();
    private final Map<Node, Set<Node>> use = new HashMap<>();
    private final Map<Node, Set<Node>> succ = new HashMap<>();

    private final Map<Node, Set<Node>> live = new HashMap<>();
    private boolean liveChanged;

    public LivenessAnalysis(SsaGraph graph) {
        this.graph = graph;
    }

    public InterferenceGraph calculateInterferenceGraph() {
        for (Block block : this.graph.blocks()) {
            applyJRules(block);
        }

        // if (Main.DEBUG) {
        //     System.out.println("def: " + def);
        //     System.out.println("use: " + use);
        //     System.out.println("succ: " + succ);
        // }

        for (Node l : use.keySet()) {
            K1(l);
        }
        do {
            liveChanged = false;
            for (Node l : succ.keySet()) {
                K2(l);
            }
        } while (liveChanged);

        if (Main.DEBUG) {
            System.out.println("live: " + live);
        }

        InterferenceGraph interferenceGraph = new InterferenceGraph(live);
        return interferenceGraph;
    }

    private void applyJRules(Block block) {
        List<Node> nodes = block.schedule();

        // Apply to non-control flow nodes
        for (int index = 0; index < nodes.size(); index++) {
            Node l = nodes.get(index);

            switch (l) {
                case BinaryOperationNode binaryOperation -> J1Binary(binaryOperation, nodes.get(index + 1));
                case UnaryOperationNode unaryOperation -> J1Unary(unaryOperation, nodes.get(index + 1));
                case Phi phi -> J1Phi(phi, nodes.get(index + 1));
                case ReturnNode ret -> J2(ret);
                case ConstIntNode _,BoolNode _ -> J3(l, nodes.get(index + 1));
                case JumpNode jump -> J4(jump);
                case ConditionalJumpNode conditionalJump -> J5(conditionalJump);
                case CallNode call -> J6(call, nodes.get(index + 1));
                default -> {
                    continue;
                }
            }
        }
    }

    private void J1Binary(BinaryOperationNode l, Node lPlusOne) {
        Node x = l;
        Node y = predecessorSkipProj(l, BinaryOperationNode.LEFT);
        Node z = predecessorSkipProj(l, BinaryOperationNode.RIGHT);

        addFact(def, l, x);
        addFact(use, l, y);
        addFact(use, l, z);
        addFact(succ, l, lPlusOne);
    }

    private void J1Unary(UnaryOperationNode l, Node lPlusOne) {
        Node x = l;
        Node y = predecessorSkipProj(l, UnaryOperationNode.IN);

        addFact(def, l, x);
        addFact(use, l, y);
        addFact(succ, l, lPlusOne);
    }

    // x <- φ(y1, ...)
    private void J1Phi(Phi l, Node lPlusOne) {
        Node x = l;

        addFact(def, l, x);
        for (Node yi : predecessorsSkipProj(l)) {
            addFact(use, l, yi);
        }
        addFact(succ, l, lPlusOne);
    }

    private void J2(ReturnNode l) {
        Node x = predecessorSkipProj(l, ReturnNode.RESULT);

        addFact(use, l, x);
    }

    private void J3(Node l, Node lPlusOne) {
        Node x = l;

        addFact(def, l, x);
        addFact(succ, l, lPlusOne);
    }

    private void J4(JumpNode l) {
        Node lPrime = l.target(JumpNode.TARGET).schedule().get(0);

        addFact(succ, l, lPrime);
    }

    private void J5(ConditionalJumpNode l) {
        Node x = predecessorSkipProj(l, ConditionalJumpNode.CONDITION);
        Node lPrime = l.target(ConditionalJumpNode.TRUE_TARGET).schedule().get(0);
        Node lPlusOne = l.target(ConditionalJumpNode.FALSE_TARGET).schedule().get(0);

        addFact(use, l, x);
        addFact(succ, l, lPrime);
        addFact(succ, l, lPlusOne);
    }

    // x <- f(y1, ...)
    private void J6(CallNode l, Node lPlusOne) {
        Node x = l;

        addFact(def, l, x);
        for (int i = CallNode.ARGS_START; i < l.predecessors().size(); i++) {
            Node yi = predecessorSkipProj(x, i);
            addFact(use, l, yi);
        }
        addFact(succ, l, lPlusOne);
    }

    private void K1(Node l) {
        for (Node x : use.get(l)) {
            addFact(live, l, x);
        }
    }

    private void K2(Node l) {
        // Try to apply this rule for all l' with succ(l, l')
        for (Node lPrime : succ.get(l)) {
            // Try to apply this rule for all u with live(l', u)
            Set<Node> liveAtLPrime = live.get(lPrime);

            if (liveAtLPrime == null) {
                // There are no such u
                continue;
            }

            for (Node u : liveAtLPrime) {
                if (def.get(l) == null || !def.get(l).contains(u)) {
                    // ¬def(l, u) is fulfilled
                    liveChanged = addFact(live, l, u) || liveChanged;
                }
            }
        }
    }

    private boolean addFact(Map<Node, Set<Node>> predicate, Node l, Node subject) {
        if (!predicate.containsKey(l)) {
            predicate.put(l, new HashSet<>());
        }

        return predicate.get(l).add(subject);
    }
}
