package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationFillingArgs {
    private String agentFullNameName;
    private String agentJobTitle;
}
