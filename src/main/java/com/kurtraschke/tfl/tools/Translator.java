/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurtraschke.tfl.tools;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsWriter;

import com.google.common.collect.ArrayListMultimap;

import org.osgeo.proj4j.ProjCoordinate;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import uk.org.transxchange.AbstractVehicleJourneyStructure;
import uk.org.transxchange.JourneyPatternSectionStructure;
import uk.org.transxchange.JourneyPatternStopUsageStructure;
import uk.org.transxchange.JourneyPatternStructure;
import uk.org.transxchange.JourneyPatternTimingLinkStructure;
import uk.org.transxchange.OperatorStructure;
import uk.org.transxchange.ServiceStructure;
import uk.org.transxchange.StopPointStructure;
import uk.org.transxchange.TransXChange;
import uk.org.transxchange.VehicleJourneyStructure;
import uk.org.transxchange.VehicleModesEnumeration;

/**
 *
 * @author kurt
 */
public class Translator {

  private final Map<String, OperatorStructure> operatorsById = new HashMap<>();
  private final Map<String, StopPointStructure> stopPointsById = new HashMap<>();
  private final Map<String, ServiceStructure> servicesById = new HashMap<>();
  private final Map<String, VehicleJourneyStructure> vehicleJourneysById = new HashMap<>();
  private final Map<String, JourneyPatternStructure> journeyPatternsById = new HashMap<>();
  private final Map<String, JourneyPatternSectionStructure> journeyPatternSectionsById = new HashMap<>();

  private final AgencyExtraData aed;

  private final Map<String, Agency> agenciesById = new HashMap<>();
  private final Map<String, Stop> stopsById = new HashMap<>();
  private final Map<String, Route> routesById = new HashMap<>();
  private final Map<String, Trip> tripsById = new HashMap<>();

  private final Map<String, AgencyAndId> serviceIdByTrip = new HashMap<>();

  private GtfsWriter writer;

  public Translator(AgencyExtraData aed) {
    this.aed = aed;
  }

  public void status() {
    System.out.format("Operators: %d, Stop points: %d, Services: %d, Vehicle journeys: %d, Journey patterns: %d\n", operatorsById.size(), stopPointsById.size(), servicesById.size(), vehicleJourneysById.size(), journeyPatternSectionsById.size());
  }

  public void load(TransXChange txc) {
    loadOperators(txc.getOperators().getOperatorAndLicensedOperator());
    loadStopPoints(txc.getStopPoints().getStopPoint());
    loadServices(txc.getServices().getService());
    loadVehicleJourneys(txc.getVehicleJourneys().getVehicleJourneyAndFlexibleVehicleJourney());
    loadJourneyPatternSections(txc.getJourneyPatternSections().getJourneyPatternSection());
  }

  public void write(File gtfsPath) throws IOException {
    writer = new GtfsWriter();

    writer.setOutputLocation(gtfsPath);
    System.out.println("Writing agencies...");
    writeAgencies();
    System.out.println("Writing stops...");
    writeStops();
    System.out.println("Writing routes...");
    writeRoutes();
    System.out.println("Writing services...");
    configureAndWriteService();
    System.out.println("Writing trips...");
    writeTrips();
    System.out.println("Writing stoptimes...");
    writeStopTimes();
    writer.close();
  }

  private void loadOperators(Iterable<OperatorStructure> operators) {
    for (OperatorStructure op : operators) {
      String operatorId = op.getId();

      if (!operatorsById.containsKey(operatorId)) {
        operatorsById.put(operatorId, op);
      }
    }
  }

  private void loadStopPoints(Iterable<StopPointStructure> stopPoints) {
    for (StopPointStructure sp : stopPoints) {
      String atcoCode = sp.getAtcoCode();

      if (!stopPointsById.containsKey(atcoCode)) {
        stopPointsById.put(atcoCode, sp);
      }
    }
  }

  private void loadServices(Iterable<ServiceStructure> services) {
    for (ServiceStructure service : services) {
      String serviceCode = service.getServiceCode();

      if (!servicesById.containsKey(serviceCode)) {
        servicesById.put(serviceCode, service);
        for (JourneyPatternStructure jps : service.getStandardService().getJourneyPattern()) {
          journeyPatternsById.put(jps.getId(), jps);
        }
      }
    }
  }

  private void loadVehicleJourneys(Iterable<AbstractVehicleJourneyStructure> vehicleJourneys) {
    for (AbstractVehicleJourneyStructure avj : vehicleJourneys) {

      if (avj instanceof VehicleJourneyStructure) {
        VehicleJourneyStructure vj = (VehicleJourneyStructure) avj;
        String vehicleJourneyCode = vj.getVehicleJourneyCode();

        if (!vehicleJourneysById.containsKey(vehicleJourneyCode)) {
          vehicleJourneysById.put(vehicleJourneyCode, vj);
        }
      }
    }
  }

  private void loadJourneyPatternSections(Iterable<JourneyPatternSectionStructure> journeyPatternSections) {
    for (JourneyPatternSectionStructure jps : journeyPatternSections) {
      String journeyPatternSectionId = jps.getId();
      if (!journeyPatternSectionsById.containsKey(journeyPatternSectionId)) {
        journeyPatternSectionsById.put(journeyPatternSectionId, jps);
      }
    }
  }

  private void writeAgencies() {
    for (OperatorStructure op : operatorsById.values()) {
      Agency a = new Agency();

      a.setId(op.getId());
      a.setName(op.getTradingName());
      a.setUrl(aed.getUrl());
      a.setTimezone(aed.getTimezone());
      a.setLang(aed.getLang());
      a.setPhone(aed.getPhone());
      a.setFareUrl(aed.getFareUrl());

      agenciesById.put(op.getId(), a);

      writer.handleEntity(a);
    }
  }

  private void writeStops() {
    for (StopPointStructure sp : stopPointsById.values()) {
      Stop s = new Stop();

      s.setId(new AgencyAndId(null, sp.getAtcoCode()));
      s.setName(sp.getDescriptor().getCommonName().getValue());

      ProjCoordinate pc = CoordinateTransformer.transformCoordinates(sp.getPlace().getLocation().getEasting(),
              sp.getPlace().getLocation().getNorthing());

      s.setLon(pc.x);
      s.setLat(pc.y);

      //s.setCode(null); //get from bus stop CSV
      stopsById.put(sp.getAtcoCode(), s);
      writer.handleEntity(s);
    }
  }

  private static int mapMode(VehicleModesEnumeration vme) {
    switch (vme) {
      case BUS:
        return 3;
      case COACH:
        return 3;
      case FERRY:
        return 4;
      case METRO:
        return 1;
      case RAIL:
        return 2;
      case TRAM:
        return 0;
      case UNDERGROUND:
        return 1;
      default:
        throw new IllegalArgumentException(vme.name());
    }
  }

  private void writeRoutes() {
    for (ServiceStructure service : servicesById.values()) {
      Route r = new Route();

      r.setId(new AgencyAndId(service.getRegisteredOperatorRef().getValue(), service.getServiceCode()));
      r.setAgency(agenciesById.get(service.getRegisteredOperatorRef().getValue()));
      r.setShortName(service.getLines().getLine().get(0).getLineName().getValue());
      r.setLongName(service.getDescription().getValue());
      if (service.getMode() == null) {
        System.out.println("Null mode for " + service.getServiceCode());
        r.setType(3);
      } else {
        r.setType(mapMode(service.getMode()));
      }

      routesById.put(service.getServiceCode(), r);

      writer.handleEntity(r);
    }
  }

  private void configureAndWriteService() {
    ArrayListMultimap<ServiceData, String> serviceDataMap = ArrayListMultimap.create();

    for (VehicleJourneyStructure vj : vehicleJourneysById.values()) {
      ServiceStructure service = servicesById.get(vj.getServiceRef().getValue());

      ServiceData sd = new ServiceData();

      sd.setOperatingPeriod(service.getOperatingPeriod());
      sd.setOperatingProfile(vj.getOperatingProfile());
      sd.clampPeriodEnd();

      serviceDataMap.put(sd, vj.getVehicleJourneyCode());
    }

    int i = 1;

    for (Map.Entry<ServiceData, Collection<String>> e : serviceDataMap.asMap().entrySet()) {
      ServiceData sd = e.getKey();
      Collection<String> tripIds = e.getValue();
      AgencyAndId serviceId = new AgencyAndId(null, "S" + Integer.toString(i));

      writer.handleEntity(sd.writeServiceCalendar(serviceId));
      for (Object o : sd.writeServiceCalendarDates(serviceId)) {
        writer.handleEntity(o);
      }

      for (String tripId : tripIds) {
        serviceIdByTrip.put(tripId, serviceId);
      }

      i++;
    }
  }

  private void writeTrips() {
    for (VehicleJourneyStructure vj : vehicleJourneysById.values()) {
      Trip t = new Trip();

      ServiceStructure service = servicesById.get(vj.getServiceRef().getValue());

      t.setId(new AgencyAndId(service.getRegisteredOperatorRef().getValue(), vj.getVehicleJourneyCode()));
      t.setServiceId(serviceIdByTrip.get(vj.getVehicleJourneyCode()));
      t.setRoute(routesById.get(vj.getLineRef().getValue()));

      t.setTripHeadsign(vj.getDestinationDisplay().getValue());

      tripsById.put(vj.getVehicleJourneyCode(), t);

      writer.handleEntity(t);
    }
  }

  private void writeStopTimes() {
    for (VehicleJourneyStructure vj : vehicleJourneysById.values()) {
      Trip t = tripsById.get(vj.getVehicleJourneyCode());

      XMLGregorianCalendar departureTime = vj.getDepartureTime();

      List<JourneyPatternTimingLinkStructure> timingLinks = journeyPatternSectionsById.get(journeyPatternsById.get(vj.getJourneyPatternRef().getValue()).getJourneyPatternSectionRefs().get(0).getValue()).getJourneyPatternTimingLink();

      int stopSequence = 0;

      int time = (departureTime.getHour() * 60 + departureTime.getMinute()) * 60 + departureTime.getSecond();

      for (JourneyPatternTimingLinkStructure timingLink : timingLinks) {

        if (stopSequence == 0) {
          JourneyPatternStopUsageStructure from = timingLink.getFrom();

          StopTime st = new StopTime();
          st.setTrip(t);
          st.setStop(stopsById.get(from.getStopPointRef().getValue()));
          st.setStopSequence(++stopSequence);
          st.setDepartureTime(time);
          st.setArrivalTime(time);

          writer.handleEntity(st);

        }

        Duration runTime = timingLink.getRunTime();

        time += (runTime.getTimeInMillis(new Date()) / 1000L);

        JourneyPatternStopUsageStructure to = timingLink.getTo();

        StopTime st = new StopTime();
        st.setTrip(t);
        st.setStop(stopsById.get(to.getStopPointRef().getValue()));
        st.setStopSequence(++stopSequence);
        st.setDepartureTime(time);

        if (to.getWaitTime() != null) {
          time += (to.getWaitTime().getTimeInMillis(new Date()) / 1000L);
        }

        st.setArrivalTime(time);

        writer.handleEntity(st);
      }
    }
  }
}
