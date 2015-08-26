package controllers;

import play.*;
import play.mvc.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import views.html.*;
import models.*;
import helper.CountryHelper;
import helper.CreateSimpleGraph;
import helper.EmailHelper;
import helper.PasswordHash;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
public class Profile extends Controller{

    public static Result editProfile(){
        User user = null;
        String email = session().get("email"); 
        if(email != null && !email.equals("")) {
            user = User.findByEmail(email);
        }
        List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
        
        return ok(profile.render(user, countryList));
    }

	public static Result updateProfile(){
        
    	DynamicForm requestData = Form.form().bindFromRequest();

        String email = session().get("email");
        String phone = requestData.get("phone");
        String phone2 = requestData.get("phone2");
        String fName = requestData.get("fName");
        String lName = requestData.get("lName");
        String twitter = requestData.get("twitter");
        String facebook = requestData.get("facebook");
        String linkedin = requestData.get("linkedin");
        String addressLn1 = requestData.get("add1");
        String addressLn2 = requestData.get("add2");
        String city = requestData.get("city");
        String state = requestData.get("state");
        long zip = 0;
        if(requestData.get("zip") != null && !requestData.get("zip").equals("")){
        	zip = Long.parseLong(requestData.get("zip"));
        }
        long fax = 0;
        if(requestData.get("fax") != null && !requestData.get("fax").equals("")){
        	fax = Long.parseLong(requestData.get("fax"));
        }
        String companyName = requestData.get("cName");
        String designation = requestData.get("desig");
        String website = requestData.get("website");
        String country = requestData.get("country");
        long photoId = 0;
        photoId = upload("profile-pic");
        long companyLogoId = 0;
        companyLogoId = upload("cPic");
        String query = "";
        if(photoId > 0 && companyLogoId > 0){
	        query = "MATCH (n {email : \'"+email+"\'}) SET n.phone2 = \'"+phone2+"\', n.phone = \'"+phone+"\', n.fName = \'"+fName+"\', n.lName = \'"+lName+"\', n.twitter = \'"+twitter+"\', n.facebook = \'"+facebook+"\', "
	        		+ "n.linkedIn = \'"+linkedin+"\', n.addressLn1 = \'"+addressLn1+"\', n.addressLn2 = \'"+addressLn2+"\', n.city = \'"+city+"\',"
	        		+ "n.state = \'"+state+"\', n.zip = "+zip+", n.fax = "+fax+", n.companyName = \'"+companyName+"\',"
	        		+ "n.designation = \'"+designation+"\', n.website = \'"+website+"\', "
	        		+ "n.photoId = \'"+photoId+"\', n.companyLogoId = \'"+companyLogoId+"\', "
	        		+ "n.country = \'"+country+"\' RETURN n;";
        }else if(photoId > 0 && companyLogoId == 0){
        	query = "MATCH (n {email : \'"+email+"\'}) SET n.phone2 = \'"+phone2+"\', n.phone = \'"+phone+"\', n.fName = \'"+fName+"\', n.lName = \'"+lName+"\', n.twitter = \'"+twitter+"\', n.facebook = \'"+facebook+"\', "
	        		+ "n.linkedIn = \'"+linkedin+"\', n.addressLn1 = \'"+addressLn1+"\', n.addressLn2 = \'"+addressLn2+"\', n.city = \'"+city+"\',"
	        		+ "n.state = \'"+state+"\', n.zip = "+zip+", n.fax = "+fax+", n.companyName = \'"+companyName+"\',"
	        		+ "n.designation = \'"+designation+"\', n.website = \'"+website+"\', "
	        		+ "n.photoId = \'"+photoId+"\', "
	        		+ "n.country = \'"+country+"\' RETURN n;";
        }else if(companyLogoId > 0 && photoId == 0){
        	query = "MATCH (n {email : \'"+email+"\'}) SET n.phone2 = \'"+phone2+"\', n.phone = \'"+phone+"\', n.fName = \'"+fName+"\', n.lName = \'"+lName+"\', n.twitter = \'"+twitter+"\', n.facebook = \'"+facebook+"\', "
	        		+ "n.linkedIn = \'"+linkedin+"\', n.addressLn1 = \'"+addressLn1+"\', n.addressLn2 = \'"+addressLn2+"\', n.city = \'"+city+"\',"
	        		+ "n.state = \'"+state+"\', n.zip = "+zip+", n.fax = "+fax+", n.companyName = \'"+companyName+"\',"
	        		+ "n.designation = \'"+designation+"\', n.website = \'"+website+"\', "
	        		+ " n.companyLogoId = \'"+companyLogoId+"\', "
	        		+ "n.country = \'"+country+"\' RETURN n;";
        }else if(companyLogoId == 0 && photoId == 0){
        	query = "MATCH (n {email : \'"+email+"\'}) SET n.phone2 = \'"+phone2+"\', n.phone = \'"+phone+"\', n.fName = \'"+fName+"\', n.lName = \'"+lName+"\', n.twitter = \'"+twitter+"\', n.facebook = \'"+facebook+"\', "
	        		+ "n.linkedIn = \'"+linkedin+"\', n.addressLn1 = \'"+addressLn1+"\', n.addressLn2 = \'"+addressLn2+"\', n.city = \'"+city+"\',"
	        		+ "n.state = \'"+state+"\', n.zip = "+zip+", n.fax = "+fax+", n.companyName = \'"+companyName+"\',"
	        		+ "n.designation = \'"+designation+"\', n.website = \'"+website+"\', "
	        		+ "n.country = \'"+country+"\' RETURN n;";
        }
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        User user = new User();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
            Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	JsonNode node  = it.next();
            	user.email = node.get("row").findPath("email").asText();
            	if(user.email.equalsIgnoreCase(email)){
            		flash("profile-error", "Profile Successfully Updated");
            	}else{
            		flash("profile-error", "Error Updating Profile");
            	}
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return redirect(
                routes.Dashboard.contacts()
        );
    }

	private static long upload(String name) {
		
		MultipartFormData body = request().body().asMultipartFormData();
	    MultipartFormData.FilePart picture = body.getFile(name);
	    if (picture != null && picture.getFilename() != null) {
	        String fileName = session().get("email")+name+picture.getFilename();
	        File file = picture.getFile();
	        Image image = new Image(fileName, file);
	        return image.id;
	    } else {
	        flash("profile-error", "Problem with file upload");
	        return 0;
	    }
	}
	
	public static Result getSettings() throws JsonProcessingException, IOException{
		String query = "MATCH (p:Account) where p.email=\'"+session().get("email")+"\' return p.privacy;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
		ArrayNode results = (ArrayNode)json;
		Boolean privacy = false;
		if(results.size()>0){
			Iterator<JsonNode> it = results.iterator();
	        while (it.hasNext()) {
	        	JsonNode node  = it.next();
	        	if(node.get("row").get(0).asText().equalsIgnoreCase("on")){
	        		privacy = true;
	        	}
	        }
		}
		return ok(settings.render(privacy));
	}
	
	public static Result updateSettings() throws NoSuchAlgorithmException, InvalidKeySpecException{
		DynamicForm requestData = Form.form().bindFromRequest();
		
		String email = session().get("email");
		String pin = requestData.get("pin");
		String password = PasswordHash.createHash(pin);
		String query = "MATCH (n {email : \'"+email+"\'}) SET n.pin = \'"+password+"\';";
		CreateSimpleGraph.sendTransactionalCypherQuery(query);
		flash("pin", "Successfully updated your password");
		
		return redirect(
                routes.Profile.getSettings()
        );
	}
	
	public static Result feedback(){
		return ok(feedback.render());
	}
	
	public static Result sendFeedback(){
		DynamicForm requestData = Form.form().bindFromRequest();
		String body = requestData.get("feedback");
		String userEmail = session().get("email");
		String subject = "Feedback from "+userEmail;
		
		EmailHelper.sendEmail("info@kincards.com", subject, body, "forgotPassword.ftl");
		return ok(feedback.render());
	}
	
	public static Result deleteProfile(){
		DynamicForm requestData = Form.form().bindFromRequest();
		String delete = requestData.get("delete");
		if(delete.equalsIgnoreCase("DELETE")){
			String query = "MATCH (p {email: \'"+session().get("email")+"\'})-[r]-() DELETE p, r;";
			CreateSimpleGraph.sendTransactionalCypherQuery(query);
			session().clear();
	        flash("logout", "Damn! You have permanentaly deleted your profile.");
	        return redirect(
	                routes.Application.index()
	        );
		}else{
			flash("pin", "Do you really want to DELETE your Profile? Try typing the correct text again.");
			return redirect(
	                routes.Profile.getSettings()
	        );
		}
	}
	
	public static Result updatePublicSettings(){
		
		DynamicForm requestData = Form.form().bindFromRequest();
		String pub = requestData.get("public");
		if(pub != null && !pub.equals("")){
			String query = "MATCH (p:Account) where p.email=\'"+session().get("email")+"\' set p.privacy = 'on' return p;";
			CreateSimpleGraph.sendTransactionalCypherQuery(query);
		}else{
			String query = "MATCH (p:Account) where p.email=\'"+session().get("email")+"\' set p.privacy = 'off' return p;";
			CreateSimpleGraph.sendTransactionalCypherQuery(query);
		}
		
		return redirect(
                routes.Profile.getMyCard(session().get("userName"))
        );
	}
	
	public static Result getMyCard(String userName) throws JsonProcessingException, IOException{
		userName = userName.replaceAll("\\s+","").toLowerCase();
		
    	String query = "MATCH (a:Account) where a.userName=\'"+userName+"\' RETURN a";
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
            	user.phone2 = node.get("row").findPath("phone2").asText();
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
            	user.privacy = node.get("row").findPath("privacy").asText();
            	userList.add(user);
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String query1 = "MATCH (p:Account) where p.email=\'"+session().get("email")+"\' return p.privacy;";
		String resp1 = CreateSimpleGraph.sendTransactionalCypherQuery(query1);
		JsonNode json = new ObjectMapper().readTree(resp1).findPath("results").findPath("data");
		ArrayNode results = (ArrayNode)json;
		Boolean privacy = false;
		if(results.size()>0){
			Iterator<JsonNode> it = results.iterator();
	        while (it.hasNext()) {
	        	JsonNode node  = it.next();
	        	if(node.get("row").get(0).asText().equalsIgnoreCase("on")){
	        		privacy = true;
	        	}
	        }
		}
        
        return ok(myKinCard.render(userList, privacy));
	}
}
