package dev.compactmods.machines.core.attachment.persistence;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;

public class AttachmentPersistence {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "data_attachments");
}
