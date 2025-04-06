package ru.itmo.infra.storage;

import lombok.Builder;
import lombok.Data;
import ru.itmo.domain.type.StudentStatus;

import java.util.List;

@Data
@Builder
public class Filter {
    private String eduStreamName;
    private List<String> stGroups;
    private List<StudentStatus> stStatuses;

    public boolean isEmpty() {
        return (eduStreamName == null
                && (stGroups == null || stGroups.isEmpty())
                && (stStatuses == null || stStatuses.isEmpty()));
    }
}
