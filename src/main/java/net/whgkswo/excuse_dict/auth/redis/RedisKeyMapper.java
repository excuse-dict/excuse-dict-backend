package net.whgkswo.excuse_dict.auth.redis;

import net.whgkswo.excuse_dict.entities.members.email.dto.VerificationPurpose;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyMapper {

    public RedisKey.Prefix getVerificationCodePrefix(VerificationPurpose purpose){
        return switch (purpose){
            case REGISTRATION -> RedisKey.Prefix.VERIFICATION_CODE_FOR_REGISTRATION;
            case RESET_PASSWORD -> RedisKey.Prefix.VERIFICATION_CODE_TO_RESET_PASSWORD;
        };
    }

    public RedisKey.Prefix getVerificationCompletePrefix(VerificationPurpose purpose){
        return switch (purpose){
            case REGISTRATION -> RedisKey.Prefix.VERIFICATION_COMPLETE_REGISTRATION;
            case RESET_PASSWORD -> RedisKey.Prefix.VERIFICATION_COMPLETE_RESET_PASSWORD;
        };
    }
}
