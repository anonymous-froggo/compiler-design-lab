package edu.kit.kastel.vads.compiler.ir.nodes;

public final class ProjNode extends Node {

    public static final int IN = 0;
    private final ProjectionInfo projectionInfo;

    public ProjNode(Block block, Node in, ProjectionInfo projectionInfo) {
        super(block, in);
        this.projectionInfo = projectionInfo;
    }

    @Override
    protected String info() {
        return this.projectionInfo.toString();
    }

    public ProjectionInfo projectionInfo() {
        return this.projectionInfo;
    }

    public sealed interface ProjectionInfo {

    }

    public enum SimpleProjectionInfo implements ProjectionInfo {
        RESULT,
        SIDE_EFFECT,
        TRUE,
        FALSE,
    }
}
