/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurtraschke.tfl.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.org.transxchange.TransXChange;

/**
 *
 * @author kurt
 */
public class TranslatorMain {

  public static void main(String... args) throws JAXBException, IOException {
    JAXBContext jc = JAXBContext.newInstance(TransXChange.class);
    Unmarshaller u = jc.createUnmarshaller();

    List<File> inputFiles = new ArrayList<>();

    for (String s : args) {
      File f = new File(s);

      if (f.exists()) {
        if (f.isDirectory()) {
          inputFiles.addAll(Arrays.asList(f.listFiles()));
        } else {
          inputFiles.add(f);
        }
      }
    }

    Translator tr = new Translator(new AgencyExtraData("http://www.tfl.gov.uk", "Europe/London", null, null, null));

    for (File f : inputFiles) {
      System.out.println("Loading file " + f.getName());
      TransXChange txc = (TransXChange) u.unmarshal(f);
      tr.load(txc);

      tr.status();
    }

    System.out.println("Writing GTFS...");
    tr.write(new File("/Users/kurt/Desktop/test/"));
  }
}
