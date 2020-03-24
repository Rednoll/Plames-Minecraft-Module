package com.inwaiders.plames.modules.minecraft.domain.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.inwaiders.plames.api.messenger.MessengerException;
import com.inwaiders.plames.api.messenger.keyboard.MessengerKeyboard;
import com.inwaiders.plames.api.messenger.message.Message;
import com.inwaiders.plames.api.utils.DescribedFunctionResult;
import com.inwaiders.plames.api.utils.DescribedFunctionResult.Status;
import com.inwaiders.plames.domain.messenger.profile.impl.UserProfileBase;
import com.inwaiders.plames.modules.market.domain.cart.Cart;
import com.inwaiders.plames.modules.market.domain.profile.SupportMarket;
import com.inwaiders.plames.modules.minecraft.dao.profile.MinecraftProfileRepository;
import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@Entity
@Table(name = "minecraft_profiles")
public class MinecraftProfile extends UserProfileBase implements SupportMarket {

	private static transient MinecraftProfileRepository repository;
	
	@Column(name = "mojang_uuid")
	private UUID mojangUUID = null;
	
	@Column(name = "player_name")
	private String playerName = null;
	
	@ManyToMany(cascade = CascadeType.MERGE, mappedBy = "members")
	private List<MinecraftServer> servers = new ArrayList<>();
	
	public DescribedFunctionResult collectMarketCart(Cart cart) {
		
		MinecraftServer server = this.getCurrentServer();
		
		if(cart.isEmpty()) {
			
			return new DescribedFunctionResult(Status.OK, "$market.minecraft.cart_empty");
		}
		
		if(server != null) {
			
			return server.collectPlayerCart(this, cart);
		}
		
		return new DescribedFunctionResult(Status.ERROR, "$market.minecraft.server_nf");
	}
	
	public boolean receiveMessage(Message message) {
		
		MinecraftServer server = this.getCurrentServer();
		
		if(server != null) {
			
			return server.sendMessage(message);
		}
		
		return false;
	}
	
	public MinecraftServer getCurrentServer() {
		
		for(MinecraftServer server : servers) {
			
			if(server.checkOnline(this)) {
				
				return server;
			}
		}
		
		return null;
	}
	
	public boolean isOnline() {
		
		return getCurrentServer() != null;
	}
	
	public void joinServer(MinecraftServer server) {
		
		this.servers.add(server);
		server.getMembers().add(this);
	
		this.save();
		server.save();
	}
	
	public void setPlayerName(String name) {
		
		this.playerName = name;
	}

	public String getPlayerName() {
		
		return this.playerName;
	}
	
	public void setMojangUUID(UUID uuid) {
		
		this.mojangUUID = uuid;
	}
	
	public UUID getMojangUUID() {
		
		return this.mojangUUID;
	}
	
	public List<MinecraftServer> getServers() {
		
		return this.servers;
	}
	
	public String getHumanSign() {
		
		return "[mc] Nickname: "+getPlayerName()+" UUID: "+mojangUUID.toString();
	}
	
	@Override
	public String getMessengerType() {

		return "mc";
	}
	
	public void save() {
		
		if(!deleted) {
			
			repository.save(this);
		}
	}
	
	public void delete() {
		
		deleted = true;
		repository.save(this);
	}
	
	public static MinecraftProfile create(UUID mojanUUID, String playerName) {
		
		MinecraftProfile mp = new MinecraftProfile();
			mp.setMojangUUID(mojanUUID);
			mp.setPlayerName(playerName);
		
		mp = repository.save(mp);
		
		return mp;
	}
	
	public static MinecraftProfile getById(long id) {
		
		return repository.getOne(id);
	}
	
	public static MinecraftProfile getByPlayerName(String playerName) {
		
		return repository.getByPlayerName(playerName);
	}
	
	public static MinecraftProfile getByMojangUUID(UUID uuid) {
		
		return repository.getByMojangUUID(uuid);
	}
	
	public static List<MinecraftProfile> getAll() {
	
		return repository.findAll();
	}
	
	public static long getCount() {
		
		return repository.count();
	}
	
	public static void setRepository(MinecraftProfileRepository rep) {
		
		repository = rep;
	}
}
