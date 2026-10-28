package com.serilum.alllootdrops.events;

import com.serilum.alllootdrops.config.ConfigHandler;
import com.serilum.alllootdrops.util.Util;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.functions.TaskFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityDroppingEvent {
	public static void onWorldLoad(ServerLevel level) {
		ItemFunctions.generateEntityDropsFromLootTableOverTime(level);
	}

	public static void mobItemDrop(Level level, Entity entity, DamageSource damageSource) {
		if (level.isClientSide()) {
			return;
		}

		if (entity instanceof Player || !(entity instanceof LivingEntity)) {
			return;
		}

		if (entity instanceof MagmaCube && damageSource.getEntity() instanceof Frog) {
			return;
		}

		EntityType<?> type = entity.getType();
		if (Util.isIgnoredEntityType(type)) {
			return;
		}

		if (GlobalVariables.entityDropStacks == null || !GlobalVariables.entityDropStacks.containsKey(type)) {
			return;
		}

		Entity source = damageSource.getEntity();
		boolean killedByPlayer = source instanceof Player;

		List<ItemStack> dropStacks = Util.getDropStacks(type, killedByPlayer);
		if (dropStacks.isEmpty()) {
			return;
		}

		int amount = ConfigHandler.lootQuantity;
		if (ConfigHandler.lootingEnchantAffectsQuantity && killedByPlayer) {
			int lootinglevel = EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), (LivingEntity)source);
			double increasechance = ConfigHandler.lootingEnchantExtraQuantityChance;
			for (int n = 0; n < lootinglevel; n++) {
				double num = GlobalVariables.random.nextDouble();
				if (num <= increasechance) {
					amount += 1;
				}
			}
		}

		ServerLevel serverLevel = (ServerLevel)level;
		BlockPos epos = entity.blockPosition();
		boolean wasOnFire = entity.isOnFire();
		int finalAmount = amount;
		TaskFunctions.enqueueCollectiveServerTask(serverLevel.getServer(), () -> {
			List<ItemEntity> freshItemEntities = Util.getFreshItemEntitiesAround(serverLevel, epos);

			for (ItemStack dropStack : dropStacks) {
				ItemStack replacementStack = dropStack;
				List<ItemEntity> originalDrops = Util.getItemEntitiesWithItem(freshItemEntities, dropStack.getItem());
				if (originalDrops.isEmpty() && wasOnFire) {
					ItemStack smeltedStack = Util.getSmeltedStack(serverLevel, dropStack);
					if (!smeltedStack.isEmpty()) {
						originalDrops = Util.getItemEntitiesWithItem(freshItemEntities, smeltedStack.getItem());
					}
				}

				boolean keepOriginalDrop = false;
				for (ItemEntity originalDrop : originalDrops) {
					ItemStack originalStack = originalDrop.getItem();
					replacementStack = originalStack;

					if (ConfigHandler.keepOriginalLootQuantityIfHigher && originalStack.getCount() > finalAmount) {
						keepOriginalDrop = true;
						continue;
					}
					originalDrop.remove(Entity.RemovalReason.DISCARDED);
				}

				if (keepOriginalDrop) {
					continue;
				}

				entity.spawnAtLocation(serverLevel, replacementStack.copyWithCount(finalAmount));
			}
		}, 0);
	}
}
