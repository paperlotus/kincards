package models;

import helper.CreateSimpleGraph;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import play.db.ebean.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class User extends Model {

    public String phone;
    public String phone2;
    public String pin;
    public String email;
    public String fName;
    public String lName;
    public Date	lastSignedIn;
    public Date	createDate;
    public String twitter;
    public String facebook;
    public String linkedIn;
    public String addressLn1;
    public String addressLn2;
    public String city;
    public String state;
    public long zip;
    public long fax;
    public String companyName;
    public String designation;
    public String website;
    public long photoId;
    public long companyLogoId;
    public boolean active;
    public String country;
    
    public static User findUser(String email, String pin){
    	String query = "MATCH (ee:Account) WHERE ee.email = \'"+email+"\' and ee.pin = "+pin+" RETURN ee;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        User user = new User();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	JsonNode node  = it.next();
            	
            	user.phone = node.get("row").findPath("phone").asText();
            	user.phone2 = node.get("row").findPath("phone2").asText();
                user.pin = node.get("row").findPath("pin").asText();
                user.addressLn1 = node.get("row").findPath("addressLn1").asText();
                user.addressLn2 = node.get("row").findPath("addressLn2").asText();
                user.city = node.get("row").findPath("city").asText();
                user.companyLogoId = node.get("row").findPath("companyLogoId").asLong();
                user.companyName = node.get("row").findPath("companyName").asText();
                user.designation = node.get("row").findPath("designation").asText();
                user.email = node.get("row").findPath("email").asText();
                user.facebook = node.get("row").findPath("facebook").asText();
                user.fax = node.get("row").findPath("fax").asLong();
                user.fName = node.get("row").findPath("fName").asText();
                user.linkedIn = node.get("row").findPath("linkedIn").asText();
                user.lName = node.get("row").findPath("lName").asText();
                user.photoId = node.get("row").findPath("photoId").asLong();
                user.state = node.get("row").findPath("state").asText();
                user.twitter = node.get("row").findPath("twitter").asText();
                user.website = node.get("row").findPath("website").asText();
                user.zip = node.get("row").findPath("zip").asLong();
                user.active = node.get("row").findPath("active").asBoolean();
                user.country = node.get("row").findPath("country").asText();
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
    }
    
    public static User findByPhone(String phone){
    	
    	String query = "MATCH (ee:Account) WHERE ee.phone = \'"+phone+"\' RETURN ee;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        User user = new User();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	JsonNode node  = it.next();
            	
            	user.phone = node.get("row").findPath("phone").asText();
            	user.phone2 = node.get("row").findPath("phone2").asText();
                user.pin = node.get("row").findPath("pin").asText();
                user.addressLn1 = node.get("row").findPath("addressLn1").asText();
                user.addressLn2 = node.get("row").findPath("addressLn2").asText();
                user.city = node.get("row").findPath("city").asText();
                user.companyLogoId = node.get("row").findPath("companyLogoId").asLong();
                user.companyName = node.get("row").findPath("companyName").asText();
                user.designation = node.get("row").findPath("designation").asText();
                user.email = node.get("row").findPath("email").asText();
                user.facebook = node.get("row").findPath("facebook").asText();
                user.fax = node.get("row").findPath("fax").asLong();
                user.fName = node.get("row").findPath("fName").asText();
                user.linkedIn = node.get("row").findPath("linkedIn").asText();
                user.lName = node.get("row").findPath("lName").asText();
                user.photoId = node.get("row").findPath("photoId").asLong();
                user.state = node.get("row").findPath("state").asText();
                user.twitter = node.get("row").findPath("twitter").asText();
                user.website = node.get("row").findPath("website").asText();
                user.zip = node.get("row").findPath("zip").asLong();
                user.active = node.get("row").findPath("active").asBoolean();
                user.country = node.get("row").findPath("country").asText();
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
    }
    
    public static User findByEmail(String email){
    	
    	String query = "MATCH (ee:Account) WHERE ee.email = \'"+email+"\' RETURN ee;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        User user = new User();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	JsonNode node  = it.next();
            	
            	user.phone = node.get("row").findPath("phone").asText();
            	user.phone2 = node.get("row").findPath("phone2").asText();
                user.pin = node.get("row").findPath("pin").asText();
                user.addressLn1 = node.get("row").findPath("addressLn1").asText();
                user.addressLn2 = node.get("row").findPath("addressLn2").asText();
                user.city = node.get("row").findPath("city").asText();
                user.companyLogoId = node.get("row").findPath("companyLogoId").asLong();
                user.companyName = node.get("row").findPath("companyName").asText();
                user.designation = node.get("row").findPath("designation").asText();
                user.email = node.get("row").findPath("email").asText();
                user.facebook = node.get("row").findPath("facebook").asText();
                user.fax = node.get("row").findPath("fax").asLong();
                user.fName = node.get("row").findPath("fName").asText();
                user.linkedIn = node.get("row").findPath("linkedIn").asText();
                user.lName = node.get("row").findPath("lName").asText();
                user.photoId = node.get("row").findPath("photoId").asLong();
                user.state = node.get("row").findPath("state").asText();
                user.twitter = node.get("row").findPath("twitter").asText();
                user.website = node.get("row").findPath("website").asText();
                user.zip = node.get("row").findPath("zip").asLong();
                user.active = node.get("row").findPath("active").asBoolean();
                user.country = node.get("row").findPath("country").asText();
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
    }
    
    public static User createUser(String email, String pin){
    	String query = "CREATE (n:Account {email : \'"+email+"\' , pin : "+pin+", createDate : \'"+new Date()+"\' , active : true }) RETURN n;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        User user = new User();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	JsonNode node  = it.next();
            	
            	user.phone = node.get("row").findPath("email").asText();
                user.pin = node.get("row").findPath("pin").asText();
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
    }

}