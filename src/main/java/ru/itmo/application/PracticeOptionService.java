package ru.itmo.application;

import ru.itmo.domain.model.PracticeOption;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.PracticeOptionRepository;

import java.util.List;

public class PracticeOptionService {
    public static List<PracticeOption> getAllOptions() throws InternalException {
        return PracticeOptionRepository.findAll();
    }

    public static List<PracticeOption> getEnabledOptions() throws InternalException {
        return PracticeOptionRepository.findAllEnabled();
    }

    public static PracticeOption addOption(String title) throws InternalException, BadRequestException {
        return addOption(title, false, true);
    }

    public static PracticeOption addOption(String title, boolean requiresItmoInfo, boolean requiresCompanyInfo) throws InternalException, BadRequestException {
        validateTitle(title);
        validateFlags(requiresItmoInfo, requiresCompanyInfo);
        return PracticeOptionRepository.create(title.trim(), requiresItmoInfo, requiresCompanyInfo);
    }

    public static void enableOption(long id) throws InternalException, BadRequestException {
        ensureExists(id);
        PracticeOptionRepository.updateEnabled(id, true);
    }

    public static void disableOption(long id) throws InternalException, BadRequestException {
        ensureExists(id);
        PracticeOptionRepository.updateEnabled(id, false);
    }

    public static void renameOption(long id, String newTitle) throws InternalException, BadRequestException {
        ensureExists(id);
        validateTitle(newTitle);
        PracticeOptionRepository.updateTitle(id, newTitle.trim());
    }

    public static void updateOptionFlags(long id, boolean requiresItmoInfo, boolean requiresCompanyInfo) throws InternalException, BadRequestException {
        ensureExists(id);
        validateFlags(requiresItmoInfo, requiresCompanyInfo);
        PracticeOptionRepository.updateFlags(id, requiresItmoInfo, requiresCompanyInfo);
    }

    public static void deleteOption(long id) throws InternalException, BadRequestException {
        ensureExists(id);
        PracticeOptionRepository.deleteById(id);
    }

    public static PracticeOption getEnabledOptionByTitleChecked(String title) throws InternalException, BadRequestException {
        var optionOpt = PracticeOptionRepository.findByTitle(title);
        if (optionOpt.isEmpty()) {
            throw new BadRequestException("Выбранный вариант не найден");
        }
        var option = optionOpt.get();
        if (!option.isEnabled()) {
            throw new BadRequestException("Этот вариант сейчас недоступен, выберите другой");
        }
        return option;
    }

    public static String optionsAsTextTable() throws InternalException {
        var all = getAllOptions();
        var sb = new StringBuilder("Список мест практики:\n");
        for (var option : all) {
            sb.append("- id=").append(option.getId())
                    .append(", ").append(option.isEnabled() ? "включен" : "выключен")
                    .append(", requires_itmo_info=").append(option.isRequiresItmoInfo())
                    .append(", requires_company_info=").append(option.isRequiresCompanyInfo())
                    .append(", title=").append(option.getTitle())
                    .append("\n");
        }
        if (all.isEmpty()) {
            sb.append("пусто\n");
        }
        return sb.toString().trim();
    }

    private static void ensureExists(long id) throws InternalException, BadRequestException {
        if (PracticeOptionRepository.findById(id).isEmpty()) {
            throw new BadRequestException("Вариант с id=%d не найден".formatted(id));
        }
    }

    private static void validateTitle(String title) throws BadRequestException {
        if (title == null || title.trim().isBlank()) {
            throw new BadRequestException("Название варианта не может быть пустым");
        }
    }

    private static void validateFlags(boolean requiresItmoInfo, boolean requiresCompanyInfo) throws BadRequestException {
        if (requiresItmoInfo && requiresCompanyInfo) {
            throw new BadRequestException("Нельзя одновременно требовать и ITMO, и company данные");
        }
    }
}
