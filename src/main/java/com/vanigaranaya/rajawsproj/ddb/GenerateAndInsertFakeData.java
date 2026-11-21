package com.vanigaranaya.rajawsproj.ddb;

import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Name;
import org.springframework.web.util.UriComponents;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateAndInsertFakeData {

    private static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    private static final List<WriteRequest> buffer = new ArrayList<>();
    private static final int BATCHSIZE = 5;
    private static final boolean ISBATCH = true;

    public static void insertFakedataToDDB() throws Exception {
        Faker faker = new Faker();
        List<String> states = List.of("GA", "TN", "CA", "MS");
        List<String> educationStatus = List.of("MASTERS", "BACHELORS_DEGREE", "ASSOCIATE_DEGREE", "DOCTORATE", "HIGH_SCHOOL", "OTHER");
        List<String> employmentStatus = List.of("FULL_TIME", "PART_TIME", "RETIRED", "STUDENT");
        List<String> channel = List.of("WEB", "API");
        for (String state : states) {
        for (int i = 0; i < 1; i++) {
            // 1. Pick a random state

            Map<String, Object> appIdWithDate = generateRandomAppId();
            // 1. Clean the names (remove spaces or special characters just in case)
            String firstName = faker.name().firstName().toLowerCase().replaceAll("[^a-z]", "");
            String lastName = faker.name().lastName().toLowerCase().replaceAll("[^a-z]", "");

            // 2. Pick a random domain (or just use one)
            List<String> domains = List.of("gmail.com", "hotmail.com", "yahoo.com");
            String randomDomain = domains.get(faker.random().nextInt(domains.size()));

            // 3. Construct the "Professional" email
            String fakeEmail = String.format("%s.%s@%s", firstName, lastName, randomDomain);

            String fakeSSN = randomSSNByState(state, faker);
            String fakeDOB = randomDOBByState(state, faker);
            String fakePhoneNumber = randomPhoneByState(state, faker);
            String fakeEduStatus = educationStatus.get(faker.random().nextInt(educationStatus.size()));
            String fakeEmpStatus = employmentStatus.get(faker.random().nextInt(employmentStatus.size()));
            int fakeIncome = faker.number().numberBetween(45000, 250000);
            String fakeChannel = channel.get(faker.random().nextInt(channel.size()));

            Address physicalAddress = faker.address();
            String physicalStreet = physicalAddress.streetAddress();
            String physicalCty = physicalAddress.city();
            String physicalZipcode = randomZipByState(state, faker);
            Address mailingAddress;
            String mailingStreet, mailingCity, mailingZipcode;
            Boolean mailingSameasPhysical = false;
            if (faker.bool().bool()) {
                mailingSameasPhysical = true;
                mailingStreet = physicalStreet;
                mailingCity = physicalCty;
                mailingZipcode = physicalZipcode;
            } else {
                mailingAddress = faker.address();
                mailingStreet = mailingAddress.streetAddress();
                mailingCity = mailingAddress.city();
                mailingZipcode = randomZipByState(state, faker);
            }
            int income = faker.number().numberBetween(45000, 250000);

            Map<String, AttributeValue> itemsValues = new HashMap<>();
            itemsValues.put("applicationId", AttributeValue.builder().s(appIdWithDate.get("appId").toString()).build());
            itemsValues.put("firstName", AttributeValue.builder().s(firstName).build());
            itemsValues.put("lastName", AttributeValue.builder().s(lastName).build());
            itemsValues.put("middleName", AttributeValue.builder().s("").build());
            itemsValues.put("phone", AttributeValue.builder().s(fakePhoneNumber).build());
            itemsValues.put("dob", AttributeValue.builder().s(fakeDOB).build());
            itemsValues.put("email", AttributeValue.builder().s(fakeEmail).build());
            itemsValues.put("govId", AttributeValue.builder().s(fakeSSN).build());
            itemsValues.put("govIdType", AttributeValue.builder().s("SSN").build());
            itemsValues.put("empStatus", AttributeValue.builder().s(fakeEmpStatus).build());
            itemsValues.put("eduStatus", AttributeValue.builder().s(fakeEduStatus).build());
            itemsValues.put("income", AttributeValue.builder().n(String.valueOf(fakeIncome)).build());
            itemsValues.put("emailConsent", AttributeValue.builder().bool(Boolean.TRUE).build());
            itemsValues.put("textConsent", AttributeValue.builder().bool(Boolean.TRUE).build());
            itemsValues.put("submittedUser", AttributeValue.builder().s(fakeEmail).build());
            itemsValues.put("submittedChannel", AttributeValue.builder().s(fakeChannel).build());
            itemsValues.put("submittedDate", AttributeValue.builder().n(Long.toString((Long) appIdWithDate.get("createdDateTime"))).build());
            itemsValues.put("appStatus", AttributeValue.builder().s("INPROGRESS").build());
            itemsValues.put("submittedDateTimeISO", AttributeValue.builder().s(appIdWithDate.get("createdDateTimeISO").toString()).build());

            Map<String, AttributeValue> mailingaddressMap = new HashMap<>();
            mailingaddressMap.put("addressLine1", AttributeValue.builder().s(mailingStreet).build());
            mailingaddressMap.put("city", AttributeValue.builder().s(mailingCity).build());
            mailingaddressMap.put("state", AttributeValue.builder().s(state).build());
            mailingaddressMap.put("zipCode", AttributeValue.builder().s(mailingZipcode).build());
            mailingaddressMap.put("addressType", AttributeValue.builder().s("MAILING").build());

            Map<String, AttributeValue> physicaladdressMap = new HashMap<>();
            physicaladdressMap.put("addressLine1", AttributeValue.builder().s(physicalStreet).build());
            physicaladdressMap.put("city", AttributeValue.builder().s(physicalCty).build());
            physicaladdressMap.put("state", AttributeValue.builder().s(state).build());
            physicaladdressMap.put("zipCode", AttributeValue.builder().s(physicalZipcode).build());
            physicaladdressMap.put("addressType", AttributeValue.builder().s("PHYSICAL").build());

            itemsValues.put("physicalAddress", AttributeValue.builder().m(physicaladdressMap).build());
            itemsValues.put("mailingAddress", AttributeValue.builder().m(mailingaddressMap).build());
            samplePuItem(itemsValues,ISBATCH);
        }

    }

        flushBuffer("rajsample");
        System.out.println("Finished generating 200k records.");
    }

    private static void samplePuItem(Map<String,AttributeValue> appIdWithDate,boolean isBatch) throws Exception {
        String tableName = "rajsample";
        List<WriteRequest> writeRequests = new ArrayList<>();
        PutItemRequest putItemRequest = PutItemRequest.builder().tableName("rajsample").item(appIdWithDate).build();
        try {
            if (!isBatch) {
                    PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);
            System.out.println(tableName + " was successfully updated. The request id is "
                    + putItemResponse.responseMetadata().requestId());
            } else {
                handleBatch(appIdWithDate, tableName);
            }
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    private static void handleBatch(Map<String, AttributeValue> appIdWithDate, String tableName) {
        PutRequest putRequest = PutRequest.builder().item(appIdWithDate).build();
        WriteRequest writeRequest = WriteRequest.builder().putRequest(putRequest).build();
        buffer.add(writeRequest);
        if (buffer.size() >= BATCHSIZE){
            flushBuffer(tableName);
        }
    }

    private static void flushBuffer( String tableName) {
        if (buffer.isEmpty()){
                return;}
        else {
            BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                    .requestItems(Map.of("rajsample", buffer))
                    .build();
            BatchWriteItemResponse batchWriteItemResponse = dynamoDbClient.batchWriteItem(batchRequest);
            System.out.println(tableName + " was successfully batch updated. The request id is "
                    + batchWriteItemResponse.responseMetadata().requestId());
            buffer.clear();
        }
    }


    public static void main(String[] args) throws Exception {
        insertFakedataToDDB();
    }

    public static Map<String,Object> generateRandomAppId() {
        // 1. Spread over first 3 months (Day 1 to 90 of 2026)
        int randomDayOfYear = ThreadLocalRandom.current().nextInt(1, 91);
        LocalDate randomDate = LocalDate.ofYearDay(2026, randomDayOfYear);
        // 2. Julian Date part (YYYYDDD)
        String julianDate = randomDate.format(DateTimeFormatter.ofPattern("yyDDD"));

        // 3. Time part (HHmmssSSS) - randomly generated for variety
        int randomHour = ThreadLocalRandom.current().nextInt(0, 24);
        int randomMinute = ThreadLocalRandom.current().nextInt(0, 60);
        int randomSecond = ThreadLocalRandom.current().nextInt(0, 60);
        int randomMilliSecond = ThreadLocalRandom.current().nextInt(0, 1000);
        String timePart = String.format("%02d%02d%02d%03d",
                randomHour,
                randomMinute,
                randomSecond,
                randomMilliSecond);

        // 4. Static String (99)
        String staticPart = "99";
        Instant appCreatedInstant = randomDate.atTime(randomHour, randomMinute,
                randomSecond,  randomMilliSecond*1000000).toInstant(ZoneOffset.UTC);
        long createdDateTime = appCreatedInstant.toEpochMilli();
        Map<String, Object> result = new HashMap<>();
        result.put("appId", julianDate + timePart + staticPart);
        result.put("createdDateTime", createdDateTime);
        result.put("createdDateTimeISO",appCreatedInstant.toString());
        return result;
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

    private static String randomFakerNumber(String[] strrange, int digits , Faker faker){
        String str = faker.options().option(strrange) + faker.number().digits(digits);
        return str;
    }
}
