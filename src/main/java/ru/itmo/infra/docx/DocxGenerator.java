package ru.itmo.infra.docx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import ru.itmo.domain.dto.ApplicationDTO;
import ru.itmo.exception.InternalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class DocxGenerator {

    private static final String TEMPLATE = "src/main/resources/templates/application.docx";

    public static File fillApplicationTemplate(ApplicationDTO dto) throws InternalException {
        try (FileInputStream fis = new FileInputStream(TEMPLATE)) {
            var doc = new XWPFDocument(fis);

            for (var paragraph : doc.getParagraphs()) {
                for (var run : paragraph.getRuns()) {
                    var text = run.getText(0);
                    if (text != null) {
                        text = text.replace("{ФИО}", dto.getFullName())
                                .replace("{Группа}", dto.getGroup())
                                .replace("{Дата1} - {Дата2}", dto.getStartDate() + " - " + dto.getEndDate())
                                .replace("{Формат}", dto.getFormatType())
                                .replace("{Компания}", dto.getCompanyName());
                        run.setText(text, 0);
                    }
                }
            }

            List<XWPFTable> tables = doc.getTables();
            for (var table : tables) {
                for (var row : table.getRows()) {
                    for (var cell : row.getTableCells()) {
                        for (var paragraph : cell.getParagraphs()) {
                            for (var run : paragraph.getRuns()) {
                                var text = run.getText(0);
                                if (text != null) {
                                    text = text.replace("{ФИО}", dto.getFullName())
                                            .replace("{Группа}", dto.getGroup())
                                            .replace("{Дата1}", dto.getStartDate())
                                            .replace("{Дата2}", dto.getEndDate())
                                            .replace("{Формат}", dto.getFormatType())
                                            .replace("{Компания}", dto.getCompanyName());
                                    run.setText(text, 0);
                                }
                            }
                        }
                    }
                }
            }

            var outputFile = new File("заявка – %s.docx".formatted(dto.getFullName()));
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                doc.write(fos);
            }

            return outputFile;
        } catch (Exception e) {
            throw new InternalException("Произошла ошибка при генерации файла: " + e.getMessage(), e);
        }
    }
}
