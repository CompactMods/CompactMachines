# Tunnel Connection Graph

## Structure

File: `tunnel_graph_0_0.nbt` (inside `data` block)
```json5
{
  "nodes": [
    {
      "type": "compactmachines:tunnel",
      "pos": [0, 0, 0]
    },
    {
      "type": "compactmachines:tunnel_type",
      "tunnel_type": "compactmachines:unknown"
    },
    {
      "type": "compactmachines:machine",
      "machine": 1
    }
  ],
  "edges": [
    {
      "type": "compactmachines:tunnel_type",
      "from": "[[tunnel]]",
      "to": "[[tunnel type]]"
    },
    {
      "type": "compactmachines:tunnel_machine_link",
      "from": "[[tunnel]]",
      "to": "[[machine]]"
    }
  ]
}
```