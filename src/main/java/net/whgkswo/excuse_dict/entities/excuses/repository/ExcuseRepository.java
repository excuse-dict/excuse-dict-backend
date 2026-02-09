package net.whgkswo.excuse_dict.entities.excuses.repository;

import net.whgkswo.excuse_dict.entities.excuses.Excuse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ExcuseRepository extends JpaRepository<Excuse, Long> {

    @Query(
            value = "SELECT DISTINCT morpheme FROM excuse_situation_morpheme",
            nativeQuery = true)
    List<String> findAllSituationMorphemes();

    @Query(
            value = "SELECT DISTINCT morpheme FROM excuse_excuse_morpheme",
            nativeQuery = true)
    List<String> findAllExcuseMorphemes();

    @Query(
            value = "SELECT DISTINCT morpheme FROM excuse_situation_morpheme " +
                    "UNION " +
                    "SELECT DISTINCT morpheme FROM excuse_excuse_morpheme"
            ,
            nativeQuery = true
    )
    List<String> findAllMorphemes();

    // 형태소에 연결된 게시물 조회
    @Query(value =
            "SELECT p.id, esm.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN excuse_situation_morpheme esm ON esm.excuse_id = e.id " +
                    "WHERE p.status = 'ACTIVE' " +
                    "AND esm.morpheme IN (:morphemes)",
            nativeQuery = true)
    List<Object[]> findPostIdsWithSituationMorphemes(
            @Param("morphemes") List<String> morphemes
    );

    @Query(value =
            "SELECT p.id, eem.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN excuse_excuse_morpheme eem ON eem.excuse_id = e.id " +
                    "WHERE eem.morpheme IN :morphemes " +
                    "AND p.status = 'ACTIVE'",
            nativeQuery = true)
    List<Object[]> findPostIdsWithExcuseMorphemes(
            @Param("morphemes") List<String> morphemes
    );

    @Query(value =
            "SELECT p.id, m.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN (" +
                    "  SELECT excuse_id, morpheme FROM excuse_situation_morpheme " +
                    "  WHERE morpheme IN :morphemes " +
                    "  UNION ALL " +
                    "  SELECT excuse_id, morpheme FROM excuse_excuse_morpheme " +
                    "  WHERE morpheme IN :morphemes" +
                    ") m ON m.excuse_id = e.id " +
                    "WHERE p.status = 'ACTIVE'",
            nativeQuery = true)
    List<Object[]> findPostIdsWithAllMorphemes(
            @Param("morphemes") List<String> morphemes
    );

    @Query(value =
            "SELECT p.id, esm.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN excuse_situation_morpheme esm ON esm.excuse_id = e.id " +
                    "WHERE p.status = 'ACTIVE' " +
                    "AND esm.morpheme IN (:morphemes) " +
                    "AND p.id IN (:postIds)",
            nativeQuery = true)
    List<Object[]> findPostIdsWithSituationMorphemesFiltered(
            @Param("morphemes") List<String> morphemes,
            @Param("postIds") Set<Long> postIds
    );

    @Query(value =
            "SELECT p.id, eem.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN excuse_excuse_morpheme eem ON eem.excuse_id = e.id " +
                    "WHERE eem.morpheme IN :morphemes " +
                    "AND p.status = 'ACTIVE' " +
                    "AND p.id IN (:postIds)",
            nativeQuery = true)
    List<Object[]> findPostIdsWithExcuseMorphemesFiltered(
            @Param("morphemes") List<String> morphemes,
            @Param("postIds") Set<Long> postIds
    );

    @Query(value =
            "SELECT p.id, m.morpheme " +
                    "FROM post p " +
                    "JOIN excuse e ON p.excuse_id = e.id " +
                    "JOIN (" +
                    "  SELECT excuse_id, morpheme FROM excuse_situation_morpheme " +
                    "  WHERE morpheme IN :morphemes " +
                    "  UNION ALL " +
                    "  SELECT excuse_id, morpheme FROM excuse_excuse_morpheme " +
                    "  WHERE morpheme IN :morphemes" +
                    ") m ON m.excuse_id = e.id " +
                    "WHERE p.status = 'ACTIVE' " +
                    "AND p.id IN (:postIds)",
            nativeQuery = true)
    List<Object[]> findPostIdsWithAllMorphemesFiltered(
            @Param("morphemes") List<String> morphemes,
            @Param("postIds") Set<Long> postIds
    );
}
