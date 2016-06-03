/*
 * Copyright 2016 Jordan Carr
 * Project Name: finalProject
 * Class Name: computerScience_301_FinalProject.VocabularyGenerator
 * Last Modification Date: 6/2/16 11:55 PM
 */

package computerScience_301_FinalProject;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class VocabularyGenerator {

    private static boolean verbosity = false;

    /**
     * Takes user input on the location of the file they desire to have a sorted dictionary created. This method passes
     * and handles all relevant data and exceptions. When an exception is encountered the error message is printed to
     * screen so that the user can fix the issue or forward the error message to the developer
     *
     * @param args If "v" is passed as an argument any encountered exceptions will have a verbose output.
     */
    public static void main(String[] args) {
        if (args != null && args[0].equals("v")) {
            verbosity = true;
        }
        try {
            System.out.print("Please enter the location of the file you wish generate vocabulary lists for: ");
            final String inputLocation = new BufferedReader(new InputStreamReader(System.in)).readLine();
            final String[] outputLocations = {inputLocation, inputLocation + ".lowercase.output", inputLocation + ".uppercase.output"};
            fileOutput(outputLocations[1], sortData(deduplicate(fileInput(inputLocation, "[a-z]*"))));
            fileOutput(outputLocations[2], sortData(deduplicate(fileInput(inputLocation, "[A-Z][a-zA-Z]*"))));
            compressData(outputLocations);
            hashSHA512(outputLocations[0] + ".zip");
            deleteExcessFiles(outputLocations);
            System.out.println("No exceptions encountered.\nA compressed file containing the output files and a " +
                    "SHA-512 hash have been generated");
        } catch (Exception e) {
            if (verbosity) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Takes in the input file and after adding all valid word (beginning with a lower case letter and at least 3
     * letter long it returns the filled LinkedList.
     *
     * @param location The location of the input file.
     * @return The now filled LinkedList is returned with the valid words it contains.
     * @throws Exception The likely exception is that the file cannot be read though any exception can be thrown.
     */
    private static LinkedList<String> fileInput(String location, String regex) throws Exception {
        LinkedList<String> tmp = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(location));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            Scanner scanner = new Scanner(line);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if ((word.length() > 2) && word.matches(regex)) {
                    tmp.add(word);
                }
            }
            scanner.close();
        }
        return tmp;
    }

    /**
     * Sorts the data by passing the String array to the mergeSort method where the merge sorting takes place.
     *
     * @param input LinkedList containing the raw unsorted words that will be added to an array to be sorted.
     * @return The sorted array is returned.
     */
    private static String[] sortData(String[] input) {
        mergeSort(input);
        return input;
    }

    /**
     * Splits and sorts the portions of the input data which will be merged in the merge method.
     *
     * @param input Input String array containing the raw words.
     */
    private static void mergeSort(String[] input) {
        if (input.length > 1) {
            String[] left = new String[input.length / 2];
            String[] right = new String[input.length - (input.length / 2)];
            System.arraycopy(input, 0, left, 0, left.length);
            System.arraycopy(input, input.length / 2, right, 0, right.length);
            mergeSort(left);
            mergeSort(right);
            merge(input, left, right);
        }
    }

    /**
     * Takes the sorted pieces from the mergeSort method and merges them into one array.
     *
     * @param result The resultant merged array.
     * @param left   The left side of the non-merged array.
     * @param right  The right side of the non-merged array.
     */
    private static void merge(String[] result, String[] left, String[] right) {
        int a = 0;
        int b = 0;
        for (int i = 0; i < result.length; i++) {
            if ((b >= right.length) || ((a < left.length) && (left[a].compareToIgnoreCase(right[b]) < 0))) {
                result[i] = left[a++];
            } else {
                result[i] = right[b++];
            }
        }
    }

    /**
     * Writes the output to dick at a desired location from a specified input.
     *
     * @param location Where the file will be written and with what file name.
     * @param input    The source of the data to be written to file.
     * @throws Exception The likely exception is that the file cannot be written though any exception can be thrown.
     */
    private static void fileOutput(String location, String[] input) throws Exception {
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(location));
        for (String anInput : input) {
            bufferedWriter.write(anInput + "\n");
        }
        bufferedWriter.close();
    }

    /**
     * Sets up and passes the output hash to the appropriate methods.
     *
     * @param location Where the output file will be written by passing it to the fileOutput method.
     * @throws Exception The likely exception is that the hash algorithm doesn't exist although this will catch any
     *                   exception.
     */
    private static void hashSHA512(String location) throws Exception {
        final File file = new File(location);
        final String[] checksum = {getFileChecksum(MessageDigest.getInstance("SHA-512"), file)};
        if (checksum[0] == null) {
            throw new NullPointerException();
        }
        fileOutput(location + ".sha512", checksum);
    }

    /**
     * Takes the digest instance and file source, and computes a checksum from those values.
     *
     * @param digest The digest instance which in SHA-512 in this implementation.
     * @param file   The source file to have the checksum calculated from.
     * @return returns the completed checksum or, null if an exception is encountered.
     * @throws Exception The likely exception is that the file cannot be read though any exception can be thrown.
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        final FileInputStream fileInputStream = new FileInputStream(file);
        final byte[] byteArray = new byte[1024];
        int bytesCount;
        while ((bytesCount = fileInputStream.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fileInputStream.close();
        for (byte aByte : digest.digest()) {
            stringBuilder.append(Integer.toString(((int) aByte & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    /**
     * Takes an input String array and return a String array that has all duplicate entries removed.
     *
     * @param input The array to have duplicates removed from.
     * @return The array that had the duplicates removed
     */
    private static String[] deduplicate(LinkedList<String> input) {
        final HashSet<String> hashSet = new HashSet<>(input);
        return hashSet.toArray(new String[hashSet.size()]);
    }

    /**
     * Takes all the files in the input string and compresses them int a file named as the first element in the input
     * String + ".zip".
     *
     * @param input The String array containing all the file locations that will be used in the compression process.
     * @throws Exception The likely exception is that the files cannot be read although this will catch any exception.
     */
    private static void compressData(String[] input) throws Exception {
        byte[] buffer = new byte[1024];
        final ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(input[0] + ".zip"));
        for (String sourceFile : input) {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            zipOutputStream.putNextEntry(new ZipEntry(sourceFile));
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            zipOutputStream.closeEntry();
            fileInputStream.close();
        }
        zipOutputStream.close();
    }

    /**
     * Deletes the files in the outputLocations String array starting after the first element as to
     * not destroy the original file.
     *
     * @param input Takes in the String array of values that have previously been written, for deletion.
     * @throws Exception The likely exception is that the files cannot be deleted or read though any exception can be
     *                   thrown.
     */
    private static void deleteExcessFiles(String[] input) throws Exception {
        for (int i = 1; i < input.length; i++) {
            File file = new File(input[i]);
            if (!file.delete()) {
                throw new IOException();
            }
        }
    }
}