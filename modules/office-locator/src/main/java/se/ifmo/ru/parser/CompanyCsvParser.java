package se.ifmo.ru.parser;

import se.ifmo.ru.exception.InvalidCompanyDataException;
import se.ifmo.ru.reader.RawCsvRecord;

import java.util.List;

public class CompanyCsvParser {
    public static CompanyRecord parseFromRawRecord(RawCsvRecord rawCsvRecord) throws InvalidCompanyDataException {
        List<String> columns = rawCsvRecord.getRawColumns();
        if (columns == null || columns.size() < 5) {
            throw new InvalidCompanyDataException("CSV row has insufficient columns at line " + rawCsvRecord.getLineNumber());
        }
        String name = columns.get(1).trim();
        String INN = columns.get(4).trim();
        String address = columns.get(3).trim();
        if (name.isEmpty() || INN.isEmpty() || address.isEmpty()) {
            throw new InvalidCompanyDataException("CSV row contains empty required fields at line " + rawCsvRecord.getLineNumber());
        }
        return new CompanyRecord(name, INN, address);
    }

    public static List<CompanyRecord> parseFromRawRecords(List<RawCsvRecord> rawCsvRecordList)
            throws InvalidCompanyDataException {
        java.util.ArrayList<CompanyRecord> result = new java.util.ArrayList<>(rawCsvRecordList.size());
        for (RawCsvRecord record : rawCsvRecordList) {
            result.add(CompanyCsvParser.parseFromRawRecord(record));
        }
        return result;
    }
}
