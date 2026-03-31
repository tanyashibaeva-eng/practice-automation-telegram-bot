package ru.itmo.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UpdateResult {
    private int added;
    private int updated;
    private List<String> errors;
}