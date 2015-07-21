package controllers;

import helper.CountryHelper;
import helper.CreateSimpleGraph;
import helper.EmailHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import models.Country;
import models.Image;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;


public class Application extends Controller {

    public static Result index() {
    	
        return ok(index2.render());
    }

    public static class Login {

        public String email;
        public String password;
        public String validate() {
        	User user = User.findUser(email, password);
        	if(user == null || user.email == null || user.email.equals("")){
        		return "Error finding user from the supplied email & password";
        	}
            return null;
        }
    }

    public static Result authenticate(String userName, String password) {
//        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
    	userName = formatUserName(userName);
    	User user = User.findUserByUserName(userName, password);
    	if(user == null || user.email == null || user.email.equals("")){
    		return ok("Bad Request");
    	}else {
            session().clear();
            session("email", user.email);
            session("userName", user.userName);
            return redirect(
                    routes.Dashboard.contacts()
            );
        }
    }
    
    public static Result authentication() {
    DynamicForm requestData = Form.form().bindFromRequest();
    String userName = requestData.get("userName");
    String password = requestData.get("password");
  	userName = formatUserName(userName);
  	User user = User.findUserByUserName(userName, password);
  	if(user == null || user.email == null || user.email.equals("")){
  		flash("login-error", "We are not able to find account with supplied user name/password.");
  		return redirect(
               routes.Application.login()
        );
  	}else {
          session().clear();
          session("email", user.email);
          session("userName", user.userName);
          return redirect(
                  routes.Dashboard.contacts()
          );
      }
  }

    public static Result login(){
    	if(session().get("email") != null){
    		return redirect(
                    routes.Dashboard.contacts()
            );
    	}else{
    		return ok(login.render(Form.form(Login.class)));
    	}
    }

    public static Result logout() {
        session().clear();
        flash("logout", "You've been successfully logged out.");
        return redirect(
                routes.Application.index()
        );
    }

    public static Result register(String email, String userName, String pin) {
    	userName = formatUserName(userName);
        if(userName != null && !userName.equals("") && pin != null && !pin.equals("") && email != null && !email.equals("")){
        	User bob = User.findByEmail(email);
	        if(bob != null && bob.email != null){
	        	flash("login-error", "Account with this email already exists. Try logging in!");
	        	return ok("Bad Request");
	        }else{
	        	bob = User.findByUserName(userName);
	        	if(bob != null && bob.userName != null && (bob.userName).equalsIgnoreCase(userName)){
		        	flash("login-error", "This User Name is not available. Please try something else!");
		        	return ok("User Error");
		        }else{
		            session().clear();
		            session("email", email);
		            session("userName", userName);
		            bob = User.createUser(email, userName, pin);
		            String subject = "Welcome to KinCards";
		            String body = "Friend, Welcome to KinCards. You've just joined a community who have discoved how easy and efficient is to share contacts.<br/>You can use your KinCard url: http://kincards.com/mycard/"+userName+" to easily share your contacts with others.<br/><br/> Thank you for joining us.";
		            EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
		        }
	        }
        }else{
        	flash("login-error", "We need User Name and Password both to log you in.");
            return ok("Bad Request");
        }
        return redirect(
            routes.Profile.editProfile()
        );
    }
    
    public static Result registration() {
    	DynamicForm requestData = Form.form().bindFromRequest();
        String userName = requestData.get("userName");
        String pin = requestData.get("password");
        String email = requestData.get("email");
    	userName = formatUserName(userName);
        if(userName != null && !userName.equals("") && pin != null && !pin.equals("") && email != null && !email.equals("")){
        	User bob = User.findByEmail(email);
	        if(bob != null && bob.email != null){
	        	flash("login-error", "Account with this email already exists. Try logging in!");
	        	return redirect(
	                    routes.Application.login()
	                );
	        }else{
	        	bob = User.findByUserName(userName);
	        	if(bob != null && bob.userName != null && (bob.userName).equalsIgnoreCase(userName)){
		        	flash("login-error", "This User Name is not available. Please try something else!");
		        	return redirect(
		                    routes.Application.login()
		                );
		        }else{
		            session().clear();
		            session("email", email);
		            session("userName", userName);
		            bob = User.createUser(email, userName, pin);
		            String subject = "Welcome to KinCards";
		            String body = "Friend, Welcome to KinCards. You've just joined a community who have discoved how easy and efficient is to share contacts.<br/>You can use your KinCard url: http://kincards.com/mycard/"+userName+" to easily share your contacts with others.<br/><br/> Thank you for joining us.";
		            EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
		        }
	        }
        }else{
        	flash("login-error", "We need User Name and Password both to log you in.");
        	return redirect(
                    routes.Application.login()
                );
        }
        return redirect(
            routes.Profile.editProfile()
        );
    }

	public static Result forgotPassword(){
    	return ok(forgotPassword.render());
    }
    
    
    public static Result recoverPassword(){
    	DynamicForm requestData = Form.form().bindFromRequest();
        String userName = requestData.get("userName");
        String query = "MATCH (n {userName : \'"+userName+"\'}) RETURN n.pin, n.email;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		String subject = "Your KinCards Password";
		String pin = "";
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			results = (ArrayNode)json;
			if (results.size() > 0){
		        Iterator<JsonNode> it = results.iterator();
		        while (it.hasNext()) {
		        	JsonNode node  = it.next();
		        	pin = node.get("row").get(0).asText();
		        	String email = pin = node.get("row").get(1).asText();
		        	String body = "You asked for your KinCards password. Here it is: "+pin;
		        	EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
		        }
			}else{
				flash("pin", "We are not able to find your account with this email address. Why don't you try creating an account using this email.");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
    	return ok(forgotPassword.render());
    }
    
    public static Result company(){
    	
    	return ok(company.render());
    }
    
    public static Result privacy(){
    	
    	return ok(privacy.render());
    }

	public static Result terms(){
		
		return ok(terms.render());
	}
	
	public static Result contact(){
		return ok(contactUs.render());
	}
	
	public static Result sendContact(){
		DynamicForm requestData = Form.form().bindFromRequest();
		String body = requestData.get("feedback");
		String userEmail = requestData.get("email");
		String phone = requestData.get("phone");
		String subject = "Sales request from "+userEmail+" ,ph:"+phone;
		
		EmailHelper.sendEmail("info@kincards.com", subject, body, "forgotPassword.ftl");
		flash("login-error", "Thank you for your interest. Someone will get in touch with you shortly.");
		return ok(login.render(Form.form(Login.class)));
	}
	
	public static Result business(){
		return ok(business.render());
	}
	
	public static Result socialConnections() throws Exception{
		SocialAuthConfig config = SocialAuthConfig.getDefault();
		config.load();
		
		SocialAuthManager manager = new SocialAuthManager();
		manager.setSocialAuthConfig(config);
		
		String successUrl = "http://opensource.brickred.com/socialauthdemo/socialAuthSuccessAction.do";
		
		String url = manager.getAuthenticationUrl("yahoo", successUrl);
		redirect(url);
		return ok();
	}
	
	public static Result getMyCard(String userName){
		userName = formatUserName(userName);
		
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
            	userList.add(user);
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return ok(mycard.render(userList));
	}
	
	public static Result getImage(long id) {
        Image image = Image.find.byId(id);
        
        if (image != null) {
            
            /*** here happens the magic ***/
            return ok(image.data).as("image");
            /************************** ***/
            
        } else {
            flash("profile-error", "Picture not found.");
            return redirect(routes.Profile.editProfile());
        }
    }
	
	public static String formatUserName(String userName){
		userName = userName.replaceAll("\\s+","").toLowerCase();
		return userName;
	}
	
	public static Result mktEmails(){
		String subject = "Important changes to your KinCards";
		String body = "";
		String query = "Match (a:Account) where a.email is not null return a;";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
                
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	User user = new User();
            	JsonNode node  = it.next();
            	user.email = "pryadav84@gmail.com";//node.get("row").findPath("email").asText();
            	user.fName = node.get("row").findPath("fName").asText();
            	user.userName = node.get("row").findPath("userName").asText();
            	body = "Dear "+user.fName+", KinCards team is working hard to add great new features for you. We strive hard to make KinCards better each day. <br/> Now you can share your KinCard by your user name:"+user.userName+" or by following url: http://kincards.com/mycard/"+user.userName+". You can also add your KinCards to your website/blog page by copy pasting the code available in your profile <a href=\"http://kincards.com/settings\">settings</a>. Hope you will enjoy these new features.<br/><br/>KinCards Team";
//                EmailHelper.sendEmail(user.email, subject, body, "forgotPassword.ftl");
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ok();
	}
}
