package com.inwaiders.plames.modules.minecraft.web.server.ajax;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwaiders.plames.api.locale.PlamesLocale;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@RestController
@RequestMapping("web/controller/ajax/mc/server")
public class MinecraftServerWebAjax {

	@PostMapping("/active")
	public ResponseEntity activeToggle(@RequestBody JsonNode json) {
		
		if(!json.has("server") || !json.get("server").isNumber()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		if(!json.has("active") || !json.get("active").isBoolean()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
	
		long serverId = json.get("server").asLong();
		boolean active = json.get("active").asBoolean();
	
		MinecraftServer server = MinecraftServer.getById(serverId);
		
		if(server != null) {
			
			server.setActive(active);
			server.save();
		
			return new ResponseEntity(HttpStatus.OK);
		}
		
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}
	
	@PostMapping(value = "/description", consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> description(@RequestBody JsonNode json) {
	
		if(!json.has("server") || !json.get("server").isNumber()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		
		long serverId = json.get("server").asLong();
		
		MinecraftServer server = MinecraftServer.getById(serverId);
		
		if(server != null) {
			
			try {
				
				return new ResponseEntity<String>(server.getDescription(PlamesLocale.getSystemLocale()), HttpStatus.OK);
			}
			catch(Exception e) {
				
				return new ResponseEntity<String>("Ошибка загрузки данных", HttpStatus.OK);
			}
		}
		
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}
}
