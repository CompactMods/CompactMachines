package org.dave.CompactMachines.reference;

public final class Names
{
	public static final class Items
	{
		public static final String	PSD					= "psd";
		public static final String	SHRINKER			= "shrinker";
		public static final String	ENLARGER			= "enlarger";
		public static final String	INTERFACEITEM		= "interfaceitem";
		public static final String	QUANTUMENTANGLER	= "quantumentangler";
	}

	public static final class Blocks
	{
		public static final String	MACHINE					= "machine";
		public static final String	INTERFACE				= "interface";
		public static final String	INTERFACE_DECORATIVE	= "interfacedecor";
		public static final String	INNERWALL				= "innerwall";
		public static final String	INNERWALL_DECORATIVE	= "innerwalldecor";
		public static final String	RESIZINGCUBE			= "resizingcube";
	}

	public static final class NBT
	{
		public static final String	DIRECTION				= "teDirection";
		public static final String	STATE					= "teState";
		public static final String	CUSTOM_NAME				= "CustomName";
		public static final String	OWNER					= "owner";

		public static final String	OWNER_UUID_MOST_SIG		= "OwnerUUIDmost";
		public static final String	OWNER_UUID_LEAST_SIG	= "OwnerUUIDleast";

		public static final String	UUID_MOST_SIG			= "UUIDmost";
		public static final String	UUID_LEAST_SIG			= "UUIDleast";

		public static final String	INTERFACE_ITEMS			= "interfaceItems";
		public static final String	INTERFACE_UP			= "interfaceUp";
		public static final String	INTERFACE_DOWN			= "interfaceDown";
		public static final String	INTERFACE_EAST			= "interfaceEast";
		public static final String	INTERFACE_WEST			= "interfaceWest";
		public static final String	INTERFACE_NORTH			= "interfaceNorth";
		public static final String	INTERFACE_SOUTH			= "interfaceSouth";
	}

	public static final class Containers
	{
		public static final String	VANILLA_INVENTORY	= "container.inventory";
		public static final String	MACHINE				= "container.cm:" + Blocks.MACHINE;
		public static final String	INTERFACE			= "container.cm:" + Blocks.INTERFACE;
	}
}
