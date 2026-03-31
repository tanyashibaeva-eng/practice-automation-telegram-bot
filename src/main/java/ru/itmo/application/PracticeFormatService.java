package ru.itmo.application;

import ru.itmo.domain.model.PracticeFormatEntity;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.PracticeFormatRepository;
import ru.itmo.infra.storage.StudentRepository;

import java.util.List;
import java.util.Optional;

public class PracticeFormatService {

    public static List<PracticeFormatEntity> findAllActive() throws InternalException {
        return PracticeFormatRepository.findAllActive();
    }

    public static Optional<PracticeFormatEntity> findByDisplayNameIgnoreCase(String displayName) throws InternalException {
        return PracticeFormatRepository.findByDisplayNameIgnoreCase(displayName);
    }

    public static Optional<PracticeFormatEntity> findById(long id) throws InternalException {
        return PracticeFormatRepository.findById(id);
    }

    public static Optional<PracticeFormatEntity> findByCodeIgnoreCase(String code) throws InternalException {
        return PracticeFormatRepository.findByCodeIgnoreCase(code);
    }

    public static PracticeFormatEntity create(String displayName) throws InternalException, BadRequestException {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new BadRequestException("Название формата не должно быть пустым");
        }
        if (PracticeFormatRepository.existsByDisplayNameIgnoreCase(displayName.trim())) {
            throw new BadRequestException("Формат с таким названием уже существует");
        }
        return PracticeFormatRepository.create(displayName.trim());
    }

    public static boolean rename(String oldDisplayName, String newDisplayName) throws InternalException, BadRequestException {
        if (oldDisplayName == null || oldDisplayName.trim().isEmpty()
                || newDisplayName == null || newDisplayName.trim().isEmpty()) {
            throw new BadRequestException("Неверный формат: ожидаются непустые старое и новое названия");
        }

        var oldOpt = PracticeFormatRepository.findByDisplayNameIgnoreCase(oldDisplayName.trim());
        if (oldOpt.isEmpty()) {
            throw new BadRequestException("Формат \"%s\" не найден".formatted(oldDisplayName));
        }

        if (PracticeFormatRepository.existsByDisplayNameIgnoreCase(newDisplayName.trim())) {
            throw new BadRequestException("Формат с названием \"%s\" уже существует".formatted(newDisplayName));
        }

        long formatId = oldOpt.get().getId();
        boolean renamed = PracticeFormatRepository.renameById(formatId, newDisplayName.trim());
        if (renamed) {
            StudentRepository.resetApplicationsByPracticeFormatIdInActiveStreams(formatId, false);
        }
        return renamed;
    }

    public static boolean delete(String displayName) throws InternalException, BadRequestException {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new BadRequestException("Неверный формат: название не должно быть пустым");
        }
        var oldOpt = PracticeFormatRepository.findByDisplayNameIgnoreCase(displayName.trim());
        if (oldOpt.isEmpty()) {
            throw new BadRequestException("Формат \"%s\" не найден".formatted(displayName));
        }
        long formatId = oldOpt.get().getId();
        StudentRepository.resetApplicationsByPracticeFormatIdInActiveStreams(formatId, true);
        return PracticeFormatRepository.deleteById(formatId);
    }
}

