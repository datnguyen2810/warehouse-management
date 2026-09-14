package vn.xuandat.Warehouse_management.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.xuandat.Warehouse_management.entity.Export;
import vn.xuandat.Warehouse_management.entity.ExportDetail;
import vn.xuandat.Warehouse_management.entity.ExportStatisticDTO;
import vn.xuandat.Warehouse_management.entity.User;
import vn.xuandat.Warehouse_management.repository.ExportDetailRepository;
import vn.xuandat.Warehouse_management.repository.ExportRepository;

@Service
public class ExportService {
    private final ExportRepository exportRepository;
    private final ExportDetailRepository exportDetailRepository;
    private final UserService userService;

    public ExportService(ExportRepository exportRepository, ExportDetailRepository exportDetailRepository, UserService userService) {
        this.exportRepository = exportRepository;
        this.exportDetailRepository = exportDetailRepository;
        this.userService = userService;
    }

    public Page<Export> getPagedExport(Long userId, String exportCode, Pageable pageable) {
        if(exportCode != null && exportCode.trim().isEmpty()) {
            exportCode = null;
        }
        Page<Export> exportPage = this.exportRepository.getPagedExport(userId, exportCode, pageable);
        for (Export exp : exportPage.getContent()) {
            // tính số lượng vật tư của từng phiếu
            long count = exportDetailRepository.sumQuantityByExportId(exp.getId());
            exp.setTotalItems(count);
        }
        return exportPage;
    }

    public List<ExportStatisticDTO> getExportStatistics(String statType) {
        return switch (statType) {
            case "day" -> buildLast30DaysStatistics();
            case "year" -> buildLast10YearsStatistics();
            case "month" -> buildLast12MonthsStatistics();
            default -> buildLast12MonthsStatistics();
        };
    }

    @Transactional
    public void handleSaveFinalExport(List<ExportDetail> tempList, String exportCode) {
        // 1. Tạo phiếu xuất mới (Bảng Exports)
        Export exp = new Export();
        exp.setDate(LocalDateTime.now());
        exp.setCode(exportCode);

        // Tính tổng tiền từ danh sách tạm
        double total = tempList.stream().mapToDouble(it -> it.getExport_price() * it.getExport_quantity()).sum();
        exp.setTotalAmount(total);

        // Lấy thông tin người dùng đang login (lấy từ Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userService.getUserByEmail(email);
        exp.setUserExport(currentUser);

        this.exportRepository.save(exp);

        // 2. Lưu chi tiết (Bảng Export_Details)
        for(ExportDetail detail : tempList){
            detail.setExportEntity(exp);
            this.exportDetailRepository.save(detail);
        }
    }

    @Transactional
    public void handleDeleteExport(Long exportId) {
        // Cascade delete sẽ tự động xóa ExportDetail
        this.exportRepository.deleteById(exportId);
    }

    public Export handleGetExportById(Long id) {
        return this.exportRepository.findById(id).orElse(null);
    }

    public boolean isCodeExists(String exportCode) {
        return exportRepository.existsByCode(exportCode);
    }

    private List<ExportStatisticDTO> buildLast30DaysStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(29);
        Map<String, Long> quantityByDate = toStringQuantityMap(exportRepository.getExportStatisticsByDate(fromDate.atStartOfDay())); // chuyển sang LocalDateTime để truy vấn
        List<ExportStatisticDTO> statistics = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd/MM");
        for (LocalDate date = fromDate; !date.isAfter(today); date = date.plusDays(1)) {
            statistics.add(new ExportStatisticDTO(labelFormatter.format(date), quantityByDate.getOrDefault(date.toString(), 0L)));
        }
        return statistics;
    }

    private List<ExportStatisticDTO> buildLast12MonthsStatistics() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth fromMonth = currentMonth.minusMonths(11);
        Map<String, Long> quantityByMonth = toStringQuantityMap(exportRepository.getExportStatisticsByMonth(fromMonth.atDay(1).atStartOfDay()));
        List<ExportStatisticDTO> statistics = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (YearMonth month = fromMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            statistics.add(new ExportStatisticDTO(labelFormatter.format(month.atDay(1)), quantityByMonth.getOrDefault(month.toString(), 0L)));
        }
        return statistics;
    }

    private List<ExportStatisticDTO> buildLast10YearsStatistics() {
        int currentYear = LocalDate.now().getYear();
        int fromYear = currentYear - 9;
        Map<Integer, Long> quantityByYear = toIntegerQuantityMap(exportRepository.getExportStatisticsByYear(LocalDate.of(fromYear, 1, 1).atStartOfDay()));
        List<ExportStatisticDTO> statistics = new ArrayList<>();
        for (int year = fromYear; year <= currentYear; year++) {
            statistics.add(new ExportStatisticDTO(String.valueOf(year), quantityByYear.getOrDefault(year, 0L)));
        }
        return statistics;
    }

    private Map<String, Long> toStringQuantityMap(List<Object[]> rawStatistics) {
        Map<String, Long> quantityMap = new HashMap<>();
        for (Object[] row : rawStatistics) {
            String labelValue = row[0].toString();
            long totalQuantity = ((Number) row[1]).longValue();
            quantityMap.put(labelValue, totalQuantity);
        }
        return quantityMap;
    }

    private Map<Integer, Long> toIntegerQuantityMap(List<Object[]> rawStatistics) {
        Map<Integer, Long> quantityMap = new HashMap<>();
        for (Object[] row : rawStatistics) {
            int labelValue = ((Number) row[0]).intValue();
            long totalQuantity = ((Number) row[1]).longValue();
            quantityMap.put(labelValue, totalQuantity);
        }
        return quantityMap;
    }
}
