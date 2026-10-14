package kh.edu.istad.platform.business.application.port.out;

import kh.edu.istad.platform.business.domain.entity.Business;
import reactor.core.publisher.Mono;
public interface BusinessRepository { Mono<Business> save(Business business); }
