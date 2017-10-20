package cn.transpad.transpadui.http;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public class RstSerializer {
	private static final String CHARSET = "UTF-8";
	
	private final Serializer serializer;

	public RstSerializer() {
		 serializer = new Persister();
	}
 
	public String toString(Object src){
		
		OutputStreamWriter osw = null;

	    try {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      osw = new OutputStreamWriter(bos, CHARSET);
	      serializer.write(src, osw);
	      osw.flush();
	      return bos.toString();
	    } catch (Exception e) {
	      throw new AssertionError(e);
	    } finally {
	      try {
	        if (osw != null) {
	          osw.close();
	        }
	      } catch (IOException e) {
	        throw new AssertionError(e);
	      }
	    }
		
	}
	
	public  <T> T fromString(Class<T> classOfT, String rst) throws Exception {


			return serializer.read(classOfT, rst);

	}

	public  <T> T fromInputStream(Class<T> classOfT, InputStream inputStream) throws Exception {


		return serializer.read(classOfT,inputStream);

	}

}
