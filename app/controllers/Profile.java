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
        User bob = User.findByPhone(phone);
        List<Country> countryList = new ArrayList<Country>();
        countryList = CountryHelper.getCountryList();
        
        return ok(profile.render(bob, countryList));
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
	
	public static Result getSettings(){
		return ok(settings.render());
	}
	
	public static Result updateSettings(){
		DynamicForm requestData = Form.form().bindFromRequest();
		
		String email = session().get("email");
		String pin = requestData.get("pin");
		String query = "MATCH (n {email : \'"+email+"\'}) SET n.pin = "+pin+";";
		CreateSimpleGraph.sendTransactionalCypherQuery(query);
		flash("pin", "Successfully updated your pin");
		
		return ok(settings.render());
	}
	
	public static Result feedback(){
		return ok(feedback.render());
	}
	
	public static Result sendFeedback(){
		DynamicForm requestData = Form.form().bindFromRequest();
		String body = requestData.get("feedback");
		String userEmail = session().get("email");
		String subject = "Feedback from "+userEmail;
		
		EmailHelper.sendEmail("info@kincards.com", subject, body);
		return ok(feedback.render());
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
			return ok(settings.render());
		}
	}
}
