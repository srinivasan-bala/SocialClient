/**
 * A class to encapsulate serialization and deserialization of json requests/responses 
 * @author srini
 *
 */

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

public class SerDe {
	/**
	 * A method to create FB request payload for account creation. 
	 * @param acntMap
	 * @param version
	 * @return A HttpPost object which can be executed on a HttpClient or null if required params are missing
	 */
	static HttpPost FBCreateAccountRequest(HashMap<String,String> acntMap,int version)
	{
		if(version==1)
		{
			return FBCreateAccountRequestV1(acntMap);
		}
		return null;
	}
	/**
	 * Internal version 1 implementation of FBCreateAccountRequest
	 * @param acntMap
	 * @return
	 */
	static private HttpPost FBCreateAccountRequestV1(HashMap<String,String> acntMap)
	{
		final List<String> reqkeys= Arrays.asList("name", "currency", "timezone_id","access_token");
		//final List<String> optionalKeys;
		HttpPost post=new HttpPost();
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		
		Iterator<String> reqKeyIter = reqkeys.iterator();
		while(reqKeyIter.hasNext())
		{
			String key=reqKeyIter.next();
			String val=acntMap.get(key);
			if(val==null)
			{
				//Required key is missing; So return null
				return null;
			}else
			{
				nvps.add(new BasicNameValuePair(key,val));
			}
		}
		
		try
		{
			post.setEntity(new UrlEncodedFormEntity(nvps));
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		}
		return post;
	}
	static public boolean FBVerifyCreateAccount(FBRet ret,int version)
	{
		if(version==1)
		{
			return FBVerifyCreateAccountV1(ret);
		}
		return false;
	}
	/**
	 * A method to verify and transform FB account creation payload. The transformed payload
	 * will be in FBRet class  
	 * @param ret
	 * @return success only if valid account has been created.
	 */
	static private boolean FBVerifyCreateAccountV1(FBRet ret)
	{
		if(ret!=null && !ret.isCallSuccessFull && ret.retBufAsStr!=null)
			return false;
		if(ret.isVerified)
			return ret.isValidPayload;
		else
		{
			 ObjectMapper objectMapper = new ObjectMapper();
			 JsonNode rootNode=null;
		     //read JSON like DOM Parser
		     try
		     {
		    	 rootNode = objectMapper.readTree(ret.retBufAsStr);
		     }catch(java.io.IOException e)
		     {
		    	 System.out.println(e.getMessage());
		    	 ret.isVerified=true;
		    	 ret.isValidPayload=false;		         
		     }
			 JsonNode errorNode=rootNode.get("error");
			 if(errorNode!=null && errorNode.has("code"))
			 {
				 //Error has occurred
				 ret.fbErrorCode=errorNode.get("code").asInt();
				 ret.isVerified=true;
				 ret.isValidPayload=false;
			 }else if(rootNode.has("id") && rootNode.has("account_id"))
			 {
				 ret.isVerified=true;
				//For account, there is no need for any transformation
				 ret.transFormedPayload=ret.retBufAsStr;
				 ret.isValidPayload=true;
			 }
			 return ret.isValidPayload;
		}
	}

}
