package dev.compactmods.machines.room.graph;

import dev.compactmods.feather.edge.OutboundGraphEdgeLookupFunction;
import dev.compactmods.machines.room.graph.node.RoomChunkNode;
import dev.compactmods.machines.room.graph.node.RoomReferenceNode;

public class GraphNodes {

    public static final OutboundGraphEdgeLookupFunction<RoomReferenceNode, RoomChunkNode> ROOM_CHUNKS =
            (edgeAccessor, regNode) -> edgeAccessor.outboundEdges(regNode, RoomChunkNode.class);

}
