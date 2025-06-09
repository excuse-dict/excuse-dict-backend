package net.whgkswo.excuse_bundle.entities.members.core.mapper;

import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberRegistrationDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member dtoToUser(MemberRegistrationDto dto);

    @Mapping(target = "rank", source = "memberRank.type")
    MemberResponseDto memberToMemberResponseDto(Member member);

    @AfterMapping
    default void debugMemberMapping(@MappingTarget MemberResponseDto dto, Member member) {
        System.out.println("=== MEMBER MAPPER DEBUG ===");
        System.out.println("Source Member: " + member);
        System.out.println("Source MemberRank: " + member.getMemberRank());
        if (member.getMemberRank() != null) {
            System.out.println("Source MemberRank type: " + member.getMemberRank().getType());
        }
        System.out.println("Target DTO: " + dto);
        System.out.println("Target DTO rank: " + dto.rank());
        System.out.println("========================");
    }
}