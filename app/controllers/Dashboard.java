package controllers;

import play.*;
import play.mvc.*;
import play.data.DynamicForm;
import play.data.Form;
import views.html.*;
import models.*;
import helper.CreateSimpleGraph;
import helper.VCFHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Created by secret on 5/19/15.
 */

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller{
    
	public static Result contacts(){
		
    	String email = session().get("email");
    	String query = "MATCH (a)-[r:CONNECTED]-(b) where a.email=\'"+email+"\' RETURN b order by b.fName";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        int count = 0;
        
        List<User> userList = new ArrayList<User>();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	User user = new User();
            	JsonNode node  = it.next();
            	user.userName = node.get("row").findPath("userName").asText();
            	user.phone = node.get("row").findPath("phone").asText();
            	user.phone2 = node.get("row").findPath("phone2").asText();
            	user.addressLn1 = node.get("row").findPath("addressLn1").asText();
            	user.addressLn2 = node.get("row").findPath("addressLn2").asText();
            	user.city = node.get("row").findPath("city").asText();
            	user.companyLogoId = node.get("row").findPath("companyLogoId").asLong();
            	user.companyName = node.get("row").findPath("companyName").asText();
            	user.designation = node.get("row").findPath("designation").asText();
            	user.email = node.get("row").findPath("email").asText();
            	user.facebook = node.get("row").findPath("facebook").asText();
            	user.fax = node.get("row").findPath("fax").asText();
            	user.fName = node.get("row").findPath("fName").asText();
            	user.linkedIn = node.get("row").findPath("linkedIn").asText();
            	user.lName = node.get("row").findPath("lName").asText();
            	user.photoId = node.get("row").findPath("photoId").asLong();
            	user.state = node.get("row").findPath("state").asText();
            	user.twitter = node.get("row").findPath("twitter").asText();
            	user.website = node.get("row").findPath("website").asText();
            	user.zip = node.get("row").findPath("zip").asText();
            	user.style = node.get("row").findPath("style").asText();
            	userList.add(user);
            }
            query = "MATCH (a)-[r:KNOWS]-(b) where a.email=\'"+email+"\' RETURN count(*)";
            resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
            json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
    		results = (ArrayNode)json;
    		if(results.size()>0){
    			Iterator<JsonNode> ite = results.iterator();
    	        while (ite.hasNext()) {
    	        	JsonNode node  = ite.next();
    	        	count = node.get("row").get(0).asInt();
    	        }
    		}
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return ok(dashboard.render(userList, count));
    }
	
	public static Result exportVCF(String userName){
		File file = VCFHelper.createVCF(userName);
		return ok(file);
	}

}
