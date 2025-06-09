package net.whgkswo.excuse_bundle.entities.excuses.mapper;

import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.tags.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {TagMapper.class})
public interface ExcuseMapper {

    ExcuseResponseDto excuseToExcuseResponseDto(Excuse excuse);
}
