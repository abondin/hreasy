package ru.abondin.hreasy.platform.service.skills.dto;

import org.apache.commons.lang3.StringUtils;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper to map rating assembled string.
 */
public interface RatingsMapper {

    String SKILLS_SEPARATOR = "|$|";
    String SKILL_ATTRS_SEPARATOR = "|#|";

    /**
     * Every rating separated by '/'.<br>
     * Rating consists of employeeId and rating value separated by ','.<br>
     * For example <code>'3,4/5,2'</code> means that the skill was rated twice:<br>
     * Employee 3 rated with 4 and employee 5 rated with 2.
     *
     * @param skillRatingsAssembledStr
     * @return
     */
    default List<SkillRatingDto> parseSkillRatings(String skillRatingsAssembledStr) {
        if (StringUtils.isNotBlank(skillRatingsAssembledStr)) {
            return Arrays.stream(StringUtils.split(skillRatingsAssembledStr, '/'))
                    .map(s -> StringUtils.split(s, ',')).map(array -> {
                        var employeeId = Integer.parseInt(array[0]);
                        var rateValue = Float.parseFloat(array[1]);
                        return new SkillRatingDto(employeeId, rateValue);
                    }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * For employee table we collect all skills with ratings to one string per employee<br>
     * Every skill information block separated by '|$|'<br>
     * Every skill information block consists of
     * skill id, skill name, skill group id, skill group name, skill ratings assembled string separated by '#'<br>
     * For example <code>1|#|Java|#|1|#|Backend languages|#|3,4|$|2|#|Java Script|#|2|#|Frontend languages|#|3,4|$|3|#|Python|#|3|#|Backend languages|#|3,4/5,4</code> means that employee have
     * Java (id=1 from Backend languages), Java Script(id=2 from Frontend languages), Python(id=3 from Backend languages) and Python skills with corresponding ratings
     *
     * @see RatingsMapper#parseSkillRatings
     */
    default List<SkillDto> parseAssembledSkills(String skillRatingsAssembledStr, int loggedInEmployeeId) {
        var result = new ArrayList<SkillDto>();
        if (skillRatingsAssembledStr == null) {
            return result;
        }
        for (var skillStr : StringUtils.splitByWholeSeparator(skillRatingsAssembledStr, SKILLS_SEPARATOR)) {
            var skillAttrs = StringUtils.splitByWholeSeparator(skillStr, SKILL_ATTRS_SEPARATOR);
            var skillId = Integer.parseInt(skillAttrs[0]);
            var skillName = skillAttrs[1].trim();
            var groupId = Integer.parseInt(skillAttrs[2].trim());
            var groupName = skillAttrs[3].trim();
            var ratings = ratings(skillAttrs[4], loggedInEmployeeId);
            var skill = new SkillDto();
            skill.setGroup(new SimpleDictDto(groupId, groupName));
            skill.setId(skillId);
            skill.setName(skillName);
            skill.setRatings(ratings);
            result.add(skill);
        }
        return result;
    }

    default SkillDto.Ratings ratings(String skillRatingsAssembledStr, int loggedInEmployeeId) {
        var allRatings = parseSkillRatings(skillRatingsAssembledStr);
        var result = new SkillDto.Ratings();
        result.setRatingsCount(allRatings.size());
        if (!allRatings.isEmpty()) {
            result.setAverageRating((float) allRatings.stream().mapToDouble(r -> r.getRating() == null ? 0 : r.getRating()).average().getAsDouble());
            result.setMyRating(allRatings.stream().filter(r -> r.getEmployeeId() == loggedInEmployeeId)
                    .findFirst()
                    .map(SkillRatingDto::getRating)
                    .orElse(null));
        }
        return result;
    }


}
