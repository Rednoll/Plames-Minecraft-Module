package com.inwaiders.plames.modules.minecraft.web.server.ajax;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwaiders.plames.api.user.User;
import com.inwaiders.plames.modules.market.domain.cart.Cart;
import com.inwaiders.plames.modules.market.domain.stack.ItemStack;
import com.inwaiders.plames.modules.minecraft.MinecraftModule;
import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@RestController
@RequestMapping("api/mc/ajax/server/")
public class MinecraftServerAjaxController {

	
	@PostMapping(value = "/player_cart", consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> getPlayerCart(@RequestBody JsonNode json) {
	
		if(!json.has("server") || !json.get("server").isNumber()) return new ResponseEntity(HttpStatus.BAD_REQUEST);
		
		long serverId = json.get("server").asLong();
		
		MinecraftServer server = MinecraftServer.getById(serverId);
		
		if(server != null) {
			
			String playerName = null;
			UUID playerUUID = null;
			
			if(json.has("player_name") && json.get("player_name").isTextual()) {
				
				playerName = json.get("player_name").asText();
			}
			
			if(json.has("player_uuid") && json.get("player_uuid").isTextual()) {
				
				playerUUID = UUID.fromString(json.get("player_uuid").asText());
			}
			
			MinecraftProfile profile = server.getPlayerProfile(playerName, playerUUID);
			User user = profile.getUser();
			
			Cart cart = Cart.getCart(user, MinecraftModule.getInstance().getTag());
			
			if(cart != null) {
				
				String result = cart.toJson();
		
				if(result != null) {
				
					return new ResponseEntity<String>(result, HttpStatus.OK);
				}
			}
		}
		
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}
	
	@PostMapping(value = "/player_cart/decr", consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> decrItemStackFromPlayerCart(@RequestBody JsonNode json) {
	
		if(!json.has("server") || !json.get("server").isNumber()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		if(!json.has("item_stack_id") || !json.get("item_stack_id").isNumber()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		if(!json.has("quantity") || !json.get("quantity").isNumber()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		
		long serverId = json.get("server").asLong();
		
		MinecraftServer server = MinecraftServer.getById(serverId);
		
		if(server != null) {
			
			String playerName = null;
			UUID playerUUID = null;
			
			if(json.has("player_name") && json.get("player_name").isTextual()) {
				
				playerName = json.get("player_name").asText();
			}
			
			if(json.has("player_uuid") && json.get("player_uuid").isTextual()) {
				
				playerUUID = UUID.fromString(json.get("player_uuid").asText());
			}
			
			MinecraftProfile profile = server.getPlayerProfile(playerName, playerUUID);
			User user = profile.getUser();
			
			Cart cart = Cart.getCart(user, MinecraftModule.getInstance().getTag());
			
			if(cart != null) {
				
				long itemStackId = json.get("item_stack_id").asLong();
				int quantity = json.get("quantity").asInt();
				
				ItemStack itemStack = cart.getItemStackById(itemStackId);
				
					itemStack.decrQuantity(quantity);
					
				itemStack.save();
			}
		}
		
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}
}
