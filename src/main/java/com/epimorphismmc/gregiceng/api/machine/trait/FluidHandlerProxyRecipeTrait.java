package com.epimorphismmc.gregiceng.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class FluidHandlerProxyRecipeTrait extends NotifiableFluidTank {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FluidHandlerProxyRecipeTrait.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

    @Getter
    private final Collection<NotifiableItemStackHandler> handlers;

    public FluidHandlerProxyRecipeTrait(
            MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        this(machine, slots, capacity, io, capabilityIO, null);
    }

    public FluidHandlerProxyRecipeTrait(
            MetaMachine machine,
            int slots,
            long capacity,
            IO io,
            IO capabilityIO,
            Collection<NotifiableItemStackHandler> handlers) {
        super(machine, slots, capacity, io, capabilityIO);
        this.handlers = handlers;
    }

    public FluidHandlerProxyRecipeTrait(
            MetaMachine machine,
            int slots,
            long capacity,
            IO io,
            Collection<NotifiableItemStackHandler> handlers) {
        this(machine, slots, capacity, io, io, handlers);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(
            IO io,
            GTRecipe recipe,
            List<FluidIngredient> left,
            @Nullable String slotName,
            boolean simulate) {
        return handleIngredient(
                io, recipe, left, simulate, this.handlerIO, getStorages(), handlers, this.isDistinct());
    }

    @Nullable public static List<FluidIngredient> handleIngredient(
            IO io,
            GTRecipe recipe,
            List<FluidIngredient> left,
            boolean simulate,
            IO handlerIO,
            FluidStorage[] storages,
            Collection<NotifiableItemStackHandler> handlers,
            boolean isDistinct) {
        if (io != handlerIO) return left;
        // When fluid distinct is enabled,check all items whether match the recipe
        if (isDistinct && simulate) {
            Object2IntMap<ItemStack> itemMap =
                    new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
            for (NotifiableItemStackHandler handler : handlers) {
                for (int i = 0; i < handler.storage.getSlots(); i++) {
                    itemMap.putIfAbsent(handler.storage.getStackInSlot(i), 1);
                }
            }
            for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
                Ingredient recipeIngredient = ItemRecipeCapability.CAP.of(content.content);
                boolean isMatch = false;
                for (ItemStack is : recipeIngredient.getItems()) {
                    if (itemMap.containsKey(is)) {
                        isMatch = true;
                        break;
                    }
                }
                if (!isMatch) return left;
            }
        }
        return NotifiableFluidTank.handleIngredient(io, recipe, left, simulate, handlerIO, storages);
    }
}
