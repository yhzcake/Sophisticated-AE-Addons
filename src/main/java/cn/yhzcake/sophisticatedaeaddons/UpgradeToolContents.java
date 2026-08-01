package cn.yhzcake.sophisticatedaeaddons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record UpgradeToolContents(NonNullList<ItemStack> items) {
    public static final int SLOTS = 9;

    public static final UpgradeToolContents EMPTY = new UpgradeToolContents(createEmpty());

    public static final Codec<UpgradeToolContents> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(UpgradeToolContents::asList)
        ).apply(instance, UpgradeToolContents::fromList)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeToolContents> STREAM_CODEC =
        StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
            UpgradeToolContents::asList,
            UpgradeToolContents::fromList
        );

    private static NonNullList<ItemStack> createEmpty() {
        NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        return list;
    }

    private static UpgradeToolContents fromList(java.util.List<ItemStack> list) {
        NonNullList<ItemStack> result = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(list.size(), SLOTS); i++) {
            result.set(i, list.get(i));
        }
        return new UpgradeToolContents(result);
    }

    private java.util.List<ItemStack> asList() {
        return items;
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= SLOTS) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public UpgradeToolContents setStackInSlot(int slot, ItemStack stack) {
        NonNullList<ItemStack> newItems = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS; i++) {
            newItems.set(i, i == slot ? stack.copy() : items.get(i).copy());
        }
        return new UpgradeToolContents(newItems);
    }
}
