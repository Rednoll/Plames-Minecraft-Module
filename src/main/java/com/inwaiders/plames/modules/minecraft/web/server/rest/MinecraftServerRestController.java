package com.inwaiders.plames.modules.minecraft.web.server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@RestController
@RequestMapping("/api/mc/rest")
public class MinecraftServerRestController {

	@Autowired
	private ObjectMapper mapper;
	
	@GetMapping(value = "/servers/{id}")
	public ObjectNode get(@PathVariable long id) {
		
		MinecraftServer server = MinecraftServer.getById(id);
		
		ObjectNode node = mapper.createObjectNode();
		
			node.put("id", server.getId());
			node.put("name", server.getName());
			node.put("address", server.getAddress());
			node.put("port", server.getPort());
			node.put("apiPort", server.getApiPort());
			node.put("secret", server.getSecret());
			node.put("active", server.isActive());
			
		return node;
	}
	
	@PutMapping(value = "/servers/{id}")
	public ResponseEntity save(@PathVariable long id, @RequestBody JsonNode node) {
		
		MinecraftServer server = MinecraftServer.getById(id);
	
		if(server == null) return new ResponseEntity(HttpStatus.NOT_FOUND);
		
		if(node.has("name") && node.get("name").isTextual()) {
		
			server.setName(node.get("name").asText());
		}
		
		if(node.has("address") && node.get("address").isTextual()) {
			
			server.setAddress(node.get("address").asText());
		}

		if(node.has("port") && node.get("port").isNumber()) {
			
			server.setPort(node.get("port").asInt());
		}
		
		if(node.has("apiPort") && node.get("apiPort").isNumber()) {
			
			server.setApiPort(node.get("apiPort").asInt());
		}
		
		if(node.has("secret") && node.get("secret").isTextual()) {
			
			server.setSecret(node.get("secret").asText());
		}
		
		server.save();
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@PostMapping(value = "/servers")
	public ObjectNode create(@RequestBody MinecraftServer server) {

		server.save();
		
		server = MinecraftServer.getById(server.getId());
		
		return get(server.getId());
	}
	
	@DeleteMapping(value = "/servers/{id}")
	public ResponseEntity delete(@PathVariable long id) {
	
		MinecraftServer server = MinecraftServer.getById(id);
		
		if(server != null) {		
			
			server.delete();
					
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}
