package com.inwaiders.plames.modules.minecraft.web.callback;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;
import com.inwaiders.plames.modules.minecraft.web.callback.impl.MinecraftCallbackNewMessage;

@RestController
@RequestMapping("api/minecraft")
public class MinecraftMessengerWebCallback {

	private Map<String, MinecraftCallback> callbacks = new HashMap<>();
	
	public MinecraftMessengerWebCallback() {
		
		registerCallback(new MinecraftCallbackNewMessage());
	}
	
	@PostMapping(value = "/callback", consumes = "application/json;charset=UTF-8")
	public ResponseEntity newMessage(@RequestBody JsonNode json) {

		if(!json.has("server_id") || !json.get("server_id").isNumber()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		if(!json.has("secret") || !json.get("secret").isTextual()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		if(!json.has("type") || !json.get("type").isTextual()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		if(!json.has("object")) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		
		MinecraftServer server = MinecraftServer.getById(json.get("server_id").asInt());
	
		if(server == null) {
			
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(!server.getSecret().equals(json.get("secret").asText())) return new ResponseEntity(HttpStatus.FORBIDDEN);
		
		MinecraftCallback callback = callbacks.get(json.get("type").asText());
		
		if(callback != null) {
			
			String result = callback.run(server, json.get("object"));
		
			if(result != null && !result.isEmpty()) {
			
				return new ResponseEntity<String>(result, HttpStatus.OK);
			}
			else {
				
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public void registerCallback(MinecraftCallback callback) {
		
		callbacks.put(callback.getType(), callback);
	}
}