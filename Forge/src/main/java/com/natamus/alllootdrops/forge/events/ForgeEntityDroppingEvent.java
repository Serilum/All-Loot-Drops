package com.natamus.alllootdrops.forge.events;

import com.natamus.alllootdrops.events.EntityDroppingEvent;
import com.natamus.collective.functions.WorldFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEntityDroppingEvent {
	@SubscribeEvent
	public static void onWorldLoad(LevelEvent.Load e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		EntityDroppingEvent.onWorldLoad((ServerLevel)level);
	}
	
	@SubscribeEvent
	public static void mobItemDrop(LivingDropsEvent e) {
		LivingEntity livingEntity = e.getEntity();
		EntityDroppingEvent.mobItemDrop(livingEntity.level(), livingEntity, e.getSource());
	}
}
