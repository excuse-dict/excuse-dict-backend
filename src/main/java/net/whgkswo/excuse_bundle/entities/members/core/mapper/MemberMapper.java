package net.whgkswo.excuse_bundle.entities.members.core.mapper;

import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberRegistrationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member dtoToUser(MemberRegistrationDto dto);

    @Mapping(target = "rank", source = "memberRank")
    MemberResponseDto memberToMemberResponseDto(Member member);
}