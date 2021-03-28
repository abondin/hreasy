package ru.abondin.hreasy.platform.service.skills;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.api.employee.EmployeeSkillsController;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.skills.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.currentproject.EmployeeProjectSecurityValidator;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.skills.dto.SharedSkillNameDto;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDto;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDtoMapper;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {
    private final DateTimeService dateTimeService;
    private final EmployeeProjectSecurityValidator securityValidator;
    private final SkillRepo skillRepo;
    private final SkillRatingRepo ratingRepo;
    private final SkillGroupRepo groupRepo;
    private final SkillDtoMapper mapper;
    private final HrEasyCommonProps props;


    public Flux<SimpleDictDto> groups(AuthContext auth) {
        return groupRepo.findNotArchived().map(mapper::groupToDto);
    }

    public Flux<SkillDto> mySkills(AuthContext auth) {
        var now = dateTimeService.now();
        return skillRepo.findWithRatingByEmployeeId(auth.getEmployeeInfo().getEmployeeId(), now).map(mapper::skillWithRating);
    }

    @Transactional
    public Mono<Integer> addSkill(AuthContext auth, int employeeId, EmployeeSkillsController.AddSkillBody body) {
        log.info("New skill {} for {} added by {}", auth.getUsername(), employeeId, body);
        var now = dateTimeService.now();
        return
                securityValidator.validateAddSkill(auth, employeeId)
                        .then(validateNotExists(employeeId, body.getGroupId(), body.getName()))
                        .then(doSave(auth, employeeId, body, now)
                                .flatMap(saved -> {
                                    if (body.getRating() == null) {
                                        return Mono.just(saved.getId());
                                    } else {
                                        var rating = new SkillRatingEntry();
                                        rating.setRating(body.getRating().getRating());
                                        rating.setCreatedAt(now);
                                        rating.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                                        rating.setSkillId(saved.getId());
                                        rating.setNotes(body.getRating().getNotes());
                                        rating.setUpdatedAt(now);
                                        return ratingRepo.save(rating).map((r) -> saved.getId());
                                    }
                                })
                        );
    }

    public Flux<SharedSkillNameDto> sharedSkillsNames(AuthContext auth) {
        return skillRepo.sharedSkills().map(e -> new SharedSkillNameDto(e.getGroupId(), e.getName()));
    }

    @Transactional
    public Mono<SkillDto> updateRating(AuthContext auth, Integer skillId, EmployeeSkillsController.SkillRatingBody body) {
        log.info("Update {} for skill {} added by {}", body, skillId, auth.getUsername());
        var createdBy = auth.getEmployeeInfo().getEmployeeId();
        var now = dateTimeService.now();
        var newSkillRating = new SkillRatingEntry();
        newSkillRating.setSkillId(skillId);
        newSkillRating.setUpdatedAt(now);
        newSkillRating.setCreatedAt(now);
        newSkillRating.setCreatedBy(createdBy);
        // 1. Get skill from DB
        return skillRepo.findById(skillId)
                .switchIfEmpty(Mono.error(new BusinessError("entity.not.found", Integer.toString(skillId))))
                .flatMap(skillEntry ->
                        //2. Validate security
                        securityValidator.validateUpdateRating(auth, skillEntry.getEmployeeId(), skillId)
                                .flatMap(b ->
                                        //3. Get current value of skill rating
                                        ratingRepo.findByCreatedByAndSkillId(createdBy, skillId)
                                                // 4. Prepare new skill rating in case of first rating from given employee
                                                .switchIfEmpty(Mono.just(newSkillRating))
                                                // 5. Create/Update rating
                                                .flatMap(ratingToSave -> {
                                                    ratingToSave.setRating(body.getRating());
                                                    ratingToSave.setNotes(body.getNotes());
                                                    ratingToSave.setUpdatedAt(now);
                                                    return ratingRepo.save(ratingToSave);
                                                    // 6. Reload skill to recalculate average rating
                                                }).flatMap(persisted -> skillRepo.findWithRatingByEmployeeAndSkillId(skillEntry.getEmployeeId(), skillId, now)
                                                .map(mapper::skillWithRating)
                                        )));
    }


    private Mono<SkillEntry> doSave(AuthContext auth, int employeeId, EmployeeSkillsController.AddSkillBody body, OffsetDateTime now) {
        var skillEntry = new SkillEntry();
        skillEntry.setEmployeeId(employeeId);
        skillEntry.setCreatedAt(now);
        skillEntry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        skillEntry.setGroupId(body.getGroupId());
        skillEntry.setName(body.getName());
        skillEntry.setShared(props.isSkillAddDefaultShared());

        return skillRepo.save(skillEntry);
    }

    private Mono<Object> validateNotExists(int employeeId, int skillGroupId, String skillName) {
        return skillRepo.findUnique(employeeId, skillGroupId, skillName)
                .flatMap(r -> Mono.error(
                        new BusinessError("errors.skill.already.exists",
                                Integer.toString(employeeId),
                                Integer.toString(skillGroupId),
                                skillName))
                ).switchIfEmpty(Mono.just(1));
    }

}
