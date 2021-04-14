package ru.abondin.hreasy.platform.api.employee;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.skills.SkillService;
import ru.abondin.hreasy.platform.service.skills.dto.SharedSkillNameDto;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDto;

import javax.validation.constraints.NotNull;

@RestController()
@RequestMapping("/api/v1/employee/skills")
@RequiredArgsConstructor
@Slf4j
public class EmployeeSkillsController {

    private final SkillService service;

    @GetMapping
    public Flux<SkillDto> mySkills() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.mySkills(auth));
    }

    @GetMapping("/shared/names")
    public Flux<SharedSkillNameDto> sharedSkillsNames() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.sharedSkillsNames(auth));
    }

    @GetMapping("/groups")
    public Flux<SimpleDictDto> skillsGroups() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.groups(auth));
    }

    /**
     * Add new skill to employee
     *
     * @param body
     * @return
     */
    @PostMapping("{employeeId}")
    public Mono<SkillDto> addSkill(@PathVariable int employeeId, @RequestBody AddSkillBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.addSkill(auth,
                employeeId, body));
    }

    /**
     * @param skillId
     * @param body
     * @return skill with updated
     */
    @PutMapping("{skillId}/rating")
    public Mono<SkillDto> updateRating(@PathVariable int skillId, @RequestBody SkillRatingBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.updateRating(auth,
                skillId, body));
    }

    @Data
    @ToString(of = "name")
    public static class AddSkillBody {
        @NotNull
        private int groupId;
        @NotNull
        private String name;
        @Nullable
        private SkillRatingBody rating;
    }

    @Data
    public static class SkillRatingBody {
        private float rating;
        private String notes;
    }
}
