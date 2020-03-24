package com.inwaiders.plames.modules.minecraft.web.ajax;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@RestController
@RequestMapping("api/minecraft/ajax/item")
public class ItemsWebAjax {

	@Autowired
	private ObjectMapper mapper = null;
	
	@PostMapping(value = "/sync")
	public ResponseEntity<String> sync(@RequestBody ObjectNode json) throws JsonMappingException, JsonProcessingException {
		
		if(!json.has("server_id") || !json.get("server_id").isNumber()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		if(!json.has("secret") || !json.get("secret").isTextual()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		
		MinecraftServer server = MinecraftServer.getById(json.get("server_id").asInt());
	
		if(server == null) {
			
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(!server.getSecret().equals(json.get("secret").asText())) return new ResponseEntity(HttpStatus.FORBIDDEN);
	
		String totalHash = json.get("total_hash").asText();
		List<String> hashes = mapper.readValue(json.get("hashes").toString(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));

		server.syncMarketItems(totalHash, hashes);
		
		return new ResponseEntity<String>(HttpStatus.OK);
	}
}