package controllers;

import helper.CountryHelper;
import helper.CreateSimpleGraph;
import helper.EmailHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import models.Country;
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
        	System.out.println("email="+email+" ,password="+password);
        	User user = User.findUser(email, password);
        	if(user == null || user.email == null || user.email.equals("")){
        		return "Error finding user from the supplied email & password";
        	}
            return null;
        }
    }

    public static Result authenticate(String email, String password) {
//        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
    	User user = User.findUser(email, password);
    	if(user == null || user.email == null || user.email.equals("")){
    		return ok("Bad Request");
    	}else {
            session().clear();
            session("email", user.email);
            return redirect(
                    routes.Dashboard.contacts()
            );
        }
    }

    public static Result login(){
        return ok(login.render(Form.form(Login.class)));
    }

    public static Result logout() {
        session().clear();
        flash("logout", "You've been successfully logged out.");
        return redirect(
                routes.Application.index()
        );
    }

    public static Result register(String email, String pin) {
        if(email != null && !email.equals("") && pin != null && !pin.equals("")){
        	User bob = User.findByEmail(email);
        
	        if((bob != null && bob.email != null)){
	        	flash("login-error", "Account with this email already exists. Try logging in!");
//	            return badRequest(login.render(Form.form(Login.class)));
	        	return ok("Bad Request");
	        }else{
	            session().clear();
	            session("email", email);
	            bob = User.createUser(email, pin);
	            String subject = "Welcome to KinCards";
	            String body = "Friend, Welcome to KinCards. You've just joined a community who have discoved how easy and efficient is to share contacts.<br/><br/> Thank you for joining us.";
	            EmailHelper.sendEmail(email, subject, body, "forgotPassword.ftl");
	        }
        }else{
        	flash("login-error", "We need Email and Password both to log you in.");
            return ok("Bad Request");
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
        String email = requestData.get("email");
        String query = "MATCH (n {email : \'"+email+"\'}) RETURN n.pin;";
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
		System.out.println("Hello");
		SocialAuthConfig config = SocialAuthConfig.getDefault();
		config.load();
		
		SocialAuthManager manager = new SocialAuthManager();
		manager.setSocialAuthConfig(config);
		
		String successUrl = "http://opensource.brickred.com/socialauthdemo/socialAuthSuccessAction.do";
		
		String url = manager.getAuthenticationUrl("yahoo", successUrl);
		System.out.println("url="+url);
		redirect(url);
		return ok();
	}
}
