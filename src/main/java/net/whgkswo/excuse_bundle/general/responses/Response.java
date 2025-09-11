package net.whgkswo.excuse_bundle.general.responses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;
import net.whgkswo.excuse_bundle.general.responses.dtos.SimpleBooleanDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.SimpleNumberDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.SimpleStringDto;

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

        public static Response<SimpleNumberDto> simpleNumber(long number){
            return new Response<>(new SimpleNumberDto(number));
        }
}
