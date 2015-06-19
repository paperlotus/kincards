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
import helper.VCFHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
    	String phone = requestData.get("phone");
    	String email = requestData.get("email");
    	String userPhone = session().get("phone");
    	String query = "";
    	String resp = "";
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	PhoneNumber NumberProto = null;
        try {
    	  NumberProto = phoneUtil.parse(phone, "US");
    	  phone = phoneUtil.format(NumberProto, PhoneNumberFormat.E164);
    	} catch (Exception e) {
    		System.err.println("NumberParseException was thrown: " + e.toString());
    	}
    	
    	if (phone != null && phone != ""){
    		if(!phone.equalsIgnoreCase(userPhone)){
    			boolean alreadyConnected = checkConnectionStatus(phone, userPhone);
    			if(!alreadyConnected){
	    			query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+userPhone+"\' and b.phone = \'"+phone+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
	    			resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    			}else{
    				flash("request-error", "You are already connected with "+phone);
    			}
    		}else{
    			flash("request-error", "Do you really want to connect with self? Try Meditation!");
    		}
    	}else if(email != null && email != ""){
    		query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+userPhone+"\' and b.email = \'"+email+"\' CREATE (a)-[r:KNOWS]->(b) return r;";
    		resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
    	}else{
    		flash("request-error", "Please enter either Phone Number or Email.");
    		return ok(contact.render());
    	}
        if(resp != null && resp != ""){
	        try {
				JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
				ArrayNode results = (ArrayNode)json;
				String subject = "KinCards Connection Request";
				String body = "<h2 style=\"color: #FF9500;\"> Hola! </h2><p><b>"+session().get("phone")+"</b> would like to add you to his KinCards. Please take necessary action.</p>";
				if (results.size() > 0){
					if(email != "" && email != null){
						EmailHelper.sendEmail(email, subject, body);
					}else if(phone != "" && phone != null){
						query ="MATCH (n{phone:\'"+phone+"\'}) return n.email;";
						resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
											
						json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
						results = (ArrayNode)json;
						
			            Iterator<JsonNode> it = results.iterator();
			            while (it.hasNext()) {
			            	User user = new User();
			            	JsonNode node  = it.next();
			            	user.email = node.get("row").get(0).asText();
			            	EmailHelper.sendEmail(user.email, subject, body);
			            }
					}
					flash("request-error", "Request sent");
				}else{
					flash("request-error", "Unfortunately, your contact has not joined KinCards yet.");
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
			    		String query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+session().get("phone")+"\' and b.email = \'"+email+"\' CREATE (a)-[r:CONNECTED]->(b) return r;";
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

	private static boolean checkConnectionStatus(String phone, String userPhone) {
		String query = "MATCH (a { phone:\'"+phone+"\' })-[r]-(b {phone: \'"+userPhone+"\'}) RETURN r";
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
    	
    	String phone = session().get("phone");
    	String query = "MATCH (in { phone:\'"+phone+"\' })<-[r:KNOWS]-(Account) RETURN Account order by Account.fName";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
        List<User> userList = new ArrayList<User>();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	User user = new User();
            	JsonNode node  = it.next();
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
            	user.linkedIn = node.get("row").findPath("linkedin").asText();
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
    
    public static Result acceptRequest(String phone){
    	phone = "+"+phone;
    	String userPhone = session().get("phone");
    	String subject = "KinCards Connection Request Accepted";
		String body = "Wollah!, "+userPhone+" has accepted your KinContacts connection request";
    	String query = "MATCH (in { phone:\'"+phone+"\' })-[r:KNOWS]-({phone:\'"+userPhone+"\'}) DELETE r;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
        query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+phone+"\' and b.phone = \'"+userPhone+"\' CREATE (a)-[r:CONNECTED]->(b) return a.email;";
        resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	User user = new User();
            	JsonNode node  = it.next();
            	user.email = node.get("row").get(0).asText();
            	EmailHelper.sendEmail(user.email, subject, body);
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
    
public static Result rejectRequest(String phone){
	phone = "+"+phone;
	String userPhone = session().get("phone");
	String query = "MATCH (in { phone:\'"+phone+"\' })-[r:KNOWS]-({phone:\'"+userPhone+"\'}) DELETE r;";
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
	
	public static Result emailContact(String phone, String email){
		File file = VCFHelper.createVCF(phone);
		String fileName = file.getAbsolutePath();
		String userPhone = session().get("phone");
		String subject = userPhone+" has shared a KinCard with you.";
		String body = "";
		EmailHelper.sendEmailWithAttachment(email, subject, body, fileName);
		return ok("Email Sent");
	}
	
	public static Result shareContact(String phone, String email){
		phone = "+"+phone;
		String query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+phone+"\' and (b.email = \'"+email+"\' or b.phone = \'"+email+"\') CREATE (a)-[r:KNOWS]->(b) return b.email, a.email;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			Iterator<JsonNode> it = results.iterator();
			
            while (it.hasNext()) {
            	JsonNode node  = it.next();
				
				String email1 = node.get("row").get(0).asText();
				String email2 = node.get("row").get(1).asText();
				String subject = session().get("phone")+" would like to see you connected.";
				String body = "Connection request has been sent. Please take necessary action.";
				EmailHelper.sendEmail(email1, subject, body);
				body = "Connection request has been sent. You don't have to do anything at this moment.";
				EmailHelper.sendEmail(email2, subject, body);
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
	
	public static Result mergeContacts(){
		DynamicForm requestData = Form.form().bindFromRequest();
    	String phone = requestData.get("phone");
    	String pin = requestData.get("pin");
    	String query = "MATCH (a)-[r:CONNECTED]-(b) WHERE a.phone = \'"+phone+"\' AND a.pin="+pin+" RETURN b.phone";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			Iterator<JsonNode> it = results.iterator();
			
            while (it.hasNext()) {
            	JsonNode node  = it.next();
				String destPhone = node.get("row").get(0).asText();				
				query = "MATCH (a:Account), (b:Account) WHERE a.phone = \'"+phone+"\' and b.phone = \'"+destPhone+"\' CREATE (a)-[r:CONNECTED]->(b) return a.email;";
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
		return ok(contact.render());
	}
}
