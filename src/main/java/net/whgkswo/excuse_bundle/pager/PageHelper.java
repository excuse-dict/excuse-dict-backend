package net.whgkswo.excuse_bundle.pager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageHelper {

    public <T> Page<T> paginate(List<T> contents, Pageable pageable){
        if(contents.isEmpty()) return Page.empty(pageable);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), contents.size());

        List<T> subList = start >= contents.size() ? new ArrayList<>() : contents.subList(start, end);

        return new PageImpl<>(subList, pageable, contents.size());
    }
}
