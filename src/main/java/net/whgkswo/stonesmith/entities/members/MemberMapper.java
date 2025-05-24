package net.whgkswo.stonesmith.entities.members;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member dtoToUser(MemberDto dto);
}