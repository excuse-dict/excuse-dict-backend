package net.whgkswo.excuse_bundle.entities.members.core;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member dtoToUser(MemberRegistrationDto dto);
}