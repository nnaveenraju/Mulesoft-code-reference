package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.util.FileUtils;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectorConfig;

public class SFAnalytics implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		//****************************Start***************************************//
		//This block of code loads the dynamic properties based on the mule.env
		Properties prop = new Properties();
		String type = System.getProperty("mule.env"); //<-- here you get local or test
		InputStream input = getClass().getClassLoader().getResourceAsStream(type + ".properties"); // here you get the file 
		prop.load(input);
		//*****************************End****************************************//
		
		//*****************************Start****************************************//
		//Load the required auth properties from the property file 
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String endpoint = prop.getProperty("endpoint");
		//*****************************End****************************************//
		
		//*****************************Start****************************************//
				//Load the required auth properties from the property file
		ConnectorConfig config = new ConnectorConfig();
		config.setUsername(username);
		config.setPassword(password);
		//config.setPassword(secAuthcode);
		config.setAuthEndpoint(endpoint);
		PartnerConnection partnerConnection = new PartnerConnection(config);
		//*****************************End****************************************//
		
		
		
		BufferedReader metadataJson = new BufferedReader(new FileReader("src/main/resources/test.json"));
		
		//String message = org.apache.commons.io.IOUtils.toString(metadataJson);
		String message = "";		
		String sCurrentLine;
		
		while ((sCurrentLine = metadataJson.readLine()) != null) {
			//System.out.println(sCurrentLine);
			message = message + sCurrentLine + "\n";
		}

		
//		byte[] encodedStr = Base64.encodeBase64(message.toString().getBytes());
//		System.out.println("encodedBytes " + new String(encodedStr));
//		
//	
		metadataJson.close();
		InputStream ls = null;
		ls = new FileInputStream("src/main/resources/test.json");
		
		SObject sobj = new SObject();
		sobj.setType("InsightsExternalData"); 
		sobj.setField("Format","Csv");
		sobj.setField("EdgemartAlias", "demo_test");
		sobj.setField("EdgemartContainer", "Integration_App");
		sobj.setField("MetadataJson",FileUtils.readFileToByteArray(new File("src/main/resources/test.json")));
		//sobj.setField("MetadataJson",ls.toString().getBytes());
		//sobj.setField("Operation","overwrite");
		sobj.setField("Operation","upsert");
		sobj.setField("Action","None");

		SaveResult[] results = partnerConnection.create(new SObject[] { sobj });
		
		String parentID = "";
		for(SaveResult sv:results)
		    if(sv.isSuccess())
		        parentID = sv.getId();
		
		SObject sobj1 = new SObject();
		ls.close();
		sobj1.setType("InsightsExternalDataPart"); 
//		sobj1.setField("Format","Csv"); 
		//sobj1.setField("DataFile", FileUtils.readFileToByteArray(new File("C:\\Bisk\\Wave\\upload\\CsvDemoTestData.csv")));
		sobj1.setField("DataFile", eventContext.getMessage().getPayload().toString().getBytes());
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
