package com.serilum.alllootdrops.util;

import com.serilum.alllootdrops.config.ConfigHandler;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.ConfigFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Util {
	public static boolean isIgnoredEntityType(EntityType<?> entityType) {
		return ConfigFunctions.isInIdList(ConfigHandler.entityTypesToIgnore, EntityType.getKey(entityType).toString());
	}

	public static List<ItemStack> getDropStacks(EntityType<?> entityType, boolean killedByPlayer) {
		List<ItemStack> possibleDropStacks = new ArrayList<>(GlobalVariables.entityDropStacks.get(entityType));
		if (killedByPlayer && GlobalVariables.entityPlayerKillDropStacks.containsKey(entityType)) {
			possibleDropStacks.addAll(GlobalVariables.entityPlayerKillDropStacks.get(entityType));
		}

		List<ItemStack> dropStacks = new ArrayList<>();
		for (ItemStack possibleDropStack : possibleDropStacks) {
			String itemId = BuiltInRegistries.ITEM.getKey(possibleDropStack.getItem()).toString();
			if (!ConfigFunctions.isInIdList(ConfigHandler.itemsToIgnore, itemId)) {
				dropStacks.add(possibleDropStack);
			}
		}
		return dropStacks;
	}

	public static List<ItemEntity> getFreshItemEntitiesAround(Level level, BlockPos pos) {
		List<ItemEntity> freshItemEntities = new ArrayList<>();
		for (Entity entity : level.getEntities(null, new AABB(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+1, pos.getY()+1, pos.getZ()+1))) {
			if (entity instanceof ItemEntity && entity.tickCount <= 1) {
				freshItemEntities.add((ItemEntity)entity);
			}
		}
		return freshItemEntities;
	}

	public static List<ItemEntity> getItemEntitiesWithItem(List<ItemEntity> itemEntities, Item item) {
		List<ItemEntity> itemEntitiesWithItem = new ArrayList<>();
		for (ItemEntity itemEntity : itemEntities) {
			if (itemEntity.getItem().getItem().equals(item)) {
				itemEntitiesWithItem.add(itemEntity);
			}
		}
		return itemEntitiesWithItem;
	}

	public static ItemStack getSmeltedStack(ServerLevel serverLevel, ItemStack itemStack) {
		SingleRecipeInput input = new SingleRecipeInput(itemStack);
		Optional<RecipeHolder<SmeltingRecipe>> optionalRecipe = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, serverLevel);
		if (optionalRecipe.isPresent()) {
			return optionalRecipe.get().value().assemble(input);
		}
		return ItemStack.EMPTY;
	}
}
