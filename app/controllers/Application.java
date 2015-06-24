package controllers;

import helper.CountryHelper;
import helper.CreateSimpleGraph;
import helper.EmailHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        	User user = User.findUser(email, password);
        	if(user == null || user.email == null || user.email.equals("")){
        		return "Error finding user from the supplied email & password";
        	}
            return null;
        }
    }

    public static Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            flash("login-error", "Problem logging in. Please verify your Email and Password.");
            return badRequest(login.render(Form.form(Login.class)));
        } else {
            session().clear();
            session("email", loginForm.get().email);

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

    public static Result register() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String email = requestData.get("email");
        String pin = requestData.get("pin");
        if(email != null && !email.equals("") && pin != null && !pin.equals("")){
        	User bob = User.findByEmail(email);
        
	        if((bob != null && bob.email != null)){
	        	flash("login-error", "Account with this email already exists. Try logging in!");
	            return badRequest(login.render(Form.form(Login.class)));
	        }else{
	            session().clear();
	            session("email", email);
	            bob = User.createUser(email, pin);
	        }
        }else{
        	flash("login-error", "We need Email and Password both to log you in.");
            return badRequest(login.render(Form.form(Login.class)));
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
		String subject = "Your KinCards Pin";
		String pin = "";
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			results = (ArrayNode)json;
	        Iterator<JsonNode> it = results.iterator();
	        while (it.hasNext()) {
	        	JsonNode node  = it.next();
	        	pin = node.get("row").get(0).asText();
	        	String body = "Your KinCards pin is "+pin;
	        	EmailHelper.sendEmail(email, subject, body);
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
		String subject = "Sales request from "+userEmail;
		
		EmailHelper.sendEmail("info@kincards.com", subject, body);
		flash("contact", "Thank you. Someone will get in touch with you.");
		return ok(login.render(Form.form(Login.class)));
	}
	
	public static Result business(){
		return ok(business.render());
	}
}
