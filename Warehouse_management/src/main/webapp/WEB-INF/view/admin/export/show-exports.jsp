<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<% request.setAttribute("activeMenu", "exports"); %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <!-- <script src="https://cdn.jsdelivr.net/npm/chart.js"></script> -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử xuất kho - Quản lý kho</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/style-export.css">
</head>
<body>
    <style>
        .html-chart {
            display: flex;
            align-items: flex-end;
            gap: 10px;
            height: 380px;
            overflow-x: auto;
            padding-top: 20px;
            padding-bottom: 8px;
        }

        .chart-col {
            min-width: 48px;
            flex: 1 0 48px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-end;
        }

        .chart-value {
            font-size: 12px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 8px;
            line-height: 1;
        }

        .chart-bar-box {
            width: 100%;
            height: 260px;
            display: flex;
            align-items: flex-end;
            justify-content: center;
            background: linear-gradient(to top, #eff6ff, #ffffff);
            border-radius: 10px;
            padding: 0 4px;
        }

        .chart-bar {
            width: 100%;
            min-height: 4px;
            background: linear-gradient(180deg, #60a5fa 0%, #2563eb 100%);
            border-radius: 10px 10px 0 0;
            transition: 0.25s ease;
        }

        .chart-bar:hover {
            filter: brightness(1.08);
        }

        .chart-label {
            margin-top: 10px;
            font-size: 12px;
            color: #64748b;
            text-align: center;
            word-break: break-word;
        }

        .chart-empty {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #94a3b8;
            font-size: 14px;
            font-style: italic;
        }


        .chart-wrap {
            height: 380px;
        }


        .data-card{
            min-height: 400px;
        }
    </style>

    <jsp:include page="../layout/sidebar.jsp" />

    <div class="main-content">
        <h1 class="page-header">Lịch sử xuất kho</h1>

        <form class="toolbar" action="/admin/exports" method="get">
            <div class="search-box">
                <i class="fas fa-search"></i>
                <input type="text" name="exportCode" value="${param.exportCode}" placeholder="Tìm kiếm theo mã phiếu (ví dụ: XK001)...">
            </div>

            <div class="filter-box">
                <select name="userId" onchange="this.form.submit()">
                    <option value="">Tất cả người thực hiện</option>
                    <c:forEach var="user" items="${users}">
                        <option value="${user.id}" <c:if test="${user.id==selectedUserId}">selected="selected"</c:if>>
                            ${user.fullName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <input type="hidden" name="statType" value="${selectedStatType}">

            <div class="action-box">
                <a href="/admin/exports/create" class="btn-create-export">
                    <i class="fas fa-plus"></i> Tạo phiếu xuất
                </a>
            </div>
        </form>

        <div class="data-card" modelAttribute="exports">
            <table>
                <thead>
                    <tr>
                        <th>Mã phiếu</th>
                        <th>Ngày xuất</th>
                        <th>Người thực hiện</th>
                        <th>Số lượng vật tư</th>
                        <th>Tổng tiền</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="export" items="${exports}">
                        <tr>
                            <td>${export.code}</td>
                            <td>${export.date}</td>
                            <td>${export.userExport.fullName}</td>
                            <td><strong>${export.totalItems}</strong></td>
                            <td><strong>${export.totalAmount}</strong></td>
                            <td class="action-links">
                                <a href="/admin/exports/${export.id}" class="detail-link">Chi ti&#7871;t</a>
                                <sec:authorize access="hasRole('ADMIN')">
                                    <a href="/admin/exports/delete/${export.id}" class="delete-link"
                                       onclick="return confirm('Cảnh báo: Xóa phiếu xuất sẽ làm thay đổi số lượng kho! Bạn chắc chắn chứ?')">
                                        Xóa
                                    </a>
                                </sec:authorize>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <c:if test="${totalPages > 0}">
                <div class="pagination-container" style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px;">
                    <div class="page-info" style="color: #64748b; font-size: 14px;">
                        Trang ${currentPage + 1} tr&#234;n ${totalPages}
                    </div>

                    <c:set var="maxPages" value="5" />
                    <c:set var="half" value="2" />
                    <c:set var="begin" value="${currentPage - half}" />
                    <c:set var="end" value="${currentPage + half}" />

                    <c:if test="${begin < 0}">
                        <c:set var="begin" value="0" />
                        <c:set var="end" value="${totalPages - 1 < maxPages - 1 ? totalPages - 1 : maxPages - 1}" />
                    </c:if>

                    <c:if test="${end > totalPages - 1}">
                        <c:set var="end" value="${totalPages - 1}" />
                        <c:set var="begin" value="${end - maxPages + 1 < 0 ? 0 : end - maxPages + 1}" />
                    </c:if>

                    <div class="page-buttons" style="display: flex; gap: 5px; align-items: center;">
                        <c:if test="${begin > 0}">
                            <a href="/admin/exports?page=0&userId=${selectedUserId}&exportCode=${param.exportCode}&statType=${selectedStatType}" class="btn-page">1</a>
                            <c:if test="${begin > 1}">
                                <span style="color: #94a3b8; padding: 0 4px;">...</span>
                            </c:if>
                        </c:if>

                        <c:forEach begin="${begin}" end="${end}" var="i">
                            <a href="/admin/exports?page=${i}&userId=${selectedUserId}&exportCode=${param.exportCode}&statType=${selectedStatType}" class="btn-page ${i == currentPage ? 'active' : ''}">
                                ${i + 1}
                            </a>
                        </c:forEach>

                        <c:if test="${end < totalPages - 1}">
                            <c:if test="${end < totalPages - 2}">
                                <span style="color: #94a3b8; padding: 0 4px;">...</span>
                            </c:if>
                            <a href="/admin/exports?page=${totalPages - 1}&userId=${selectedUserId}&exportCode=${param.exportCode}&statType=${selectedStatType}" class="btn-page">
                                ${totalPages}
                            </a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>

        <!-- Biểu đồ thống kê xuất kho -->
        <c:set var="maxQuantity" value="0" />
        <c:forEach var="item" items="${chartStats}">
            <c:if test="${item.totalQuantity > maxQuantity}">
                <c:set var="maxQuantity" value="${item.totalQuantity}" />
            </c:if>
        </c:forEach>

        <div class="stats-card">
            <div class="stats-header">
                <div class="stats-title">
                    <h2>Biểu đồ thống kê xuất kho</h2>
                    <p>${chartTitle}</p>
                </div>

                <form class="stats-filter" action="/admin/exports" method="get">
                    <input type="hidden" name="userId" value="${selectedUserId}">
                    <input type="hidden" name="exportCode" value="${param.exportCode}">

                    <div class="field">
                        <label for="statType">Thống kê theo</label>
                        <select id="statType" name="statType">
                            <option value="day" ${selectedStatType == 'day' ? 'selected' : ''}>Ngày</option>
                            <option value="month" ${selectedStatType == 'month' ? 'selected' : ''}>Tháng</option>
                            <option value="year" ${selectedStatType == 'year' ? 'selected' : ''}>Năm</option>
                        </select>
                    </div>

                    <button type="submit">Xem biểu đồ</button>
                </form>
            </div>

            <div class="chart-wrap html-chart">
                <c:choose>
                    <c:when test="${not empty chartStats}">
                        <c:forEach var="item" items="${chartStats}">
                            <c:set var="barHeight" value="${maxQuantity > 0 ? (item.totalQuantity * 100.0 / maxQuantity) : 0}" />
                            <div class="chart-col">
                                <div class="chart-value">${item.totalQuantity}</div>
                                <div class="chart-bar-box">
                                    <div class="chart-bar" style="height: ${barHeight}%;"></div>
                                </div>
                                <div class="chart-label">${item.label}</div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="chart-empty">Không có dữ liệu thống kê.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

</body>
</html>
