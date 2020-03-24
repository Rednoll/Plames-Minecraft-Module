package com.inwaiders.plames.modules.minecraft.domain.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inwaiders.plames.api.locale.PlamesLocale;
import com.inwaiders.plames.api.messenger.message.Message;
import com.inwaiders.plames.api.utils.DescribedFunctionResult;
import com.inwaiders.plames.api.utils.DescribedFunctionResult.Status;
import com.inwaiders.plames.modules.market.domain.cart.Cart;
import com.inwaiders.plames.modules.market.domain.item.Item;
import com.inwaiders.plames.modules.market.domain.market.Market;
import com.inwaiders.plames.modules.market.domain.stack.ItemStack;
import com.inwaiders.plames.modules.minecraft.dao.server.MinecraftServerRepository;
import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;

@Cache(region = "messengers-additionals-cache-region", usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "minecraft_servers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MinecraftServer {

	private static transient MinecraftServerRepository repository; 
	
	private static transient ObjectMapper jsonMapper = new ObjectMapper();
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(name = "name")
	private String name = null;
	
	@Column(name = "active")
	private boolean active = false;
	
	@Column(name = "address")
	private String address = null;

	@Column(name = "port")
	private int port = -1;
	
	@Column(name = "api_port")
	private int apiPort = -1;
	
	@Column(name = "secret")
	private String secret = null;
	
	@Column(name = "deleted")
	private volatile boolean deleted = false;
	
	@Column(name = "player_find_strategy")
	@Enumerated(EnumType.STRING)
	private PlayerPickStrategy playerFindStrategy = PlayerPickStrategy.PLAYER_NAME;
	
	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "minecraft_profiles_servers_mtm", joinColumns = @JoinColumn(name = "minecraft_server_id"), inverseJoinColumns = @JoinColumn(name = "mineсraft_profile_id"))
	private Set<MinecraftProfile> members = new HashSet<>();
	
	@Column(name = "market_id")
	private Long marketId = null;
	
	@Transient
	private Market market = null;
	
	@PostUpdate
	public void postUpdate() {
		
		if(market != null) {
			
			market.save();
		}
	}
	
	@PostRemove
	public void postDelete() {
		
		if(market != null) {
			
			market.delete();
		}
	}
	
	public void syncMarketItems(String receivedTotalHash, List<String> receivedHashes) {
		
		List<String> myHashes = calcItemsHashes();
		String myTotalHash = calcItemsTotalHash(myHashes);
	
		if(myTotalHash.equals(receivedTotalHash)) return;
	
		Map<String, Item> hashItemsMap = createHashItemMap();	
		
		List<String> newHashes = new ArrayList<>();
		
			for(String receivedHash : receivedHashes) {
		
				if(!myHashes.contains(receivedHash)) {
					
					newHashes.add(receivedHash);
				}
			}
			
		for(String newHash : newHashes) {
			
			ObjectNode itemData = requestItemData(newHash);
			
			Item item = Item.getByMetadata(itemData.get("metadata").toString());
			
			if(item == null) {
			
				item = Item.create();
					item.setTargetApplicationName("mc");
					item.setName(itemData.get("name").asText());
					item.getAliases().add(item.getName());
					
					if(itemData.has("aliases")) {
						
						ArrayNode jsonAliases = (ArrayNode) itemData.get("aliases");
					
						for(JsonNode jsonAliase : jsonAliases) {
							
							item.getAliases().add(jsonAliase.asText());
						}
					}
					
					item.setMetadata(itemData.get("metadata").toString());
			
				Logger.getLogger(MinecraftServer.class).info(PlamesLocale.getSystemMessage("$market.minecraft.create_item", item.getId(), item.getName()));
			}
			
			market.getItems().add(item);
			item.save();
			
			Logger.getLogger(MinecraftServer.class).info(PlamesLocale.getSystemMessage("$market.minecraft.add_item_to_market", item.getId(), item.getName(), getId(), getName()));
		}
			
		List<String> deletedHashes = new ArrayList<>();
		
			for(String myHash : myHashes) {
				
				if(!receivedHashes.contains(myHash)) {
					
					deletedHashes.add(myHash);
				}
			}
		
		for(String deletedHash : deletedHashes) {
			
			Item item = hashItemsMap.get(deletedHash);
		
			Logger.getLogger(MinecraftServer.class).info(PlamesLocale.getSystemMessage("$market.minecraft.remove_item_from_market", item.getId(), item.getName(), getId(), getName()));
			
			market.getItems().remove(item);
		}
		
		market.save();
	}
	
	private Map<String, Item> createHashItemMap() {
	
		Map<String, Item> result = new HashMap<>();
	
		if(market != null) {
			
			List<Item> items = market.getItems();
		
			for(Item item : items) {
				
				result.put(DigestUtils.md5Hex(item.getMetadata()), item);
			}
		}
		
		return result;
	}
	
	private String calcItemsTotalHash() {
		
		return calcItemsTotalHash(calcItemsHashes());
	}
	
	private String calcItemsTotalHash(List<String> hashes) {
		
		if(hashes != null) {
			
			return DigestUtils.sha256Hex(String.join("", hashes));
		}
		
		return null;
	}
	
	private List<String> calcItemsHashes() {
	
		Market market = getMarket();
		
		if(market != null) {
			
			List<Item> items = market.getItems();
			List<String> hashes = new ArrayList<>();
			
			for(Item item : items) {
				
				hashes.add(DigestUtils.md5Hex(item.getMetadata()));
			}
			
			return hashes;
		}
		
		return null;
	}
	
	public boolean fromUser(MinecraftProfile profile, String text) {
		
		if(!isActive()) return false;
		
		profile.fromUser(text);
		
		return true;
	}
	
	public void setMarket(Market market) {
		
		this.market = market;
	}
	
	public Market getMarket() {
		
		if(market == null) {
			
			if(marketId == null) {
				
				market = Market.create("mc");
				
				if(market != null) {
					
					marketId = market.getId();
					market.save();
					save();
				}
			}
			else {
				
				market = Market.getById(marketId);
			}
		}
		
		return this.market;
	}
	
	public MinecraftProfile getPlayerProfile(String playerName, UUID mojangUUID) { 
		
		MinecraftProfile mcProfile = null;
		
		if(getPlayerFindStrategy() == PlayerPickStrategy.PLAYER_NAME) {
			
			mcProfile = MinecraftProfile.getByPlayerName(playerName);
		}
		
		if(getPlayerFindStrategy() == PlayerPickStrategy.MOJANG_UUID) {
			
			mcProfile = MinecraftProfile.getByMojangUUID(mojangUUID);
		}
		
		return mcProfile;
	}
	
	public DescribedFunctionResult collectPlayerCart(MinecraftProfile profile, Cart cart) {
		
		Collection<ItemStack> itemStacks = cart.getItemStacks();

		ObjectNode data = jsonMapper.createObjectNode();
			data.put("secret", getSecret());
			data.put("player_name", profile.getPlayerName());
			data.put("player_uuid", profile.getMojangUUID().toString());
			
			try {
				
				data.put("cart",  jsonMapper.readTree(cart.toJson()));
			}
			catch(JsonMappingException e1) {
				
				e1.printStackTrace();
				return new DescribedFunctionResult(Status.ERROR, "$market.minecraft.cart.serialization_ex");
			}
			catch(JsonProcessingException e1) {
				
				e1.printStackTrace();
				return new DescribedFunctionResult(Status.ERROR, "$market.minecraft.cart.serialization_ex");
			}
				
		try {
			
			HttpPost post = new HttpPost(getMethodUrl("collect_cart"));
				post.setEntity(new StringEntity(data.toString(), "UTF-8"));
				
			CloseableHttpClient httpClient = HttpClients.createDefault();
				
			CloseableHttpResponse response = httpClient.execute(post);
			String rawResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
			int statusCode = response.getStatusLine().getStatusCode();
			
			EntityUtils.consume(response.getEntity());
			
			if(statusCode == HttpStatus.SC_OK) {
				
				ArrayNode collectedItemStacks = (ArrayNode) jsonMapper.readTree(rawResponse);
				
				for(JsonNode element : collectedItemStacks) {
					
					ObjectNode collectedItemStack = (ObjectNode) element;
				
					long id = collectedItemStack.get("id").asLong();
					int quantity = collectedItemStack.get("quantity").asInt();
					
					ItemStack is = cart.getItemStackById(id);
				
					if(is != null) {
						
						is.setQuantity(is.getQuantity()-quantity);
						is.save();
					}
					else {
						
						//TODO: ADD ROLLBACK! IMPORTANT!
					}
				}
				
				return null;
			}
			else {
				
				return new DescribedFunctionResult(Status.ERROR, "$server.error");
			}
		
		} 
		catch(IOException e) {
			
			e.printStackTrace();
			return new DescribedFunctionResult(Status.ERROR, "$server.error");
		}
	}
	
	public boolean isAvailable() {
		
		if(!isActive()) return false;
		
		try {
			
			URLConnection con = new URL(getMethodUrl("/")).openConnection();
				con.connect();
		}
		catch(IOException e) {
			
			return false; 
		}
		
		return true;
	}
	
	public ObjectNode requestItemData(String hash) {
		
		if(!isActive()) return null;
		if(!isAvailable()) return null;
		
		ObjectNode data = jsonMapper.createObjectNode();
			data.put("secret", getSecret());
			data.put("item_hash", hash);
			
		try {
			
			HttpPost get = new HttpPost(getMethodUrl("items"));
				get.setEntity(new StringEntity(data.toString(), "UTF-8"));

			CloseableHttpClient httpClient = HttpClients.createDefault();
				
			CloseableHttpResponse response = httpClient.execute(get);
		
				int statusCode = response.getStatusLine().getStatusCode();
			
				if(statusCode == HttpStatus.SC_OK) {
			
					String rawResponseData = EntityUtils.toString(response.getEntity(), "UTF-8");
					
					EntityUtils.consume(response.getEntity());
					
					ObjectNode jsonResponseData = (ObjectNode) jsonMapper.readTree(rawResponseData); 
					
					return jsonResponseData;
				}
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean checkOnline(MinecraftProfile profile) {
		
		if(!isActive()) return false;
		if(!isAvailable()) return false;
		
		ObjectNode data = jsonMapper.createObjectNode();
			data.put("secret", getSecret());
			data.put("player_name", profile.getPlayerName());
			data.put("player_uuid", profile.getMojangUUID().toString());
		
		try {
			
			HttpURLConnection con = (HttpURLConnection) new URL(getMethodUrl("check_online")).openConnection();
				con.setRequestMethod("GET");
				con.setDoOutput(true);
				con.setDoInput(true);
				
				OutputStream os = con.getOutputStream();
				
					os.write(jsonMapper.writeValueAsBytes(data));

				os.close();
				
				InputStream is = con.getInputStream();
		
					boolean result = is.read() == 1;
				
				is.close();
				
			return result;
			
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean sendMessage(Message message) {
		
		if(!isActive()) return false;
		
		MinecraftProfile profile = (MinecraftProfile) message.getReceiver();
		String text = message.getDisplayText();
		
		boolean result = sendMessage(profile.getPlayerName(), profile.getMojangUUID(), text);
	
		if(result) {
			
			message.setDelivered(true);
			message.setDeliveryDate(System.currentTimeMillis());
		}
		
		return result;
	}
	
	private boolean sendMessage(String playerName, UUID mojangUUID, String text) {
		
		if(!isActive()) return false;

		try {
			
			ObjectNode data = jsonMapper.createObjectNode();
				data.put("secret", getSecret());
				data.put("text", URLEncoder.encode(text, "UTF-8"));
				data.put("player_name", playerName);
				data.put("player_uuid", mojangUUID.toString());
			
			HttpPost post = new HttpPost(getMethodUrl("send_message"));
				post.setEntity(new StringEntity(data.toString(), "UTF-8"));

			CloseableHttpClient httpClient = HttpClients.createDefault();
				
			CloseableHttpResponse response = httpClient.execute(post);

				int statusCode = response.getStatusLine().getStatusCode();
				
				EntityUtils.consume(response.getEntity());
				
				if(statusCode != HttpStatus.SC_OK) {
					
					return false;
				}
				
			return true;
		} 
		catch(IOException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	private String getMethodUrl(String method) {
		
		StringBuilder builder = new StringBuilder();
			builder.append("http://");
			builder.append(getAddress()+":"+getApiPort());
			builder.append("/"+method);
			
		return builder.toString();
	}

	public String getDescription(PlamesLocale locale) {
		
		String result = "";
		
		boolean available = isAvailable();
		
		if(available) {
		
			int allPlayersCount = allPlayersCount();
			
			result += locale.getMessage("minecraft_server.description.players_count", allPlayersCount, getMembers().size());
			result += "<br/>";
			
			double rawMpp = calcMembersPerPlayers();
			
			String mpp = String.valueOf(rawMpp);
			
			if(mpp.length() > 4) {
				
				mpp = mpp.substring(0, 4);
			}
			
			result += "MPP: "+mpp;
			result += "<br/>";
			result += locale.getMessage("minecraft_server.description.online_count", onlinePlayersCount());
		}
		else {
			
			result += "<span style=\"color: red;\">"+locale.getMessage("minecraft_server.description.server_offline")+"</span>";
			result += "<br/>";
			result += "<span style=\"color: red;\">------------------------</span>";
		}
		
		result += "<br/>";
		result += locale.getMessage("minecraft_server.description.address", getAddress());
		result += "<br/>";
		result += locale.getMessage("minecraft_server.description.port", getPort());
		result += "<br/>";
		result += locale.getMessage("minecraft_server.description.api_port", getApiPort());
		result += "<br/>";
		result += locale.getMessage("minecraft_server.description.secret", getSecret());
		
		return result;
	}
	
	public int allPlayersCount() {
		
		if(!isActive()) return -1;
		
		ObjectNode data = jsonMapper.createObjectNode();
		
			data.put("secret", getSecret());
		
		try {
			
			HttpURLConnection con = (HttpURLConnection) new URL(getMethodUrl("players_count")).openConnection();
				con.setRequestMethod("GET");
				con.setDoOutput(true);
				con.setDoInput(true);
				
				OutputStream os = con.getOutputStream();
				
					os.write(jsonMapper.writeValueAsBytes(data));

				os.close();
				
				InputStream is = con.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				
					int result = dis.readInt();
				
				dis.close();
				
			return result;
			
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int onlinePlayersCount() {
		
		if(!isActive()) return -1;
		
		ObjectNode data = jsonMapper.createObjectNode();
		
			data.put("secret", getSecret());
		
		try {
			
			HttpURLConnection con = (HttpURLConnection) new URL(getMethodUrl("online_count")).openConnection();
				con.setRequestMethod("GET");
				con.setDoOutput(true);
				con.setDoInput(true);
				
				OutputStream os = con.getOutputStream();
				
					os.write(jsonMapper.writeValueAsBytes(data));

				os.close();
				
				InputStream is = con.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				
					int result = dis.readInt();
				
				dis.close();
				
			return result;
			
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public double calcMembersPerPlayers() {
		
		double membersCount = getMembers().size();
		double playersCount = allPlayersCount();
	
		if(playersCount == 0) {
			
			return 0;
		}
		
		return membersCount / playersCount;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public void setActive(boolean active) {
		
		this.active = active;
	}
	
	public boolean isActive() {
		
		return this.active;
	}
	
	public Set<MinecraftProfile> getMembers() {
	
		return this.members;
	}
	
	public void setSecret(String secret) {
		
		this.secret = secret;
	}
	
	public String getSecret() {
		
		return this.secret;
	}
	
	public void setApiPort(int port) {
		
		this.apiPort = port;
	}
	
	public int getApiPort() {
		
		return this.apiPort;
	}
	
	public void setPort(int port) {
		
		this.port = port;
	}
	
	public int getPort() {
		
		return this.port;
	}
	
	public void setAddress(String address) {
		
		this.address = address;
	}
	
	public String getAddress() {
		
		return this.address;
	}
	
	public void setPlayerFindStrategy(PlayerPickStrategy i) {
		
		this.playerFindStrategy = i;
	}
	
	public PlayerPickStrategy getPlayerFindStrategy() {
		
		return this.playerFindStrategy;
	}
	
	public Long getId() {
		
		return this.id;
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
	
	public static MinecraftServer create() {
		
		MinecraftServer server = new MinecraftServer();
		
		server = repository.save(server);
		
		return server;
	}
	
	public static MinecraftServer getById(long id) {
		
		return repository.getOne(id);
	}
	
	public static List<MinecraftServer> getAll() {
	
		return repository.findAll();
	}
	
	public static long getCount() {
		
		return repository.count();
	}
	
	public static void setRepository(MinecraftServerRepository rep) {
		
		repository = rep;
	}
	
	public static enum PlayerPickStrategy {
		PLAYER_NAME, MOJANG_UUID;
	}
}
