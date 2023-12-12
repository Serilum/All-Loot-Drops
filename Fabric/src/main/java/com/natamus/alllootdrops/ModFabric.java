package com.natamus.alllootdrops;

import com.natamus.alllootdrops.events.EntityDroppingEvent;
import com.natamus.alllootdrops.util.Reference;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		ServerWorldEvents.LOAD.register((MinecraftServer server, ServerLevel serverLevel) -> {
			EntityDroppingEvent.onWorldLoad(serverLevel);
		});

		CollectiveEntityEvents.ON_ENTITY_IS_DROPPING_LOOT.register((Level level, Entity entity, DamageSource damageSource) -> {
			EntityDroppingEvent.mobItemDrop(level, entity, damageSource);
		});
	}

	private static void setGlobalConstants() {

	}
}
