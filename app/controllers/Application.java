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
        return ok(index.render());
    }

    public static class Login {

        public String phone;
        public String pin;
        public String country;
        public String validate() {
        	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        	PhoneNumber NumberProto = null;
            try {
        	  NumberProto = phoneUtil.parse(phone, country);
        	  phone = phoneUtil.format(NumberProto, PhoneNumberFormat.E164);
        	} catch (Exception e) {
        		flash("login-error", "Error Parsing Phone Number. Please try again.");
        		System.err.println("NumberParseException was thrown: " + e.toString());
        	}
        	User user = User.findUser(phone, Integer.parseInt(pin));
        	if(user == null || user.phone == null || user.phone == ""){
        		return "Error finding user from the supplied phone & pin";
        	}
        	
            return null;
        }
    }

    public static Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
        	List<Country> countryList = new ArrayList<Country>();
            countryList = CountryHelper.getCountryList();
            flash("login-error", "Problem logging in. Please verify your phone number and pin.");
            return badRequest(login.render(Form.form(Login.class), countryList));
        } else {
            session().clear();
            session("phone", loginForm.get().phone);

            return redirect(
                    routes.Dashboard.contacts()
            );
        }
    }

    public static Result login(){
    	List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
        return ok(login.render(Form.form(Login.class), countryList));
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
        String phone = requestData.get("phone");
        int pin = Integer.parseInt(requestData.get("pin"));
        String country = requestData.get("country");
        PhoneNumber NumberProto = null;
        List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
        boolean isValid = true; 
        
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
    	  NumberProto = phoneUtil.parse(phone, country);
    	  phone = phoneUtil.format(NumberProto, PhoneNumberFormat.E164);
    	} catch (Exception e) {
    		isValid = false;
    		flash("login-error", "Error Parsing Phone Number. Please try again.");
    		System.err.println("NumberParseException was thrown: " + e.toString());
    		return badRequest(login.render(Form.form(Login.class), countryList));
    	}
        User bob = User.findByPhone(phone);
        
        if((bob != null && bob.email != null) || !isValid){
        	flash("login-error", "Phone number already registered.");
            return badRequest(login.render(Form.form(Login.class), countryList));
        }else{
            session().clear();
            session("phone", phone);
            bob = User.createUser(phone, pin, country);
        }
        return redirect(
            routes.Profile.editProfile()
        );
    }

	public static Result forgotPassword(){
		List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
    	return ok(forgotPassword.render(countryList));
    }
    
    
    public static Result recoverPassword(){
    	DynamicForm requestData = Form.form().bindFromRequest();
        String phone = requestData.get("phone");
        String country = requestData.get("country");
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	PhoneNumber NumberProto = null;
        try {
    	  NumberProto = phoneUtil.parse(phone, country);
    	  phone = phoneUtil.format(NumberProto, PhoneNumberFormat.E164);
    	} catch (Exception e) {
    		System.err.println("NumberParseException was thrown: " + e.toString());
    	}
        String query = "MATCH (n {phone : \'"+phone+"\'}) RETURN n.pin, n.email;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		String subject = "Your KinCards Pin";
		String pin = "";
		String email = "";
		try{
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			results = (ArrayNode)json;
	        Iterator<JsonNode> it = results.iterator();
	        while (it.hasNext()) {
	        	JsonNode node  = it.next();
	        	pin = node.get("row").get(0).asText();
	        	email = node.get("row").get(1).asText();
	        	String body = "Your KinCards pin is "+pin;
	        	EmailHelper.sendEmail(email, subject, body);
	        }
		}catch (Exception e){
			e.printStackTrace();
		}
		List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
    	return ok(forgotPassword.render(countryList));
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
}
