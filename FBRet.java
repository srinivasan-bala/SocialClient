/**
 * A class to encapsulate all the returned values of FB connection calls
 * @author srini
 *
 */
public class FBRet {
	public boolean isCallSuccessFull; 
	public boolean isVerified;
	public boolean isValidPayload;
	public int fbErrorCode;
	public String retBufAsStr; /*Will be valid only if isCallSuccessFull*/
	public String transFormedPayload;	
	FBRet()
	{
		isCallSuccessFull=false;
		fbErrorCode=-1;
		isVerified=false;
		isValidPayload=false;
	}
}
