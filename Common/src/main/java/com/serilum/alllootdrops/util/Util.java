package com.serilum.alllootdrops.util;

import com.serilum.alllootdrops.config.ConfigHandler;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.ConfigFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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

	public static List<ItemStack> getDropStacks(Level level, EntityType<?> entityType, boolean killedByPlayer) {
		List<ItemStack> possibleDropStacks = new ArrayList<>(GlobalVariables.entityDropStacks.get(entityType));
		if (killedByPlayer && GlobalVariables.entityPlayerKillDropStacks.containsKey(entityType)) {
			possibleDropStacks.addAll(GlobalVariables.entityPlayerKillDropStacks.get(entityType));
		}

		Registry<Item> itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);
		List<ItemStack> dropStacks = new ArrayList<>();
		for (ItemStack possibleDropStack : possibleDropStacks) {
			String itemId = itemRegistry.getKey(possibleDropStack.getItem()).toString();
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
		SimpleContainer container = new SimpleContainer(itemStack);
		Optional<SmeltingRecipe> optionalRecipe = serverLevel.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, serverLevel);
		if (optionalRecipe.isPresent()) {
			return optionalRecipe.get().assemble(container, serverLevel.registryAccess());
		}
		return ItemStack.EMPTY;
	}
}
