package net.whgkswo.stonesmith.responses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE) // 생성자를 숨기고 팩토리 메서드 사용 강제
@Getter
public class Response<D extends Dto>{
        D data;

        public static<D extends Dto> Response<D> of(D data){
            return new Response<>(data);
        }

        public static Response<SimpleTextDto> simpleText(String text){
            return new Response<>(new SimpleTextDto(text));
        }
}
