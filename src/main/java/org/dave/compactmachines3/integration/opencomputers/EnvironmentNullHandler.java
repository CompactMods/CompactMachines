package org.dave.compactmachines3.integration.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.dave.compactmachines3.integration.AbstractNullHandler;
import org.dave.compactmachines3.integration.CapabilityNullHandler;

@CapabilityNullHandler(mod = "opencomputers")
public class EnvironmentNullHandler extends AbstractNullHandler implements Environment {

	@CapabilityInject(Environment.class)
	public static Capability<Environment> ENVIRONMENT_CAPABILITY = null;

	private Node node = null;

	@Override
	public Node node() {
		return node != null ? node : (node = Network.newNode(this, Visibility.Network).create());
	}

	@Override
	public void onConnect(Node node) {
	}

	@Override
	public void onDisconnect(Node node) {
	}

	@Override
	public void onMessage(Message message) {
	}

	@Override
	public Capability getCapability() {
		return ENVIRONMENT_CAPABILITY;
	}
}
