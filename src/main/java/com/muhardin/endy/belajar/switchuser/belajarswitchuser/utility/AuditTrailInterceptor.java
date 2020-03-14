package com.muhardin.endy.belajar.switchuser.belajarswitchuser.utility;

import com.muhardin.endy.belajar.switchuser.belajarswitchuser.dao.AuditLogDao;
import com.muhardin.endy.belajar.switchuser.belajarswitchuser.dao.PenggunaDao;
import com.muhardin.endy.belajar.switchuser.belajarswitchuser.entity.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AuditTrailInterceptor extends HandlerInterceptorAdapter {

    @Autowired private PenggunaDao penggunaDao;
    @Autowired private AuditLogDao auditLogDao;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        log.info("URL yang diakses : {}", request.getRequestURL());
        if (!request.getRequestURL().toString().contains("transaksi")) {
            log.info("Bukan url transaksi");
            return true;
        }
        Authentication currentUser = SecurityContextHolder.getContext()
                .getAuthentication();
        log.info("Current user : {}", currentUser.getName());
        Authentication userAsli = SwitchUserHelper.userAsli(currentUser);
        if (userAsli != null) {
            log.info("User asli : {}", userAsli.getName());
            AuditLog auditLog = new AuditLog();
            auditLog.setKeterangan("Mengakses "
                    +request.getRequestURL().toString()
                    +" sebagai user "
                    +currentUser.getName());
            auditLog.setWaktuKegiatan(LocalDateTime.now());
            auditLog.setPenggunaAsli(
                    penggunaDao.findByUsername(
                            userAsli.getName()).get());
            auditLog.setPenggunaDipakai(
                    penggunaDao.findByUsername(
                            currentUser.getName()).get());
            auditLogDao.save(auditLog);
        }

        return true;
    }
}
