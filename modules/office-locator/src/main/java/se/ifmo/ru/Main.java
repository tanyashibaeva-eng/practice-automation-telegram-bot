package se.ifmo.ru;

import se.ifmo.ru.parser.CompanyCsvParser;
import se.ifmo.ru.parser.CompanyRecord;
import se.ifmo.ru.reader.CsvReader;
import se.ifmo.ru.reader.RawCsvRecord;
import se.ifmo.ru.service.CompanyService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
//        Optional<List<RawCsvRecord>> rawCsvRecordListOptional =  csvReader.readRawCsvData("src/main/resources/SPARK_IT.csv");
//        if (rawCsvRecordListOptional.isPresent()){
//            List<CompanyRecord> companyRecordList = CompanyCsvParser.parseFromRawRecords(rawCsvRecordListOptional.get());
//        }
        CompanyService companyService = new CompanyService("src/main/resources/SPARK_IT.csv");
        System.out.println(companyService.findCompanyRecordByINN("7802170553"));
//        companyService.removeCompanyRecordByINN("7736207543");
//        companyService.addCompanyRecord("Яндекс", "7736207543",
//                "Санкт-Петербург, пр. Пискаревский, д. 2, к. 2, лит. Щ (Бизнес-центр Бенуа)");
    }
}
