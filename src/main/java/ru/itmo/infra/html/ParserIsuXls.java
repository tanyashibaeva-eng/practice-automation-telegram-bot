package ru.itmo.infra.html;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import ru.itmo.domain.dto.ExcelStudentInfoDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Log
public class ParserIsuXls {

    public static List<ExcelStudentInfoDTO> parseISUXls(File file) {
        List<ExcelStudentInfoDTO> dtos = new ArrayList<>();
        try {
            var doc = Jsoup.parse(file, "UTF-8");

            var groupElement = doc.select("p.c1 span.c2").first();
            String groupText = groupElement != null ? groupElement.text() : "";
            var group = "";
            var pattern = Pattern.compile("группы\\s+(\\S+)");
            var matcher = pattern.matcher(groupText);
            if (matcher.find()) {
                group = matcher.group(1);
            }

            var table = doc.select("table").first();
            if (table == null) {
                log.severe("Таблица не найдена");
                return dtos;
            }

            var rows = table.select("tr");

            for (int i = 1; i < rows.size(); i++) {
                var rowElement = rows.get(i);
                var cells = rowElement.select("td");

                if (cells.size() >= 3) {
                    List<String> errors = new ArrayList<>();

                    int rowNumber = i;

                    Integer isu = null;
                    try {
                        isu = Integer.parseInt(cells.get(1).text().trim());
                    } catch (NumberFormatException e) {
                        errors.add("Неверный формат номера");
                    }

                    String fullName = cells.get(2).text().trim();

                    ExcelStudentInfoDTO dto = new ExcelStudentInfoDTO(group, isu, fullName, rowNumber, errors);
                    dtos.add(dto);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dtos;
    }
}