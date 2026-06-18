package com.sme.erp.accounting.service.impl;
import com.sme.erp.accounting.dto.CostCenterDTO; import com.sme.erp.accounting.entity.CostCenter; import com.sme.erp.accounting.repository.CostCenterRepository; import com.sme.erp.accounting.service.CostCenterService; import com.sme.erp.common.exception.*; import com.sme.erp.common.util.RequestValueUtils; import com.sme.erp.enums.Status; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service public class CostCenterServiceImpl implements CostCenterService {
 private final CostCenterRepository repo; public CostCenterServiceImpl(CostCenterRepository r){repo=r;}
 @Transactional(readOnly=true) public List<CostCenterDTO> all(){return repo.findAllByOrderByCode().stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public Page<CostCenterDTO> page(String q,Status s,int p,int z){return repo.search(RequestValueUtils.normalize(q),s,PageRequest.of(Math.max(0,p),Math.min(100,Math.max(1,z)),Sort.by("code"))).map(this::dto);}
 @Transactional(readOnly=true) public CostCenterDTO get(Long id){return dto(find(id));}
 @Transactional public CostCenterDTO create(CostCenterDTO d){String code=RequestValueUtils.normalizeRequired(d.code(),"Code"); if(repo.existsByCodeIgnoreCase(code))throw new BadRequestException("Cost center code already exists"); CostCenter c=new CostCenter(); fill(c,d,code); return dto(repo.save(c));}
 @Transactional public CostCenterDTO update(Long id,CostCenterDTO d){CostCenter c=find(id); String code=RequestValueUtils.normalizeRequired(d.code(),"Code"); repo.findByCodeIgnoreCase(code).filter(x->!x.getId().equals(id)).ifPresent(x->{throw new BadRequestException("Cost center code already exists");}); fill(c,d,code); return dto(repo.save(c));}
 @Transactional public void deactivate(Long id){CostCenter c=find(id);c.setStatus(Status.INACTIVE);repo.save(c);}
 private void fill(CostCenter c,CostCenterDTO d,String code){c.setCode(code);c.setName(RequestValueUtils.normalizeRequired(d.name(),"Name"));c.setDescription(RequestValueUtils.normalize(d.description()));c.setStatus(d.status()==null?Status.ACTIVE:d.status());}
 private CostCenter find(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Cost center not found with id: "+id));}
 private CostCenterDTO dto(CostCenter c){return new CostCenterDTO(c.getId(),c.getCode(),c.getName(),c.getDescription(),c.getStatus(),c.getCreatedAt(),c.getUpdatedAt());}
}
