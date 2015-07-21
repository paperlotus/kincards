package helper;

import java.io.File;
import java.util.Iterator;

import models.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.Address;
import ezvcard.property.Kind;
import ezvcard.property.StructuredName;

public class VCFHelper {

	public static File createVCF(String userName) {
		String query = "MATCH (n {userName : \'"+userName+"\'}) RETURN n;";
		String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
		File file = null;
		try{
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
            	user.linkedIn = node.get("row").findPath("linkedIn").asText();
            	user.lName = node.get("row").findPath("lName").asText();
            	user.photoId = node.get("row").findPath("photoId").asLong();
            	user.state = node.get("row").findPath("state").asText();
            	user.twitter = node.get("row").findPath("twitter").asText();
            	user.website = node.get("row").findPath("website").asText();
            	user.zip = node.get("row").findPath("zip").asLong();
            	
            	VCard vcard = new VCard();
                vcard.setKind(Kind.individual());
                StructuredName n = new StructuredName();
                n.setFamily(user.lName);
                n.setGiven(user.fName);
                vcard.setStructuredName(n);
                vcard.addTitle(user.designation);
                vcard.setOrganization(user.companyName);
                
                Address adr = new Address();
                adr.setStreetAddress(user.addressLn1 + user.addressLn2);
                adr.setLocality(user.city);
                adr.setRegion(user.state);
                adr.setPostalCode(String.valueOf(user.zip));
                adr.setCountry(user.country);
                vcard.addAddress(adr);
                vcard.addTelephoneNumber(user.phone);
                vcard.addEmail(user.email);
                vcard.addUrl(user.website);
                
              //write vCard
                file = new File(user.fName+"_"+user.phone+".vcf");
                
                VCardVersion version = VCardVersion.V3_0;
                VCardWriter writer = new VCardWriter(file, version);

                writer.write(vcard);
                writer.close();
            }
		}catch (Exception e){
			e.printStackTrace();
		}
		return new File(file.getAbsolutePath());
	}
}
