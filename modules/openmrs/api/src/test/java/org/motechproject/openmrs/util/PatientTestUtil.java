package org.motechproject.openmrs.util;

import org.motechproject.mrs.domain.Facility;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;

import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PatientTestUtil {

    private FacilityTestUtil facilityTestUtil = new FacilityTestUtil();

    public org.openmrs.Patient setUpOpenMRSPatient(Person person, String first, String middle, String last, String address1, Date birthdate, boolean birthdateEstimated, String gender, Facility facility, String motechId) {
        PersonName personName = new PersonName(first, middle, last);
        person.addName(personName);
        setAddress(person, address1);
        final org.openmrs.Patient patient = new org.openmrs.Patient(person);
        patient.setBirthdate(birthdate);
        patient.setBirthdateEstimated(birthdateEstimated);
        patient.setGender(gender);
        patient.addIdentifier(new PatientIdentifier(motechId, null, new Location(Integer.parseInt(facility.getFacilityId()))));
        return patient;
    }

    private void setAddress(Person person, String address1) {
        final PersonAddress address = new PersonAddress();
        address.setAddress1(address1);
        final HashSet<PersonAddress> addresses = new HashSet<PersonAddress>();
        addresses.add(address);
        person.setAddresses(addresses);
    }

    public void verifyReturnedPatient(String first, String middle, String last, String address1, Date birthdate, Boolean birthDateEstimated, String gender, Facility facility, org.motechproject.mrs.domain.Patient actualPatient, String motechId) {
        org.motechproject.mrs.domain.Person actualMRSPerson = actualPatient.getPerson();
        assertThat(actualMRSPerson.getFirstName(), is(first));
        assertThat(actualMRSPerson.getLastName(), is(last));
        assertThat(actualMRSPerson.getMiddleName(), is(middle));
        assertThat(actualMRSPerson.getAddress(), is(address1));
        assertThat(actualMRSPerson.getDateOfBirth().toDate(), is(birthdate));
        assertThat(actualMRSPerson.getGender(), is(gender));
        assertThat(actualPatient.getFacility(), is(equalTo(facility)));
    }

    public void assertEqualsForOpenMrsPatient(Patient openMrsPatient1, Patient openMRSPatient2) {
        assertThat(openMrsPatient1.getPersonName().getGivenName(), is(equalTo(openMRSPatient2.getPersonName().getGivenName())));
        assertThat(openMrsPatient1.getPersonName().getMiddleName(), is(equalTo(openMRSPatient2.getPersonName().getMiddleName())));
        assertThat(openMrsPatient1.getPersonName().getFamilyName(), is(equalTo(openMRSPatient2.getPersonName().getFamilyName())));
        assertThat(openMrsPatient1.getPersonAddress().getAddress1(), is(equalTo(openMRSPatient2.getPersonAddress().getAddress1())));
        assertThat(openMrsPatient1.getBirthdate(), is(equalTo(openMRSPatient2.getBirthdate())));
        assertThat(openMrsPatient1.getBirthdateEstimated(), is(equalTo(openMRSPatient2.getBirthdateEstimated())));
        assertThat(openMrsPatient1.getGender(), is(equalTo(openMRSPatient2.getGender())));
        assertThat(openMrsPatient1.getPatientIdentifier().getIdentifier(), is(equalTo(openMRSPatient2.getPatientIdentifier().getIdentifier())));
        facilityTestUtil.assertEqualsForOpenMrsLocation(openMrsPatient1.getPatientIdentifier().getLocation(), openMRSPatient2.getPatientIdentifier().getLocation());
    }
}
