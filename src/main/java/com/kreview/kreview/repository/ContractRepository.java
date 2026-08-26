package com.kreview.kreview.repository;

import com.kreview.kreview.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractRepository extends JpaRepository<Contract, Long> {

  @Query(value = """
      SELECT * FROM contract
      WHERE search_vector @@ plainto_tsquery('english', :query)
      ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
      LIMIT 50
      """, nativeQuery = true)
  List<Contract> searchByText(@Param("query") String query);
}