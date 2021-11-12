package com.example.practice_task.repository.specification;

import com.example.practice_task.model.FileInfo;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileInfoSpecification {

    public static Specification<FileInfo> filenameStartsWith(final String filename) {
        if(filename==null)
            return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("filename"), "%");
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("filename"), filename+"%");
    }
    public static Specification<FileInfo> dateBetween(
            Date from, Date to) {

        return (Specification<FileInfo>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(builder.greaterThan(root.get("uploadDate"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThan(root.get("uploadDate"), to));
            }

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
