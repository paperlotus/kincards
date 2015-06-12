package helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import models.Country;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;

public class CountryHelper {
	
	public static List<Country> getCountryList() {
    	String query = "MAtch n where n:Country RETURN n";
        String resp = CreateSimpleGraph.sendTransactionalCypherQuery(query);
        List<Country> countryList = new ArrayList<Country>();
        
        try {
			JsonNode json = new ObjectMapper().readTree(resp).findPath("results").findPath("data");
			ArrayNode results = (ArrayNode)json;
			
			Iterator<JsonNode> it = results.iterator();
            while (it.hasNext()) {
            	Country country = new Country();
            	JsonNode node  = it.next();
            	country.name = node.get("row").findPath("name").asText();
            	country.code = node.get("row").findPath("code").asText();
            	countryList.add(country);
            }				
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return countryList;
	}
}
