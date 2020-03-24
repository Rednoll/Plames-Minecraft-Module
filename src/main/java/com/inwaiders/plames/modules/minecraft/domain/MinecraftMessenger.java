package com.inwaiders.plames.modules.minecraft.domain;

import javax.persistence.Entity;

import com.inwaiders.plames.api.locale.PlamesLocale;
import com.inwaiders.plames.domain.messenger.impl.MessengerImpl;
import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@Entity
public class MinecraftMessenger extends MessengerImpl<MinecraftProfile>{
	
	@Override
	public String getWebDescription() {
		
		return "- "+PlamesLocale.getSystemMessage("messenger.minecraft.description.profiles", MinecraftProfile.getCount())+"<br/>- "+PlamesLocale.getSystemMessage("messenger.minecraft.description.servers", MinecraftServer.getCount());
	}

	@Override
	public String getName() {
		
		return "minecraft";
	}
	
	@Override
	public String getType() {
		
		return "mc";
	}
}
