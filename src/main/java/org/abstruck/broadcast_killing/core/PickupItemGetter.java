package org.abstruck.broadcast_killing.core;

import net.minecraft.world.item.ItemStack;

public interface PickupItemGetter {
    default ItemStack broadcast_killing$getPublicPickupItem() {
        return ItemStack.EMPTY;
    }
}
