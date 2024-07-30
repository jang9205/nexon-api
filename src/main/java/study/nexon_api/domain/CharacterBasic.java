package study.nexon_api.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter @Getter
public class CharacterBasic {

    private ZonedDateTime date;
    private String character_name;
    private String world_name;
    private String character_gender;
    private String character_class;
    private String character_class_level;
    private String character_level;
    private Long character_exp;
    private String character_exp_rate;
    private String character_guild_name;
    private String character_image;
    private ZonedDateTime character_date_create;
    private Boolean access_flag;
    private Boolean liberation_quest_clear_flag;
}
