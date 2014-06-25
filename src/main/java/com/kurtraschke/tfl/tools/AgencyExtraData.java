/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kurtraschke.tfl.tools;

/**
 *
 * @author kurt
 */
public class AgencyExtraData {
 
  private String url;
  private String timezone;
  private String lang;
  private String phone;
  private String fareUrl;

  public AgencyExtraData(String url, String timezone, String lang, String phone, String fareUrl) {
    this.url = url;
    this.timezone = timezone;
    this.lang = lang;
    this.phone = phone;
    this.fareUrl = fareUrl;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFareUrl() {
    return fareUrl;
  }

  public void setFareUrl(String fareUrl) {
    this.fareUrl = fareUrl;
  }
  
  
  
}
