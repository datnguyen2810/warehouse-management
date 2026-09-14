package vn.xuandat.Warehouse_management.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.xuandat.Warehouse_management.entity.Export;

@Repository
public interface ExportRepository extends JpaRepository<Export, Long> {
    @Query("""
            select e
            from Export e
            where (:userId is null or e.userExport.id = :userId)
            and (:exportCode is null or e.code like %:exportCode%)
            """)
    Page<Export> getPagedExport(@Param("userId") Long userID,
                                @Param("exportCode") String exportCode,
                                Pageable pageable);

    List<Export> findTop5ByOrderByDateDesc();

    List<Export> findByDateGreaterThanEqual(LocalDateTime fromDate);

    boolean existsByCode(String exportCode);

    @Query(value = """
            select date(e.date) as label, coalesce(sum(ed.export_quantity), 0) as totalQuantity
            from exports e
            join export_details ed on ed.export_id = e.id
            where e.date >= :fromDate
            group by date(e.date)
            order by date(e.date)
            """, nativeQuery = true)
    List<Object[]> getExportStatisticsByDate(@Param("fromDate") LocalDateTime fromDate);

    @Query(value = """
            select date_format(e.date, '%Y-%m') as label, coalesce(sum(ed.export_quantity), 0) as totalQuantity
            from exports e
            join export_details ed on ed.export_id = e.id
            where e.date >= :fromDate
            group by date_format(e.date, '%Y-%m')
            order by date_format(e.date, '%Y-%m')
            """, nativeQuery = true)
    List<Object[]> getExportStatisticsByMonth(@Param("fromDate") LocalDateTime fromDate);

    @Query(value = """
            select year(e.date) as label, coalesce(sum(ed.export_quantity), 0) as totalQuantity
            from exports e
            join export_details ed on ed.export_id = e.id
            where e.date >= :fromDate
            group by year(e.date)
            order by year(e.date)
            """, nativeQuery = true)
    List<Object[]> getExportStatisticsByYear(@Param("fromDate") LocalDateTime fromDate);
}
