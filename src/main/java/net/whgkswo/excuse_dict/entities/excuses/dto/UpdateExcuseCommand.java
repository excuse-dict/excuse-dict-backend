package net.whgkswo.excuse_dict.entities.excuses.dto;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

import java.util.Optional;
import java.util.Set;

public record UpdateExcuseCommand(Optional<String> situation, Optional<String> excuseStr, Optional<Set<String>> tagKeys) implements Dto {
}
