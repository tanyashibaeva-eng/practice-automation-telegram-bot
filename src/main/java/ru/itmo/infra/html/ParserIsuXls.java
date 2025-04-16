package ru.itmo.infra.html;

import org.jsoup.Jsoup;
import ru.itmo.domain.dto.ExcelStudentInfoDTO;
import ru.itmo.exception.BadRequestException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ParserIsuXls {

    public static List<ExcelStudentInfoDTO> parseISUXls(File file) throws BadRequestException {
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
                throw new BadRequestException("Неверный шаблон, ожидается файл скаченный из ИСУ (список группы). Попробуйте загрузить другой файл или вернитесь в меню. При переходе обратно в меню все загруженные на данный момент файлы уже сохранены");
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