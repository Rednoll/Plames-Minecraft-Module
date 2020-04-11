package com.inwaiders.plames.modules.minecraft.web.server.ajax;

import java.util.concurrent.ForkJoinPool;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@RestController
@RequestMapping("web/controller/ajax/long_poll/mc/server")
public class MinecraftServerWebAjaxLongPoll {

	@GetMapping("/available/{id}")
	public DeferredResult<ResponseEntity<Boolean>> available(@PathVariable long id) {
		
		DeferredResult<ResponseEntity<Boolean>> output = new DeferredResult<ResponseEntity<Boolean>>(3600000L);
		
		MinecraftServer server = MinecraftServer.getById(id);
		
		if(server != null) {
			
			ForkJoinPool.commonPool().submit(()-> {
				
				output.setResult(new ResponseEntity<Boolean>(server.isAvailable(), HttpStatus.OK));
			});
		}
		else {
			
			output.setResult(new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND));
		}
		
		return output;
	}
}
