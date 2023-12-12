package com.natamus.alllootdrops.neoforge.events;

import com.natamus.alllootdrops.events.EntityDroppingEvent;
import com.natamus.collective.functions.WorldFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NeoForgeEntityDroppingEvent {
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
