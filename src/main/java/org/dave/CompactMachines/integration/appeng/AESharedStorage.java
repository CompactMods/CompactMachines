package org.dave.CompactMachines.integration.appeng;

import java.util.HashMap;
import java.util.UUID;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.utility.WorldUtils;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridNode;

public class AESharedStorage extends AbstractSharedStorage {
	public int			coord;
	public int			side;

	public HashMap<Integer, Boolean> isConnected;
	public HashMap<Integer, IGridNode>	machineNodes;
	public IGridNode	interfaceNode;

	public String random = UUID.randomUUID().toString();

	public AESharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		this.side = side;
		this.coord = coord;

		if(machineNodes == null) {
			machineNodes = new HashMap<Integer, IGridNode>();
		}

		if(isConnected == null) {
			isConnected = new HashMap<Integer, Boolean>();
		}
	}

	@Override
	public String type() {
		return "appeng";
	}

	public void connectNodes(int entangledInstance) {

		if (interfaceNode == null || machineNodes == null || machineNodes.get(entangledInstance) == null) {
			return;
		}

		if (isConnected != null && isConnected.containsKey(entangledInstance)) {
			return;
		}

		if (!Reference.AE_AVAILABLE) {
			return;
		}

		try {
			AEApi.instance().createGridConnection(interfaceNode, machineNodes.get(entangledInstance));
			isConnected.put(entangledInstance, true);
		} catch (FailedConnection e) {
		}
	}

	public void connectAll() {
		if(machineNodes == null) {
			return;
		}

		for(int entangledIndex : machineNodes.keySet()) {
			if(!isConnected.containsKey(entangledIndex) || !isConnected.get(entangledIndex)) {
				connectNodes(entangledIndex);
			}
		}

	}

	public IGridNode getInterfaceNode(CMGridBlock gridBlock) {
		if (interfaceNode == null) {
			interfaceNode = AEApi.instance().createGridNode(gridBlock);
			interfaceNode.updateState();
		}

		connectAll();

		return interfaceNode;
	}

	public void destroyMachineNode(int entangledIndex) {
		IGridNode machineNode = machineNodes.get(entangledIndex);
		if (machineNode != null) {
			machineNode.destroy();
			machineNodes.remove(entangledIndex);
			isConnected.remove(entangledIndex);
		}

		connectAll();
	}

	public IGridNode getMachineNode(CMGridBlock gridBlock, int entangledIndex) {
		IGridNode machineNode = machineNodes.get(entangledIndex);
		if (machineNode == null) {
			machineNode = AEApi.instance().createGridNode(gridBlock);
			machineNode.updateState();

			machineNodes.put(entangledIndex, machineNode);

			// Update neighbor blocks since we are now having a gridnode
			WorldUtils.updateNeighborAEGrids(gridBlock.getLocation().getWorld(), gridBlock.getLocation().x, gridBlock.getLocation().y, gridBlock.getLocation().z);
		}

		return machineNode;
	}
}
