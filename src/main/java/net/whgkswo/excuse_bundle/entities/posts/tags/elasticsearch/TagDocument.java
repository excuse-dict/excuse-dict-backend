package net.whgkswo.excuse_bundle.entities.posts.tags.elasticsearch;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

// 엘라스틱서치 검색용 객체
@Document(indexName = TagDocument.INDEX_NAME)
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // ES에서 임의로 추가한 필드 무시 -> 역직렬화 오류 방지
public class TagDocument {

    public static final String INDEX_NAME = "tags";

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String value;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Text, analyzer = "nori")
    private Set<String> tagKeywords;

    @Field(type = FieldType.Text, analyzer = "nori")
    private Set<String> categoryKeywords;

    @Field(type = FieldType.Integer)
    private int popularity;

    // JPA -> ES 변환
    public static TagDocument from(Tag tag) {
        TagDocument doc = new TagDocument();
        doc.setId(tag.getId().toString());
        doc.setValue(tag.getValue());
        doc.setCategory(tag.getCategory().name());
        doc.setTagKeywords(tag.getTagKeywords());
        doc.setCategoryKeywords(tag.getCategory().getCategoryKeywords());
        doc.setPopularity(tag.getPopularity());
        return doc;
    }
}