package helper;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Neo4jClient {
	
	private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data";
    private static final String AUTH_PARAMS = "Basic bmVvNGo6cDJzc3cwcmQ=";
    
    public static String postNeoData(String query){
    	final String txUri = SERVER_ROOT_URI + "/transaction/commit";
        WebResource resource = Client.create().resource(txUri);
        String payload = "{\"statements\" : [ {\"statement\" : \"" +query + "\"} ]}";
        
        ClientResponse response = resource
        		.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .header("Authorization", AUTH_PARAMS)
                .entity(payload)
                .get(ClientResponse.class);
                
        String resp = String.format(
              "POST [%s] to [%s], status code [%d], returned data: "
              + System.getProperty( "line.separator" ) + "%s",
              payload, txUri, response.getStatus(),
              response.getEntity( String.class ) );
      
        response.close();
        
    	return resp;
    }
    
    public static String getNeoData(String query){
		
    	
    	return query;
    }

}
