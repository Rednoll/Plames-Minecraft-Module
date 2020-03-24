package com.inwaiders.plames.modules.minecraft.web.ajax;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.inwaiders.plames.api.command.Command;
import com.inwaiders.plames.api.command.CommandRegistry;

@RestController
@RequestMapping("api/minecraft/ajax")
public class CommandsWebAjax {

	@Autowired
	private ObjectMapper mapper = null;
	
	@GetMapping(value = "/commands")
	public ResponseEntity<ArrayNode> getCommandsList() {
		
		Set<Command> commands = CommandRegistry.getAll();
	
		ArrayNode result = mapper.createArrayNode();
	
			for(Command command : commands) {
				
				for(String aliase : command.getAliases()) {
					
					result.add(aliase);
				}
			}
		
		return new ResponseEntity<ArrayNode>(result, HttpStatus.OK);
	}
}
