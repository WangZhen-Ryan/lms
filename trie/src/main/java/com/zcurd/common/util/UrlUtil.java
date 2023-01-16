package com.zcurd.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UrlUtil {
  public static String formatBaseUrl(String url) {
    url = removeUrlParam(url).replaceAll("//+", "/").replaceAll("/$", "");
    if (url.split("/").length >= 2)
      url = url.replaceAll("/\\w*$", ""); 
    return url;
  }
  
  public static String formatUrl(String url) { return url.replaceAll("//+", "/").replaceAll("/$", ""); }
  
  public static String removeUrlParam(String url) { return url.replaceAll("[?#].*", ""); }
  
  public static String getAsText(String url) {
    String urlString = "";
    try {
      URLConnection urlConnection = (new URL(url)).openConnection();
      urlConnection.setConnectTimeout(30000);
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      String current;
      while ((current = in.readLine()) != null)
        urlString = String.valueOf(urlString) + current; 
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return urlString;
  }
}
