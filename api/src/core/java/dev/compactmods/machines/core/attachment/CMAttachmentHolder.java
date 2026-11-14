/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package dev.compactmods.machines.core.attachment;

import dev.compactmods.machines.core.attachment.persistence.AttachmentPersistence;
import dev.compactmods.machines.core.attachment.persistence.ValueIOPersistenceHandler;
import dev.compactmods.machines.core.attachment.storage.AttachmentDataStorage;
import dev.compactmods.machines.core.attachment.storage.SimpleAttachmentDataStorage;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public class CMAttachmentHolder implements AttachmentDataAccessor, IAttachmentHolder {
    @Deprecated(forRemoval = true)
    public static final String ATTACHMENTS_NBT_KEY = AttachmentPersistence.ATTACHMENTS_NBT_KEY;

    private final SimpleAttachmentDataStorage dataStorage = new SimpleAttachmentDataStorage(this);

    private final ValueIOPersistenceHandler persistenceHandler = new ValueIOPersistenceHandler(this, dataStorage);

    @Override
    public AttachmentDataStorage dataStorage() {
        return dataStorage;
    }

    public ValueIOPersistenceHandler persistenceHandler() {
        return persistenceHandler;
    }
}
