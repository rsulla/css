package com.relsulla.css;

import com.bedatadriven.spss.SpssDataFileReader;
import com.bedatadriven.spss.SpssVariable;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class TestSPSS {

    private static String endLine;
    private static HashSet<String> ingoreList = new HashSet<>();

    private String reportYear;

    public static void main(String[] args) throws  Exception {
        TestSPSS instance = new TestSPSS();

        if ( System.getProperty("os.name").toLowerCase().startsWith("win") ) {
            endLine = "\r\n";
        } else {
            endLine = "\n";
        }

        ingoreList.add("ADDRE0");
        ingoreList.add("ADDRE1");
        ingoreList.add("ADDRE2");
        ingoreList.add("ADDRE3");
        ingoreList.add("BRANC0");
        ingoreList.add("BRANC1");
        ingoreList.add("BRANC2");
        ingoreList.add("BRANC3");
        ingoreList.add("CITY0");
        ingoreList.add("CITY1");
        ingoreList.add("CITY2");
        ingoreList.add("CITY3");
        ingoreList.add("FIRE_0");
        ingoreList.add("INSTN0");
        ingoreList.add("INSTN1");
        ingoreList.add("INSTN2");
        ingoreList.add("INSTN3");

        instance.run2();
    }

    private void run() throws Exception {

        BufferedWriter bw = null;
        FileWriter fw = null;
        StringBuilder out = new StringBuilder();

        //System.out.println(System.getProperty("os.name").toLowerCase());
       // System.exit(8);
        fw = new FileWriter("C:\\Users\\Bob\\Downloads\\Campus\\Crime2015SPSS\\tout.txt");
        bw = new BufferedWriter(fw);

        File folder = new File("C:\\Users\\Bob\\Downloads\\Campus\\Crime2015SPSS");
        File[] listOfFiles = folder.listFiles();

        for ( int idx=0; idx < listOfFiles.length; idx++ ) {
            if ( listOfFiles[idx].isFile() ) {
                if ( listOfFiles[idx].getName().endsWith(".sav") ) {
                    procFile(listOfFiles[idx], bw);
                    //System.out.println(idx + " " + listOfFiles[idx]);
                }
            }
        }

        bw.close();

        System.out.println("DONE");

        System.exit(8);


        SpssDataFileReader spssDataFileReader = new SpssDataFileReader("C:\\Users\\Bob\\Downloads\\Campus\\Crime2015SPSS\\oncampusarrest121314.sav");

        for (SpssVariable variable : spssDataFileReader.getVariables()) {
            System.out.println(variable.getVariableLabel() + " " + variable.getTypeCode() + " " + variable.getVariableName() + " " + variable.isNumeric() );
        }

//        while(spssDataFileReader.readNextCase()) {
//        }

        List<SpssVariable> vars = spssDataFileReader.getVariables();

        out.setLength(0);

        for ( SpssVariable var : vars) {
            if ( out.length() > 0 ) {
                out.append("\t");
            }
            out.append(var.getVariableName());
        }
        out.append("\n");

        bw.write(out.toString());

        String sv;
        Double dv;

        int cnt = 0;

        while(spssDataFileReader.readNextCase()) {
            out.setLength(0);
            for ( SpssVariable var : vars) {
                if ( out.length() > 0 ) {
                    out.append("\t");
                }
                if ( var.isNumeric() ) {
                    dv = spssDataFileReader.getDoubleValue(var.getVariableName());
                    if ( dv.isNaN() ) {
                        out.append("NULL");
                    } else {
                        out.append(dv.toString());
                    }
                } else {
                    sv = spssDataFileReader.getStringValue(var.getVariableName());
                    out.append(sv);
                }

            }
            out.append("\n");
            bw.write(out.toString());

            cnt++;

            if ( (cnt % 100) == 0 ) {
                System.out.println(cnt);
            }
        }

        bw.close();

        System.out.println("DONE");
    }

    private void procFile(File savFile
                         ,BufferedWriter bw) {

        StringBuilder out = new StringBuilder();
        String[] nameYear;

        try {
            System.out.println(savFile.getName());

            SpssDataFileReader spssDataFileReader = new SpssDataFileReader(savFile.getPath());

            for (SpssVariable variable : spssDataFileReader.getVariables()) {
                if ( !ingoreList.contains(variable.getVariableName()) && !variable.getVariableName().startsWith("FILTER")) {
                    out.setLength(0);
                    out.append(stripNumericSuffix(savFile.getName().substring(0,savFile.getName().length()-4)).toLowerCase());
                    out.append("\t");
                    out.append(reportYear);
                    out.append("\t");

                    nameYear = fieldNameParts(variable.getVariableName());

                    //out.append(stripNumericSuffix(variable.getVariableName()));

                    out.append(nameYear[0].toUpperCase());
                    out.append("\t");
                    out.append(nameYear[1]);
                    out.append("\t");

                    out.append(variable.isNumeric() ? "YES" : "NO");
                    out.append(endLine);

                    bw.write(out.toString());
                    System.out.print(out.toString());
                }

                //System.out.println(variable.getVariableLabel() + " " + variable.getTypeCode() + " " + variable.getVariableName() + " " + variable.isNumeric() );
            }

            int cnt = 0;
            while(spssDataFileReader.readNextCase()) {
                cnt++;
            }
            System.out.println("ROW COUNT = " + cnt);
            spssDataFileReader = null;
            Thread.sleep(333);
            System.gc();
            Thread.sleep(333);


        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(8);
        }
    }

    private String stripNumericSuffix(String val) {

        StringBuilder retVal = new StringBuilder();
        int idx = 0;
        int len = val.length();

        while ( idx < len && !strIsNumber(val.charAt(idx)) ) {
            retVal.append(val.charAt(idx));
            idx++;
        }


        return(retVal.toString());

    }

    private boolean strIsNumber(Character val) {

        if ( val >= '0' && val <= '9' ) {
            return(true);
        } else {
            return(false);
        }

    }

    private String[] fieldNameParts(String fieldName) {

        String[] retParts = new String[2];
        String year = "";
        int idx = fieldName.length()-1;

        while ( fieldName.charAt(idx) >= '0' && fieldName.charAt(idx) <= '9' ) {
            year = fieldName.charAt(idx) + year;
            idx--;
        }

        if ( year.length() > 0 ) {
            retParts[1] = String.valueOf(Integer.parseInt(year) + 2000);
        } else {
            retParts[1] = "";
        }

        retParts[0] = fieldName.substring(0,idx+1);

        return(retParts);

    }

    private void run2() {

        //System.out.println(fieldNameParts("UNITID_P")[0] + "|" + fieldNameParts("UNITID_P")[1] + "|");
        //System.out.println(fieldNameParts("ARSON7")[0] + "|" + fieldNameParts("ARSON7")[1] + "|");
        //System.out.println(fieldNameParts("ARSON05")[0] + "|" + fieldNameParts("ARSON05")[1] + "|");
        //System.out.println(fieldNameParts("ARSON15")[0] + "|" + fieldNameParts("ARSON15")[1] + "|");

        //System.exit(88);
        try {
            BufferedWriter bw = null;
            FileWriter fw = null;
            StringBuilder out = new StringBuilder();

            fw = new FileWriter("C:\\Users\\Bob\\Downloads\\Campus\\SPSS\\tout.txt");
            bw = new BufferedWriter(fw);

            File folder = new File("C:\\Users\\Bob\\Downloads\\Campus\\SPSS");
            File[] listOfFiles = folder.listFiles();

            for ( int idx=0; idx < listOfFiles.length; idx++ ) {
                if ( listOfFiles[idx].isFile() ) {
                    if ( listOfFiles[idx].getName().endsWith(".zip") ) {

                        reportYear = listOfFiles[idx].getName().substring(5,9);

                        System.out.println(idx + " " + listOfFiles[idx]);
                        unZipIt(listOfFiles[idx].getPath(),"C:\\Users\\Bob\\Downloads\\Campus\\SPSS\\tmp");

                        File tmpFolder = new File("C:\\Users\\Bob\\Downloads\\Campus\\SPSS\\tmp");
                        File[] listOfSavFiles = tmpFolder.listFiles();

                        for ( int idx2=0; idx2 < listOfSavFiles.length; idx2++ ) {
                            if ( listOfSavFiles[idx2].isFile() && listOfSavFiles[idx2].getName().toLowerCase().endsWith(".sav") ) {
                                procFile(listOfSavFiles[idx2],bw);
                            }
                        }

                        deleteFolder("C:\\Users\\Bob\\Downloads\\Campus\\SPSS\\tmp");
                    }
                }
            }

            bw.close();

            System.out.println("DONE");

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(8);
        }
    }

    public void unZipIt(String zipFile
                       ,String outputFolder) {

        byte[] buffer = new byte[1024];

        try{
            deleteFolder(outputFolder);

            File folder = new File(outputFolder);
            folder.mkdir();

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
            System.exit(8);
        }
    }

    private void deleteFolder(String path) {

        File folder = new File(path);

        if ( folder.isDirectory() ) {
            File[] listOfFiles = folder.listFiles();

            for ( int idx=0; idx < listOfFiles.length; idx++ ) {
                if ( listOfFiles[idx].isFile() ) {
                    System.out.println("DELETE FILE: " + listOfFiles[idx].getPath());
                    if ( !listOfFiles[idx].delete() ) {
                        System.exit(99);
                    }
                } else if ( listOfFiles[idx].isDirectory() ) {
                    deleteFolder( listOfFiles[idx].getPath() );
                }
            }

            System.out.println("DELETE FOLDER: " + folder.getPath());
            if ( !folder.delete() ) {
                System.exit(99);
            }
        }
    }
}
