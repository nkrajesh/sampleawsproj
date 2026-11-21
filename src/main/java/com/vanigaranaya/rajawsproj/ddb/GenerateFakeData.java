package com.vanigaranaya.rajawsproj.ddb;

import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Name;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class GenerateFakeData {

    public static void generateFakedata() throws Exception {
        Faker faker = new Faker();
        List<String> states = List.of("GA", "TN", "CA", "MS");

        try (PrintWriter writer = new PrintWriter(new FileWriter("items.jsonl"))) {
            for (int i = 0; i < 10; i++) {
                // 1. Pick a random state
                String state = states.get(faker.random().nextInt(states.size()));

                // 2. Fix the "301##" issue using expression()
                String zip = randomZipByState(state, faker);
                // 3. Generate Address & Income
                String city = faker.address().city();
                String street = faker.address().streetAddress();
                int income = faker.number().numberBetween(45000, 250000);

                // 4. Create the JSON Line
                String json = String.format(
                        "{\"Item\": {\"email\": {\"S\": \"%s\"}, \"lastName\": {\"S\": \"%s\"}, " +
                                "\"state\": {\"S\": \"%s\"}, \"zip\": {\"S\": \"%s\"}, " +
                                "\"city\": {\"S\": \"%s\"}, \"income\": {\"N\": \"%d\"}}}",
                        faker.internet().emailAddress(),
                        faker.name().lastName(),
                        state, zip, city, income
                );
                writer.println(json);
            }
        }
        System.out.println("Finished generating 200k records.");
    }

    public static void insertFakedataToDDB() throws Exception {
        Faker faker = new Faker();
        List<String> states = List.of("GA", "TN", "CA", "MS");
        List<String> educationStatus = List.of("MASTERS", "BACHELORS_DEGREE", "ASSOCIATE_DEGREE","DOCTORATE", "HIGH_SCHOOL", "OTHER");
        List<String> employmentStatus = List.of("FULL_TIME", "PART_TIME", "UNEMPLOYED", "RETIRED", "STUDENT");

        try (PrintWriter writer = new PrintWriter(new FileWriter("items.jsonl"))) {
            for (int i = 0; i < 10; i++) {
                // 1. Pick a random state
                String state = states.get(faker.random().nextInt(states.size()));
                String eduStatus = educationStatus.get(faker.random().nextInt(educationStatus.size()));
                String empStatus = educationStatus.get(faker.random().nextInt(employmentStatus.size()));


                Address physicalAddress = faker.address();
                String physicalStreet = physicalAddress.streetAddress();
                String physicalCty = physicalAddress.city();
                String physicalZipcode = randomZipByState(state, faker);
                Address mailingAddress;
                String mailingStreet, mailingCity, mailingZipcode;
                Boolean mailingSameasPhysical=false;
                if (faker.bool().bool()){
                    mailingSameasPhysical=true;
                } else {
                    mailingAddress = faker.address();
                    mailingStreet = mailingAddress.streetAddress();
                    mailingCity = mailingAddress.city();
                    mailingZipcode = randomZipByState(state, faker);
                }
                int income = faker.number().numberBetween(45000, 250000);
            }
        }
        System.out.println("Finished generating 200k records.");
    }


    public static void main(String[] args) throws Exception {
        Faker faker = new Faker();

        Name name = faker.name();
        String streetAddress = faker.address().streetAddress(); // Generates a street address
        String email = faker.internet().emailAddress(); // Generates an email address
        String phoneNumber = faker.numerify("601#######");
        faker.address().zipCodeByState("GA");// Generates a phone number
        System.out.println("Name: " + name.firstName());
        System.out.println("Name: " + name.lastName());
        System.out.println("Address: " + streetAddress);
        System.out.println("Address: " + faker.address().zipCodeByState("GA"));
        System.out.println("Address: " + faker.address().zipCode());
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + faker.options().option("386", "397") + faker.number().digits(2));
        System.out.println("SSN: " + faker.idNumber().ssnValid());
        System.out.println("SSN: " + faker.options().option("70", "20") + faker.number().digits(7));
        //generateFakeData();
    }

    private static String randomFakerNumber(String[] strrange, int digits , Faker faker){
        String str = faker.options().option(strrange) + faker.number().digits(digits);
        return str;

    }
    private static String randomZipByState(String state, Faker faker ) {
        String[] strcoderange;
        if ("MS".equalsIgnoreCase(state)) {
            strcoderange= new String[] {"386", "397"};
        } else if ("GA".equalsIgnoreCase(state)){
            strcoderange=  new String[] {"300", "303"};
        } else if ("TN".equalsIgnoreCase(state)){
            strcoderange=  new String[] {"370", "385"};
        } else if ("CA".equalsIgnoreCase(state)){
            strcoderange=  new String[] {"900", "961"};
        } else{
            strcoderange=  new String[] {"123", "456"};
        }
        return randomFakerNumber(strcoderange,2,faker);
    }

    private static String randomPhoneByState(String state, Faker faker) {
        String[] strcoderange;
        if ("MS".equalsIgnoreCase(state)) {
            strcoderange = new String[] {"601", "796"};
        } else if ("GA".equalsIgnoreCase(state)){
            strcoderange = new String[] {"404", "770"};
        } else if ("TN".equalsIgnoreCase(state)){
            strcoderange = new String[] {"615", "628"};
        } else if ("CA".equalsIgnoreCase(state)){
            strcoderange = new String[] {"925", "909"};
        } else{
            strcoderange = new String[] {"123", "456"};
        }
        return randomFakerNumber(strcoderange,7,faker);
    }

    private static String randomSSNByState(String state, Faker faker) {
        String[] strcoderange;
        if ("MS".equalsIgnoreCase(state)) {
            strcoderange= new String[] {"60", "79"};
        } else if ("GA".equalsIgnoreCase(state)){
            strcoderange= new String[] {"40", "77"};
        } else if ("TN".equalsIgnoreCase(state)){
            strcoderange= new String[] {"61", "62"};
        } else if ("CA".equalsIgnoreCase(state)){
            strcoderange= new String[] {"92", "90"};
        } else{
            strcoderange= new String[] {"12", "34"};
        }
        return randomFakerNumber(strcoderange,7,faker);
    }

    private static String randomDOBByState(String state, Faker faker) {
        int year;
        if ("MS".equalsIgnoreCase(state)) {
            year = faker.number().numberBetween(1969, 1970);
        } else if ("GA".equalsIgnoreCase(state)){
            year = faker.number().numberBetween(1971, 1972);
        } else if ("TN".equalsIgnoreCase(state)){
            year = faker.number().numberBetween(1973, 1974);
        } else if ("CA".equalsIgnoreCase(state)){
            year = faker.number().numberBetween(1975, 1976);
        } else{
            year = faker.number().numberBetween(1977, 1978);
        }
        int month = faker.number().numberBetween(1, 13);
        int day = faker.number().numberBetween(1, 29); // 28 is safe for all months

        String dob = String.format("%d-%02d-%02d", year, month, day);
        return dob;
    }
}
