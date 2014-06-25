/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurtraschke.tfl.tools;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.datatype.XMLGregorianCalendar;

import uk.org.transxchange.AnnotatedClosedDateRangeStructure;
import uk.org.transxchange.HalfOpenDateRangeStructure;
import uk.org.transxchange.OperatingProfileStructure;
import uk.org.transxchange.RegularOperationStructure;

/**
 *
 * @author kurt
 */
public class ServiceData {

  private ServiceDate periodBegin;
  private ServiceDate periodEnd;

  private final Set<ServiceDate> exclusions = new HashSet<>();
  private final Set<ServiceDate> additions = new HashSet<>();

  private boolean monday;
  private boolean tuesday;
  private boolean wednesday;
  private boolean thursday;
  private boolean friday;
  private boolean saturday;
  private boolean sunday;

  public void setOperatingPeriod(HalfOpenDateRangeStructure op) {
    periodBegin = serviceDateFromCalendar(op.getStartDate());
    periodEnd = serviceDateFromCalendar(op.getEndDate());
  }

  public void setOperatingProfile(OperatingProfileStructure op) {
    setDays(op.getRegularDayType().getDaysOfWeek());

    if (op.getSpecialDaysOperation() != null) {
      if (op.getSpecialDaysOperation().getDaysOfOperation() != null) {
        addSpecialDates(op.getSpecialDaysOperation().getDaysOfOperation().getDateRange(), additions);
      }

      if (op.getSpecialDaysOperation().getDaysOfNonOperation() != null) {
        addSpecialDates(op.getSpecialDaysOperation().getDaysOfNonOperation().getDateRange(), exclusions);
      }
      /* If the specified days of non-operation overlap with days of operation,
       the days of non-operation take precedence over days of operation. */
      additions.removeAll(exclusions);
    }
  }

  public void clampPeriodEnd() {
    if (exclusions.isEmpty()) {
      return;
    }
    
    ServiceDate maxExcludedDate = Collections.max(exclusions);

    /*System.out.format("Period end: %s, Max excluded date: %s, difference: %d\n",
     periodEnd,
     maxExcludedDate,
     Math.abs(maxExcludedDate.difference(periodEnd))
     );*/
    if (Math.abs(maxExcludedDate.difference(periodEnd)) <= 7) {
      periodEnd = maxExcludedDate;
    }

    Set<ServiceDate> allServiceDates = new HashSet<>(serviceDateIterator(periodBegin, periodEnd));

    allServiceDates.removeAll(exclusions);
    final ServiceDate maxServiceDate = Collections.max(allServiceDates);

    if (!maxServiceDate.equals(periodEnd)) {
      periodEnd = maxServiceDate;

      exclusions.removeIf(new Predicate<ServiceDate>() {
        @Override
        public boolean test(ServiceDate t) {
          return t.compareTo(maxServiceDate) > 0;
        }
      });
    }
  }

  public ServiceCalendar writeServiceCalendar(AgencyAndId id) {
    ServiceCalendar sc = new ServiceCalendar();

    sc.setServiceId(id);

    sc.setStartDate(periodBegin);
    sc.setEndDate(periodEnd);

    sc.setMonday(monday ? 1 : 0);
    sc.setTuesday(tuesday ? 1 : 0);
    sc.setWednesday(wednesday ? 1 : 0);
    sc.setThursday(thursday ? 1 : 0);
    sc.setFriday(friday ? 1 : 0);
    sc.setSaturday(saturday ? 1 : 0);
    sc.setSunday(sunday ? 1 : 0);

    return sc;
  }

  public List<ServiceCalendarDate> writeServiceCalendarDates(AgencyAndId id) {
    List<ServiceCalendarDate> out = new ArrayList<>(exclusions.size() + additions.size());

    for (ServiceDate sd : exclusions) {
      ServiceCalendarDate scd = new ServiceCalendarDate();
      scd.setServiceId(id);
      scd.setDate(sd);
      scd.setExceptionType(2);
      out.add(scd);
    }

    for (ServiceDate sd : additions) {
      ServiceCalendarDate scd = new ServiceCalendarDate();
      scd.setServiceId(id);
      scd.setDate(sd);
      scd.setExceptionType(1);
      out.add(scd);
    }

    return out;
  }

  private void setDays(RegularOperationStructure.DaysOfWeek dow) {
    if (dow.getMonday() != null || dow.getMondayToFriday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      monday = true;
    }

    if (dow.getTuesday() != null || dow.getMondayToFriday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      tuesday = true;
    }

    if (dow.getWednesday() != null || dow.getMondayToFriday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      wednesday = true;
    }

    if (dow.getThursday() != null || dow.getMondayToFriday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      thursday = true;
    }
    if (dow.getFriday() != null || dow.getMondayToFriday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      friday = true;
    }

    if (dow.getSaturday() != null || dow.getMondayToSaturday() != null || dow.getMondayToSunday() != null) {
      saturday = true;
    }

    if (dow.getSunday() != null || dow.getMondayToSunday() != null) {
      sunday = true;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.periodBegin);
    hash = 97 * hash + Objects.hashCode(this.periodEnd);
    hash = 97 * hash + Objects.hashCode(this.exclusions);
    hash = 97 * hash + Objects.hashCode(this.additions);
    hash = 97 * hash + (this.monday ? 1 : 0);
    hash = 97 * hash + (this.tuesday ? 1 : 0);
    hash = 97 * hash + (this.wednesday ? 1 : 0);
    hash = 97 * hash + (this.thursday ? 1 : 0);
    hash = 97 * hash + (this.friday ? 1 : 0);
    hash = 97 * hash + (this.saturday ? 1 : 0);
    hash = 97 * hash + (this.sunday ? 1 : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ServiceData other = (ServiceData) obj;
    if (!Objects.equals(this.periodBegin, other.periodBegin)) {
      return false;
    }
    if (!Objects.equals(this.periodEnd, other.periodEnd)) {
      return false;
    }
    if (!Objects.equals(this.exclusions, other.exclusions)) {
      return false;
    }
    if (!Objects.equals(this.additions, other.additions)) {
      return false;
    }
    if (this.monday != other.monday) {
      return false;
    }
    if (this.tuesday != other.tuesday) {
      return false;
    }
    if (this.wednesday != other.wednesday) {
      return false;
    }
    if (this.thursday != other.thursday) {
      return false;
    }
    if (this.friday != other.friday) {
      return false;
    }
    if (this.saturday != other.saturday) {
      return false;
    }
    if (this.sunday != other.sunday) {
      return false;
    }
    return true;
  }

  private static void addSpecialDates(List<AnnotatedClosedDateRangeStructure> dates, Set<ServiceDate> serviceDateSet) {
    for (AnnotatedClosedDateRangeStructure drs : dates) {
      ServiceDate startDate = serviceDateFromCalendar(drs.getStartDate());
      ServiceDate endDate = serviceDateFromCalendar(drs.getEndDate());

      serviceDateSet.addAll(serviceDateIterator(startDate, endDate));
    }
  }

  private static ServiceDate serviceDateFromCalendar(XMLGregorianCalendar in) {
    return new ServiceDate(in.getYear(), in.getMonth(), in.getDay());
  }

  private static List<ServiceDate> serviceDateIterator(ServiceDate start, ServiceDate end) {
    if (start.equals(end)) {
      return Collections.singletonList(start);
    } else {
      List<ServiceDate> out = new ArrayList<>();
      for (ServiceDate i = new ServiceDate(start); i.compareTo(end) < 0; i = i.next()) {
        out.add(i);
      }
      return out;
    }
  }
}
