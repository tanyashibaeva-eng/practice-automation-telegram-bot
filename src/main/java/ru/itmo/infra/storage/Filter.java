package ru.itmo.infra.storage;

import lombok.Builder;
import lombok.Data;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.type.StudentStatus;

import java.util.List;

@Data
@Builder
public class Filter {
    private EduStream eduStream;
    private List<String> stGroups;
    private List<StudentStatus> stStatuses;

    public boolean isEmpty() {
        return (eduStream == null
                && (stGroups == null || stGroups.isEmpty())
                && (stStatuses == null || stStatuses.isEmpty()));
    }
}
