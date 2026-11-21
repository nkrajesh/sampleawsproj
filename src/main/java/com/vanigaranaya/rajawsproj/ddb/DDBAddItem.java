package com.vanigaranaya.rajawsproj.ddb;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DDBAddItem {
    static DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    public static void main(String[] args) {
        System.out.println("hello ddb test");
        samplePuItem();
    }
    public static String generateRandomAppId() {
        // 1. Spread over first 3 months (Day 1 to 90 of 2026)
        int randomDayOfYear = ThreadLocalRandom.current().nextInt(1, 91);
        LocalDate randomDate = LocalDate.ofYearDay(2026, randomDayOfYear);

        // 2. Julian Date part (YYYYDDD)
        String julianDate = randomDate.format(DateTimeFormatter.ofPattern("yyDDD"));

        // 3. Time part (HHmmssSSS) - randomly generated for variety
        String timePart = String.format("%02d%02d%02d%03d",
                ThreadLocalRandom.current().nextInt(0, 24),
                ThreadLocalRandom.current().nextInt(0, 60),
                ThreadLocalRandom.current().nextInt(0, 60),
                ThreadLocalRandom.current().nextInt(0, 1000));

        // 4. Static String (99)
        String staticPart = "99";

        return julianDate + timePart + staticPart;
    }
    private static String generateAppId(){
//        /Clock.systemDefaultZone()
        //Clock clock = Clock.system(ZoneId.of("America/Los_Angeles"));
        LocalDateTime now = LocalDateTime.now();
        /*
        System.out.println("now getdayofyear "+ getPaddedValue(now.getDayOfYear(), 3));
        System.out.println("now hour "+ getPaddedValue(now.getHour(),3));
        System.out.println("now minute "+ getPaddedValue(now.getMinute(), 2));
        System.out.println("now second "+ getPaddedValue(now.getSecond(), 2));
        System.out.println("now millisecond "+ getPaddedValue(now.get(ChronoField.MILLI_OF_SECOND), 3));
        System.out.println("cardType .." + CardType.CREDIT.getCardTypeId());
*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        System.out.println(formatter.format(now));
        return new StringBuilder()
                .append(formatter.format(now))
                .append(getPaddedValue(now.getDayOfYear(), 3))
                .append(getPaddedValue(now.getHour(), 2))
                .append(getPaddedValue(now.getMinute(), 2))
                .append(getPaddedValue(now.getSecond(), 2))
                .append(getPaddedValue(now.get(ChronoField.MILLI_OF_SECOND), 3))
                .append("99")
                .toString();
    }

    private static void samplePuItem() {
        String tableName = "rajsample";
        Map<String, AttributeValue> itemsValues = new HashMap<>();
        itemsValues.put("applicationId", AttributeValue.builder().s(generateAppId()).build());
        //itemsValues.put("submitteddate", AttributeValue.builder().s("2026777273474737").build());
        PutItemRequest putItemRequest = PutItemRequest.builder().tableName("rajsample").item(itemsValues).build();
        try {
            PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);
            System.out.println(tableName + " was successfully updated. The request id is "
                    + putItemResponse.responseMetadata().requestId());
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    private static String getPaddedValue(int val, int size){
        return  StringUtils.leftPad(String.valueOf(val), size,"0");
    }
}
