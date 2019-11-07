package picocli.nativeimage.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.jansi.graalvm.AnsiConsole;



import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.util.concurrent.Callable;

@Command(name = "httpsget", mixinStandardHelpOptions = true,
        version = "httpsget 4.0",
        description = "Uses https protocol to get a remote resource.")
public class HttpsClient implements Callable<Integer> {
    private static final String DEFAULT_URL = "https://github.com/remkop/picocli-native-image-demo/blob/master/java.security.overrides";

    @Parameters(description = "The URL to download", defaultValue = DEFAULT_URL)
    URL url;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new HttpsClient()).execute(args);
        System.exit(exitCode);
    }

    public Integer call() throws Exception {
       HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			
       //dumpl all cert info
       print_https_cert(con);
			
       //dump all the content
       print_content(con);
      
       return 0;
   }
	
   private void print_https_cert(HttpsURLConnection con){
     
    if(con!=null){
			
      try {
				
	System.out.println("Response Code : " + con.getResponseCode());
	System.out.println("Cipher Suite : " + con.getCipherSuite());
	System.out.println("\n");
				
	Certificate[] certs = con.getServerCertificates();
	for(Certificate cert : certs){
	   System.out.println("Cert Type : " + cert.getType());
	   System.out.println("Cert Hash Code : " + cert.hashCode());
	   System.out.println("Cert Public Key Algorithm : " 
                                    + cert.getPublicKey().getAlgorithm());
	   System.out.println("Cert Public Key Format : " 
                                    + cert.getPublicKey().getFormat());
	   System.out.println("\n");
	}
				
	} catch (SSLPeerUnverifiedException e) {
		e.printStackTrace();
	} catch (IOException e){
		e.printStackTrace();
	}

     }
	
   }
	
   private void print_content(HttpsURLConnection con){
	if(con!=null){
			
	try {
		
	   System.out.println("****** Content of the URL ********");			
	   BufferedReader br = 
		new BufferedReader(
			new InputStreamReader(con.getInputStream()));
				
	   String input;
				
	   while ((input = br.readLine()) != null){
	      System.out.println(input);
	   }
	   br.close();
				
	} catch (IOException e) {
	   e.printStackTrace();
	}
       }
   }
}
