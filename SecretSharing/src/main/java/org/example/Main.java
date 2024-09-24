package org.example;

import java.math.BigInteger;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // Process both test cases
            processTestFile("src/main/resources/input.json");
            processTestFile("src/main/resources/input2.json");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // Method to process a single test case
    public static void processTestFile(String fileName) throws IOException, ParseException {
        // Read JSON input
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonData = (JSONObject) jsonParser.parse(new FileReader(fileName)); // Ensure correct file path

        // Extract 'n' and 'k'
        JSONObject jsonKeys = (JSONObject) jsonData.get("keys");
        int numPoints = Integer.parseInt(jsonKeys.get("n").toString());
        int thresholdK = Integer.parseInt(jsonKeys.get("k").toString());

        // Store the decoded (x, y) values
        List<Coordinate> coordinates = new ArrayList<>();

        // Iterate over each root in JSON and decode y-values
        for (int index = 1; index <= numPoints; index++) {
            if (jsonData.containsKey(Integer.toString(index))) {
                JSONObject rootNode = (JSONObject) jsonData.get(Integer.toString(index));
                int xValue = Integer.parseInt(Integer.toString(index));
                String baseString = rootNode.get("base").toString();
                String valueString = rootNode.get("value").toString();
                BigInteger yValue = new BigInteger(valueString, Integer.parseInt(baseString));  // Decode y from the given base

                // Store the coordinate (x, y)
                coordinates.add(new Coordinate(xValue, yValue));
            }
        }

        // Identify anomaly points if processing test_case2.json
        if (fileName.equals("src/test_case2.json")) {
            List<Coordinate> anomalyCoordinates = findAnomalyCoordinates(coordinates);
            if (!anomalyCoordinates.isEmpty()) {
                System.out.println("Anomalies detected in " + fileName + ": " + anomalyCoordinates);
            } else {
                System.out.println("No anomalies detected in " + fileName + ".");
            }
        }

        // Use Lagrange interpolation to find the constant term 'c'
        BigInteger constantValue = lagrangeInterpolation(coordinates, thresholdK);
        System.out.println("The value of c is: " + constantValue);
    }

    // Method to calculate the constant value 'c' using Lagrange Interpolation
    public static BigInteger lagrangeInterpolation(List<Coordinate> coordinates, int threshold) {
        BigInteger finalResult = BigInteger.ZERO;

        // Loop through the first k coordinates
        for (int i = 0; i < threshold; i++) {
            BigInteger xi = BigInteger.valueOf(coordinates.get(i).xCoord);
            BigInteger yi = coordinates.get(i).yCoord;

            // Calculate the Lagrange basis polynomial L_i(0)
            BigInteger termResult = yi;
            for (int j = 0; j < threshold; j++) {
                if (i != j) {
                    BigInteger xj = BigInteger.valueOf(coordinates.get(j).xCoord);
                    termResult = termResult.multiply(xj.negate()); // Multiply by -xj (since we are calculating for x=0)
                    termResult = termResult.divide(xi.subtract(xj)); // Divide by (xi - xj)
                }
            }

            // Add the term to the final result
            finalResult = finalResult.add(termResult);
        }

        return finalResult;
    }

    // Helper class to store (x, y) coordinates
    static class Coordinate {
        int xCoord;
        BigInteger yCoord;

        Coordinate(int xCoord, BigInteger yCoord) {
            this.xCoord = xCoord;
            this.yCoord = yCoord;
        }

        @Override
        public String toString() {
            return "(" + xCoord + ", " + yCoord + ")";
        }
    }

    // Method to identify anomaly points
    public static List<Coordinate> findAnomalyCoordinates(List<Coordinate> coordinates) {
        List<Coordinate> anomalies = new ArrayList<>();

        // Calculate the mean of y values
        BigInteger totalSum = BigInteger.ZERO;
        for (Coordinate coordinate : coordinates) {
            totalSum = totalSum.add(coordinate.yCoord);
        }
        BigInteger avgY = totalSum.divide(BigInteger.valueOf(coordinates.size()));

        // Calculate the standard deviation
        BigInteger varSum = BigInteger.ZERO;
        for (Coordinate coordinate : coordinates) {
            BigInteger deviation = coordinate.yCoord.subtract(avgY);
            varSum = varSum.add(deviation.multiply(deviation));
        }
        BigInteger variance = varSum.divide(BigInteger.valueOf(coordinates.size()));
        BigInteger standardDeviation = BigInteger.valueOf((long) Math.sqrt(variance.doubleValue()));

        // Define the threshold for anomaly detection (e.g., 2 standard deviations from the mean)
        BigInteger upperThreshold = avgY.add(standardDeviation.multiply(BigInteger.valueOf(2)));
        BigInteger lowerThreshold = avgY.subtract(standardDeviation.multiply(BigInteger.valueOf(2)));

        // Identify anomaly points
        for (Coordinate coordinate : coordinates) {
            if (coordinate.yCoord.compareTo(upperThreshold) > 0 || coordinate.yCoord.compareTo(lowerThreshold) < 0) {
                anomalies.add(coordinate);
            }
        }

        // Limit to a maximum of 3 anomaly points
        return anomalies.size() > 3 ? anomalies.subList(0, 3) : anomalies;
    }
}
