import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.net.URI;
/**
 * FBDirectConnector: A class which encapsulates direct connection to fb and is used to
 * invoke FB api's. This class internally uses a connection pool to manage a pool of connections
 * to FB. This class uses SerDe to handle formating of request and interpretation of responses
 * @author srini
 *
 */
public class FBDirectConnector {
	PoolingHttpClientConnectionManager mConMgr;
	CloseableHttpClient mHttpClient;
	String mFbToken;
	int mConTimeOutInMs;
	static final String mAccntURL="https://graph.facebook.com/me/adaccounts";
	URI mAccntURI;
	FBDirectConnector(int numOfConnections,int timeOutInMs,String fbtoken)
	{
		mConMgr=new PoolingHttpClientConnectionManager();
		mConMgr.setMaxTotal(numOfConnections);
		mConMgr.setDefaultMaxPerRoute(20);
		mHttpClient= HttpClients.custom()
				.setConnectionManager(mConMgr)
				.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(timeOutInMs).build())
				.build();
		mFbToken=new String(fbtoken);
		mConTimeOutInMs=timeOutInMs;
		try
		{
			mAccntURI=new URI(mAccntURL);
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
		}		
	}
	
	/**
	 * Create a FB account 
	 * @param map
	 * @return
	 */
	FBRet createAccount(HashMap<String,String> accntMap) throws ConnectionPoolTimeoutException
	{
		//Add access token to the map
		accntMap.put("access_token", mFbToken);
		HttpPost post=SerDe.FBCreateAccountRequest(accntMap, 1);
		FBRet fbRet=new FBRet();
		if(post!=null)
		{
			
			post.setURI(mAccntURI);
	        HttpContext context = new BasicHttpContext();
	        try {
	        	    System.out.println("Doing aget on " + post.getURI());
	                CloseableHttpResponse response = mHttpClient.execute(post, context);
	                try {
	                    System.out.println("FB call got executed");
	                    // get the response body as an array of bytes
	                    HttpEntity entity = response.getEntity();
	                    
	                    if (entity != null) {
	                        fbRet.retBufAsStr = EntityUtils.toString(entity);
	                        if(fbRet.retBufAsStr!=null)
	                        {
	                        	fbRet.isCallSuccessFull=true;
		                    }
	                        //System.out.println(id + " - " + bytes.length + " bytes read");
	                    }else 
	                    {
	                    	fbRet.isCallSuccessFull=false; //Entity is null
	                    }
	                } finally {
	                    response.close();
	                }
	            }catch(ConnectionPoolTimeoutException conPolE) 
	            {
	            	System.out.println("Connection pooltimeout");
	            	throw conPolE;
	            }
	        	catch (Exception e) {
	                System.out.println(" - error: " + e);
	            }
		}
		return fbRet;	
	}

    public static void main(String[] args)
    {
    	FBDirectConnector fbDircon=new FBDirectConnector(10,30,"CAAEDOFD5jeEBAJuIUXVm6OU1bsP7QelAvJCRzQUZBYZAwZBKKaAZAjVIRlfTlahP1xk9ZAZBrTRMUZBXCtS3EO2F3yqt5qvcnFJoZAlHePZAyDA6VzxSBZBXGmSBwuHInp5mPho7rtZClVc5BkqO01BhxXyJ6wQM9Kw3ewQ5tGymi5AZA7jtQMe2bGgA");
    	HashMap<String,String> acntMap=new HashMap<String,String>();
    	acntMap.put("name","sriniAccount");
    	acntMap.put("currency","USD");
    	acntMap.put("timezone_id","1");
    	FBRet retVal=null;
    	try
    	{
    		retVal=fbDircon.createAccount(acntMap);
    		if(SerDe.FBVerifyCreateAccount(retVal))
    		{
    			System.out.println("<----AccountCreationSuccessfull---->");
    			System.out.println(retVal.transFormedPayload);
    		}else
    		{
       			System.out.println("----AccountCreationFailed----");
    			System.out.println(retVal.retBufAsStr);    			
    		}
    	}catch(ConnectionPoolTimeoutException TimeOutEx)
    	{
    		//retVal=fbInDirCon.createAccount(acntMap);st
    		System.out.println("ConnectionPool Timeout");
    	}
     	System.out.println("Reached End");
    }

}
