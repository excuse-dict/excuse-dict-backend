package net.whgkswo.stonesmith.responses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.whgkswo.stonesmith.responses.dtos.Dto;
import net.whgkswo.stonesmith.responses.dtos.SimpleBooleanDto;
import net.whgkswo.stonesmith.responses.dtos.SimpleStringDto;

@AllArgsConstructor(access = AccessLevel.PRIVATE) // 생성자를 숨기고 팩토리 메서드 사용 강제
@Getter
public class Response<D extends Dto>{
        D data;

        public static<D extends Dto> Response<D> of(D data){
            return new Response<>(data);
        }

        public static Response<SimpleStringDto> simpleString(String text){
            return new Response<>(new SimpleStringDto(text));
        }

        public static Response<SimpleBooleanDto> simpleBoolean(boolean data){
            return new Response<>(new SimpleBooleanDto(data));
        }
}
