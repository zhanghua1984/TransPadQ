package cn.transpad.transpadui.http;

/**
 * A {@link Converter} which uses SimpleXML for reading and writing entities.
 * @author 刘昆  (liukun@100tv.com)
 * @since  2014-04-22
 */


import android.util.Base64;

import com.google.gson.Gson;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.lang.reflect.Type;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;


public class SimpleXMLConverter implements Converter {
  private static final String CHARSET = "UTF-8";
  private static final String MIME_TYPE = "text/xml; charset=" + CHARSET;

  private final Serializer serializer;

  public SimpleXMLConverter() {
    this(new Persister());
  }

  public SimpleXMLConverter(Serializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public Object fromBody(TypedInput body, Type type) throws ConversionException {

    try {
      return serializer.read((Class<?>) type, body.in());
    } catch (Exception e) {
      throw new ConversionException(e);
    }
  }

  @Override
  public TypedOutput toBody(Object source) {
    
    try {
    	Gson gson = new Gson();
    	SecretKeySpec skeySpec = new SecretKeySpec(Configure.getAesKey().getBytes(), "AES");
		Cipher ci = Cipher.getInstance("AES");
		ci.init(Cipher.ENCRYPT_MODE, skeySpec);
		
		return new TypedByteArray(MIME_TYPE, ci.doFinal(Base64.encode(gson.toJson(source).getBytes(), Base64.URL_SAFE
                | Base64.NO_WRAP)));
 
    
    } catch (Exception e) {
      throw new AssertionError(e);
    } 
  }
}
