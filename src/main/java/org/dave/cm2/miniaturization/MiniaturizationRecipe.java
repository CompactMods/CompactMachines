package org.dave.cm2.miniaturization;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MiniaturizationRecipe {
    private Block sourceBlock;

    private int width;
    private int height;
    private int depth;

    private Item targetItem;
    private Block targetBlock;

    private int targetQuantity;
    private int targetMeta;
    private boolean requiresFloor;

    private Item catalyst;
    private int catalystMeta;

    public MiniaturizationRecipe(Block sourceBlock, Item catalyst, int catalystMeta, int width, int height, int depth, Item targetItem, int targetQuantity, int targetMeta, boolean requiresFloor) {
        this.depth = depth;
        this.sourceBlock = sourceBlock;
        this.width = width;
        this.height = height;
        this.targetQuantity = targetQuantity;
        this.targetMeta = targetMeta;
        this.targetItem = targetItem;
        this.requiresFloor = requiresFloor;
        this.catalyst = catalyst;
        this.catalystMeta = catalystMeta;
    }

    public MiniaturizationRecipe(Block sourceBlock, Item catalyst, int catalystMeta, int width, int height, int depth, Block targetBlock, int targetQuantity, int targetMeta, boolean requiresFloor) {
        this.depth = depth;
        this.sourceBlock = sourceBlock;
        this.width = width;
        this.height = height;
        this.targetQuantity = targetQuantity;
        this.targetMeta = targetMeta;
        this.targetBlock = targetBlock;
        this.requiresFloor = requiresFloor;
        this.catalyst = catalyst;
        this.catalystMeta = catalystMeta;
    }

    public ItemStack getTargetStack() {
        if(targetItem != null) {
            return new ItemStack(targetItem, targetQuantity, targetMeta);
        } else {
            return new ItemStack(targetBlock, targetQuantity, targetMeta);
        }
    }

    public Item getCatalyst() {
        return catalyst;
    }

    public ItemStack getCatalystStack() {
        return new ItemStack(catalyst, 1, catalystMeta);
    }

    public void spawnResultInWorld(World world, BlockPos pos) {
        float posX = pos.getX() + 0.5f - (float)Math.floor((float)this.width / 2.0f);
        float posY = pos.getY() + 1;
        float posZ = pos.getZ() + 0.5f - (float)Math.floor((float)this.depth / 2.0f);

        ItemStack toSpawn = getTargetStack();

        EntityItem entityItem = new EntityItem(world, posX, posY, posZ, toSpawn);
        entityItem.lifespan = 2400;
        entityItem.setPickupDelay(10);

        entityItem.motionX = 0.0f;
        entityItem.motionY = 0.55F;
        entityItem.motionZ = 0.0f;

        world.spawnEntityInWorld(entityItem);
    }

    public Block getSourceBlock() {
        return sourceBlock;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public int getRequiredSourceBlockCount() {
        int outer = width * height * depth;
        int inner = Math.max((width-2) * (height-2) * (depth-2), 0);

        return outer - inner;
    }

    public boolean isRequiresFloor() {
        return requiresFloor;
    }

    public String getTargetName() {
        if(targetItem != null) {
            return targetItem.getUnlocalizedName() + ":" + targetMeta + " x" + targetQuantity;
        } else {
            return targetBlock.getUnlocalizedName() + ":" + targetMeta + " x" + targetQuantity;
        }
    }
}
