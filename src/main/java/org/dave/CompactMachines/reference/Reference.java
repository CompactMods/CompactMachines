package org.dave.CompactMachines.reference;

public class Reference {
    public static final String MOD_ID = "CompactMachines";
    public static final String MOD_NAME = "Compact Machines";
    public static final String VERSION = "1.7.10-1.3";
    public static final String CLIENT_PROXY_CLASS = "org.dave.CompactMachines.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "org.dave.CompactMachines.proxy.ServerProxy";
    public static final String GUI_FACTORY_CLASS = "org.dave.CompactMachines.client.gui.GuiFactory";

    public static boolean AE_AVAILABLE = false;
    public static boolean PR_AVAILABLE = false;

    public static int getBoxSize(int type) {
    	if(type <= 0) {
			return 4;
		}
    	if(type == 1) {
			return 6;
		}
    	if(type == 2) {
			return 8;
		}
    	if(type == 3) {
			return 10;
		}
    	if(type == 4) {
			return 12;
		}
    	if(type >= 5) {
			return 14;
		}

    	return 8;
    }
}
