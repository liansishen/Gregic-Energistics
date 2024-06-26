package com.epimorphismmc.gregiceng.integration.jade.provider;

import com.epimorphismmc.gregiceng.GregicEng;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOBufferPartMachine;
import com.epimorphismmc.gregiceng.utils.GregicEngUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CraftingIOBufferProvider
        implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();

        ListTag itemTags = serverData.getList("items", Tag.TAG_COMPOUND);
        ListTag fluidTags = serverData.getList("fluids", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemTags.size(); ++i) {
            CompoundTag itemTag = itemTags.getCompound(i);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("item")));
            long count = itemTag.getLong("count");
            if (item != null) {
                iTooltip.add(item.getDescription()
                        .copy()
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" * ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("" + count).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }
        for (int i = 0; i < fluidTags.size(); ++i) {
            CompoundTag fluidTag = fluidTags.getCompound(i);
            @Nullable FluidType fluid = ForgeRegistries.FLUID_TYPES
                    .get()
                    .getValue(new ResourceLocation(fluidTag.getString("fluid")));
            long count = fluidTag.getLong("count");
            if (fluid != null) {
                iTooltip.add(fluid
                        .getDescription()
                        .copy()
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(" * ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("" + count).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity machineBlockEntity) {
            if (machineBlockEntity.getMetaMachine() instanceof CraftingIOBufferPartMachine buffer) {
                var merged = GregicEngUtils.mergeInternalSlot(buffer.getInternalInventory());
                var items = merged.getLeft();
                var fluids = merged.getRight();

                ListTag itemTags = new ListTag();
                for (Item item : items.keySet()) {
                    ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                    if (key != null) {
                        CompoundTag itemTag = new CompoundTag();
                        itemTag.putString("item", key.toString());
                        itemTag.putLong("count", items.getLong(item));
                        itemTags.add(itemTag);
                    }
                }
                compoundTag.put("items", itemTags);

                ListTag fluidTags = new ListTag();
                for (Fluid fluid : fluids.keySet()) {
                    ResourceLocation key = ForgeRegistries.FLUID_TYPES.get().getKey(fluid.getFluidType());
                    if (key != null) {
                        CompoundTag fluidTag = new CompoundTag();
                        fluidTag.putString("fluid", key.toString());
                        fluidTag.putLong("count", fluids.getLong(fluid));
                        fluidTags.add(fluidTag);
                    }
                }
                compoundTag.put("fluids", fluidTags);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GregicEng.id("crafting_io_buffer");
    }
}
