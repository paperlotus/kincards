package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.data.DynamicForm;
import play.data.Form;
import views.html.*;
import models.*;
import helper.CreateSimpleGraph;
import helper.EmailHelper;
import helper.PasswordHash;
import helper.VCFHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Email;

/**
 * Created by secret on 5/19/15.
 */
@Security.Authenticated(Secured.class)
public class Requests extends Controller {

    public static Result requestContact() throws URISyntaxException{
        //Add code to check if the contact exist in our system, if user already have access to this contact
    	DynamicForm requestData = Form.form().bindFromRequest();
    	String userName = requestData.get("userName");
    	String email = requestData.get("email");
    	String phone = requestData.get("phone");
    	String userEmail = session().get("email");
    	String userUserName = session().get("userName");
    	
    	String query = "";
    	String resp = "";
    	
    	if (userName != null && userName != ""){
    		boolean isConnected = checkConnectionStatusByUserName(userName, userUserName);
    		if(isConnected){
    			flash("request-error", "You are already connected.");
    			return ok(contact.render());
    		}else{
    			query = "MATCH (a:Account), (b:Account) WHERE a.userName = \'"+userUserName+"\' and b.userName = \'"+userName+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
    			resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    		}
    	}else if(email != null && !email.equals("")){
    		boolean isConnected = checkConnectionStatus(email, userEmail);
    		if(isConnected){
    			flash("request-error", "You are already connected.");
    			return ok(contact.render());
    		}else{
	    		query = "MATCH (a:Account), (b:Account) WHERE a.email = \'"+userEmail+"\' and b.email = \'"+email+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
	    		resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    		}
    	}else if (phone != null && phone != ""){
//    		boolean isConnected = checkConnectionStatusByUserName(phone, userUserName);
//    		if(isConnected){
//    			flash("request-error", "You are already connected.");
//    			return ok(contact.render());
//    		}else{
    			query = "MATCH (a:Account), (b:Account) WHERE a.userName = \'"+userUserName+"\' and b.phone = \'"+phone+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
    			resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
//    		}
    	}else{
    		flash("request-error", "Please enter User Name or Email or Phone number.");
    		return ok(contact.render());
    	}
        if(resp != null && resp != ""){
	        try {
				JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
				ArrayNode results = (ArrayNode)json;
				String subject = "KinCards Connection Request";
				String body = session().get("email")+" would like to add you to their KinCards. Please take necessary action <a href=\"http://kincards.com/myRequests\">here</a>.";
				if (results.size() > 0){
					if(email == null || email.equals("")){
						query = "Match (a:Account) where a.userName = \'"+userName+"\' return a.email;";
		    			resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		    			JsonNode json1 = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
						ArrayNode results1 = (ArrayNode)json1;
						if(results1.size()>0){
							Iterator<JsonNode> it = results1.iterator();
					        while (it.hasNext()) {
					        	JsonNode node  = it.next();
					        	email = node.get("row").get(0).asText();
					        }
						}
					}
					EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
					flash("request-error", "Request sent");
				}else{
					if(!email.equals("") && email != null){
						body = session().get("email")+" would like to add you to their KinCards. Kincards is a great way to share, and manage contacts. <a href=\"http://kincards.com/login\">Try us now.</a>";
						EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
						flash("request-error", "Unfortunately, your contact has not joined KinCards yet. We have told your contact that you are missing them here.");
					}else {
						if(!phone.equals("") && phone != null){
							flash("request-error", "We couldn't find any KinCards user with that phone number. Are you sure you typed the correct phone number?");
						}else{
							flash("request-error", "We couldn't find any KinCards user with that user name. Are you sure you typed the correct user name?");
						}
					}
					
				}
				
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

        //Add code to get all pending requests
        return ok(contact.render());
    }
    
    public static Result uploadContact() throws URISyntaxException{
    	uploadVCF();
        return ok(contact.render());
    }
    
    private static boolean uploadVCF() {
    	MultipartFormData body = request().body().asMultipartFormData();
	    MultipartFormData.FilePart contact = body.getFile("contact");
	    String email = "";
	    if (contact != null) {
	        File file = contact.getFile();
	        try {
				VCard vcard = Ezvcard.parse(file).first();
				Iterator<Email> it = vcard.getEmails().iterator();
				while(it.hasNext()){
					email = it.next().getValue();		
					if(email != null && email != ""){
			    		String query = "MATCH (a:Account), (b:Account) WHERE a.email = \'"+session().get("email")+"\' and b.email = \'"+email+"\' CREATE (a)-[r:CONNECTED]->(b) return r;";
			    		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
			    		JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
						ArrayNode results = (ArrayNode)json;
						if(results.size()>0){
							flash("request-error", "Contact added to your dashboard.");
						}else{
							flash("request-error", "Problem adding contact.");
						}
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return false;
	}

	private static boolean checkConnectionStatus(String email, String userEmail) {
		String query = "MATCH (a { email:\'"+email+"\' })-[r]-(b {email: \'"+userEmail+"\'}) RETURN r";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		
		try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			if (results.size() > 0){
				return true;
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean checkConnectionStatusByUserName(String userName, String userUserName) {
		String query = "MATCH (a { userName:\'"+userName+"\' })-[r]-(b {userName: \'"+userUserName+"\'}) RETURN r";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		
		try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			if (results.size() > 0){
				return true;
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public static Result addContact(){
        return ok(contact.render());
    }
    
    public static Result myRequests(){
    	
    	String email = session().get("email");
    	String query = "MATCH (in { email:\'"+email+"\' })<-[r:KNOWS]-(Account) RETURN Account order by Account.fName";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
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
            	userList.add(user);
            	
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	return ok(myRequests.render(userList));
    }
    
    public static Result acceptRequest(String email){
    	String userEmail = session().get("email");
    	String subject = "KinCards Connection Request Accepted";
		String body = userEmail+" has accepted your KinContacts connection request";
    	String query = "MATCH (in { email:\'"+email+"\' })-[r:KNOWS]-({email:\'"+userEmail+"\'}) DELETE r;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
        query = "MATCH (a:Account), (b:Account) WHERE a.email = \'"+email+"\' and b.email = \'"+userEmail+"\' CREATE (a)-[r:CONNECTED]->(b) return a.email;";
        resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	User user = new User();
            	JsonNode node  = it.next();
            	user.email = node.get("row").get(0).asText();
            	EmailHelper.sendEmail(user.email, subject, body, "forgotPassword.ftl");
            	flash("request", "Request Accepted! Contact has been added to your dashboard.");
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return redirect(
                routes.Requests.myRequests()
        );
    }
    
public static Result rejectRequest(String email){
	String userEmail = session().get("email");
	String query = "MATCH (in { email:\'"+email+"\' })-[r:KNOWS]-({email:\'"+userEmail+"\'}) DELETE r;";
    String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
    try {
		JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
		ArrayNode results = (ArrayNode)json;
		if(results.size() > 0){
			flash("request", "Request Rejected!");
		}
		
	} catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return redirect(
            routes.Requests.myRequests()
    );
    }

	public static Result deleteContact(){
		
		return ok();
	}
	
	public static Result emailContact(String email){
		String userName = session().get("userName");
		String subject = userName+" has shared their KinCard with you.";
		String body = "You can see their beautiful KinCard at: <a href=\"http://kincards.com/mycard/"+userName+"\">http://kincards.com/mycard/"+userName+"</a>";
		System.out.println(body);
		EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
		return ok("Email Sent");
	}
	
	public static Result shareContact(String userName, String email){
		String query = "MATCH (a:Account), (b:Account) WHERE a.userName = \'"+userName+"\' and (b.email = \'"+email+"\' or b.phone = \'"+email+"\') CREATE (a)-[r:KNOWS]->(b) return b.email, a.email;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			Iterator<JsonNode> it = results.iterator();
			
            while (it.hasNext()) {
            	JsonNode node  = it.next();
				
				String email1 = node.get("row").get(0).asText();
				String email2 = node.get("row").get(1).asText();
				String subject = session().get("email")+" would like to see you connected.";
				String body = "Connection request has been sent.";
				EmailHelper.sendEmail(email1, subject, body, "forgotPassword.ftl");
				body = "Connection request has been sent. You don't have to do anything at this moment.";
				EmailHelper.sendEmail(email2, subject, body, "forgotPassword.ftl");
				return ok("Email Sent");
            }
		}
		catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ok();
	}
	
	public static Result mergeContacts() throws NoSuchAlgorithmException, InvalidKeySpecException{
		DynamicForm requestData = Form.form().bindFromRequest();
    	String userName = requestData.get("userName");
    	String pin = requestData.get("pin");
    	//Pin Validation code
    	User user = User.findByUserName(userName);
    	if(user != null && user.email != null & !user.email.equals("")){
    		if(PasswordHash.validatePassword(pin, user.pin)){
    			String query = "MATCH (a)-[r:CONNECTED]-(b) WHERE a.userName = \'"+userName+"\' AND a.pin="+pin+" RETURN b.userName";
    			String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    			try{
    				JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
    				ArrayNode results = (ArrayNode)json;
    				Iterator<JsonNode> it = results.iterator();
    				
    	            while (it.hasNext()) {
    	            	JsonNode node  = it.next();
    					String destUserName = node.get("row").get(0).asText();				
    					query = "MATCH (a:Account), (b:Account) WHERE a.userName = \'"+userName+"\' and b.userName = \'"+destUserName+"\' CREATE (a)-[r:CONNECTED]->(b) return a.email;";
    			        resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);	
    	            }
    			}
    			catch (JsonProcessingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	return redirect(
                routes.Profile.getSettings()
        );
	}
	
	public static Result addKinCard(String userName){
		System.out.println("userName="+userName);
		String userUserName = session().get("userName");
    	
    	String query = "";
    	String resp = "";
    	
    	if (userName != null && userName != ""){
    		boolean isConnected = checkConnectionStatusByUserName(userName, userUserName);
    		if(isConnected){
    			flash("request-error", "You are already connected.");
    			return ok(contact.render());
    		}else{
    			query = "MATCH (a:Account), (b:Account) WHERE a.userName = \'"+userUserName+"\' and b.userName = \'"+userName+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
    			resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    			flash("request-error", "Connection Request Sent.");
    		}
    	}else{
    		flash("request-error", "Something went wrong. Please try again.");
    	}
		return ok();
	}
}
