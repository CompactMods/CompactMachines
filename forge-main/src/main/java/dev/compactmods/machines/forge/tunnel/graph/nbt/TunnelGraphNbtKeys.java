package dev.compactmods.machines.forge.tunnel.graph.nbt;

public interface TunnelGraphNbtKeys {

    String NODE_GROUP = "nodes";
    String NODE_COUNT = "node_count";
    String NODE_ID = "id";
    String NODE_DATA = "data";
    String NODE_GROUP_TUNNEL_LIST = "tunnels";
    String NODE_GROUP_TUNNEL_TYPE_LIST = "tunnel_types";
    String NODE_GROUP_MACHINE_LIST = "machines";


    String EDGE_GROUP = "edges";
    String EDGE_COUNT = "edge_count";
    String EDGE_CONNECTION_FROM_ID = "from";
    String EDGE_CONNECTION_TO_ID = "to";
    String EDGE_CONNECTION_DATA = "data";
    String EDGE_GROUP_MACHINE_LIST = "machines";
    String EDGE_GROUP_TUNNEL_TYPE_LIST = "tunnel_types";

}
