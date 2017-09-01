package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.util.FileUtils;
import com.sforce.ws.ConnectorConfig;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import org.apache.commons.codec.binary.Base64;

public class Working_OfSFAnalytics implements Callable {

	//access codes
	private String username = "naveen-raju@bisk.com.skywaydev";
	private String password = "Vishal3@";
	private String endpoint = "https://test.salesforce.com/services/Soap/u/31.0";
//	private String secAuthcode = "7tLyrkxlkSeaxhNTXAkB2c39u";
	
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		ConnectorConfig config = new ConnectorConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setAuthEndpoint(endpoint);

		PartnerConnection partnerConnection = new PartnerConnection(config);
		
		BufferedReader metadataJson = new BufferedReader(new FileReader("C:\\Mulesoft_ws_new\\demo\\src\\main\\resources\\test.json"));
		
		String message = "";		
		String sCurrentLine;
		
		while ((sCurrentLine = metadataJson.readLine()) != null) {
			//System.out.println(sCurrentLine);
			message = message + sCurrentLine + "\n";
		}
//		JSONObject json = new JSONObject(sb.toString());
		
//		byte[] encodedBytes = Base64.encodeBase64(metadataJson.toString().getBytes());
//		System.out.println("encodedBytes " + new String(encodedBytes));
		
		metadataJson.close();
		
		SObject sobj = new SObject();
		sobj.setType("InsightsExternalData"); 
		sobj.setField("Format","Csv");
		sobj.setField("EdgemartAlias", "demo");
		//sobj.setField("MetadataJson",encodedBytes);
		sobj.setField("Operation","overwrite");
		sobj.setField("Action","None");

		SaveResult[] results = partnerConnection.create(new SObject[] { sobj });
		
		String parentID = "";
		for(SaveResult sv:results)
		    if(sv.isSuccess())
		        parentID = sv.getId();
		
		SObject sobj1 = new SObject();

		sobj1.setType("InsightsExternalDataPart"); 
		sobj1.setField("DataFile", FileUtils.readFileToByteArray(new File("C:\\Bisk\\Wave\\upload\\CsvDemoTestData.csv")));
		sobj1.setField("InsightsExternalDataId", parentID);
		sobj1.setField("PartNumber",1); 
		
	    //sobj.setField("PartNumber",1); //Part numbers should start at 1    
	    SaveResult[] results1 = partnerConnection.create(new SObject[] { sobj1 });
	    
	    String rowId="";
		for(SaveResult sv:results1)
	        if(sv.isSuccess())
	            rowId = sv.getId();
	    
//		List<File> fileParts = chunkBinary(""); //Split the file

/*		for(int i = 0;i<fileParts.size();i++)
		{
		    sobj.setType("InsightsExternalDataPart"); 
		    sobj.setField("DataFile", FileUtils.readFileToByteArray(fileParts.get(i)));
		    sobj.setField("InsightsExternalDataId", parentID);
		    obj.setField("PartNumber",i+1); //Part numbers should start at 1    
		    SaveResult[] results = partnerConnection.create(new SObject[] { sobj });
		    for(SaveResult sv:results)
		        if(sv.isSuccess())
		            rowId = sv.getId();
		}
*/
		SObject sobj2 = new SObject();
		sobj2.setType("InsightsExternalData");
		sobj2.setField("Action","Process");
		sobj2.setId(parentID); // This is the rowID from the previous example.
		SaveResult[] results3 = partnerConnection.update(new SObject[] { sobj2 });
		 
		for(SaveResult sv:results3)
		    if(sv.isSuccess())
		        rowId = sv.getId();
		
		return rowId;
	}
	
	  
}
