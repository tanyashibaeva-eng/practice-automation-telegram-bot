package ru.itmo.application;

import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.EduStreamRepository;

import java.util.List;
import java.util.Optional;

public class EduStreamService {

    public static void createEduStream(EduStream eduStream) throws BadRequestException, InternalException {
        if (EduStreamRepository.existsByName(eduStream))
            throw new BadRequestException("Поток с таким именем уже существует");
        EduStreamRepository.save(eduStream);
    }

    public static boolean updateEduStream(EduStream oldEduStream, EduStream newEduStream) throws BadRequestException, InternalException {
        doesExistOrThrow(oldEduStream);
        return EduStreamRepository.updateByName(oldEduStream, newEduStream);
    }

    public static boolean deleteEduStream(EduStream eduStream) throws BadRequestException, InternalException {
        doesExistOrThrow(eduStream);
        return EduStreamRepository.deleteByName(eduStream);
    }

    public static Optional<EduStream> findEduStreamByName(EduStream eduStream) throws InternalException {
        return EduStreamRepository.findByName(eduStream);
    }

    public static List<EduStream> findAllEduStreams() throws InternalException {
        return EduStreamRepository.findAll();
    }

    public static void doesExistOrThrow(EduStream eduStream) throws InternalException, BadRequestException {
        if (!EduStreamRepository.existsByName(eduStream))
            throw new BadRequestException("Поток с таким именем не найден");
    }
}
