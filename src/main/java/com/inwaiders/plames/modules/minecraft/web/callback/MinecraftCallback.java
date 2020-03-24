package com.inwaiders.plames.modules.minecraft.web.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

public abstract class MinecraftCallback {

	public abstract String run(MinecraftServer server, JsonNode json);

	public abstract String getType();
}
