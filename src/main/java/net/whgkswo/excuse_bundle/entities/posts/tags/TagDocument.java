package net.whgkswo.excuse_bundle.entities.posts.tags;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

// ElasticSearch 검색용 객체
@Document(indexName = "tags")
@NoArgsConstructor
@Getter
@Setter
public class TagDocument {

    @Id // JPA ID랑 다름
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String value;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Long)
    private Long entityId; // JPA 객체와 매핑

    public TagDocument(String value, String type, Long entityId){
        this.value = value;
        this.type = type;
        this.entityId = entityId;
    }

    // JPA 엔티티 -> Document 변환
    public static TagDocument from(Tag tag){
        return new TagDocument(
            tag.getValue(),
            tag.getType().name(),
            tag.getId()
        );
    }

    // Document -> JPA 엔티티
    public Tag toEntity(){
        Tag tag = new Tag();
        tag.setId(this.entityId);
        tag.setValue(this.value);
        tag.setType(Tag.Type.valueOf(this.type));
        return tag;
    }
}
