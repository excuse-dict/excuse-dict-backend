package net.whgkswo.excuse_bundle.entities.members.core.mappers;

import net.whgkswo.excuse_bundle.entities.members.core.entities.Member;
import net.whgkswo.excuse_bundle.entities.members.core.dtos.MemberRegistrationDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member dtoToUser(MemberRegistrationDto dto);
}