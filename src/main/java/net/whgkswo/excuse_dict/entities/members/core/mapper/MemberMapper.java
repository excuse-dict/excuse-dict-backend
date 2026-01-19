package net.whgkswo.excuse_dict.entities.members.core.mapper;

import net.whgkswo.excuse_dict.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_dict.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_dict.entities.members.core.dto.MemberRegistrationDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member dtoToUser(MemberRegistrationDto dto);

    @Mapping(target = "rank", source = "memberRank.type")
    MemberResponseDto memberToMemberResponseDto(Member member);
}