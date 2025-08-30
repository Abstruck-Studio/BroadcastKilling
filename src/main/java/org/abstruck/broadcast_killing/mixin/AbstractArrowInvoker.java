package org.abstruck.broadcast_killing.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.abstruck.broadcast_killing.core.PickupItemGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowInvoker implements PickupItemGetter {
    @Shadow
    protected abstract ItemStack getPickupItem();

    @Override
    public ItemStack broadcast_killing$getPublicPickupItem() {
        return getPickupItem();
    }
}
