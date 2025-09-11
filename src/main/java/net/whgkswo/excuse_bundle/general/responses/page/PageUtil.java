package net.whgkswo.excuse_bundle.general.responses.page;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

public class PageUtil {

    // 리스트로 페이지 객체 생성
    public static <T> Page<T> createPageFromList(List<T> data, int page, int size){
        int start = page * size;
        int end = Math.min(start + size, data.size());

        if (start < 0 || start > data.size()){
            // new ArrayList<>() 보다 Collections.emptyList() 가 좋음 (싱글톤이라 메모리 절약)
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), data.size());
        }

        List<T> pageContent = data.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), data.size());
    }
}
