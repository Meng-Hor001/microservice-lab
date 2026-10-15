package kh.edu.istad.platform.business.restapi.mapper;

import kh.edu.istad.platform.business.application.dto.RegisterBusinessCommand;
import kh.edu.istad.platform.business.restapi.dto.RegisterBusinessRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BusinessWebMapper {
    RegisterBusinessCommand toCommand(RegisterBusinessRequest request);
}
