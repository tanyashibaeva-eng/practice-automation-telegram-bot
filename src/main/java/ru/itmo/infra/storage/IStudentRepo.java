package ru.itmo.infra.storage;

import java.util.List;

public interface IStudentRepo {
    List<String> getAllGroupsByEduStreamID(long ID);
}
