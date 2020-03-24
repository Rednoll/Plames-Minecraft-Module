package com.inwaiders.plames.modules.minecraft.web.callback.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwaiders.plames.domain.messenger.profile.procedures.ProfileBindProcedure;
import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer.PlayerPickStrategy;
import com.inwaiders.plames.modules.minecraft.web.callback.MinecraftCallback;

public class MinecraftCallbackNewMessage extends MinecraftCallback{

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String run(MinecraftServer server, JsonNode json) {
	
		String text = null;
		
		try {
			
			text = URLDecoder.decode(json.get("text").asText(), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
		
			e.printStackTrace();
		}
		
		UUID mojangUUID = UUID.fromString(json.get("player_uuid").asText());
		String playerName = json.get("player_name").asText();
		
		MinecraftProfile profile = null;
		
		if(server.getPlayerFindStrategy() == PlayerPickStrategy.PLAYER_NAME) {
			
			profile = MinecraftProfile.getByPlayerName(playerName);
		}
		
		if(server.getPlayerFindStrategy() == PlayerPickStrategy.MOJANG_UUID) {
			
			profile = MinecraftProfile.getByMojangUUID(mojangUUID);
		}
		
		if(profile == null) {
			
			profile = MinecraftProfile.create(mojangUUID, playerName);
				profile.joinServer(server);
		}
		
		server.fromUser(profile, text);
		
		return null;
	}

	@Override
	public String getType() {
	
		return "new_message";
	}
}
