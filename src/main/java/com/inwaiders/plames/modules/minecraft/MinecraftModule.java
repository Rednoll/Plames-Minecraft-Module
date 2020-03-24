package com.inwaiders.plames.modules.minecraft;

import com.inwaiders.plames.api.application.ApplicationAgent;
import com.inwaiders.plames.api.locale.PlamesLocale;
import com.inwaiders.plames.domain.messenger.impl.MessengerImpl;
import com.inwaiders.plames.domain.module.impl.ModuleBase;
import com.inwaiders.plames.modules.market.domain.cart.CartHlRepository;
import com.inwaiders.plames.modules.market.domain.cart.CartImpl;
import com.inwaiders.plames.modules.minecraft.domain.MinecraftMessenger;

public class MinecraftModule extends ModuleBase implements ApplicationAgent {

	private static MinecraftModule instance = new MinecraftModule();
	
	private MinecraftModule() {
		
	}
	
	@Override
	public void preInit() {

		CartHlRepository.addRepository(new CartImpl.HighLevelRepository("mc"));
	}
	
	@Override
	public void init() {
		
		MessengerImpl mess = MessengerImpl.getByType("mc");
	
		if(mess == null) {
			
			MinecraftMessenger mc = new MinecraftMessenger();
			
			mc.save();
		}
	}
	
	public String getDescription() {
		
		return PlamesLocale.getSystemMessage("module.minecraft.description");
	}

	@Override
	public String getName() {
		
		return "Minecraft Integration";
	}

	@Override
	public String getLicenseKey() {
		
		return "";
	}

	@Override
	public long getId() {
		
		return 55816;
	}

	@Override
	public String getType() {
		
		return "integration";
	}

	@Override
	public String getVersion() {
		
		return "1V";
	}

	@Override
	public long getSystemVersion() {
		
		return 0;
	}

	public static MinecraftModule getInstance() {
		
		return instance;
	}

	@Override
	public String getDisplayName() {
		
		return "Minecraft";
	}

	@Override
	public String getTag() {
		
		return "mc";
	}
}
