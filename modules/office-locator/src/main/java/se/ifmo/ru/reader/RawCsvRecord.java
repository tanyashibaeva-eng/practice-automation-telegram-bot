package se.ifmo.ru.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RawCsvRecord {
    private long lineNumber;
    private List<String> rawColumns;
}
