package com.shortner.service;

import com.shortner.entity.AccessLog;
import com.shortner.entity.ShortLink;
import com.shortner.entity.User;
import com.shortner.repository.AccessLogRepository;
import com.shortner.util.IpHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    /**
     * REQUIRES_NEW so a logging failure (or the log write itself) never rolls back
     * the actual redirect/access decision it's recording - logging is best-effort,
     * not part of the core transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(ShortLink link, User accessedBy, String rawIpAddress, boolean granted) {
        AccessLog log = AccessLog.builder()
            .link(link)
            .accessedBy(accessedBy)
            .ipHash(IpHashUtil.hash(rawIpAddress))
            .accessGranted(granted)
            .build();

        accessLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AccessLog> getLogsForLink(UUID linkId, Pageable pageable) {
        return accessLogRepository.findByLinkIdOrderByAccessedAtDesc(linkId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AccessLog> getDeniedAttemptsForLink(UUID linkId, Pageable pageable) {
        return accessLogRepository.findByLinkIdAndAccessGrantedFalseOrderByAccessedAtDesc(linkId, pageable);
    }
}