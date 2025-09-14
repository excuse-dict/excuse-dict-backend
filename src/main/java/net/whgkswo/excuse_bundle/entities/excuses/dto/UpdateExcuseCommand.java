package net.whgkswo.excuse_bundle.entities.excuses.dto;

import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

import java.util.Optional;
import java.util.Set;

public record UpdateExcuseCommand(Optional<String> situation, Optional<String> excuseStr, Optional<Set<String>> tagKeys) implements Dto {
}
