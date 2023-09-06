package ru.netology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        String[][] employees = new String[][]{
                "1,John,Smith,USA,25".split(","),
                "2,Inav,Petrov,RU,23".split(",")};

        createCVS(employees);

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "staff.csv";

        List<Employee> list1 = parseCSV(columnMapping, fileName);
        String json = listToJson(list1);
        writeString(json);

        List<Employee> list2 = parseXML("staff.xml");
        String json2 = listToJson(list2);
        writeString(json2);

        String json3 = readString("new_staff.json");
        List<Employee> list3 = jsonToList(json3);
        for (Employee e : list3) {
            System.out.println(e);
        }
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(json);
            JSONArray array = (JSONArray) object;
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (Object obj : array) {
                String employeeString = obj.toString();
                Employee employee = gson.fromJson(employeeString, Employee.class);
                list.add(employee);
            }
            return list;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readString(String s) {
        String json = null;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new BufferedReader(new FileReader(s)));
            JSONArray jsonArray = (JSONArray) obj;
            json = String.valueOf(jsonArray);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static List<Employee> parseXML(String s) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> list = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(s));
        Node root = doc.getDocumentElement();

        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element element = (Element) node;
                Employee employee = new Employee();
                employee.id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                employee.firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                employee.lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                employee.country = element.getElementsByTagName("country").item(0).getTextContent();
                employee.age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                list.add(employee);
            }
        }
        return list;
    }

    private static void writeString(String json) {
        try (FileWriter file = new FileWriter("new_staff.json")) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(list, listType);
        return json;
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping(columnMapping);

        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> list = csv.parse();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createCVS(String[][] employees) {
        try (CSVWriter writer = new CSVWriter(new FileWriter("staff.csv"))) {
            for (String[] s : employees) {
                writer.writeNext(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}