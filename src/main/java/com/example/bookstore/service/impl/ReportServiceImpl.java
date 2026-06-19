package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.BookSalesReportResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.model.User;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.repository.ReportRepository;
import com.example.bookstore.repository.StoreRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookSalesReportResponse> getSalesByBook(LocalDateTime from, LocalDateTime to) {
        Long storeId = resolveStoreId();
        return reportRepository.findSalesByBook(from, to, storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary(LocalDateTime from, LocalDateTime to) {
        Long storeId = resolveStoreId();
        return reportRepository.findSummary(from, to, storeId);
    }

    // ADMIN → null (ve todas las librerías). STORE → filtra por su propia librería.
    private Long resolveStoreId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getRole() == Role.STORE) {
            return storeRepository.findByOwnerId(user.getId())
                    .map(store -> store.getId())
                    .orElse(null);
        }
        return null;
    }
}
