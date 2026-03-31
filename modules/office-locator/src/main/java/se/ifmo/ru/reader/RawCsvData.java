package se.ifmo.ru.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RawCsvData {
    private List<String> headers;
    private List<RawCsvRecord> records;
}
